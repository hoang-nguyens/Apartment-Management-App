package controllers.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import models.enums.InvoiceStatus;
import models.enums.Role;
import models.fee.FeeCategory;
import models.invoice.Invoice;
import models.resident.Resident;
import models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import repositories.notification.UserNotificationRepository;
import services.fee.FeeCategoryService;
import services.invoice.InvoiceService;
import services.resident.ResidentService;
import utils.SoPhongUtil;
import utils.UserUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class InvoiceViewController {
    private final InvoiceService invoiceService;
    private final FeeCategoryService feeCategoryService;
    private final ResidentService residentService;
    @Autowired
    public InvoiceViewController(InvoiceService invoiceService, FeeCategoryService feeCategoryService, ResidentService residentService) {
        this.invoiceService = invoiceService;
        this.feeCategoryService = feeCategoryService;
        this.residentService = residentService;
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String endpoint = "http://localhost:8080/api/invoices";
    private final String paymentManualEndpoint = "http://localhost:8080/api/payments/manual";

    private final Set<Role> adminRoles = Set.of(Role.ADMIN, Role.ADMIN_ROOT);
    private User currentUser;

    @FXML
    private TableView<Invoice> invoiceTable;

    @FXML private TableColumn<Invoice, Invoice> selectedColumn;
    @FXML private TableColumn<Invoice, Integer> invoiceIdColumn;
    @FXML private TableColumn<Invoice, String> payerNameColumn;
    @FXML private TableColumn<Invoice, String> apartmentColumn;
    @FXML private TableColumn<Invoice, String> issueDateColumn;
    @FXML private TableColumn<Invoice, String> dueDateColumn;
    @FXML private TableColumn<Invoice, String> categoryColumn;
    @FXML private TableColumn<Invoice, BigDecimal> amountColumn;
    @FXML private TableColumn<Invoice, InvoiceStatus> statusColumn;
    @FXML private TableColumn<Invoice, String> titleColumn;

    @FXML private Label totalAmountLabel;

    @FXML private Button payButton;
    @FXML private Button searchButton;
    @FXML private Label statusLabel;
    @FXML private Button markAsPaidButton;
    @FXML private Button markAsUnpaidButton;
    @FXML private TextField usernameField;
    @FXML private TextField apartmentField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> invoiceStatusComboBox;

    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private ObservableSet<Invoice> selectedInvoices = FXCollections.observableSet();
    private ObservableList<String> categoryList = FXCollections.observableArrayList();

    private boolean inited = false;


    @FXML
    public void initialize() {
        currentUser = UserUtils.getCurrentUser();
        invoiceTable.setEditable(true);

        if (!inited) {
            invoiceService.createMonthlyInvoices();
            invoiceService.updateOverdueInvoice();
            inited = true;
        }
//        invoiceService.createMonthlyInvoices(currentUser);
        setupTableColumns();
        loadCategories();
        loadFeeStatus();
        loadInvoices();
        updateTotalAmount(invoiceList);

        payButton.setOnAction(event -> handlePayment());
    }

    private void setupTableColumns() {
        selectedColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        selectedColumn.setCellFactory(col -> {
            BooleanProperty selected = new SimpleBooleanProperty();
            CheckBoxTableCell<Invoice, Invoice> cell = new CheckBoxTableCell<>(index -> selected);
            selected.addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    selectedInvoices.add(cell.getItem());
                } else {
                    selectedInvoices.remove(cell.getItem());
                }
                updateTotalAmount(selectedInvoices);
            });

            selectedInvoices.addListener((SetChangeListener.Change<? extends Invoice> change) -> {
                selected.set(cell.getItem() != null && selectedInvoices.contains(cell.getItem()));
            });

            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                selected.set(newItem != null && selectedInvoices.contains(newItem));
            });
            return cell;
        });

        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        payerNameColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            Long userId = user.getId();

            Resident resident = residentService.findResidentByUserId(userId);
            String hoTen = (resident != null) ? resident.getHoTen() : "N/A";
            return new SimpleStringProperty(hoTen);
        });

        apartmentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getApartment().getRoomNumber()));
