import java.util.Scanner;
import java.sql.*;
import java.text.NumberFormat;

class Account {
    private Scanner scan;
    private Connection conn;
    private int ssn;
    PreparedStatement general_stmt, accounts_stmt, emails_stmt, phones_stmt;
    ResultSet general, accounts, emails, phones;

    Account(Scanner scan, Connection conn, int ssn) throws SQLException
    {
        this.ssn = ssn;
        this.scan = scan;
        this.conn = conn;

        // create prepared statements for our info queries
        String phones_sql = String.format(
            "SELECT * FROM electronic_address WHERE SSN=%d AND TYPE='phone'",
            ssn);
        general_stmt = conn.prepareStatement(String.format(
            "SELECT * FROM user_account WHERE SSN=%d", ssn));
        accounts_stmt = conn.prepareStatement(String.format(
            "SELECT * FROM has_additional WHERE SSN=%d", ssn));
        emails_stmt = conn.prepareStatement(String.format(
            "SELECT * FROM electronic_address WHERE SSN=%d AND TYPE='email'",
            ssn));
        phones_stmt = conn.prepareStatement(String.format(
            "SELECT * FROM electronic_address WHERE SSN=%d AND TYPE='phone'",
            ssn));
    }

    // Prints out account information
    void info() throws SQLException
    {
        // execute our queries for user info
        general = general_stmt.executeQuery();
        general.first();
        accounts = accounts_stmt.executeQuery();
        emails = emails_stmt.executeQuery();
        phones = phones_stmt.executeQuery();

        // print the account information            
        System.out.println("\n=== Account Info ===");
        System.out.printf("SSN: %09d Name: %s Balance: %s\n",
            general.getInt("SSN"), general.getString("Name"),
            NumberFormat.getCurrencyInstance().format(general.getBigDecimal("Balance")));
        System.out.printf("BankID: %09d BANumber: %010d Verified: %s\n",
            general.getInt("BankID"), general.getInt("BANumber"),
            general.getBoolean("PBAVerified") ? "true" : "false");
        System.out.println("Additional Accounts:");
        while (accounts.next())
        {
            System.out.printf(
                "[%d] BankID: %09d BANumber: %010d Verified: %s\n",
                accounts.getRow(), accounts.getInt("BankID"),
                accounts.getInt("BANumber"), accounts.getBoolean("Verified"));
        }
        System.out.println("Email Addresses:");
        while (emails.next())
        {
            System.out.printf(
                "[%d] %s Verified: %s\n", emails.getRow(),
                emails.getString("Identifier"),
                emails.getBoolean("Verified") ? "true": "false");
        }
        System.out.println("Phone Numbers:");
        while (phones.next())
        {
            System.out.printf(
                "[%d] %s Verified: %s\n", phones.getRow(),
                phones.getString("Identifier"),
                phones.getBoolean("Verified") ? "true": "false");
        }
    }

    // adds a bank account to bank_account if needed and has_additional
    void add_account(int BankID, int BANumber) throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rset;
        
