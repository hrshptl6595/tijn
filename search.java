import java.util.Scanner;
import java.sql.*;
import java.math.*;
import java.text.NumberFormat;

class Search {
    private Scanner scan;
    private Connection conn;
    private int ssn;

    Search(Scanner scan, Connection conn, int ssn) throws SQLException {
        this.scan = scan;
        this.conn = conn;
        this.ssn = ssn;
    }

    void print_transactions(ResultSet rset, Boolean show_ssn) throws SQLException {
        String TFORMAT = "%03d %-8s %-21s %-20s %-10s %-21s %09d %-20s\n";

        System.out.println(
            "ID  Amount   Date/Time             Memo                 Cancelled  Claimed               SSN       Identifier"); 
        while (rset.next()) {
            System.out.printf(TFORMAT,
                rset.getInt("STid"),
                NumberFormat.getCurrencyInstance().format(rset.getBigDecimal("Amount")),
                rset.getTimestamp("ts"),
                rset.getString("Memo"),
                rset.getBoolean("Cancelled") ? "True" : "False",
                rset.getTimestamp("Claimed"),
                show_ssn ? rset.getInt("SSN") : 0,
                rset.getString("Identifier"));
        }
    }

    void menu() throws SQLException {
        ResultSet rset;
        Statement stmt = conn.createStatement();
        String input, search;

        System.out.println("\n === Search ===");
        System.out.println("[A]ccount");	
        System.out.println("[T]ransaction");
        System.out.println("[B]etween two Dates");
        input = scan.nextLine().toUpperCase();
        switch(input) {
            case "A":
                System.out.print("Please enter an identifier (email or phone number):\n> ");
                input = scan.nextLine();
                rset = stmt.executeQuery(String.format(
                    "SELECT Name " +
                    "FROM user_account, electronic_address " +
                    "WHERE user_account.SSN=electronic_address.SSN AND" +
                    "   Identifier='%s'", input));
                if (rset.next())
                    System.out.printf("%s belongs to %s\n", input,
                        rset.getString("Name"));
                else
                    System.out.println("Unable to lookup identifier");
                return;
            case "T":
                System.out.print("Search by [I]dentifier (email or phone) or [M]emo?\n> ");
                input = scan.nextLine().toUpperCase();
                if (input.equals("I") | input.equals("M")) {
                    System.out.print("Please enter a search string:\n> ");
                    search = scan.nextLine();
                    rset = stmt.executeQuery(String.format(
                        "SELECT * " +
                        "FROM send_transaction " +
                        "WHERE %s LIKE '%%%s%%' AND SSN=%s",
                        input.equals("I") ? "Identifier" : "Memo", search,
                        ssn));
                    System.out.println("Send transactions:");
                    print_transactions(rset, false);
                } else {
                    System.out.println("Invalid selection");
                }
                return;
            case "B":
                System.out.println("Not yet implemented.");
                return;
            default:
                System.out.println("Invalid selection");
        }
    }
}