//        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
//        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        titleColumn.setCellValueFactory(data -> {
            String cate = data.getValue().getCategory();
            LocalDate issueDate = data.getValue().getIssueDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            return new SimpleStringProperty(cate + " tháng " + issueDate.format(formatter));
        });
        // category + issueDate -> title
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (!adminRoles.contains(currentUser.getRole())) {
            payerNameColumn.setVisible(false);
            markAsPaidButton.setVisible(false);
            markAsUnpaidButton.setVisible(false);
            apartmentField.setVisible(false);
            categoryComboBox.setVisible(false);
            invoiceStatusComboBox.setVisible(false);
            searchButton.setVisible(false);
        } else {
            payButton.setVisible(false);
        }
//        invoiceTable.setItems(invoiceList);
    }

    private void loadCategories() {
        List<String> categories = new ArrayList<>(feeCategoryService.getFeeCategoriesNotOptional()
                .stream()
                .map(FeeCategory::getName)
                .toList());
        categories.add("Tất cả");
        categoryList.setAll(categories);
        categoryComboBox.setItems(categoryList);
    }

    private void loadFeeStatus() {
        ObservableList<String> statusList = FXCollections.observableArrayList(
                Arrays.stream(InvoiceStatus.values())
                        .map(Enum::name)
                        .toList()
        );
        invoiceStatusComboBox.setItems(statusList);
    }

    private void loadInvoices() {
        if (currentUser == null) {
            statusLabel.setText("Vui lòng đăng nhập !!!");
            return;
        }
        String url = "http://localhost:8080/api/invoices/unpaid";
        if (!adminRoles.contains(currentUser.getRole())) {
            url += "?userId=" + currentUser.getId();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Invoice[] invoices = objectMapper.readValue(response.body(), Invoice[].class);
                invoiceList.setAll(invoices);
                invoiceTable.setItems(invoiceList);
                statusLabel.setText("Tải lên dữ liệu thành công.");
            } else {
                statusLabel.setText("Tải lên dữ liệu thất bại." + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTotalAmount(Collection<Invoice> invoices) {
        BigDecimal total = invoices.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.UNPAID || invoice.getStatus() == InvoiceStatus.PENDING)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalAmountLabel.setText(total + " VNĐ");
    }

    private void handlePayment() {
        for (Invoice invoice : selectedInvoices) {
            System.out.println(invoice.getAmount());
        }
        System.out.println("Tính năng thanh toán đang hoàn thiện.");
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận thanh toán " + selectedInvoices.size() + " hóa đơn?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                for (Invoice invoice : selectedInvoices) {
                    if (invoice.getStatus() == InvoiceStatus.UNPAID || invoice.getStatus() == InvoiceStatus.OVERDUE) {
//                        invoice.setStatus(InvoiceStatus.PAID);
                        // Gọi API để cập nhật hóa đơn đã thanh toán
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(endpoint + "/" + invoice.getId() + "/status?status=PENDING"))
                                .header("Content-Type", "application/json")
                                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                                .build();
                        try {
                            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                            if (httpResponse.statusCode() == 200) {
                                Invoice newInvoice = objectMapper.readValue(httpResponse.body(), Invoice.class);
                                invoice.setStatus(newInvoice.getStatus());
                                statusLabel.setText("Ban quản lý sẽ xem xét thông tin thanh toán của bạn.");
                            } else {
                                statusLabel.setText("Lỗi request:" + httpResponse.body());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                loadInvoices();
                updateTotalAmount(selectedInvoices);
            }
        });
    }

    @FXML
    private void markAsPaid() {
        try {
            Set<Long> paidInvoiceId = new java.util.HashSet<>(Set.of());
            for (Invoice invoice : selectedInvoices) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint + "/" + invoice.getId() + "/status"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Invoice newInvoice = objectMapper.readValue(response.body(), Invoice.class);
                    paidInvoiceId.add(newInvoice.getId());
                } else {
                    statusLabel.setText("Lỗi request:" + response.body());
                }
            }

            for (int i = 0; i < invoiceList.size(); i++) {
                if (paidInvoiceId.contains(invoiceList.get(i).getId())) {
                    Invoice invoice = invoiceList.get(i);
                    Long userId = invoice.getUser().getId();
                    String url = paymentManualEndpoint;
                    List<String> queryParams = new ArrayList<>();

                    queryParams.add("invoiceId=" + invoice.getId());
                    queryParams.add("payerId=" + userId);
                    url += "?" + String.join("&", queryParams);
                    System.out.println(url);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        statusLabel.setText("Có lỗi xảy ra khi capj nhat thong tin thanh toan");
                    }
                    System.out.println(queryParams.toString());
                    System.out.println(response.body());
                    invoiceList.get(i).setStatus(InvoiceStatus.PAID);
                }
            }
            if (!paidInvoiceId.isEmpty()) {
                invoiceTable.refresh();
                statusLabel.setText("Thanh cong");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Co loi xay ra");
        }
    }

    @FXML
    private void markAsUnpaid() {
        try {
            Set<Long> selectedInvoiceId = new java.util.HashSet<>(Set.of());
            for (Invoice invoice : selectedInvoices) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint + "/" + invoice.getId() + "/status?status=UNPAID"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    Invoice newInvoice = objectMapper.readValue(response.body(), Invoice.class);
                    selectedInvoiceId.add(newInvoice.getId());
                } else {
                    statusLabel.setText("Lỗi request:" + response.body());
                }
            }
            for (int i = 0; i < invoiceList.size(); i++) {
                if (selectedInvoiceId.contains(invoiceList.get(i).getId())) {
                    invoiceList.get(i).setStatus(InvoiceStatus.UNPAID);
                }
            }
            if (!selectedInvoiceId.isEmpty()) {
                invoiceTable.refresh();
                statusLabel.setText("Đánh dâu thành công");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
//        String name = usernameField.getText();
        String apartment = apartmentField.getText();
        if (apartment != null && !apartment.isEmpty() && !SoPhongUtil.soPhongHopLe(apartment)) {
            statusLabel.setText("Không tồn tại số phong !");
            return;
        }
        String filterCategory = categoryComboBox.getValue();
        if (filterCategory!=null && filterCategory.equals("Tất cả")) filterCategory = null;
        String filterStatus = invoiceStatusComboBox.getValue();

        List<String> queryParams = new ArrayList<>();
        String url = endpoint + "/apartments";
//        if (name != null && !name.isEmpty()) {
//            queryParams.add("name=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
//        }
        if (apartment != null && !apartment.isEmpty()) {
            queryParams.add("apartment=" + URLEncoder.encode(apartment, StandardCharsets.UTF_8));
        }
        if (filterCategory != null && !filterCategory.isEmpty()) {
            queryParams.add("category=" + URLEncoder.encode(filterCategory, StandardCharsets.UTF_8));
        }
        if (filterStatus != null && !filterStatus.isEmpty()) {
            queryParams.add("status=" + URLEncoder.encode(filterStatus, StandardCharsets.UTF_8));
        }
        if (!queryParams.isEmpty()) {
            url += "?" + String.join("&", queryParams);
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response =  httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Invoice[] filteredInvoiceList = objectMapper.readValue(response.body(), Invoice[].class);
                invoiceList.setAll(filteredInvoiceList);
                //        System.out.println(filteredFeeList);
                //        System.out.println(feeList);
                invoiceTable.setItems(invoiceList);
            } else {
                statusLabel.setText("Bad Request");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            statusLabel.setText("Co loi xay ra");
        }
    }
}