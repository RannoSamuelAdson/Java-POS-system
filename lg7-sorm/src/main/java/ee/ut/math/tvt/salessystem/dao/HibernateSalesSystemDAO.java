package ee.ut.math.tvt.salessystem.dao;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.dataobjects.Transaction;

import javax.persistence.*;
import java.util.List;

public class HibernateSalesSystemDAO implements SalesSystemDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;


    public HibernateSalesSystemDAO () {
        // if you get ConnectException / JDBCConnectionException then you
        // probably forgot to start the database before starting the application
        emf = Persistence.createEntityManagerFactory("pos");
        em = emf.createEntityManager();
    }
    // TODO implement missing methods
    public void close () { // miks see siin on?
        em.close();
        emf.close();
    }

    @Override
    public List<StockItem> findAllStockItems() {
        TypedQuery<StockItem> query = em.createQuery("SELECT s FROM StockItem s", StockItem.class);
        return query.getResultList();
    }

    @Override
    public StockItem findStockItemById(long id) {
        return em.find(StockItem.class, id);
    }

    // saveStockItem kasutab transactionit, saveSoldItem mitte? kumb on õige?
    @Override
    public void saveStockItem(StockItem stockItem) {
        beginDAOTransaction();
        em.persist(stockItem);
        commitDAOTransaction();
    }

    @Override
    public void saveSoldItem(SoldItem item) {
//        EntityTransaction transaction = em.getTransaction();
        em.persist(item);
//        transaction.commit();
    }

    // transaction ning dataobjects.Transaction on erinevad
    @Override
    public void saveTransaction(Transaction transaction) {
        em.persist(transaction);
    }

    @Override
    public void beginDAOTransaction() {
        em.getTransaction().begin();
    }

    @Override
    public void rollbackDAOTransaction() {
        em.getTransaction().rollback();
    }

    @Override
    public void commitDAOTransaction() {
        em.getTransaction().commit();
    }

    @Override
    public void removeStockItem(StockItem stockItem) {
        beginDAOTransaction();
        em.remove(em.contains(stockItem) ? stockItem : em.merge(stockItem));
        commitDAOTransaction();
    }

    @Override
    public List<Transaction> findAllTransactions() {
        TypedQuery<Transaction> query = em.createQuery("SELECT t FROM Transaction t", Transaction.class);
        return query.getResultList();
    }
}
