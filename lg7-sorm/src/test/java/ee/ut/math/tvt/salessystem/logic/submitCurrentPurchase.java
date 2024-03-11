package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import org.junit.jupiter.api.*;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;


import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class submitCurrentPurchase {
    @Mock
    private InMemorySalesSystemDAO mockDAO;
    private InMemorySalesSystemDAO dao;
    @BeforeEach
    public void setup(){
        mockDAO = mock(InMemorySalesSystemDAO.class);
        dao = new InMemorySalesSystemDAO();

    }

    @Test
    void testSubmittingCurrentPurchaseDecreasesStockItemQuantity() {
        /*
        check that submitting the current purchase decreases the quantity of all StockItems
         */
        InMemorySalesSystemDAO dao = new InMemorySalesSystemDAO();//The teacher allowed to use it since  it is no longer part of the app
        StockItem stockitem1 = new StockItem(5L,"racoons","from Kherson zoo",100,9);
        dao.saveStockItem(stockitem1);
        ShoppingCart cart = new ShoppingCart(dao);
        //when(mockDAO.findStockItemById(5L)).thenReturn(mockStockItem);
        StockItem stockItem2 = new StockItem(5L,"racoons","from Kherson zoo",100,2);
        SoldItem soldItem = new SoldItem(stockItem2);
        int size1 = dao.findStockItemById(5L).getQuantity();
        cart.addItem(soldItem);
        cart.submitCurrentPurchase();
        int size2 = dao.findStockItemById(5L).getQuantity();
        assertNotEquals(size1,size2); //checking
    }

    @Test
    void testSubmittingCurrentPurchaseBeginsAndCommitsTransaction() {
        /*
        check that submitting the current purchase calls the methods of beginDAOTransaction() and commitDAOTransaction()

         */

        // Arrange
        //when(yourClassUnderTest.submitCurrentPurchase()).then();
        // Act - Call the method that should trigger the other method

        ShoppingCart cart = new ShoppingCart(mockDAO);
        cart.submitCurrentPurchase();


        // Assert - Verify that the expected method was called exactly once
        Mockito.verify(mockDAO, Mockito.times(1)).beginDAOTransaction();
        Mockito.verify(mockDAO, Mockito.times(1)).commitDAOTransaction();

    }

    @Test
    void testSubmittingCurrentOrderCreatesHistoryItem() {
        /*
        check that a new HistoryItem is saved and that it contains the correct SoldItems
         */
        InMemorySalesSystemDAO dao = new InMemorySalesSystemDAO();//The teacher allowed to use it since  it is no longer part of the app
        StockItem stockitem1 = new StockItem(5L,"racoons","from Kherson zoo",100,9);
        dao.saveStockItem(stockitem1);
        ShoppingCart cart = new ShoppingCart(dao);
        //when(mockDAO.findStockItemById(5L)).thenReturn(mockStockItem);
        StockItem stockItem2 = new StockItem(5L,"racoons","from Kherson zoo",100,2);
        SoldItem soldItem = new SoldItem(stockItem2);
        int size1 = dao.findAllTransactions().size();
        cart.addItem(soldItem);
        cart.submitCurrentPurchase();
        int size2 = dao.findAllTransactions().size();

        assertNotEquals(size1,size2); //checking
    }

    @Test
    void testSubmittingCurrentOrderSavesCorrectTime() {
        /*
        check that the timestamp on the created HistoryItem is set correctly (for example has only a small difference to the current time)
         */
        InMemorySalesSystemDAO dao = new InMemorySalesSystemDAO();//The teacher allowed to use it since  it is no longer part of the app
        StockItem stockitem1 = new StockItem(5L,"racoons","from Kherson zoo",100,9);
        dao.saveStockItem(stockitem1);
        ShoppingCart cart = new ShoppingCart(dao);
        //when(mockDAO.findStockItemById(5L)).thenReturn(mockStockItem);
        StockItem stockItem2 = new StockItem(5L,"racoons","from Kherson zoo",100,2);
        SoldItem soldItem = new SoldItem(stockItem2);
        cart.addItem(soldItem);
        cart.submitCurrentPurchase();

        LocalTime time1 = dao.findAllTransactions().get(0).getTime();
        LocalTime time2 = LocalTime.now();
        Duration duration = Duration.between(time1,time2);

        assertTrue(Math.abs(duration.toMinutes()) <= 1); //checking if the two timestamps differ by a minute at most
    }

    @Test
    void testCancellingOrder() {
        /*
        check that canceling an order (with some items) and then submitting a new order (with some different items) only saves the items from the new order (with canceled items are discarded)
         */
        InMemorySalesSystemDAO dao = new InMemorySalesSystemDAO();//The teacher allowed to use it since  it is no longer part of the app
        StockItem stockitem1 = new StockItem(5L,"racoons","from Kherson zoo",100,9);
        dao.saveStockItem(stockitem1);
        ShoppingCart cart = new ShoppingCart(dao);
        //when(mockDAO.findStockItemById(5L)).thenReturn(mockStockItem);
        StockItem stockItem2 = new StockItem(5L,"racoons","from Kherson zoo",100,2);
        SoldItem soldItem = new SoldItem(stockItem2);

        int size1 = dao.findStockItemById(5L).getQuantity();//for the cancelled purchase
        cart.addItem(soldItem);
        cart.cancelCurrentPurchase();
        int size2 = dao.findStockItemById(5L).getQuantity();//should be unchanged

        cart.addItem(soldItem);
        cart.submitCurrentPurchase();
        int size3 = dao.findStockItemById(5L).getQuantity();//now should be changed

        assertTrue((size1 == size2) && (size2 != size3)); //checking

    }

    @Test
    void testCancellingOrderQuanititesUnchanged() {
        /*
         check that after canceling an order the quantities of the related StockItems are not changed
        */
        InMemorySalesSystemDAO dao = new InMemorySalesSystemDAO();//The teacher allowed to use it since  it is no longer part of the app
        StockItem stockitem1 = new StockItem(5L,"racoons","from Kherson zoo",100,9);
        dao.saveStockItem(stockitem1);
        ShoppingCart cart = new ShoppingCart(dao);
        //when(mockDAO.findStockItemById(5L)).thenReturn(mockStockItem);
        StockItem stockItem2 = new StockItem(5L,"racoons","from Kherson zoo",100,2);
        SoldItem soldItem = new SoldItem(stockItem2);
        int size1 = dao.findStockItemById(5L).getQuantity();
        cart.addItem(soldItem);
        cart.cancelCurrentPurchase();
        int size2 = dao.findStockItemById(5L).getQuantity();
        assertEquals(size1,size2); //checking
    }


}