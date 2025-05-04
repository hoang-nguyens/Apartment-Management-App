package controllers;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Apartment;
import models.Fee;
import models.Invoice;
import models.User;
import models.enums.InvoiceStatus;
import models.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.InvoiceService;
import utils.UserUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;

@Controller
public class InvoiceViewController {
    private final InvoiceService invoiceService;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String endpoint = "http://localhost:8080/api/invoices";

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

    @FXML private Label totalAmountLabel;

    @FXML private Button payButton;
    @FXML private Label statusLabel;
    @FXML private Button markAsPaidButton;

    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private ObservableSet<Invoice> selectedInvoices = FXCollections.observableSet();


    @Autowired
    public InvoiceViewController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @FXML
    public void initialize() {
        currentUser = UserUtils.getCurrentUser();
        invoiceTable.setEditable(true);

//        invoiceService.createMonthlyInvoices(currentUser);
        setupTableColumns();
        loadInvoices();
        updateTotalAmount();

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
            return new SimpleStringProperty(user != null ? user.getUsername() : "N/A");
        });
        apartmentColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getApartment().getRoomNumber()));
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (!adminRoles.contains(currentUser.getRole())) {
            payerNameColumn.setVisible(false);
        }
//        invoiceTable.setItems(invoiceList);
    }

    private void loadInvoices() {
        if (currentUser == null) {
            statusLabel.setText("Vui lòng đăng nhập !!!");
            return;
        }
        String url = "http://localhost:8080/api/invoices";
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

    private void updateTotalAmount() {
        BigDecimal total = invoiceList.stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.UNPAID)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalAmountLabel.setText(total + " VNĐ");
    }

    private void handlePayment() {
        for (Invoice invoice : selectedInvoices) {
            System.out.println(invoice.getAmount());
        }
        System.out.println("Tính năng thanh toán đang hoàn thiện.");
//        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
//                "Xác nhận thanh toán các hóa đơn?", ButtonType.YES, ButtonType.NO);
//        confirmation.showAndWait().ifPresent(response -> {
//            if (response == ButtonType.YES) {
//                for (Invoice invoice : invoiceList) {
//                    if (invoice.getStatus() == InvoiceStatus.UNPAID) {
//                        invoice.setStatus(InvoiceStatus.PAID);
//                        // Gọi API để cập nhật hóa đơn đã thanh toán
//                        invoiceController.updateInvoice(invoice.getId(), invoice);
//                    }
//                }
//                loadInvoices();
//                updateTotalAmount();
//            }
//        });
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
                    System.out.println(response.body());
                    statusLabel.setText("Lỗi request xay ra");
                }
            }
            for (int i = 0; i < invoiceList.size(); i++) {
                if (paidInvoiceId.contains(invoiceList.get(i).getId())) {
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

    private void filterInvoices() {

    }
}