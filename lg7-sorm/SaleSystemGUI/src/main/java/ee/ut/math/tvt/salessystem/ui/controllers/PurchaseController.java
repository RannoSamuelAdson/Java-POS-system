package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.ui.QuickAlert;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "Point-of-sale" in the menu). Consists of the purchase menu,
 * current purchase dialog and shopping cart table.
 */
public class PurchaseController implements Initializable {

    private static final Logger log = LogManager.getLogger(PurchaseController.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart shoppingCart;

    @FXML
    private Button newPurchase;
    @FXML
    private Button submitPurchase;
    @FXML
    private Button cancelPurchase;
    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private ChoiceBox<StockItem> nameChoiceBox;
    @FXML
    private TextField priceField;
    @FXML
    private Button addItemButton;
    @FXML
    private TableView<SoldItem> purchaseTableView;
    @FXML
    private TextField totalsumField;
    @FXML
    private TableColumn<SoldItem, Long> cartIdCol;

    public PurchaseController(SalesSystemDAO dao, ShoppingCart shoppingCart) {
        this.dao = dao;
        this.shoppingCart = shoppingCart;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cartIdCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStockItemId()));
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        purchaseTableView.setItems(FXCollections.observableList(shoppingCart.getAllItems()));
        refreshNameChoiceBox();
        disableProductField(true);

        this.nameChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(StockItem object) {
                if (!Objects.equals(object, null))
                    return object.getName();
                else
                    return "";
            }

