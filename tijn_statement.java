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

        //Union non-cancelled send_transactions we created with claimed
        //send_transactions to any of our identifiers for the last year.
        //Use aggregate functions to get the totals grouped by month and year
        //NOTE: each select in the union creates a dummy value (0.00) for the
        //amount it doesn't lookup.
        rset = stmt.executeQuery(String.format(
            "SELECT YEAR(ts), MONTH(ts), SUM(sent), SUM(recv) " +
            "FROM (" +
            "   SELECT ts, amount AS sent, 0.00 AS recv" +
            "   FROM send_transaction" +
            "   WHERE SSN=%1$d AND Cancelled=False" +
            "   UNION" +
            "   SELECT ts, 0.00 AS sent, amount AS recv" +
            "   FROM send_transaction" +
            "   WHERE Identifier in" +
            "       (SELECT Identifier FROM electronic_address WHERE SSN=%1$d) AND" +
            "       Claimed IS NOT NULL" +
            ") AS T " +
            "WHERE ts > DATE_SUB(CURRENT_DATE, INTERVAL 1 YEAR) " +
            "GROUP BY YEAR(ts), MONTH(ts)", ssn));
        System.out.println("\n=== Summary of Last Year's Transactions ===");
        while (rset.next()) {
            System.out.printf("Year-Month: %d-%02d Sent: $%-10s Recieved: $%-10s\n", rset.getInt(1),
                rset.getInt(2), rset.getBigDecimal(3).toString(),
                rset.getBigDecimal(4).toString());
        }
    }
}
