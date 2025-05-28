package controllers.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.fee.Fee;
import models.fee.FeeCategory;
import models.enums.BillPeriod;
import models.enums.FeeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.fee.FeeCategoryService;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class FeeInsertController {
    //    private final FeeService feeService;
    private final FeeCategoryService feeCategoryService;
    private Stage stage;
    private Fee fee;
    private static final Logger logger = LoggerFactory.getLogger(FeeInsertController.class);

    @Autowired
    public FeeInsertController(FeeCategoryService feeCategoryService) {
        this.feeCategoryService = feeCategoryService;
    }
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> subCategoryComboBox;
    @FXML
    private ComboBox<FeeUnit> unitComboBox;
    @FXML
    private ComboBox<BillPeriod> billPeriodComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private ObservableList<String> categoryList = FXCollections.observableArrayList();
    private ObservableList<String> subCategoryList = FXCollections.observableArrayList();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
//        System.out.println("OK !!!!");

        loadCategories();

        // Setup combo boxes
        categoryComboBox.setItems(categoryList);
        unitComboBox.setItems(FXCollections.observableArrayList(FeeUnit.values()));
        billPeriodComboBox.setItems(FXCollections.observableArrayList(BillPeriod.values()));

        categoryComboBox.setOnAction(event -> loadSubCategories());
        categoryComboBox.setEditable(true);
        subCategoryComboBox.setEditable(true);

        categoryComboBox.getEditor().setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                addCategory();
            }
        });
        subCategoryComboBox.getEditor().setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                addSubCategory();
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setFee(Fee fee, int mode){
        this.fee = fee;
        if (fee != null) {
            populateForm(fee);
        }
        if (mode == 1) {
            amountField.setEditable(false);
            unitComboBox.setEditable(false);
            billPeriodComboBox.setEditable(false);
            endDatePicker.setEditable(false);
            descriptionArea.setEditable(false);
            saveButton.setVisible(false);
        }
    }
    public Fee getFee() {
        return fee;
    }

    private void loadCategories() {
        List<String> categories = feeCategoryService.getParentCategoryNames();
        categoryList.setAll(categories);
    }

    private void loadSubCategories() {
        String selectedCategory = categoryComboBox.getValue();
        System.out.println(selectedCategory);
        if (selectedCategory!=null && !selectedCategory.isEmpty()) {
            List<String> subCategories = feeCategoryService.getSubCategoriesNames(selectedCategory);
            System.out.println(subCategories);
            subCategoryList.setAll(subCategories);
            subCategoryComboBox.setItems(subCategoryList);
        }
    }


    private void populateForm(Fee fee) {
        categoryComboBox.setValue(fee.getCategory());
        subCategoryComboBox.setValue(fee.getSubCategory());
        amountField.setText(fee.getAmount() != null ? fee.getAmount().toString() : null);
        unitComboBox.setValue(fee.getUnit());
        billPeriodComboBox.setValue(fee.getBillPeriod());
        descriptionArea.setText(fee.getDescription());
        startDatePicker.setValue(fee.getStartDate());
        endDatePicker.setValue(fee.getEndDate());
        categoryComboBox.setDisable(true);
        subCategoryComboBox.setDisable(true);
        startDatePicker.setDisable(true);
    }

    private void addCategory() {
        String newCategory = categoryComboBox.getEditor().getText().trim();
        if (newCategory.isEmpty()) {
            showAlert("Vui lòng chọn danh mục cha trước!");
            return;
        }
        if (!categoryList.contains(newCategory)) {
//            FeeCategory newFeeCategory = feeCategoryService.createFeeCategory(newCategory, null);;
            categoryList.add(newCategory);
            categoryComboBox.setItems(FXCollections.observableArrayList(categoryList));
            categoryComboBox.setValue(newCategory);
        }
    }

    private void addSubCategory() {
        String selectedCategory = categoryComboBox.getValue(); // Lấy danh mục cha
        if (selectedCategory.isEmpty()) {
            showAlert("Vui lòng chọn danh mục cha trước!");
            return;
        }
        String newSubCategory = subCategoryComboBox.getEditor().getText().trim();
        if (newSubCategory.isEmpty()) {
            showAlert("Vui lòng nhập danh mục con!");
            return;
        }
        FeeCategory selectedFeeCategory = feeCategoryService.findTopLevelCategoryByName(selectedCategory);
        if (!subCategoryList.contains(newSubCategory)) {
//            FeeCategory subCategory = feeCategoryService.createFeeCategory(newSubCategory, selectedFeeCategory);
            subCategoryList.add(newSubCategory);
            subCategoryComboBox.setItems(FXCollections.observableArrayList(subCategoryList));
            subCategoryComboBox.setValue(newSubCategory);
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (! validateFeeInput()) return;
            if (amountField.getText()==null || amountField.getText().isEmpty()) {
                amountField.setText("0");
            }
//            User currentUser = userService.getCurrentUser();
//            System.out.println(currentUser.toString());

            boolean inserted = true;
            if (fee == null) {
                fee = new Fee();
                fee.setCategory(categoryComboBox.getValue());
                fee.setSubCategory(subCategoryComboBox.getValue());
                fee.setStartDate(startDatePicker.getValue());
            } else {
                inserted = false;
            }
            fee.setAmount(new BigDecimal(amountField.getText()));
            fee.setUnit(unitComboBox.getValue());
            fee.setBillPeriod(billPeriodComboBox.getValue());
            fee.setDescription(descriptionArea.getText());
            fee.setEndDate(endDatePicker.getValue());

            String requestBody = objectMapper.writeValueAsString(fee);
            System.out.println(requestBody);
            HttpRequest request = null;
            if (inserted) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/fees"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/fees/" + fee.getId()))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            }

            System.out.println(request.toString());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            if (response.statusCode() == 200) {
                fee = objectMapper.readValue(response.body(), Fee.class);
                statusLabel.setText("Thao tác thành công!");
                stage.close();
            } else {
                System.out.println(response.body());
                statusLabel.setText("Lỗi request: " + response.body());
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Lỗi nhập liệu: Số tiền không hợp lệ!");
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Lỗi nhập liệu: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    private boolean validateFeeInput() {
        try {
            // Kiểm tra Loại khoản thu
            String selectedCategory = categoryComboBox.getValue();
            if (selectedCategory == null || selectedCategory.trim().isEmpty()) {
                statusLabel.setText("Loại khoản thu không được để trống!");
                return false;
            }

            // Kiểm tra Nhóm khoản thu
            String selectedSubCategory = subCategoryComboBox.getValue();
            if (selectedSubCategory == null || selectedSubCategory.trim().isEmpty()) {
                statusLabel.setText("Nhóm khoản thu không được để trống!");
                return false;
            }

            // Kiểm tra Số tiền
            String amountText = amountField.getText();
            BigDecimal amount = null;
            if (!"Đóng Góp".equals(selectedCategory)) {
                if (amountText == null || amountText.trim().isEmpty()) {
                    statusLabel.setText("Vui lòng nhập số tiền!");
                    return false;
                }
                try {
                    amount = new BigDecimal(amountText);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        statusLabel.setText("Số tiền phải lớn hơn 0!");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    statusLabel.setText("Số tiền không hợp lệ!");
                    return false;
                }
            } else if (amountText != null && !amountText.trim().isEmpty()) {
                try {
                    amount = new BigDecimal(amountText);
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        statusLabel.setText("Số tiền phải lớn hơn hoặc bằng 0!");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    statusLabel.setText("Số tiền không hợp lệ!");
                    return false;
                }
            }

            // Kiểm tra Đơn vị
            FeeUnit selectedUnit = unitComboBox.getValue();
            if (selectedUnit == null) {
                statusLabel.setText("Đơn vị không được để trống!");
                return false;
            }

            // Kiểm tra Kỳ thanh toán
            BillPeriod selectedBillPeriod = billPeriodComboBox.getValue();
            if (selectedBillPeriod == null) {
                statusLabel.setText("Kỳ thanh toán không được để trống!");
                return false;
            }

            // Kiểm tra Ngày bắt đầu
            LocalDate startDate = startDatePicker.getValue();
            if (! startDatePicker.isDisabled()){
                if (startDate == null || startDate.isBefore(LocalDate.now())) {
                    statusLabel.setText("Ngày bắt đầu phải từ hôm nay trở đi!");
                    return false;
                }
            }

            // Kiểm tra Ngày kết thúc
            LocalDate endDate = endDatePicker.getValue();
            if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
                statusLabel.setText("Ngày kết thúc phải sau ngày bắt đầu!");
                return false;
            }

            // Nếu tất cả đều hợp lệ
            return true;
        } catch (Exception e) {
            statusLabel.setText("Đã xảy ra lỗi khi kiểm tra dữ liệu!");
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}