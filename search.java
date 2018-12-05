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

    void print_transactions(ResultSet rset) throws SQLException {
        String tformat = "%03d %-8s %-21s %-20s %-10s %-21s %-20s %-20s\n";

        System.out.println("ID  Amount   Date/Time             Memo                 Cancelled  Claimed               Name                 Recipient");
        while (rset.next()) {
            System.out.printf(tformat,
                rset.getInt("STid"),
                NumberFormat.getCurrencyInstance().format(rset.getBigDecimal("Amount")),
                rset.getTimestamp("ts"),
                rset.getString("Memo"),
                rset.getBoolean("Cancelled") ? "True" : "False",
                rset.getTimestamp("Claimed"),
                rset.getString("Name"),
                rset.getString("Identifier"));
        }
    }

    void print_requests(ResultSet rset) throws SQLException {
        String tformat = "%03d %-8s %-2s %-20s %-20s %-20s %-20s\n";

        System.out.println("ID  Amount    %   Date/Time             Memo                 Name                 Recipient");
        while (rset.next()) {
            System.out.printf(tformat,
                rset.getInt("RTid"),
                NumberFormat.getCurrencyInstance().format(rset.getBigDecimal("Amount")),
                rset.getInt("Percentage"),
                rset.getTimestamp("ts"),
                rset.getString("Memo"),
                rset.getString("Name"),
                rset.getString("Identifier"));
        }
    }

    void menu() throws SQLException {
        ResultSet rset;
        Statement stmt = conn.createStatement();
        String memo="", identifier="", input, search; 

        System.out.println("\n === Search ===");
        System.out.println("[A]ccount");	
        System.out.println("[T]ransaction");
        System.out.println("[D]ate Range");
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
                System.out.print("Please enter a search string:\n> ");
                input = scan.nextLine();
                System.out.printf("%s", ssn);
                System.out.println("\n=== Sent transactions ===");
                rset = stmt.executeQuery(String.format(
                    "SELECT * " +
                    "FROM send_transaction, electronic_address, user_account " +
                    "WHERE send_transaction.Identifier=electronic_address.Identifier AND" +
                    "   electronic_address.SSN=user_account.SSN AND" +
                    "   send_transaction.SSN=%s AND" +
                    "   (Memo LIKE '%%%2$s%%' OR" +
                    "   send_transaction.Identifier LIKE '%%%2$s%%' OR" +
                    "   Name LIKE '%%%2$s%%')", ssn, input)); 
                print_transactions(rset);
                System.out.println("\n=== Received transactions ===");
                rset = stmt.executeQuery(String.format(
                    "SELECT  * " +
                    "FROM send_transaction, electronic_address, user_account " +
                    "WHERE send_transaction.Identifier=electronic_address.Identifier AND" +
                    "   send_transaction.SSN=user_account.SSN AND" +
                    "   send_transaction.Identifier IN (" + // only things sent to us
                    "       SELECT Identifier" +
                    "       FROM electronic_address" +
                    "       WHERE SSN=%s) AND" +
                    "   (Memo LIKE '%%%2$s%%' OR" +  // with a Memo that matches
                    "   Name LIKE '%%%2$s%%' OR" +   // or a sender's name that matches
                    "   send_transaction.SSN IN (" + // or one of the sender's Identifiers match
                    "       SELECT SSN" +
                    "       FROM electronic_address" +
                    "       WHERE Identifier LIKE '%%%2$s%%'))", ssn, input));
                print_transactions(rset);
                System.out.println("\n=== Transaction Requests ===");
                rset = stmt.executeQuery(String.format(
                    "SELECT * " +
                    "FROM request_transaction, user_account, from_rq " +
                    "WHERE request_transaction.SSN=user_account.SSN AND " +
                    "   request_transaction.RTid=from_rq.RTid AND" +
                    "   Identifier IN (" +
                    "       SELECT Identifier"+
                    "       FROM electronic_address" +
                    "       WHERE SSN=%s) AND" +
                    "   (Memo LIKE '%%%2$s%%' OR" +
                    "   Name LIKE '%%%2$s%%')", ssn, input));
                print_requests(rset);
                return;
            case "D":
                System.out.println("Not yet implemented.");
                return;
            default:
                System.out.println("Invalid selection");
        }
    }
}
