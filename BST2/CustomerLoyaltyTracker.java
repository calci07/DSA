package BST2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Customer {
    int customerId;
    int age;
    String gender;
    boolean isLoyaltyMember;
    List<Transaction> transactions;

    int recency;
    int frequency;
    double monetary;

    public Customer(int customerId, int age, String gender, boolean isLoyaltyMember) {
        this.customerId = customerId;
        this.age = age;
        this.gender = gender;
        this.isLoyaltyMember = isLoyaltyMember;
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void RFMCalculation(LocalDate currentDate) {
        System.out.println("====================================================");
        System.out.println("       Calculating RFM for Customer ID: " + this.customerId);
        System.out.println("====================================================");

        if (transactions.isEmpty()) {
            System.out.println("No transactions for Customer ID: " + this.customerId + " to calculate RFM.");
            return;
        }

        System.out.println("Total Transactions: " + transactions.size());
        System.out.println();

        this.frequency = transactions.size();
        this.monetary = transactions.stream().mapToDouble(t -> t.totalPrice).sum();

        LocalDate lastPurchase = transactions.stream()
                .map(Transaction::getPurchaseDate)
                .max(LocalDate::compareTo)
                .orElse(currentDate);

        this.recency = (int) java.time.temporal.ChronoUnit.DAYS.between(lastPurchase, currentDate);
        System.out.println("-----------------------------------------------");
        System.out.println("     RFM calculated for Customer ID: " + this.customerId);
        System.out.println("-----------------------------------------------");
    }
}

class Transaction {
    String productType;
    String sku;
    double rating;
    String orderStatus;
    String paymentMethod;
    double totalPrice;
    double unitPrice;
    int quantity;
    LocalDate purchaseDate;
    String shippingType;
    String addOnsPurchased;
    double addOnTotal;

    public Transaction(String productType, String sku, double rating, String orderStatus,
                       String paymentMethod, double totalPrice, double unitPrice, int quantity,
                       LocalDate purchaseDate, String shippingType, String addOnsPurchased,
                       double addOnTotal) {
        this.productType = productType;
        this.sku = sku;
        this.rating = rating;
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.shippingType = shippingType;
        this.addOnsPurchased = addOnsPurchased;
        this.addOnTotal = addOnTotal;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }
}

class TreeNode {
    Customer customer;
    TreeNode left;
    TreeNode right;

    public TreeNode(Customer customer) {
        this.customer = customer;
        this.left = null;
        this.right = null;
    }
}

class CustomerBST {
    private TreeNode root;

    public CustomerBST() {
        this.root = null;
    }

    // Insert a new customer into the BST
    public void insert(Customer customer) {
        root = insertRec(root, customer);
    }

    private TreeNode insertRec(TreeNode root, Customer customer) {
        if (root == null) {
            root = new TreeNode(customer);
            return root;
        }
        if (customer.customerId < root.customer.customerId) {
            root.left = insertRec(root.left, customer);
        } else if (customer.customerId > root.customer.customerId) {
            root.right = insertRec(root.right, customer);
        }
        return root;
    }

    // Search for a customer by ID
    public Customer search(int customerId) {
        return searchRec(root, customerId);
    }

    private Customer searchRec(TreeNode root, int customerId) {
        if (root == null || root.customer.customerId == customerId) {
            return root != null ? root.customer : null;
        }
        return customerId < root.customer.customerId
                ? searchRec(root.left, customerId)
                : searchRec(root.right, customerId);
    }
}

public class CustomerLoyaltyTracker {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private CustomerBST customers = new CustomerBST();
    private int targetCustomerId;

    public CustomerLoyaltyTracker(int targetCustomerId) {
        this.targetCustomerId = targetCustomerId;
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void dataReader(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",\\s*");
                int customerId = Integer.parseInt(data[0].trim());
                LocalDate purchaseDate = LocalDate.parse(data[12].trim(), DATE_FORMAT);

                // Only process if the customerId matches the targetCustomerId
                if (customerId == targetCustomerId) {
                    // Check if the customer already exists in the BST
                    Customer customer = customers.search(customerId);
                    if (customer == null) {
                        // If customer is not found, create a new Customer and insert it into the BST
                        customer = new Customer(customerId, Integer.parseInt(data[1].trim()),
                                data[2].trim(), Boolean.parseBoolean(data[3].trim()));
                        customers.insert(customer); // Insert into BST
                    }

                    // Create the transaction
                    String productType = data[4].trim();
                    String sku = data[5].trim();
                    double rating = isNumeric(data[6].trim()) ? Double.parseDouble(data[6].trim()) : 0.0;
                    String orderStatus = data[7].trim();
                    String paymentMethod = data[8].trim();
                    double totalPrice = isNumeric(data[9].trim()) ? Double.parseDouble(data[9].trim()) : 0.0;
                    double unitPrice = isNumeric(data[10].trim()) ? Double.parseDouble(data[10].trim()) : 0.0;
                    int quantity = Integer.parseInt(data[11].trim());
                    String shippingType = data[13].trim();
                    String addOnsPurchased = data[14].trim();
                    double addOnTotal = isNumeric(data[15].trim()) ? Double.parseDouble(data[15].trim()) : 0.0;

                    // Add the transaction to the existing customer
                    Transaction transaction = new Transaction(
                            productType, sku, rating, orderStatus, paymentMethod, totalPrice,
                            unitPrice, quantity, purchaseDate, shippingType, addOnsPurchased, addOnTotal);

                    customer.addTransaction(transaction);

                    // Debugging output to show transaction parsing
                    System.out.println("---------------------------------");
                    System.out.println("          Parsed values");
                    System.out.println("---------------------------------");
                    System.out.println("Customer ID: " + customer.customerId);
                    System.out.println("Purchase Date: " + purchaseDate);
                    System.out.println("Total Price: $ " + totalPrice);
                    System.out.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calculateRFMForCustomer(LocalDate currentDate) {
        Customer customer = customers.search(targetCustomerId);
        if (customer != null) {
            customer.RFMCalculation(currentDate);
        } else {
            System.out.println("Customer with ID " + targetCustomerId + " not found.");
        }
    }

    public void displayCustomerInfo() {
        Customer customer = customers.search(targetCustomerId);
        if (customer != null) {
            System.out.println("Customer ID: " + customer.customerId);
            System.out.println("Age: " + customer.age);
            System.out.println("Gender: " + customer.gender);
            System.out.println("Loyalty Member: " + customer.isLoyaltyMember);
            System.out.println("Total Transactions: " + customer.transactions.size());
            System.out.println("Recency: " + customer.recency + " days");
            System.out.println("Frequency: " + customer.frequency);
            System.out.println("Monetary: " + "$ " + customer.monetary);
            System.out.println();

            int transactionCount = 1;
            for (Transaction transaction : customer.transactions) {
                System.out.println("------------------------------");
                System.out.println("        Transaction " + transactionCount++);
                System.out.println("------------------------------");
                System.out.println("  Product Type: " + transaction.productType);
                System.out.println("  SKU: " + transaction.sku);
                System.out.println("  Rating: " + transaction.rating);
                System.out.println("  Order Status: " + transaction.orderStatus);
                System.out.println("  Payment Method: " + transaction.paymentMethod);
                System.out.println("  Total Price: " + "$ " + transaction.totalPrice);
                System.out.println("  Unit Price: " + "$ " + transaction.unitPrice);
                System.out.println("  Quantity: " + transaction.quantity + " pcs");
                System.out.println("  Purchase Date: " + transaction.purchaseDate);
                System.out.println("  Shipping Type: " + transaction.shippingType);
                System.out.println("  Add-ons Purchased: " + transaction.addOnsPurchased);
                System.out.println("  Add-on Total: " + "$ " + transaction.addOnTotal);
                System.out.println();
            }
        } else {
            System.out.println("Customer not found.");
        }
    }

    public static void main(String[] args) {
        // Example of how to use the CustomerLoyaltyTracker
        String filePath = "D:/programming files/BST/customers.csv";
        int targetCustomerId = 19957; // Set the target customer ID

        CustomerLoyaltyTracker tracker = new CustomerLoyaltyTracker(targetCustomerId);
        tracker.dataReader(filePath);
        LocalDate currentDate = LocalDate.now();
        tracker.calculateRFMForCustomer(currentDate);
        tracker.displayCustomerInfo();
    }
}
