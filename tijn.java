import java.util.Scanner;
import java.sql.*;

class tijn
{
    static Scanner scanner = new Scanner(System.in);
    static Connection conn;
    static Statement stmt;
    static int ssn;

    // Text interface for the sign in menu
    static int sign_in_menu() throws SQLException
    {
        String input;
        ResultSet rset = stmt.executeQuery("SELECT SSN, Name FROM user_account");

        while (true)
        {
                System.out.println("=== Please select a user by row number ===");
                while (rset.next())
                {
                    System.out.printf("[%d] %09d %s\n", rset.getRow(),
                        rset.getInt("SSN"), rset.getString("Name"));
                }
                input = scanner.nextLine();
                if (! rset.absolute(Integer.parseInt(input)))
                {
                    System.out.println("Invalid selection");
                    rset.beforeFirst();
                }
                else
                    return rset.getInt("SSN");
        }
    }

    // Text interface for the main menu
    static void main_menu() throws SQLException
    {
        String input;
        
        while (true)
        {
            System.out.println("=== Main Menu ==");
            System.out.println("[A]ccount");
            System.out.println("[S]end Money");
            System.out.println("[R]equest Money");
            System.out.println("S[t]atements");
            System.out.println("S[e]arch Transactions");
            System.out.println("S[i]gn Out");
            input = scanner.nextLine();
            switch(input.toUpperCase()) {
                case "A":
                    System.out.println("Not yet implemented");
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
            stmt = conn.createStatement();
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
