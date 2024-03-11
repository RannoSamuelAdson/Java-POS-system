package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

class additem {


    @Mock
    private InMemorySalesSystemDAO mockDAO;

    private Logger logger;
    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
        logger = spy(LogManager.getLogger(ShoppingCart.class));
        //mockDAO = mock(InMemorySalesSystemDAO.class);

    }

    @Test
    void testAddingExistingItem() {
        /*
        check that adding an existing item increases the quantity
         */
        StockItem mockStockItem = new StockItem(1L,"racoons","from Kherson zoo",100,9);
        ShoppingCart cart = new ShoppingCart(mockDAO);
        when(mockDAO.findStockItemById(1L)).thenReturn(mockStockItem);
        StockItem stockItem = new StockItem(1L,"racoons","from Kherson zoo",100,2);
        SoldItem soldItem = new SoldItem(stockItem);
        cart.addItem(soldItem);//adding first
        int itemInCartQuantity1 = cart.findItemInCart(1L).getQuantity();
        cart.addToExistingItem(stockItem,5); //adding to that
        int itemInCartQuantity2 = cart.findItemInCart(1L).getQuantity();
        assertNotEquals(itemInCartQuantity1,itemInCartQuantity2); //checking
    }

    @Test
    void testAddingNewItem() {
        /*
           check that the new item is added to the shopping cart
         */
        StockItem mockStockItem = new StockItem(1L,"racoons","from Kherson zoo",100,9);
        ShoppingCart cart = new ShoppingCart(mockDAO);
        when(mockDAO.findStockItemById(1L)).thenReturn(mockStockItem);
        StockItem stockItem = new StockItem(1L,"racoons","from Kherson zoo",100,2);
        SoldItem soldItem = new SoldItem(stockItem);
        int size1 = cart.getItems().size();//original size
        cart.addItem(soldItem);
        int size2 = cart.getItems().size();//new size
        assertNotEquals(size1,size2); //checking
    }

    @Test
    void testAddingItemWithNegativeQuantity() {
        /*
        check that an exception is thrown if trying to add an item with a negative quantity
         */
        ShoppingCart cart = new ShoppingCart(mockDAO, logger);
        StockItem stockItem = new StockItem(1L, "racoons", "from Kherson zoo", 100, -2);
        SoldItem soldItem = new SoldItem(stockItem); // Negative quantity
        cart.addItem(soldItem);
        verify(logger).debug("ERROR"); // Verify that the logger's debug method was called with "ERROR"

    }

    @Test
    void testAddingItemWithQuantityTooLarge() {
        /*
        check that an exception is thrown if the quantity of the added item is larger than the quantity in the warehouse
         */
        StockItem mockStockItem = new StockItem(1L, "racoons", "from Kherson zoo", 5, 2); // Lower stock quantity
        when(mockDAO.findStockItemById(1L)).thenReturn(mockStockItem);
        StockItem mockStockItem2 = new StockItem(1L, "racoons", "from Kherson zoo", 5, 10);
        ShoppingCart cart = new ShoppingCart(mockDAO, logger);
        SoldItem soldItem = new SoldItem(mockStockItem2); // Quantity larger than stock
        cart.addItem(soldItem);
        verify(logger).debug("ERROR"); // Verify logger interaction



    }

    @Test
    void testAddingItemWithQuantitySumTooLarge() {
        /*
        check that an exception is thrown if the sum of the quantity of the added item and the
        quantity already in the shopping cart is larger than the quantity in the warehouse

         */
        StockItem mockStockItem = new StockItem(1L, "racoons", "from Kherson zoo", 10, 2); // Stock quantity
        when(mockDAO.findStockItemById(1L)).thenReturn(mockStockItem);
        ShoppingCart cart = new ShoppingCart(mockDAO, logger);
        SoldItem soldItem1 = new SoldItem(mockStockItem); // First addition
        cart.addItem(soldItem1);


        SoldItem soldItem2 = new SoldItem(mockStockItem, 6); // Second addition, total 11, more than stock
        cart.addToExistingItem(mockStockItem,8);

        verify(logger).debug("ERROR"); // Verify logger interaction
    }

}

