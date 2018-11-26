import java.util.Scanner;
import java.sql.*;
import java.math.*;

class Transaction {
    private Scanner scan;
    private Connection conn;
    private int ssn;
    
    Transaction(Scanner scan, Connection conn, int ssn) throws SQLException {
        this.scan = scan;
        this.conn = conn;
        this.ssn = ssn;
    }

    BigDecimal get_balance() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rset;

        rset = stmt.executeQuery(String.format(
            "SELECT balance FROM user_account WHERE SSN=%d", ssn));
        rset.first();
        return rset.getBigDecimal("Balance");
    }

    void set_balance(BigDecimal balance) throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.executeUpdate(String.format(
            "UPDATE user_account " +
            "SET Balance=%s " +
            "WHERE SSN=%s", balance.toString(), ssn));
    }
        
    void send_menu() throws SQLException {
        try {
            Statement stmt = conn.createStatement();
            String identifier, memo;
            ResultSet rset;
            BigDecimal amount, balance;

            System.out.print("Enter email address or phone:\n> ");
            identifier = scan.nextLine();
            // warn if we can't lookup the identifier
            rset = stmt.executeQuery(String.format(
                "SELECT * FROM electronic_address WHERE Identifier='%s'",
                identifier));
            if (! rset.next()) {
                System.out.println(
                    "Unable to look up this email address or phone number.");
                System.out.println(
                    "The owner will need to create an account to claim their money.");
                System.out.print( "Do you still want to proceed? [Y]es or [N]o\n> ");
                if (! scan.nextLine().toUpperCase().equals("Y"))
                    return;
            }
            System.out.print("Enter amount:\n> $");
            amount = new BigDecimal(scan.nextLine());
            balance = get_balance();
            if (amount.compareTo(balance) > 0) {
                System.out.println("Insufficient funds");
                return;
            }
            System.out.print("Enter memo:\n> ");
            memo = scan.nextLine();
            stmt.executeUpdate(String.format(
                "INSERT INTO send_transaction " + 
                "(Amount, Memo, Cancelled, Claimed, SSN, Identifier) VALUES " +
                "(%s, '%s', %s, %s, %s, '%s')", amount.toString(), memo,
                "False", "False", ssn, identifier));
            set_balance(balance.subtract(amount));
        } catch (RuntimeException r) {
            System.out.println("Invalid input");
        } 
    }
}
