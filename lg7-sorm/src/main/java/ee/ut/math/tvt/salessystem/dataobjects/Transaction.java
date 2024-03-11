package ee.ut.math.tvt.salessystem.dataobjects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;


@Entity
@Table(name = "transaction")
public class Transaction {

    private static final Logger log = LogManager.getLogger(Transaction.class);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "total")
    private double total; // total of transaction

    @OneToMany(mappedBy = "transaction")
    private List<SoldItem> soldItems;

    public Transaction() {
        // Empty constructor required by JPA
    }

    public Transaction(LocalDate date, LocalTime time, List<SoldItem> items) {
        this.date = date;
        this.time = time;
        for (SoldItem item: items) {
            item.setTransaction(this); // sets the transaction for every sold item to this transaction
        }
        this.soldItems = new ArrayList<>(items); // a clone of the list is needed to be saved so we can continue using the original without changing the transaction accidentally
        this.total = calculateSum(items);
        log.debug("Created new transaction: " + this);
    }

    private double calculateSum(List<SoldItem> items) { // returns sum of the transaction
        double sum = 0;
        for (SoldItem item : items) {
            sum += item.getSum();
        }
        return sum;
    }

    /**

     Checks if the transaction falls in between a start and end date
     @param start start date
     @param end end date
     @return true, if the given date is between or equal to either of the two conditional dates
     */
    public boolean isDateWithinTransactionRange(LocalDate start, LocalDate end) {
        return (!date.isBefore(start) && !date.isAfter(end));}

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<SoldItem> getSoldItems() {
        return soldItems;
    }

    public void setSoldItems(ArrayList<SoldItem> soldItems) {
        this.soldItems = soldItems;
    }

    @Override
    public String toString() {
        return  "Date: " + date + " | Time: " + time + " | Sum: " + total + " | Items: " + soldItems;
    }
}