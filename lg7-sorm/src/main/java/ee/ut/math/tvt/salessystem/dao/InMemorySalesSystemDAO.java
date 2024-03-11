package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.dataobjects.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemorySalesSystemDAO implements SalesSystemDAO {

    private List<StockItem> stockItemList;
    private final List<SoldItem> soldItemList;
    private List<Transaction> pastTransactions;

    public InMemorySalesSystemDAO() {
        List<StockItem> items = new ArrayList<StockItem>();
        items.add(new StockItem(1L, "Lays chips", "Potato chips", 11.0, 5));
        items.add(new StockItem(2L, "Chupa-chups", "Sweets", 8.0, 8));
        items.add(new StockItem(3L, "Frankfurters", "Beer sauseges", 15.0, 12));
        items.add(new StockItem(4L, "Free Beer", "Student's delight", 0.0, 100));
        this.stockItemList = items;
        this.soldItemList = new ArrayList<>();
        this.pastTransactions = new ArrayList<>();
    }



    public void setStockItemList(List<StockItem> stockItemList){
        this.stockItemList = stockItemList;
    }

    public void saveTransaction(Transaction transaction) {
        this.pastTransactions.add(transaction);
    }

    @Override
    public List<StockItem> findAllStockItems() {
        return stockItemList;
    }

    @Override
    public StockItem findStockItemById(long id) {
        for (StockItem item : stockItemList) {
            if (Objects.equals(item.getId(), id))
                return item;
        }
        return null;
    }

    @Override
    public void saveSoldItem(SoldItem item) {
        soldItemList.add(item);
    }
    @Override
    public void saveStockItem(StockItem stockItem) {
        stockItemList.add(stockItem);
    }

    @Override
    public void beginDAOTransaction() {

    }

    @Override
    public void rollbackDAOTransaction() {//Activates when there is an issue with updating the warehouse
        //clears all changes made before submitting the shopping cart.
        soldItemList.clear();
    }

    @Override

    public void commitDAOTransaction() {

        //Updates the warehouse stock when bought
        //triggered by ShoppingCart class

        //saves the changes
//        this.pastTransactions.add(new Transaction(LocalDate.now(),LocalTime.now(),soldItemList));

        // changes saved, we can now clear soldItemList
        soldItemList.clear();
    }

    @Override
    public void removeStockItem(StockItem stockItem) {
        stockItemList.remove(stockItem);
    }

    @Override
    public List<Transaction> findAllTransactions() {
        return pastTransactions;
    }
    public void setPastTransactions(){
        this.pastTransactions = new ArrayList<>();
    }
}
