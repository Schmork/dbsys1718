import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.System.exit;

public class DataBaseController {

    static String name = "dbsys49";
    static String passwd = "geheim";
    public static Connection conn = null;
    public static Statement stmt = null;

    static void init() {
        try {
            // Treiber laden
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static String get(String query) {
        return "hello world";
    }

    static void set(String query) {

    }

    static void openCon() {
        try {
            // String für DB-Connection
            String url = "jdbc:oracle:thin:@oracle12c.in.htwg-konstanz.de:1521:ora12c";
            // Verbindung erstellen
            conn = DriverManager.getConnection(url, name, passwd);

            // Transaction Isolations-Level setzen
            conn.setTransactionIsolation(conn.TRANSACTION_SERIALIZABLE);

            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.executeUpdate("ALTER SESSION SET nls_date_format = 'DD.MM.YYYY'");
        } catch (SQLException se) {
            System.out.println("SQL Exception occurred while establishing connection to DBS: \n"
                    + se.getMessage());
            try {
                conn.rollback();
                System.out.println("Failed to open connection to database, nothing has been changed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            exit(-1);
        }
    }

    static void closeCon() {
        try {
            stmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
