package controllers.contribution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;
import models.contibution.Contribution;
import models.fee.Fee;
import models.fee.FeeCategory;
import models.user.User;
import models.enums.FeeType;
import models.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.apartment.ApartmentService;
import services.fee.FeeCategoryService;
import services.user.UserService;

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
    private final UserService userService;

    @Autowired
    public ContributionViewController(FeeCategoryService feeCategoryService, ApartmentService apartmentService, UserService userService) {
        this.feeCategoryService = feeCategoryService;
        this.apartmentService = apartmentService;
        this.userService = userService;
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
    @FXML private Button addButton;
    @FXML private Button refreshButton;

    private ArrayList<String> feeNameList = new ArrayList<>();
    private ArrayList<Fee> feeList = new ArrayList<>();
    private  List<Map<String, Object>> tableData = new ArrayList<>();
    private ObservableList<User> notContributedUsers = FXCollections.observableArrayList();
    private List<User> users;


    @FXML
    public void initialize() {
        users = userService.getAllUsersWithUserRole();
        setupFeeNames();
        loadContributions();
        loadNotContributedUsers();
        setupTableColumns();
    }

    @FXML public void refresh() {
        loadContributions();
        contributionsTable.setItems(FXCollections.observableArrayList(tableData));
    }

    private void setupFeeNames() {
        feeList.clear();
        feeNameList.clear();
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
                    feeList.add(fee);
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
        contributionsTable.setEditable(true);

        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Người nộp");
        nameCol.setCellValueFactory(data -> {
            User user = (User) data.getValue().get("name");
            return new SimpleStringProperty(user.getEmail());
        });
        nameCol.setEditable(false);
        contributionsTable.getColumns().add(nameCol);

        TableColumn<Map<String, Object>, String> roomCol = new TableColumn<>("Số phòng");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("room")));
        roomCol.setEditable(false);
        contributionsTable.getColumns().add(roomCol);

        for (String fee : feeNameList) {
            TableColumn<Map<String, Object>, BigDecimal> feeCol = new TableColumn<>(fee);
            feeCol.setCellValueFactory(data -> {
                Contribution value = (Contribution) data.getValue().get(fee);
                return new SimpleObjectProperty<>(value != null ? value.getAmount() : BigDecimal.ZERO);
            });

            feeCol.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));

            feeCol.setOnEditCommit(event -> {
                Map<String, Object> row = event.getRowValue();
                BigDecimal newValue = event.getNewValue();
                BigDecimal oldValue = event.getOldValue();

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận cập nhật");
                confirm.setHeaderText("Bạn có chắc muốn cập nhật khoản thu?");
                confirm.setContentText("Từ: " + oldValue + " -> " + newValue);

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        Contribution contrib = (Contribution) row.get(fee);
                        contrib.setAmount(newValue);

                        String jsonBody = objectMapper.writeValueAsString(contrib);
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/api/contributions/" + contrib.getId())) // hoặc endpoint bạn dùng
                                .header("Content-Type", "application/json")
                                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                                .build();

                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 200) {
                            row.put(fee, contrib); // cập nhật dữ liệu trong bảng
                        } else {
                            showError("Cập nhật thất bại", response.body());
                            contributionsTable.refresh();
                        }
                        row.put(fee, event.getNewValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    contributionsTable.refresh();
                }
            });

            contributionsTable.getColumns().add(feeCol);
        }
        contributionsTable.setItems(FXCollections.observableArrayList(tableData));
    }

    private void loadContributions() {
        tableData.clear();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
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
                row.put("name", user);
                row.put("room", apartmentService.getApartmentByOwner(user).getRoomNumber());

                for (String feeName : feeNameList) {
                    Contribution matched = userContributions.stream()
                            .filter(c -> feeName.equals(c.getFee().getSubCategory()))
                            .findFirst()
                            .orElse(null);

                    row.put(feeName, matched);
                }
                tableData.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private void loadNotContributedUsers() {
        System.out.println(users.size());
        for (User user : users) {
            boolean flag = false;
            for (Map<String, Object> row : tableData) {
                if (row.get("name").equals(user.getEmail())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                notContributedUsers.add(user);
            }
        }
        System.out.println(notContributedUsers.toString());
    }

    @FXML
    private void onAddContribution() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm người đóng góp");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<User> userComboBox = new ComboBox<>();
        userComboBox.setItems(notContributedUsers);
        userComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user != null ? user.getEmail() : "";
            }

            @Override
            public User fromString(String string) {
                // Không cần thiết trong trường hợp này
                return null;
            }
        });

        VBox fieldsBox = new VBox(10);
        Map<String, TextField> feeInputs = new HashMap<>();
        for (String fee : feeNameList) {
            TextField input = new TextField();
            input.setPromptText(fee);
            fieldsBox.getChildren().add(new HBox(10, new Label(fee), input));
            feeInputs.put(fee, input);
        }

        dialogPane.setContent(new VBox(10, new Label("Chọn người dùng:"), userComboBox, fieldsBox));
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            User selectedUser = userComboBox.getValue();
            if (selectedUser != null) {
                Map<String, BigDecimal> contributions = new HashMap<>();
                for (var entry : feeInputs.entrySet()) {
                    String fee = entry.getKey();
                    String input = entry.getValue().getText();
                    BigDecimal amount = input.isBlank() ? BigDecimal.ZERO : new BigDecimal(input);
                    contributions.put(fee, amount);
                }
                submitNewContribution(selectedUser, contributions);
            }
        }
    }

    private void submitNewContribution(User user, Map<String, BigDecimal> contributions) {
        System.out.println("Submitting new contribution");
        for (Map.Entry<String, BigDecimal> entry : contributions.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                Contribution newContrib = new Contribution();
                newContrib.setUser(user);
                newContrib.setAmount(entry.getValue());
                newContrib.setFee(getFee(entry.getKey()));
                try {
                    String json = objectMapper.writeValueAsString(newContrib);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(paymentManualEndpoint))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        System.out.println("ok: " + response.body());
                    } else {
                        System.out.println(response.body());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        loadContributions();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Fee getFee(String feeName) {
        for (Fee fee: feeList) {
            if (feeName.equals(fee.getSubCategory())) {
                return fee;
            }
        }
        return null;
    }
}