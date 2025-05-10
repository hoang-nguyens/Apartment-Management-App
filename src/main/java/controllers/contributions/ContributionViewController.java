package controllers.contributions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import models.Contribution;
import models.Fee;
import models.FeeCategory;
import models.User;
import models.enums.FeeType;
import models.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.ApartmentService;
import services.ContributionService;
import services.FeeCategoryService;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ContributionViewController {
    private final FeeCategoryService feeCategoryService;
    private final ApartmentService apartmentService;

    @Autowired
    public ContributionViewController(FeeCategoryService feeCategoryService, ApartmentService apartmentService) {
        this.feeCategoryService = feeCategoryService;
        this.apartmentService = apartmentService;
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String endpoint = "http://localhost:8080/api/contributions";
    private final String getUsersEnpoint = "http://localhost:8080/api/contributions/users";
    private final String paymentManualEndpoint = "http://localhost:8080/api/contributions/manual";
    private final String feeCateEndpoint = "http://localhost:8080/api/fees";

    private final Set<Role> adminRoles = Set.of(Role.ADMIN, Role.ADMIN_ROOT);
    private User currentUser;

    @FXML private TableView<Map<String, Object>> contributionsTable;
    private ArrayList<String> feeNameList = new ArrayList<>();
    private  List<Map<String, Object>> tableData = new ArrayList<>();


    @FXML
    public void initialize() {
        setupFeeNames();
        setupTableColumns();
        loadContributions();
    }

    private void setupFeeNames() {
        List<FeeCategory> feeCateList = feeCategoryService.getFeeCategoriesByType(FeeType.OPTIONAL);
        for (FeeCategory feeCategory : feeCateList) {
            String url = feeCateEndpoint + "?category=" + URLEncoder.encode(feeCategory.getName(), StandardCharsets.UTF_8);
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .GET()
                        .build();
                HttpResponse<String> response =  httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Fee[] filteredFeeList = objectMapper.readValue(response.body(), Fee[].class);
                for (Fee fee : filteredFeeList) {
                    feeNameList.add(fee.getSubCategory());
                }
                //        System.out.println(filteredFeeList);
                //        System.out.println(feeList);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void setupTableColumns() {
        contributionsTable.getColumns().clear();

        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Người nộp");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("name")));
        contributionsTable.getColumns().add(nameCol);

        TableColumn<Map<String, Object>, String> roomCol = new TableColumn<>("Số phòng");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("room")));
        contributionsTable.getColumns().add(roomCol);

        for (String fee : feeNameList) {
            TableColumn<Map<String, Object>, BigDecimal> feeCol = new TableColumn<>(fee);
            feeCol.setCellValueFactory(data -> {
                Object value = data.getValue().get(fee);
                return new SimpleObjectProperty<>(value != null ? (BigDecimal) value : BigDecimal.ZERO);
            });
            contributionsTable.getColumns().add(feeCol);
        }
//        contributionsTable.setItems(FXCollections.observableArrayList(rowData));
    }

    private void loadContributions() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getUsersEnpoint))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response =  httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Contribution[] contributions = objectMapper.readValue(response.body(), Contribution[].class);
            Map<User, List<Contribution>> groupedByUser = Arrays.stream(contributions)
                    .collect(Collectors.groupingBy(Contribution::getUser));

            for (Map.Entry<User, List<Contribution>> entry : groupedByUser.entrySet()) {
                User user = entry.getKey();
                List<Contribution> userContributions = entry.getValue();

                Map<String, Object> row = new HashMap<>();
                row.put("Tên", user.getEmail());
                row.put("Phòng", apartmentService.getApartmentByOwner(user));

                for (String feeName : feeNameList) {
                    BigDecimal total = userContributions.stream()
                            .filter(c -> feeName.equals(c.getFee().getSubCategory())) // giả sử getFeeName là subCategory hoặc tên khoản thu
                            .map(Contribution::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    row.put(feeName, total);
                }
                tableData.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
