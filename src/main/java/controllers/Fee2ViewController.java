package controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Fee2;
import models.FeeCategory;
import models.enums.BillPeriod;
import models.enums.FeeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import services.Fee2Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class Fee2ViewController {
    @Autowired
    private Fee2Service feeService;

    @FXML
    private TableView<Fee2> feeTable;
    @FXML
    private TableColumn<Fee2, Integer> idColumn;
    @FXML
    private TableColumn<Fee2, String> categoryColumn;
    @FXML
    private TableColumn<Fee2, String> subCategoryColumn;
    @FXML
    private TableColumn<Fee2, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Fee2, FeeUnit> unitColumn;
    @FXML
    private TableColumn<Fee2, BillPeriod> billPeriodColumn;
    @FXML
    private TableColumn<Fee2, LocalDate> startDateColumn;
    @FXML
    private TableColumn<Fee2, LocalDate> endDateColumn;

    @FXML
    private ComboBox<FeeCategory> categoryComboBox;
    @FXML
    private ComboBox<FeeCategory> feeSubCategoryComboBox;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<FeeUnit> unitComboBox;
    @FXML
    private ComboBox<BillPeriod> billPeriodComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label statusLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Fee2> feeList = FXCollections.observableArrayList();
    private ObservableList<FeeCategory> categoryList = FXCollections.observableArrayList();
    private ObservableList<FeeCategory> subCategoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup table columns
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFeeCategory().getName()));
        subCategoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFeeCategory().getName()));
        amountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getAmount()));
        unitColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getUnit()));
        billPeriodColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBillPeriod()));
        startDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStartDate()));
        endDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEndDate()));

        // Load fee data
        loadFees();
        loadCategories();

        // Setup combo boxes
        unitComboBox.setItems(FXCollections.observableArrayList(FeeUnit.values()));
        categoryComboBox.setItems(categoryList);
        feeSubCategoryComboBox.setItems(subCategoryList);

        // Update sub-category list when category changes
        categoryComboBox.setOnAction(event -> loadSubCategories());

        // Table selection listener
        feeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }

    private void loadFees() {
        List<Fee2> fees = feeService.getAllFees();
        feeList.setAll(fees);
        feeTable.setItems(feeList);
    }

    private void loadCategories() {
        List<FeeCategory> categories = feeService.getAllFeeCategories();
        categoryList.setAll(categories);
        categoryComboBox.setItems(categoryList);
    }

    private void loadSubCategories() {
        FeeCategory selectedCategory = categoryComboBox.getValue();
        if (selectedCategory != null) {
            List<FeeCategory> subCategories = feeService.getSubCategories(selectedCategory.getId());
            subCategoryList.setAll(subCategories);
            feeSubCategoryComboBox.setItems(subCategoryList);
        }
    }

    private void populateForm(Fee2 fee) {
        nameField.setText(fee.getName());
        categoryComboBox.setValue(fee.getFeeCategory());
        feeSubCategoryComboBox.setValue(fee.getFeeCategory());
        amountField.setText(fee.getAmount().toString());
        unitComboBox.setValue(fee.getUnit());
        startDatePicker.setValue(fee.getStartDate());
        endDatePicker.setValue(fee.getEndDate());
    }

    @FXML
    private void handleAdd() {
        try {
            Fee2 newFee = new Fee2();
            newFee.setName(nameField.getText());
            newFee.setFeeCategory(feeSubCategoryComboBox.getValue());
            newFee.setAmount(new BigDecimal(amountField.getText()));
            newFee.setUnit(unitComboBox.getValue());
            newFee.setBillPeriod(BillPeriod.MONTHLY);
            newFee.setStartDate(startDatePicker.getValue());
            newFee.setEndDate(endDatePicker.getValue());

            Fee2 savedFee = feeService.createFee(
                    newFee.getName(), newFee.getFeeCategory(),
                    newFee.getAmount(), newFee.getUnit(),
                    newFee.getBillPeriod(), newFee.getStartDate(), newFee.getEndDate()
            );

            feeList.add(savedFee);
            feeTable.refresh();
            statusLabel.setText("Thêm khoản thu thành công!");
        } catch (Exception e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        Fee2 selectedFee = feeTable.getSelectionModel().getSelectedItem();
        if (selectedFee == null) {
            statusLabel.setText("Chọn khoản thu để cập nhật!");
            return;
        }

        try {
            selectedFee.setName(nameField.getText());
            selectedFee.setFeeCategory(feeSubCategoryComboBox.getValue());
            selectedFee.setAmount(new BigDecimal(amountField.getText()));
            selectedFee.setUnit(unitComboBox.getValue());
            selectedFee.setBillPeriod(BillPeriod.MONTHLY);
            selectedFee.setStartDate(startDatePicker.getValue());
            selectedFee.setEndDate(endDatePicker.getValue());

            feeService.updateFee(selectedFee);
            feeTable.refresh();
            statusLabel.setText("Cập nhật khoản thu thành công!");
        } catch (Exception e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }
}
