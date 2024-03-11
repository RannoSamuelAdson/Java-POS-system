package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.dataobjects.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

public class ShoppingCart {

    private Logger log = LogManager.getLogger(ShoppingCart.class);

    private final SalesSystemDAO dao;
    private ArrayList<SoldItem> items = new ArrayList<>();

    public ShoppingCart(SalesSystemDAO dao) {
        this.dao = dao;
    }

    public ShoppingCart(SalesSystemDAO dao, Logger log){//Used in test classes
        this.dao = dao;
        this.log = log;
    }
    public ArrayList<SoldItem> getItems() {
        return items;
    }

    /**
     * Add new SoldItem to table.
     */
    public void addItem(SoldItem item) {

        try {
            try {
                if (Objects.equals(dao.findStockItemById(item.getId()), null))
                    throw new SalesSystemException("The database has no item of " + item.getName());
                if (item.getQuantity() > dao.findStockItemById(item.getId()).getQuantity()) {
                    throw new SalesSystemException("The available amount of " + String.valueOf(dao.findStockItemById(item.getId()).getQuantity()) +
                            " is larger than amount bought of " + String.valueOf(item.getQuantity()) + " for the item " + item.getName());
                }
                if (item.getQuantity() < 0)
                    throw new SalesSystemException("You cannot buy less than 0 items");
            }
            catch (NullPointerException e){}


            items.add(item);
            log.debug("Added " + item.getName() + " quantity of " + item.getQuantity());
        }
        catch (SalesSystemException e){
            log.debug("ERROR");//Triggered only when exception happens. Meant for test classes.
            log.debug(e.getMessage());
            System.out.println(e.getMessage());

        }

    }
    public void addToExistingItem(StockItem stockItem, int quantity) {
        // gets triggered when you add already existing items to the shopping cart
        // triggered by PurchaseController
        // basically updates the quantity of the given stockItem by the given int amount.

        SoldItem item = findItemInCart(stockItem.getId());
        if (!Objects.equals(item, null)) {
            this.items.remove(item);//removes element
            item.setQuantity(item.getQuantity()+quantity);//sets new quantity
            addItem(item);//re-enters the element
        } else log.info("Couldn't find such element, no change to cart");
    }
    public ArrayList<SoldItem> getAllItems() {
        return items;
    }
    public SoldItem findItemInCart(long id) {
        //finds the item in the cart and returns it(if existing);
        for (SoldItem item : items) {
            if (Objects.equals(item.getStockItemId(), id)) {
                return item;
            }
        }
        return null;
    }

    public void cancelCurrentPurchase() {
        clearCart();
    }
    public void clearCart(){
        items.clear();
    }
    public void submitCurrentPurchase() {
        dao.beginDAOTransaction();
        try {
            dao.saveTransaction(new Transaction(LocalDate.now(), LocalTime.now(),items));
            for (SoldItem item : items) {
                StockItem itemInStock = dao.findStockItemById(item.getStockItemId());
                if (!Objects.equals(itemInStock, null)){//if this bought element is in the warehouse
                    int bought = item.getQuantity();
                    itemInStock.setQuantity(itemInStock.getQuantity()-bought); //sets new quantity
                }
                else //Should not be ever triggered. Mainly for extra redundancy. Also, because I was bored.
                    //ShoppingCart will catch it
                    throw new SalesSystemException("The item " + item.getName() + " is not in the warehouse. You can't buy it.");
                dao.saveSoldItem(item);
            }
            dao.commitDAOTransaction();
            clearCart();
        } catch (Exception e) {
            dao.rollbackDAOTransaction();
            log.fatal("Unexpected error, purchase has been cancelled. " +
                    "Encountered when trying to remove inventory from warehouse. Further details are: " + e.getMessage());
        }
    }

    public double getCartSum() {
        double sum = 0;
        int length = items.size();

        for (int i = 0; i < length; i++) {
            sum += items.get(i).getSum();
        }

        return sum;
    }
}
