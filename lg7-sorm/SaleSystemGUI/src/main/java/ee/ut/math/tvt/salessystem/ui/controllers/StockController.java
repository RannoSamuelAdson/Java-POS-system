package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.ui.QuickAlert;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class StockController implements Initializable {

    private static final Logger log = LogManager.getLogger(StockController.class);

    private final SalesSystemDAO dao;

    @FXML
    private Button addItem;
    @FXML
    private Button cancelEdit;
    @FXML
    private Button confirmEdit;
    @FXML
    private Button deleteItem;
    @FXML
    private TableView<StockItem> warehouseTableView;
    @FXML
    private TableColumn<StockItem, Long> idColumn;
    @FXML
    private TableColumn<StockItem, String> nameColumn;
    @FXML
    private TableColumn<StockItem, Double> priceColumn;
    @FXML
    private TableColumn<StockItem, Integer> quantityColumn;

    @FXML
    protected TextField barcodeField;
    @FXML
    protected TextField amountField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;

    public StockController(SalesSystemDAO dao) {
        this.dao = dao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshStockItemsTable();
        disableWarehouseRowEditButtons(true);

        // TODO refresh view after adding new items
    }

    @FXML
    public void refreshButtonClicked() {
        refreshStockItemsTable();
    }

    private void refreshStockItemsTable() {
        warehouseTableView.setItems(FXCollections.observableList(dao.findAllStockItems()));
        warehouseTableView.refresh();
    }

    @FXML
    public void addItemButtonClicked() { addItemToWarehouse(); }

    protected void addItemToWarehouse() {
        long id;
        double price;
        int quantity;

        Alert alert = QuickAlert.createAlert(Alert.AlertType.WARNING,"Exception","Please check the input fields.","Make sure you are entering data in the correct format.");
        Alert sameId = QuickAlert.createAlert(Alert.AlertType.WARNING,"Warning","An item with the same id/barcode already exists.","Please change the id/barcode and try again.");

        try {
            if (Long.parseLong(barcodeField.getText()) < 0) {
                alert.showAndWait();
                log.error("Id below 0");
                return;
            }
            id = Long.parseLong(barcodeField.getText());
        } catch (NumberFormatException e) {
            alert.showAndWait();
            log.error("Invalid barcode");
            return;
        }

        try {
            if (Double.parseDouble(priceField.getText()) < 0) {
                alert.showAndWait();
                log.error("Price below 0");
                return;
            }
            price = Double.parseDouble(priceField.getText());
        } catch (NumberFormatException e) {
            alert.showAndWait();
            log.error("Invalid price");
            return;
        }

        try {
            if (Integer.parseInt(amountField.getText()) < 1) {
                alert.showAndWait();
                log.error("Amount below 1");
                return;
            }
            quantity = Integer.parseInt(amountField.getText());
            if (quantity < 0){
                throw new SalesSystemException("Entered negative amount of product");
            }
        } catch (NumberFormatException e) {
            alert.showAndWait();
            log.error("Invalid amount");
            return;
        }

        //If the same id already exists in the warehouse, then it lets the user know via an alert and the id needs to be changed.
        if (Objects.equals(dao.findStockItemById(id),null))
            dao.saveStockItem(new StockItem(id, nameField.getText(), "", price, quantity));
        else {
            sameId.showAndWait();
            return;
        }
        refreshStockItemsTable();
    }

    /**
     * Disables "Confirm", "Cancel" and "Delete" buttons (grayed out/unusable).
     */
    @FXML
    private void disableWarehouseRowEditButtons(boolean disable) {
        this.confirmEdit.setDisable(disable);
        this.cancelEdit.setDisable(disable);
        this.deleteItem.setDisable(disable);
    }

    /**
     * This fills the input fields with data in the currently selected row.
     * Also enables "Confirm", "Cancel" and "Delete" buttons. Disables barcode field.
     */
    @FXML
    public void tableRowClicked(MouseEvent event) {
        disableWarehouseRowEditButtons(false);
        StockItem clickedItem = warehouseTableView.getSelectionModel().getSelectedItem();
        if (clickedItem != null) {
            barcodeField.setText(String.valueOf(clickedItem.getId()));
            amountField.setText(String.valueOf(clickedItem.getQuantity()));
            nameField.setText(String.valueOf(clickedItem.getName()));
            priceField.setText(String.valueOf(clickedItem.getPrice()));
            barcodeField.setDisable(true);
            addItem.setDisable(true);
        }
    }

    /**
     * Takes info from input fields when a row is selected in the table.
     * Changing information on the input fields and then clicking confirm, updates the currently selected row.
     * Also enables barcode field, disables the other buttons and clears the input fields after "Confirm" is clicked.
     * Item id cannot be changed.
     */
    @FXML
    public void confirmButtonClicked(ActionEvent event) {
        Alert alert = QuickAlert.createAlert(Alert.AlertType.WARNING,"Exception","Please check the input fields.","Make sure you are entering data in the correct format.");

        List<StockItem> currentItems = dao.findAllStockItems();
        long currentItemId = Long.parseLong(barcodeField.getText());
        for (StockItem item : currentItems) {
            if (item.getId() == currentItemId) {

                try {
                    if (Integer.parseInt(amountField.getText()) < 1) {
                        alert.showAndWait();
                        log.error("Amount below 1");
                        return;
                    }
                    item.setQuantity(Integer.parseInt(amountField.getText()));

                } catch (NumberFormatException e) {
                    alert.showAndWait();
                    log.error("Invalid amount");
                    return;
                }

                try {
                    if (Double.parseDouble(priceField.getText()) < 0) {
                        alert.showAndWait();
                        log.error("Price below 0");
                        return;
                    }
                    item.setPrice(Double.parseDouble(priceField.getText()));
                } catch (NumberFormatException e) {
                    alert.showAndWait();
                    log.error("Invalid price");
                    return;
                }

                item.setName(nameField.getText());

                dao.saveStockItem(item);
                refreshStockItemsTable();
                break;
            }
        }
        disableWarehouseRowEditButtons(true);
        barcodeField.clear();
        amountField.clear();
        nameField.clear();
        priceField.clear();
        barcodeField.setDisable(false);
        addItem.setDisable(false);
    }

    /**
     * Clears input fields, enables the barcode field and disables the other buttons when clicking "Cancel".
     */
    @FXML
    public void cancelButtonClicked(ActionEvent event) {
        disableWarehouseRowEditButtons(true);
        barcodeField.clear();
        amountField.clear();
        nameField.clear();
        priceField.clear();
        barcodeField.setDisable(false);
        addItem.setDisable(false);
    }


    /**
     * Deletes selected row from the table (also dao).
     * Also clears the input fields, enables barcode field and disables buttons.
     */
    @FXML
    public void deleteButtonClicked(ActionEvent event) {
        StockItem selectedID = warehouseTableView.getSelectionModel().getSelectedItem();
        dao.removeStockItem(selectedID);
        refreshStockItemsTable();
        disableWarehouseRowEditButtons(true);
        barcodeField.clear();
        amountField.clear();
        nameField.clear();
        priceField.clear();
        barcodeField.setDisable(false);
        addItem.setDisable(false);
    }
}
