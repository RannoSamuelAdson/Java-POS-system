package ee.ut.math.tvt.salessystem.ui;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.dataobjects.Transaction;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.io.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple CLI (limited functionality).
 */
public class ConsoleUI {
    private Logger log = LogManager.getLogger(ConsoleUI.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart cart;

    public ConsoleUI(SalesSystemDAO dao) {
        this.dao = dao;
        cart = new ShoppingCart(dao);
    }

    public static void main(String[] args) throws Exception {

        SalesSystemDAO dao = new HibernateSalesSystemDAO();
        ConsoleUI console = new ConsoleUI(dao);

        console.run();
    }

    /**
     * Run the sales system CLI.
     */
    public void run() throws IOException {
        System.out.println("""
        ===========================
        =       Sales System      =
        ===========================""");
        printUsage();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            processCommand(in.readLine().trim());
        }
    }

    private void showStock() {
        List<StockItem> stockItems = dao.findAllStockItems();
        System.out.println("-------------------------");

        for (StockItem si : stockItems) {
            SoldItem itemInCart = cart.findItemInCart(si.getId());

            if (!Objects.equals(itemInCart, null)){//if this item is in cart. The warehouse is unchanged until truly purchased but display doesn't have to be
                System.out.println(si.getId() + " " + si.getName() + " " + si.getPrice() + " Euro (" + (si.getQuantity() - itemInCart.getQuantity()) + " items)");
            }
            else {
                System.out.println(si.getId() + " " + si.getName() + " " + si.getPrice() + " Euro (" + si.getQuantity() + " items)");
            }
        }
        if (stockItems.size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showCart() {
        System.out.println("-------------------------");
        for (SoldItem si : cart.getAllItems()) {
            System.out.println(si.getName() + " " + si.getPrice() + " Euro (" + si.getQuantity() + " items)");
        }
        if (cart.getAllItems().size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("Total: " + cart.getCartSum());
        System.out.println("-------------------------");
    }

    private void printUsage() {
        System.out.println("-------------------------");
        System.out.println("Usage:");
        System.out.println("h\t\tShow this help");
        System.out.println("w\t\tShow warehouse contents");
        System.out.println("s IDX NA PR NR \tAdd item to the warehouse with index IDX (change name NA (add quotation marks), price PR, amount NR)");
        System.out.println("d IDX \t\t\tDelete warehouse item with index IDX");
        System.out.println("e IDX NA PR NR \tEdit warehouse item with index IDX (change name NA (add quotation marks, price PR, amount NR)");
        System.out.println("a IDX NR \t\tAdd NR of stock item with index IDX to the cart");
        System.out.println("c\t\tShow cart contents");
        System.out.println("p\t\tPurchase the shopping cart");
        System.out.println("r\t\tReset the shopping cart");
        System.out.println("v\t\tView the entire purchase history");
        System.out.println("v10\t\tView the last 10 purchases made");
        System.out.println("t\t\tDisplay development team info");
        System.out.println("-------------------------");
    }

    private void printTeamInfo() {
        Properties properties = new Properties();
        try (InputStream input = ConsoleUI.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            log.error("application.properties file not found!");
            return;
        }
        //Read guidetext from properties
        String teamNameText = properties.getProperty("team.nameText");
        String teamLeaderText = properties.getProperty("team.leaderText");
        String teamleaderemailText = properties.getProperty("team.leaderemailText");
        String teamMembersText = properties.getProperty("team.membersText");

        //Read team info from properties
        String teamName = properties.getProperty("team.name");
        String teamLeader = properties.getProperty("team.leader");
        String teamContact = properties.getProperty("team.contact.email");
        String teamMember1 = properties.getProperty("team.member1");
        String teamMember2 = properties.getProperty("team.member2");
        String teamMember3 = properties.getProperty("team.member3");
        String teamMember4 = properties.getProperty("team.member4");
        System.out.println("-------------------------");
        System.out.println(teamNameText + ": " + teamName);
        System.out.println(teamLeaderText + ": " + teamLeader);
        System.out.println(teamleaderemailText + ": " + teamContact);
        System.out.println(teamMembersText + " " + teamMember1 + ", " + teamMember2 + ", " + teamMember3 + ", " + teamMember4);
        System.out.println("-------------------------");
    }

    /**
     * Deletes item from the warehouse. Uses id to find the correct item.
     */
    private void deleteStock(long id) {
        StockItem item = dao.findStockItemById(id);
        dao.removeStockItem(item);
        System.out.println("-------------------------");
    }

    /**
     * Takes data from the command line input and finds an item from the warehouse using id.
     * Saves new item data.
     * Currently item name cannot contain any spaces.
     */
    public void editStock(Long id, String name, Double price, Integer amount) {
        List<StockItem> currentItems = dao.findAllStockItems();
        boolean noId = true;
        for (StockItem item : currentItems) {
            if (Objects.equals(item.getId(), id)) {
                    try {
                        if (amount > 0 && price >= 0) {
                            item.setQuantity(amount);
                            item.setPrice(price);
                            item.setName(name);
                            noId = false;
                            dao.saveStockItem(item);
                            break;
                        }
                        else
                            log.error("Invalid input for amount/price. Changes have not been saved.");

                    }

                    catch (NumberFormatException e) {
                        log.error("Invalid amount");
                    }

            }
        }
        if (noId)
            log.error("Id not found. Please try again.");
    }

    /**
     * Adds new stock to the warehouse.
     *
     */
    public void addStock(Long id, String name, Double price, Integer amount) {
        try {
            if (id >= 0 && price >= 0 && amount >= 1) {
                //If the same id already exists in the warehouse, then it lets the user know that the id needs to be changed.
                if (dao.findStockItemById(id) == null)
                    dao.saveStockItem(new StockItem(id, name, "", price, amount));
                else
                    log.error("Identical id/barcode already exists. Please try again.");
            }
            else if (amount < 1){
                log.error("In order to add, you need to add at least 1 item instead of " + amount);
            }
        } catch (NumberFormatException e) {
            log.error("Invalid barcode");
        }
    }

    protected void addToCart(long idx, int amount) {
        StockItem item = dao.findStockItemById(idx);
        if (item != null && item.getQuantity() >= amount && amount > 0) { //if there is an item and if it has enough quantity
            //dao.setNewQuantity(idx, amount);


            //detecting if the given element is already in the shopping cart
            List<SoldItem> cartItems = cart.getAllItems();
            boolean isItemInCart = false;
            for (SoldItem el:cartItems) {
                if (Objects.equals(el.getStockItemId(), idx)) {
                    isItemInCart = true;
                    break;
                }
            }

            if (isItemInCart){//updating the number in the cart
                if ((item.getQuantity() - cart.findItemInCart(item.getId()).getQuantity()) >= amount){//ensure that warehouse has the stock
                    cart.addToExistingItem(dao.findStockItemById(idx),amount);
                    System.out.println("Done");
                }
                else {
                    log.error("Purchase unsuccessful.");
                    log.error("The amount of " + amount + " exeeded the warehouse invertory of " + (dao.findStockItemById(idx).getQuantity() - cart.findItemInCart(idx).getQuantity()) + ".");
                }
            }
            else {//adding a new item to the cart
                cart.addItem(new SoldItem(item, Math.min(amount, item.getQuantity())));
                System.out.println("Done");
            }
        }
        else {
            log.error("Purchase unsuccessful.");
            if (Objects.equals(item, null))
                log.error("no stock item with id " + idx);
            else if (amount < 1){
                log.error("In order to buy, you need to buy at least 1 item instead of " + amount);
            }
            else
                log.error("The amount of " + amount + " exeeded the warehouse invertory of " + dao.findStockItemById(idx).getQuantity() + ".");
        }
    }


    private void processCommand(String command) {
        // Regex from https://stackoverflow.com/a/7804472
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
        while (m.find()) {
            list.add(m.group(1).replace("\"",""));
        }
        String[] c = list.toArray(String[]::new);

        switch (c[0]) {
            case "h" -> printUsage();
            case "q" -> System.exit(0);
            case "w" -> showStock();
            case "c" -> showCart();
            case "p" -> {
                if (!cart.getAllItems().isEmpty())
                    cart.submitCurrentPurchase();
                else
                    System.out.println("Cart is empty.");
            }
            case "r" -> cart.cancelCurrentPurchase();
            case "t" -> printTeamInfo();
            case "a" -> {
                if (c.length == 3) {
                    try {
                        long idx = Long.parseLong(c[1]);
                        int amount = Integer.parseInt(c[2]);
                        addToCart(idx, amount);
                    } catch (SalesSystemException | NoSuchElementException | NumberFormatException e) {
                        log.error("Encountered an error:");
                        log.error(e.getMessage(), e);
                        return;
                    }
                }
            }
            case "d" -> {
                if (c.length == 2) {
                    try {
                        deleteStock(Long.parseLong(c[1]));
                    } catch (NumberFormatException e) {
                        log.error("Encountered an error:");
                        log.error(e.getMessage(), e);
                        return;
                    }
                }
            }
            case "e" -> {
                if (c.length == 5) {
                    try {
                        editStock(Long.parseLong(c[1]), c[2], Double.parseDouble(c[3]), Integer.parseInt(c[4]));
                    } catch (NumberFormatException e) {
                        log.error("Encountered an error:");
                        log.error(e.getMessage(), e);
                        return;
                    }
                } else {
                    log.error("Incorrect number of arguments");
                    return;
                }
            }

            case "s" -> {
                if (c.length == 5) {
                    try {
                        addStock(Long.parseLong(c[1]), c[2], Double.parseDouble(c[3]), Integer.parseInt(c[4]));
                    } catch (NumberFormatException e) {
                        log.error("Encountered an error:");
                        log.error(e.getMessage(), e);
                        return;
                    }

                } else {
                    log.error("Incorrect number of arguments");
                    return;
                }
            }

            case "v" -> {
                List<Transaction> transactions = dao.findAllTransactions();
                if (!transactions.isEmpty()) {
                    for (Transaction item : transactions)
                        System.out.println(item);
                }
                else
                    System.out.println("No purchase history yet.");

            }
            case "v10" -> {
                List<Transaction> transactions = dao.findAllTransactions();
                int size = transactions.size();
                if (!transactions.isEmpty()) {
                    for (int i = Math.max(size - 10, 0); i < size; i++) {
                        String item = String.valueOf(transactions.get(i));
                        System.out.println(item);
                    }
                }
                else
                    System.out.println("No purchase history yet.");
            }

            default -> {
                log.error("Unknown command");
                return;
            }
        }
        System.out.println("Command executed. ");
    }

}
