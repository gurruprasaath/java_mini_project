import java.util.Scanner;
import java.util.HashMap;

interface StockActions {
    void addStock(int quantity);
    void removeStock(int quantity);
    void viewStock();
}

abstract class StockItem implements StockActions {
    protected String productName;
    protected int productID;
    protected int stockQuantity;

    // Constructor
    public StockItem(String productName, int productID, int stockQuantity) {
        this.productName = productName;
        this.productID = productID;
        this.stockQuantity = stockQuantity;
    }

    // Abstract method to display product details
    public abstract void displayProductDetails();
    
    @Override
    public void addStock(int quantity) {
        if (quantity > 0) {
            stockQuantity += quantity;
            System.out.println(quantity + " items added to stock.");
        } else {
            System.out.println("Invalid quantity.");
        }
    }

    @Override
    public void removeStock(int quantity) {
        if (quantity > 0 && stockQuantity >= quantity) {
            stockQuantity -= quantity;
            System.out.println(quantity + " items removed from stock.");
        } else {
            System.out.println("Insufficient stock or invalid quantity.");
        }
    }

    @Override
    public void viewStock() {
        System.out.println("Product: " + productName + " (ID: " + productID + ")");
        System.out.println("Stock Quantity: " + stockQuantity);
    }
}

class ElectronicItem extends StockItem {
    private String brand;
    private double price;

    // Constructor
    public ElectronicItem(String productName, int productID, int stockQuantity, String brand, double price) {
        super(productName, productID, stockQuantity);
        this.brand = brand;
        this.price = price;
    }

    @Override
    public void displayProductDetails() {
        System.out.println("Electronic Item Details:");
        System.out.println("Name: " + productName);
        System.out.println("Brand: " + brand);
        System.out.println("Price: $" + price);
        System.out.println("Stock Quantity: " + stockQuantity);
    }
}

public class StockManagementSystem {
    
    private static HashMap<Integer, StockItem> stockItems = new HashMap<>();
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Add some sample products
        stockItems.put(1, new ElectronicItem("Laptop", 1, 50, "Dell", 800.00));
        stockItems.put(2, new ElectronicItem("Smartphone", 2, 100, "Samsung", 500.00));
        
        while (true) {
            System.out.println("\nStock Management System:");
            System.out.println("1. View Stock");
            System.out.println("2. Add Stock");
            System.out.println("3. Remove Stock");
            System.out.println("4. View Product Details");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    viewStock();
                    break;
                case 2:
                    addStock(scanner);
                    break;
                case 3:
                    removeStock(scanner);
                    break;
                case 4:
                    viewProductDetails(scanner);
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewStock() {
        System.out.println("\nCurrent Stock:");
        for (StockItem item : stockItems.values()) {
            item.viewStock();
        }
    }

    private static void addStock(Scanner scanner) {
        System.out.print("\nEnter product ID to add stock: ");
        int productID = scanner.nextInt();
        System.out.print("Enter quantity to add: ");
        int quantity = scanner.nextInt();

        StockItem item = stockItems.get(productID);
        if (item != null) {
            item.addStock(quantity);
        } else {
            System.out.println("Product not found.");
        }
    }

    private static void removeStock(Scanner scanner) {
        System.out.print("\nEnter product ID to remove stock: ");
        int productID = scanner.nextInt();
        System.out.print("Enter quantity to remove: ");
        int quantity = scanner.nextInt();

        StockItem item = stockItems.get(productID);
        if (item != null) {
            item.removeStock(quantity);
        } else {
            System.out.println("Product not found.");
        }
    }

    private static void viewProductDetails(Scanner scanner) {
        System.out.print("\nEnter product ID to view details: ");
        int productID = scanner.nextInt();

        StockItem item = stockItems.get(productID);
        if (item != null) {
            item.displayProductDetails();
        } else {
            System.out.println("Product not found.");
        }
    }
}