/*
package ee.ut.math.tvt.salessystem.ui;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PurchaseControllerTest {

    private ConsoleUI ui;


    @Mock
    private InMemorySalesSystemDAO mockDAO;
    private InMemorySalesSystemDAO dao;
    @BeforeEach
    public void setup(){
        mockDAO = mock(InMemorySalesSystemDAO.class);
        dao = new InMemorySalesSystemDAO();
        ui = new ConsoleUI(mockDAO);
    }

    @AfterAll
    public void teardown(){//makes it so, that any possible changes made during testing don't carry on to the actual app
        mockDAO = new InMemorySalesSystemDAO();
        dao = new InMemorySalesSystemDAO();
    }
    @Test
    public void testAddingExistingItem(){
        //First I test that the quantity changed
        ArrayList<StockItem> snapshot = (ArrayList<StockItem>) dao.findStockItems();
        //System.out.println(mockDAO.getStockItemList().size());
        ConsoleUI ui = new ConsoleUI(dao);

        ui.addStock(888888888L,"racoon",0.1,5);//added an item to the stock
        //System.out.println(mockDAO.getStockItemList().get(4));
        int originalAmount = dao.findStockItem(888888888L).getQuantity();
        ui.editStock(888888888L,"racoon",0.1,9);//added more of the same item
        int newAmount = dao.findStockItem(888888888L).getQuantity();
        assertNotEquals(originalAmount, newAmount);//that the amount changed

        dao.setStockItemList(snapshot);//reverts the change

        //Now to test that the method wasn't used.
        ui = new ConsoleUI(mockDAO);
        ui.addStock(888888888L,"racoon",0.1,5);//added an item to the stock
        ui.editStock(888888888L,"racoon",0.1,9);//added more of the same item
        Mockito.verify(mockDAO, Mockito.times(0)).saveStockItem(new StockItem(888888888L,"racoon","",0.1,9));
        ui = new ConsoleUI(mockDAO);//reverts any changes made
    }


    @Test
    public void testAddingItemBeginsAndCommitsTransaction() {
        // Arrange
        //when(yourClassUnderTest.submitCurrentPurchase()).then();
        // Act - Call the method that should trigger the other method

        ShoppingCart cart = new ShoppingCart(mockDAO);
        cart.submitCurrentPurchase();


        // Assert - Verify that the expected method was called exactly once
        Mockito.verify(mockDAO, Mockito.times(1)).beginTransaction();
        Mockito.verify(mockDAO, Mockito.times(1)).commitTransaction();
    }

    @Test
    public void testAddingItemWithNegativeQuantity() {
        // Arrange
        long itemId = 888888888L;
        String itemName = "racoon";
        double itemPrice = 0.1;
        int itemQuantity = -5; // Negative quantity to trigger the error

        Logger log = mock(Logger.class);
        InMemorySalesSystemDAO dao = mock(InMemorySalesSystemDAO.class);
        ShoppingCart cart = new ShoppingCart(dao);
        ConsoleUI ui = new ConsoleUI(dao);
        ui.setLog(log); // Set the mock logger

        ArgumentCaptor<String> logMessageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        ui.addStock(itemId, itemName, itemPrice, itemQuantity); // This should trigger log.error

        // Verify
        verify(log, atLeastOnce()).error(logMessageCaptor.capture()); // Captures all log.error calls

        // Assert
        List<String> allLoggedErrors = logMessageCaptor.getAllValues();
        String expectedMessage = "In order to add, you need to add at least 1 item instead of " + itemQuantity;
        assertTrue(allLoggedErrors.stream().anyMatch(msg -> msg.contains(expectedMessage)),
                "Expected error message not logged");
    }

    @Test
    public void testAddingNewItem() {


        InMemorySalesSystemDAO dao = new InMemorySalesSystemDAO();
        ArrayList<StockItem> snapshot = (ArrayList<StockItem>) dao.findStockItems();
        int originalSize = dao.findStockItems().size();
        ConsoleUI ui = new ConsoleUI(dao);
        ui.addStock(888888888L,"racoon",0.1,5);//added the item

        int newsize = dao.findStockItems().size();

        assertNotEquals(originalSize, newsize);//that the number of elements in dao changed
        dao.setStockItemList(snapshot);//reverts the change
    }


}

 */