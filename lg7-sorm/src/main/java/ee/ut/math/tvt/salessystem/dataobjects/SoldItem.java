package ee.ut.math.tvt.salessystem.dataobjects;

import javax.persistence.*;

/**

 Already bought StockItem. SoldItem duplicates name and price for preserving history.*/
@Entity
@Table(name = "sold_item")
public class SoldItem {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "stock_item_id")
    private Long stockItemId;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private double price;

    @ManyToOne
//    @JoinColumn(name = "transaction", nullable = false)
    private Transaction transaction;


    public SoldItem() {
    }

    public SoldItem(StockItem stockItem, int quantity) {
        this.stockItemId = stockItem.getId();
        this.name = stockItem.getName();
        this.price = stockItem.getPrice();
        this.quantity = quantity;
    }
    public SoldItem(StockItem stockItem){//for test classes
        this.stockItemId = stockItem.getId();
        this.name = stockItem.getName();
        this.price = stockItem.getPrice();
        this.quantity = stockItem.getQuantity();
        this.id = stockItem.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public double getSum() {
        return price * ((double) quantity);
    }

    public Long getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(Long stockItem) {
        this.stockItemId = stockItem;
    }

    @Override
    public String toString() {
        return String.format("(Id: " + stockItemId + " | Name: " + name + " | Quantity: " + quantity + " | Price: " + price + ")");
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}