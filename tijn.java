import java.util.Scanner;
import java.sql.*;
import java.text.NumberFormat;

class tijn {
    static Scanner scanner = new Scanner(System.in);
    static Connection conn;
    static int ssn;
    static Account account;
    static Transaction transaction;
    static TIJN_Statement statement;
    static Search search;

    // Text interface for the sign in menu
    static int sign_in_menu() throws SQLException {
        String input;
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT SSN, Name FROM user_account");

        while (true) {
                System.out.println("\n=== Please select a user by number ===");
                while (rset.next()) {
                    System.out.printf("[%d] %09d %s\n", rset.getRow(),
                        rset.getInt("SSN"), rset.getString("Name"));
                }
                System.out.print("> ");
                input = scanner.nextLine();
                try {
                    if (rset.absolute(Integer.parseInt(input)))
                        return rset.getInt("SSN");
                    throw new RuntimeException("Invalid user number");
                } catch (RuntimeException r) {
                    System.out.println("Invalid selection");
                    rset.beforeFirst();
                }
        }
    }

    // Text interface for the main menu
    static void main_menu() throws SQLException {
        String input;
        
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("[A]ccount");
            System.out.println("[S]end Money");
            System.out.println("[R]equest Money");
            System.out.println("S[t]atements");
            System.out.println("S[e]arch Transactions");
            System.out.println("S[i]gn Out");
            System.out.print("> ");
            input = scanner.nextLine();
            switch(input.toUpperCase()) {
                case "A":
                    account.menu();
                    break;
                case "S":
                    transaction.send_menu();
                    break;
                case "R":
                    transaction.request_menu();
                    break;
                case "T":
                    statement.menu();
                    break;
                case "E":
		            search.menu();
                    break;
                case "I":
                    return;
                default:
                    System.out.println("Invalid selection");
            }
        }
    }

    public static void main(String arg[]) {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/tijn", "tijn", "tijn");
            while (true) {
                ssn = sign_in_menu();
                account = new Account(scanner, conn, ssn);
                transaction = new Transaction(scanner, conn, ssn);
                statement = new TIJN_Statement(scanner, conn, ssn);
		        search = new Search(scanner, conn, ssn);
                main_menu();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
	}
}
