import java.util.Scanner;
import java.sql.*;
import java.text.NumberFormat;

class tijn
{
    static Scanner scanner = new Scanner(System.in);
    static Connection conn;
    static int ssn;

    // Text interface for the sign in menu
    static int sign_in_menu() throws SQLException
    {
        String input;
        Statement stmt = conn.createStatement();
        ResultSet rset = stmt.executeQuery("SELECT SSN, Name FROM user_account");

        while (true)
        {
                System.out.println("=== Please select a user by number ===");
                while (rset.next())
                {
                    System.out.printf("[%d] %09d %s\n", rset.getRow(),
                        rset.getInt("SSN"), rset.getString("Name"));
                }
                System.out.print("> ");
                input = scanner.nextLine();
                try {
                    if (rset.absolute(Integer.parseInt(input)))
                        return rset.getInt("SSN");
                    throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    System.out.println("Invalid selection");
                    rset.beforeFirst();
                }
        }
    }

    // Text interface for the account menu
    static void account_menu() throws SQLException
    {
        String type, add_or_remove, number;
        // it takes 4 statements to get the user info we display
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        Statement stmt3 = conn.createStatement();
        Statement stmt4 = conn.createStatement();
        // store the result sets in different variables
        ResultSet general, accounts, emails, phones;

        while (true)
        {
            // execute our queries
            general = stmt1.executeQuery("SELECT * FROM user_account WHERE SSN=" +
                ssn);
            general.first();
            accounts = stmt2.executeQuery("SELECT * FROM has_additional WHERE SSN=" +
                ssn);
            emails = stmt3.executeQuery(
                "SELECT * FROM electronic_address WHERE SSN=" + ssn +
                " AND TYPE='email'");
            phones = stmt4.executeQuery(
                "SELECT * FROM electronic_address WHERE SSN=" + ssn +
                " AND TYPE='phone'");

            // print the account information            
            System.out.println("=== Account Info ===");
            System.out.printf("SSN: %09d Name: %s Balance: %s",
                general.getInt("SSN"), general.getString("Name"),
                NumberFormat.getCurrencyInstance().format(general.getBigDecimal("Balance")));
            System.out.println("Bank Accounts:");
            System.out.printf(
                "[1] BankID: %09d BANumber: %10d Verified: %s (Primary Account)\n",
                general.getInt("BankID"), general.getInt("BANumber"),
                general.getBoolean("PBAVerified") ? "true" : "false");
            while (accounts.next())
            {
                System.out.printf(
                    "[%d] BankID: %09d BANumber: %10d Verified: %s\n",
                    accounts.getRow() + 1, accounts.getInt("BankID"),
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

            // print the account menu and get particulars
            System.out.println("\n=== Account Menu ===");
            System.out.println("[B]ank accounts");
            System.out.println("[E]mail addresses");
            System.out.println("[P]hone numbers");
            System.out.println("What would you like to modify?");
            System.out.print("> ");
            type = scanner.nextLine();
            System.out.println("[A]dd or [R]emove?");
            System.out.print("> ");
            add_or_remove = scanner.nextLine();
            if (add_or_remove.toUpperCase().equals("R"))
            {
                System.out.println("Which number? See info above.");
                System.out.print("> ");
                number = scanner.nextLine();
            }
        }
    }

    // Text interface for the main menu
    static void main_menu() throws SQLException
    {
        String input;
        
        while (true)
        {
            System.out.println("=== Main Menu ===");
            System.out.println("[A]ccount");
            System.out.println("[S]end Money");
            System.out.println("[R]equest Money");
            System.out.println("S[t]atements");
            System.out.println("S[e]arch Transactions");
            System.out.println("S[i]gn Out");
            System.out.print("> ");
            input = scanner.nextLine();
            switch(input.toUpperCase())
            {
                case "A":
                    account_menu();
                    break;
                case "S":
                    System.out.println("Not yet implemented");
                    break;
                case "R":
                    System.out.println("Not yet implemented");
                    break;
                case "T":
                    System.out.println("Not yet implemented");
                    break;
                case "E":
                    System.out.println("Not yet implemented");
                    break;
                case "I":
                    return;
                default:
                    System.out.println("Invalid selection");
            }
        }
    }

    public static void main(String arg[])
    {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/tijn", "tijn", "tijn");
            while (true)
            {
                ssn = sign_in_menu();
                main_menu();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
	}
}
