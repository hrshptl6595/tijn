import java.util.Scanner;
import java.sql.*;
import java.math.*;

class Search {
    private Scanner scan;
    private Connection conn;
    private int ssn;

    Search(Scanner scan, Connection conn, int ssn) throws SQLException {
        this.scan = scan;
        this.conn = conn;
        this.ssn = ssn;
    }

    void menu() throws SQLException {
        ResultSet rset;
        Statement stmt = conn.createStatement();
        String input;

        System.out.println("\n === Search ===");
        System.out.println("[A]ccount");	
        System.out.println("[T]ransaction");
        System.out.println("[B]etween two Dates");
        input = scan.nextLine().toUpperCase();
        switch(input) {
            case "A":
                System.out.print("Please enter a phone number or email address:\n> ");
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
                System.out.println("Not yet implemented.");
                return;
            case "B":
                System.out.println("Not yet implemented.");
                return;
            default:
                System.out.println("Invalid selection");
        }
    }
}
