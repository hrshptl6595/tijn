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
                    System.out.printf("[%d] %d %s\n", rset.getRow(),
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

    public static void main(String arg[])
    {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/tijn", "tijn", "tijn");
            stmt = conn.createStatement();
            ssn = sign_in_menu();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
	}
}
