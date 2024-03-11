package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.Transaction;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "History" in the menu).
 */
public class HistoryController implements Initializable {

    private static final Logger log = LogManager.getLogger(HistoryController.class);
    private final SalesSystemDAO dao;

    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private TableView<Transaction> purchaseTableView;
    @FXML
    private TableView<SoldItem> itemsTableView;
    @FXML
    private TableColumn<Transaction, String> transactionDateCol;
    @FXML
    private TableColumn<Transaction, String> transactionTimeCol;
    @FXML
    private TableColumn<Transaction, Double> transactionSumCol;
    @FXML
    private TableColumn<SoldItem, Long> soldItemIdCol;

    public HistoryController(SalesSystemDAO dao) {
        this.dao = dao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        transactionDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTimeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        transactionSumCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        soldItemIdCol.setCellValueFactory(new PropertyValueFactory<>("stockItemId"));
    }

    private void refreshPurchaseHistoryTable(List<Transaction> transactionsList) {
        purchaseTableView.setItems(FXCollections.observableList(transactionsList));
        purchaseTableView.refresh();
    }

    private void refreshPurchaseItemsTable(Transaction transaction) {
        itemsTableView.setItems(FXCollections.observableList(transaction.getSoldItems()));
        itemsTableView.refresh();
    }

    public void showBetweenDatesButtonClicked() {
        LocalDate startingDate = startDate.getValue();
        LocalDate endingDate = endDate.getValue();

        if (startingDate == null || endingDate == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Exception");
            alert.setHeaderText("Please check the input fields.");
            alert.setContentText("Make sure you entered the dates in the correct format.");

            alert.showAndWait();
            return;
        }

        if (endingDate.isBefore(startingDate)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Exception");
            alert.setHeaderText("Please check the input fields.");
            alert.setContentText("Make sure the end date is after the start date.");

            alert.showAndWait();
            return;
        }

        List<Transaction> fittingTransactions = new ArrayList<>();
        for (Transaction t: dao.findAllTransactions()) {
            if (!t.isDateWithinTransactionRange(startingDate,endingDate)) continue;
            fittingTransactions.add(t);
        }
        refreshPurchaseHistoryTable(fittingTransactions);
        log.info("Successfully fetched fitting transactions");
    }

    public void showLast10ButtonClicked() {
        List<Transaction> all = dao.findAllTransactions();
        List<Transaction> last10 = new ArrayList<>();

        int numberOfTransactions = all.size();

        if (!all.isEmpty()) {
            for (int i = Math.max(numberOfTransactions - 10, 0); i < numberOfTransactions; i++) {
                last10.add(all.get(i));
            }
            refreshPurchaseHistoryTable(last10);
            log.info("Successfully fetched last 10 transactions");
        }
    }

    public void showAllButtonClicked() {
        refreshPurchaseHistoryTable(dao.findAllTransactions());
        log.info("Successfully fetched all transactions");
    }

    @FXML
    public void tableRowClicked(MouseEvent event) {
        Transaction clickedItem = purchaseTableView.getSelectionModel().getSelectedItem();
        if (clickedItem != null) {
            refreshPurchaseItemsTable(clickedItem);
        }
    }
}
