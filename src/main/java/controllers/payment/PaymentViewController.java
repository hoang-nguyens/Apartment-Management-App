package controllers.payment;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.payment.Payment;
import models.user.User;
import models.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.payment.PaymentService;
import utils.UserUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class PaymentViewController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentViewController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, String> colInvoice;
    @FXML private TableColumn<Payment, String> colUsername;
    @FXML
    private TableColumn<Payment, BigDecimal> colAmount;
    @FXML
    private TableColumn<Payment, String> colDate;
    @FXML
    private TableColumn<Payment, String> colMethod;
    @FXML
    private TableColumn<Payment, String> colNote;

    @FXML
    private PieChart paymentPieChart;
    @FXML
    private TableView<Map.Entry<String, BigDecimal>> statisticTable;
    @FXML
    private TableColumn<Map.Entry<String, BigDecimal>, String> colCategory;
    @FXML
    private TableColumn<Map.Entry<String, BigDecimal>, BigDecimal> colTotal;

    @FXML
    private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;


    private List<Payment> allPayments;
    private final Set<Role> adminRoles = Set.of(Role.ADMIN, Role.ADMIN_ROOT);
    private User currentUser;

    public void initialize() {
        currentUser = UserUtils.getCurrentUser();
        setupHistoryTable();
        loadPaymentHistory();

        if (!adminRoles.contains(currentUser.getRole())) {
            setupBarChart();
            loadUserStatistics();
            statisticTable.setVisible(false);
            paymentPieChart.setVisible(false);
            statisticTable.setManaged(false);
            paymentPieChart.setManaged(false);
        } else {
            setupStatisticTable();
            loadStatistics();
            barChart.setVisible(false);
            barChart.setManaged(false);
        }
    }

    private void setupHistoryTable() {
        colInvoice.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getInvoice().getCategory()));
        colUsername.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getResident().getUsername()));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy").format(data.getValue().getPaymentDate())));
        colMethod.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPaymentMethod() != null ? data.getValue().getPaymentMethod().name() : "Chưa ghi nhận"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));
    }

    private void setupStatisticTable() {
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        colTotal.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getValue()));
    }

    private void setupBarChart() {
        xAxis.setLabel("Tháng");
        yAxis.setLabel("Số tiền (VND)");
        barChart.setTitle("Số tiền chi ra hàng tháng");
    }

    private void loadPaymentHistory() {
        if (!adminRoles.contains(currentUser.getRole())) {
            allPayments = paymentService.getPaymentsByUserId(currentUser.getId());
            System.out.println(currentUser.getId());
            System.out.println(allPayments);
        } else {
            allPayments = paymentService.getAllPayments();
        }
        paymentTable.setItems(FXCollections.observableArrayList(allPayments));
    }

    private void loadStatistics() {
        // Nhóm theo loại hóa đơn
        Map<String, BigDecimal> grouped = allPayments.stream()
                .filter(p -> p.getInvoice() != null && p.getAmount() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getInvoice().getCategory(), // giả định có getCategory()
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)
                ));

        ObservableList<Map.Entry<String, BigDecimal>> tableData = FXCollections.observableArrayList(grouped.entrySet());
        statisticTable.setItems(tableData);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, BigDecimal> entry : grouped.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
        }
        paymentPieChart.setData(pieData);
    }

    private void loadUserStatistics() {
        LocalDate currentDate = LocalDate.now();
        int monthsToShow = 12;  // config số bars

        // Tạo danh sách các tháng để hiển thị
        List<String> months = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        for (int i = monthsToShow-1; i >= 0; i--) {
            // Xác định tháng và năm cần tính
            LocalDate date = currentDate.minusMonths(i);
            int month = date.getMonthValue();
            int year = date.getYear();

            BigDecimal totalAmount = paymentService.calculateMonthlyPayment(currentUser.getId(), month, year);
            System.out.println(month + "/" +  year + ": " + totalAmount);
            months.add(month + "/" + year);
            amounts.add(totalAmount);
        }
//        System.out.println(amounts.toString());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < months.size(); i++) {
            series.getData().add(new XYChart.Data<>(months.get(i), amounts.get(i)));
        }
//        System.out.println(series.toString());
//        System.out.println(series.getData());

        // Đưa dữ liệu vào BarChart
        barChart.getData().clear(); // Xóa dữ liệu cũ
        barChart.getData().add(series);
    }
}