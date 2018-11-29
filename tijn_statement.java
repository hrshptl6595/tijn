import java.util.Scanner;
import java.sql.*;
import java.math.*;

// didn't just call it "statement" because that conflics with java.sql.statement
class TIJN_Statement {
    private Scanner scan;
    private Connection conn;
    private int ssn;

    TIJN_Statement(Scanner scan, Connection conn, int ssn) throws SQLException {
        this.scan = scan;
        this.conn = conn;
        this.ssn = ssn;
    }

    void menu() throws SQLException {
        ResultSet rset;
        Statement stmt = conn.createStatement();

        rset = stmt.executeQuery(String.format(
            "SELECT YEAR(ts), MONTH(ts), SUM(amount) " +
            "FROM send_transaction " +
            "WHERE SSN=%d AND ts > DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR) " +
            "GROUP BY YEAR(ts), MONTH(ts)", ssn));
        System.out.println("\n=== Summary of Last Year's Transactions ===");
        while (rset.next()) {
            System.out.printf("Year-Month: %d-%02d Sent: $%s\n", rset.getInt(1),
                rset.getInt(2), rset.getBigDecimal(3).toString());
        }
    }
}
