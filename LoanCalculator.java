import java.sql.*;
import java.util.*;

// Base class for User
abstract class User {
    protected String username;
    protected String password;
    protected double balance;
    protected Deque<String> transactionHistory = new ArrayDeque<>(); // Deque for transaction history

    public User(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
    }

    public abstract void showMenu(Scanner scanner, Connection conn);

    // Methods to add and view transaction history
    public void addTransaction(String transaction) {
        transactionHistory.add(transaction);
        if (transactionHistory.size() > 10) { // Keep only the last 10 transactions
            transactionHistory.removeFirst();
        }
    }

    public void viewTransactionHistory() {
        System.out.println("Transaction History:");
        for (String transaction : transactionHistory) {
            System.out.println(transaction);
        }
    }

    public String getUsername() {
        return username;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

// Regular User class (can be extended for more specific roles like Admin)
class RegularUser extends User {

    public RegularUser(String username, String password, double balance) {
        super(username, password, balance);
    }

    @Override
    public void showMenu(Scanner scanner, Connection conn) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\nUser Menu:");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. View Balance");
            System.out.println("4. View Transaction History");
            System.out.println("5. Calculate EMI");
            System.out.println("6. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1:
                    deposit(scanner, conn);
                    break;
                case 2:
                    withdraw(scanner, conn);
                    break;
                case 3:
                    viewBalance(conn);
                    break;
                case 4:
                    viewTransactionHistory();
                    break;
                case 5:
                    calculateEMI(scanner);
                    break;
                case 6:
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void deposit(Scanner scanner, Connection conn) {
        System.out.print("Enter the amount to deposit: ");
        double amount = scanner.nextDouble();

        if (amount <= 0) {
            System.out.println("Deposit amount must be greater than 0.");
            return;
        }

        String depositSQL = "UPDATE users SET balance = balance + ? WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(depositSQL)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, this.username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Deposit successful!");
                addTransaction("Deposited: $" + amount);
            } else {
                System.out.println("Error depositing money.");
            }
        } catch (SQLException e) {
            System.err.println("Error depositing money: " + e.getMessage());
        }
    }

    private void withdraw(Scanner scanner, Connection conn) {
        System.out.print("Enter the amount to withdraw: ");
        double amount = scanner.nextDouble();

        if (amount <= 0) {
            System.out.println("Withdrawal amount must be greater than 0.");
            return;
        }

        String withdrawSQL = "UPDATE users SET balance = balance - ? WHERE username = ? AND balance >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(withdrawSQL)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, this.username);
            stmt.setDouble(3, amount);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Withdrawal successful!");
                addTransaction("Withdrew: $" + amount);
            } else {
                System.out.println("Insufficient balance or error in withdrawal.");
            }
        } catch (SQLException e) {
            System.err.println("Error withdrawing money: " + e.getMessage());
        }
    }

    private void viewBalance(Connection conn) {
        String balanceSQL = "SELECT balance FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(balanceSQL)) {
            stmt.setString(1, this.username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.println("Current balance: " + balance);
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error viewing balance: " + e.getMessage());
        }
    }

    private void calculateEMI(Scanner scanner) {
        try {
            System.out.print("Enter the principal amount: ");
            double principalAmount = scanner.nextDouble();

            System.out.print("Enter the annual interest rate: ");
            double annualInterestRate = scanner.nextDouble();

            System.out.print("Enter the loan tenure in months: ");
            int tenureMonths = scanner.nextInt();

            // Calculate EMI
            double emi = (principalAmount * (annualInterestRate / 12 / 100) * Math.pow(1 + annualInterestRate / 12 / 100, tenureMonths)) /
                         (Math.pow(1 + annualInterestRate / 12 / 100, tenureMonths) - 1);
            System.out.println("Calculated EMI: " + emi);
        } catch (Exception e) {
            System.out.println("Error calculating EMI: " + e.getMessage());
        }
    }
}

public class LoanCalculator {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/world";
    private static final String USER = "root";  // Replace with your MySQL username
    private static final String PASS = "admin";  // Replace with your MySQL password

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = connectToDatabase()) {
            createTableIfNotExist(conn);

            while (true) {
                System.out.println("Welcome! Please choose an option:");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        registerUser(scanner, conn);
                        break;
                    case 2:
                        loginUser(scanner, conn);
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    private static void registerUser(Scanner scanner, Connection conn) {
        System.out.print("Enter a username: ");
        String username = scanner.nextLine();
        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        String registerSQL = "INSERT INTO users (username, password, balance) VALUES (?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(registerSQL)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Registration successful!");
            }
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
        }
    }

    private static void loginUser(Scanner scanner, Connection conn) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        String loginSQL = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(loginSQL)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                RegularUser loggedInUser = new RegularUser(username, password, rs.getDouble("balance"));
                loggedInUser.showMenu(scanner, conn);
            } else {
                System.out.println("Invalid credentials.");
            }
        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage());
        }
    }

    private static Connection connectToDatabase() throws SQLException {
        try {
            // Register the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Open a connection
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found: " + e.getMessage());
        }
    }

    // Create necessary tables if they don't exist
    private static void createTableIfNotExist(Connection conn) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "username VARCHAR(255) PRIMARY KEY, "
                + "password VARCHAR(255) NOT NULL, "
                + "balance DOUBLE DEFAULT 0)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
}
