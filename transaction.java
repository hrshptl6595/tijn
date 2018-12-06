import java.util.Scanner;
import java.sql.*;
import java.math.*;
import java.text.NumberFormat;

class Transaction {
    private Scanner scan;
    private Connection conn;
    private int ssn;
    
    Transaction(Scanner scan, Connection conn, int ssn) throws SQLException {
        this.scan = scan;
        this.conn = conn;
        this.ssn = ssn;
        claim_money();
    }

    //util function to print currencies
    String currency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
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

    // claim any send transactions for which we have Identifiers
    void claim_money() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rset;
        BigDecimal amount;
        int stid;

        rset = stmt.executeQuery(String.format(
            "SELECT STid, Amount, Name, Memo " +
            "FROM send_transaction, user_account " +
            "WHERE send_transaction.SSN=user_account.SSN AND" +
            "   Claimed IS NULL AND" +
            "   Identifier in (" +
            "       SELECT Identifier" +
            "       FROM electronic_address" +
            "       WHERE SSN=%d)", ssn));
        while(rset.next()) {
            amount = rset.getBigDecimal("Amount");
            stid = rset.getInt("STid");
            System.out.printf("Collecting %s from %s for %s.\n",
                currency(amount), rset.getString("Name"),
                rset.getString("Memo"));
            stmt.executeUpdate(String.format(
                "UPDATE send_transaction " +
                "SET Claimed=CURRENT_TIMESTAMP " +
                "WHERE STid=%s", stid)); 
            set_balance(get_balance().add(amount)); 
        }
    }

    void send_money(BigDecimal amount, String memo, String identifier) throws SQLException {
        Statement stmt = conn.createStatement();
        BigDecimal balance;

        balance = get_balance();
        if (amount.compareTo(balance) > 0)
            throw new RuntimeException("Insufficient funds");
        stmt.executeUpdate(String.format(
            "INSERT INTO send_transaction " + 
            "(Amount, Memo, Cancelled, Claimed, SSN, Identifier) VALUES " +
            "(%s, '%s', %s, %s, %s, '%s')", amount.toString(), memo,
            "False", "False", ssn, identifier));
        set_balance(balance.subtract(amount));
    }
       
    void view_requests_menu() throws SQLException {
        int rtid;
        Statement stmt;
        ResultSet rset;
        String input, memo, identifier;
        BigDecimal amount;
        Boolean paid;

        while (true) {
            System.out.println("\n=== Pending Requests ===");
            stmt = conn.createStatement();
            rset = stmt.executeQuery(String.format(
                "SELECT request_transaction.RTid AS RTid, Amount * Percentage / 100 AS Amount, Name, Memo " +
                "FROM request_transaction, from_rq, user_account " +
                "WHERE request_transaction.RTid=from_rq.RTid AND " +
                "   request_transaction.SSN=user_account.SSN AND" +
                "   from_rq.Paid IS NULL AND" +
                "   Identifier IN (" +
                "       SELECT Identifier" +
                "       FROM electronic_address" +
                "       WHERE SSN=%d)", ssn));
            while (rset.next()) 
                System.out.printf("[%d] %s to %s for %s\n", rset.getRow(),
                    currency(rset.getBigDecimal("Amount")),
                    rset.getString("Name"), rset.getString("Memo"));
            System.out.println("[P]ay Request");
            System.out.println("[D]eny Request");
            System.out.print("[C]ancel\n> ");
            try {
                input = scan.nextLine().toUpperCase();
                switch (input) {
                    case "P":
                    case "D":
                        System.out.print("Which request (see above)?\n> ");
                        if (! rset.absolute(Integer.parseInt(scan.nextLine())))
                            throw new RuntimeException("Invalid user number");
                        rtid = rset.getInt("RTid");
                        if (input.equals("P")) {
                            amount = rset.getBigDecimal("Amount");
                            memo = "For: " + rset.getString("Memo");
                            rset = stmt.executeQuery(String.format(
                                "SELECT Identifier " +
                                "FROM request_transaction, electronic_address " +
                                "WHERE request_transaction.SSN=electronic_address.SSN AND" +
                                "   RTid=%d", rtid));
                            rset.next();
                            identifier = rset.getString("Identifier");
                            send_money(amount, memo, identifier);
                            paid = true;
                        } else
                            paid = false;
                        stmt.executeUpdate(String.format(
                            "UPDATE from_rq " +
                            "SET Paid=%s " +
                            "WHERE RTid=%d", paid ? "True" : "False", rtid));
                        break;
                    case "C":
                        return;
                    default:
                        throw new RuntimeException("Invalid menu input");
                }
            } catch (RuntimeException r) {
                System.out.println(r.getMessage());
            }
        } 
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
            System.out.print("Enter memo:\n> ");
            memo = scan.nextLine();
            send_money(amount, memo, identifier);
        } catch (RuntimeException r) {
            System.out.println(r.getMessage());
        } 
    }

    void request_menu() throws SQLException{
        try{
            Statement stmt = conn.createStatement();
            String id, memo;
            ResultSet rset;
            BigDecimal amount;
            Integer percentage;
            System.out.print("Enter email address or phone:\n>");
            id = scan.nextLine();
            // warn if we can't lookup the identifier
            rset = stmt.executeQuery(String.format(
                "SELECT * FROM electronic_address WHERE Identifier='%s'",
                id));
            if (! rset.next()) {
                System.out.println(
                    "Unable to look up this email address or phone number. Please try again");
                return;
            }
            System.out.print("Enter amount:\n>");
            amount = new BigDecimal(scan.nextLine());
            System.out.print("Enter percentage to request from the provided user:\n>");
            percentage = Integer.parseInt(scan.nextLine());
        }
        catch (RuntimeException r){
            System.out.println(r.getMessage());
        }


    }
}