            @Override
            public StockItem fromString(String string) {
                return null;
            }
        });

        this.barCodeField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    fillInputsByStockItemBarcodeField();
                }
            }
        });

        this.nameChoiceBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    fillInputsByStockItemNameField();
                }
            }
        });

        this.barCodeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    barCodeField.setText(newValue.replaceAll("[\\D+]", ""));
                }
            }
        });

        this.quantityField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    quantityField.setText(newValue.replaceAll("[\\D+]", ""));
                }
            }
        });

        this.priceField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*([.]\\d*)?")) {
                    priceField.setText(newValue.replaceAll("[\\D+]", ""));
                }
            }
        });

    }

    /** Event handler for the <code>new purchase</code> event. */
    @FXML
    protected void newPurchaseButtonClicked() {
        log.info("New sale process started");
        try {
            enablePurchaseInputs();
            refreshNameChoiceBox();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the <code>cancel purchase</code> event.
     */
    @FXML
    protected void cancelPurchaseButtonClicked() {
        log.info("Sale cancelled");
        try {
            shoppingCart.cancelCurrentPurchase();
            disablePurchaseInputs();
            purchaseTableView.refresh();
            refreshNameChoiceBox();
            submitPurchase.setDisable(true);
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the <code>submit purchase</code> event.
     */
    @FXML
    protected void submitPurchaseButtonClicked() {
        log.info("Sale complete");
        try {
            log.debug("Contents of the current basket: " + shoppingCart.getAllItems());
            shoppingCart.submitCurrentPurchase();
            disablePurchaseInputs();
            purchaseTableView.refresh();
            refreshNameChoiceBox();
        } catch (SalesSystemException e) {//NOTE, you can get the input for some popup errors here
            log.error(e.getMessage(), e);
        }
    }

    // switch UI to the state that allows to proceed with the purchase
    private void enablePurchaseInputs() {
        resetProductField();
        disableProductField(false);
        cancelPurchase.setDisable(false);
        newPurchase.setDisable(true);
    }

    // switch UI to the state that allows to initiate new purchase
    private void disablePurchaseInputs() {
        resetProductField();
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        newPurchase.setDisable(false);
        disableProductField(true);
    }

    private void fillInputsByStockItemBarcodeField() {
        StockItem stockItem = getStockItemByBarcodeField();
        if (!Objects.equals(stockItem, null)) {
            nameChoiceBox.setValue(stockItem);
            priceField.setText(String.valueOf(stockItem.getPrice()));
        } else {
            resetProductField();
        }
    }

    private void fillInputsByStockItemNameField() {
        StockItem stockItem = nameChoiceBox.getValue();
        if (!Objects.equals(stockItem, null)) {
            barCodeField.setText(String.valueOf(stockItem.getId()));
            priceField.setText(String.valueOf(stockItem.getPrice()));
        } else {
            resetProductField();
        }
    }

    // Search the warehouse for a StockItem with the bar code entered
    // to the barCode textfield.

    private StockItem getStockItemByBarcodeField() {
        try {
            long code = Long.parseLong(barCodeField.getText());
            return dao.findStockItemById(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Add new item to the cart.
     */
    @FXML
    public void addToCartButtonClicked() {
        StockItem stockItem = getStockItemByBarcodeField();
        if (Objects.equals(stockItem,null)) { // if the field displays an existing element in the warehouse
            return;
        }

        // check for valid barcode and quantity
        int quantity;
        long barcode;
        try {
            barcode = Long.parseLong(barCodeField.getText());
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) { // should never really be thrown as input is validated to be only whole numbers
            QuickAlert.showAlert(Alert.AlertType.WARNING, "Exception", "Invalid barcode/quantity", "These can only be integers. Please correct any mistakes.");
            return;
        }

        boolean isItemInCart = !Objects.equals(shoppingCart.findItemInCart(barcode), null); // if item is in the shopping cart
        boolean warehouseQuantityAvailable;

        if (isItemInCart) {
            int existingQuantity = shoppingCart.findItemInCart(barcode).getQuantity(); //amount already in shopping cart
            warehouseQuantityAvailable = quantity + existingQuantity <= dao.findStockItemById(barcode).getQuantity();
        } else {
            warehouseQuantityAvailable = quantity <= dao.findStockItemById(barcode).getQuantity();
        }

        if (warehouseQuantityAvailable) {
            if (quantity > 0) {
                if (isItemInCart) {
                    shoppingCart.addToExistingItem(stockItem, quantity);
                } else {
                    submitPurchase.setDisable(false); // allow ending transaction
                    shoppingCart.addItem(new SoldItem(stockItem, quantity));
                }
                totalsumField.setText(String.valueOf(shoppingCart.getCartSum()));
                }
            else { // input fields should once again guarantee that no negative numbers are allowed, but a failsafe doesn't hurt
                QuickAlert.showAlert(Alert.AlertType.WARNING, "Exception", "Wrong item amount.", "Item amount is either <= 0. Please input a correct amount.");
            }
        }
        else {
            QuickAlert.showAlert(Alert.AlertType.WARNING, "Exception", "Max quantity exceeded.", "Item amount is bigger than item quantity in the warehouse.");
        }

        purchaseTableView.refresh();
        refreshNameChoiceBox();
    }

    /**
     * Sets whether the product component is enabled.
     */
    private void disableProductField(boolean disable) {
        this.addItemButton.setDisable(disable);
        this.barCodeField.setDisable(disable);
        this.quantityField.setDisable(disable);
        this.nameChoiceBox.setDisable(disable);
        this.priceField.setDisable(disable);
    }

    /**
     * Reset dialog fields.
     */
    private void resetProductField() {
        barCodeField.setText("");
        quantityField.setText("1");
        nameChoiceBox.setValue(null);
        priceField.setText("");
        if (shoppingCart.getAllItems().isEmpty()){
            totalsumField.setText("0");
        }
        refreshNameChoiceBox();
    }

    private void refreshNameChoiceBox() {
        // we try to keep the existing choice when refreshing
        StockItem choice = nameChoiceBox.getValue();
        nameChoiceBox.setItems(FXCollections.observableList(dao.findAllStockItems()));
        if (!Objects.equals(choice,null)) {
            if (!Objects.equals(dao.findStockItemById(choice.getId()),null)) nameChoiceBox.setValue(choice);
        }
    }
}