        rset = stmt.executeQuery(String.format(
            "SELECT * FROM bank_account WHERE BankID=%d AND BANumber=%d",
            BankID, BANumber));
        if (! rset.next())
            stmt.executeUpdate(String.format(
                "INSERT INTO bank_account VALUES (%d, %d)", BankID, BANumber));
        try
        {
            stmt.executeUpdate(String.format(
                "INSERT INTO has_additional VALUES (%d, %d, %d, False)", ssn,
                BankID, BANumber));
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate entry");
        }
    }

    // deletes a bank account from has_additional based on the row number
    // shown by info
    void delete_account(int row) throws RuntimeException, SQLException
    {
        Statement stmt = conn.createStatement();

        if (accounts.absolute(row))
            stmt.executeUpdate(String.format(
                "DELETE FROM has_additional WHERE SSN=%d AND BankID=%d AND BANumber=%d",
                ssn, accounts.getInt("BankID"), accounts.getInt("BANumber")));
        else
            throw new RuntimeException("Invalid account selection");
    }

    // adds an electronic address
    void add_address(String identifier, String type) throws SQLException
    {
        Statement stmt = conn.createStatement();
        try
        {
            stmt.executeUpdate(String.format(
                "INSERT INTO electronic_address VALUES ('%s', %d, '%s', False)",
                identifier, ssn, type));
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate entry");
        }
    }
 
    // deletes an electronic address based on the row number shown by info
    void delete_address(int row, String type) throws RuntimeException, SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rset;
   
        switch (type) {
            case "email":
                rset = emails;
                break;
            case "phone":
                rset = phones;
                break;
            default:
                throw new RuntimeException("Invalid address type");
        }
        if (rset.absolute(row))
            stmt.executeUpdate(String.format(
                "DELETE FROM electronic_address WHERE Identifier='%s'",
                rset.getString("Identifier")));
        else
            throw new RuntimeException("Invalid address selection");
    }

    // changes a user's information including their primary account info
    void change_user(String Name, int BankID, int BANumber) throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rset;
        Boolean PBAVerified = false;

        // check to see if the account info is already in has_additional
        rset = stmt.executeQuery(String.format(
            "SELECT * FROM has_additional WHERE SSN=%d AND BankID=%d AND BANumber=%d",
            ssn, BankID, BANumber));
        // if it is, save the verified state and the account from has_additional
        if (rset.first())
        {
            PBAVerified = rset.getBoolean("Verified");
            stmt.executeUpdate(String.format(
                "DELETE FROM has_additional WHERE SSN=%d AND BankID=%d and BANumber=%d",
                ssn, BankID, BANumber)); 
        }
        // check to see if the account info is already in bank_account  
        rset = stmt.executeQuery(String.format(
            "SELECT * FROM bank_account WHERE BankID=%d AND BANumber=%d",
            BankID, BANumber));
        // if not add it
        if (! rset.next())
            stmt.executeUpdate(String.format(
                "INSERT INTO bank_account VALUES (%d, %d)", BankID, BANumber));

        // finally update the user
        stmt.executeUpdate(String.format(
            "UPDATE user_account " +
            "SET Name='%s', BankID=%d, BANumber=%d, PBAVerified=%s " +
            "WHERE SSN=%s", Name, BankID, BANumber,
            PBAVerified ? "True" : "False", ssn));
    }

    // Text interface for the account menu
    void menu() throws SQLException
    {
        int BankID, BANumber, row;
        String Name;

        while (true)
        {
            // print out the account info
            info();

            // print the account menu and get user input
            try {
                System.out.println("\n=== Account Menu ===");
                System.out.println("[G]eneral Info");
                System.out.println("[B]ank accounts");
                System.out.println("[E]mail addresses");
                System.out.println("[P]hone numbers");
                System.out.println("[C]ancel");
                System.out.println("What would you like to modify?");
                System.out.print("> ");
                String type = scan.nextLine().toUpperCase();
                switch (type) {
                    case "C": // cancel
                        return;
                    case "G":
                        System.out.print("Enter Name:\n> ");
                        Name = scan.nextLine();
                        System.out.print("Primary BankID:\n> ");
                        BankID = Integer.parseInt(scan.nextLine());
                        System.out.print("Primary BANumber:\n> ");
                        BANumber = Integer.parseInt(scan.nextLine());
                        change_user(Name, BankID, BANumber);
                        break;
                    case "B":
                    case "E":
                    case "P":
                        System.out.print("[A]dd or [R]emove?\n> ");
                        switch (scan.nextLine().toUpperCase()) {
                            case "A":
                                switch (type) {
                                    case "B": // add a bank account
                                        System.out.print("Enter BankID:\n> ");
                                        BankID = Integer.parseInt(
                                            scan.nextLine());
                                        System.out.print("Enter BANumber:\n> ");
                                        BANumber = Integer.parseInt(
                                            scan.nextLine());
                                        add_account(BankID, BANumber);
                                        break;
                                    case "E": // add an email
                                        System.out.print("Enter email:\n> ");
                                        add_address(scan.nextLine(), "email"); 
                                        break;
                                    case "P": // add a phone
                                        System.out.print("Enter phone:\n> ");
                                        add_address(scan.nextLine(), "phone"); 
                                        break;
                                }
                                break;
                            case "R":
                                System.out.println("Which number? See info above.");
                                System.out.print("> ");
                                row = Integer.parseInt(scan.nextLine());
                                switch (type) {
                                    case "B": // remove a bank account
                                        delete_account(row);
                                        break;
                                    case "E": // remove an email
                                        delete_address(row, "email");
                                        break;
                                    case "P": // remove a phone
                                        delete_address(row, "phone");
                                        break;
                                }
                                break;
                            default:
                                throw new RuntimeException(
                                    "Invalid add or remove selection");
                        }
                        break;
                    default:
                        throw new RuntimeException(
                            "Invalid account menu selection");
                }
            } catch (RuntimeException r) {
                System.out.println("Invalid selection");
            }
        }
    }
}
