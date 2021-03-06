import java.util.Scanner;
import java.sql.*;

public class Main {
    static String name = "dbsys49";
    static String passwd = "geheim";
    static Connection conn = null;
    static Statement stmt = null;

    // for displaying the menu
    static String welcome = "Hallo! Die Abfrage von Ferienwohnungen muss folgendem Format folgen: \n" +
            "Spanien,3,'1.1.2000','3.3.2000'\n" +
            "[Land],[Anz.Zimmer],[Anreise],[Abreise]\n" +
            "\n" +
            "oder optional mit Ausstattung:\n" +
            "Spanien,3,'1.1.2000','3.3.2000',Sauna\n" +
            "[Land],[Mind.Anz.Zimmer],[Anreise],[Abreise],[Ausstattung enthält]";

    public static void main(String[] args) {

        init();
        openCon();
        loop();
        closeCon();

        //testQuery();
    }

    private static void loop() {
        while(true) {
            displayMenu();
            String input = getInput();

            String[] split = input.split(",");

            String land = "";
            String anzzi = "";
            String anrei = "";
            String abrei = "";
            String ausst = "";

            int i = 0;
            if (split.length > i) land = split[i++];
            if (split.length > i) anzzi = split[i++];
            if (split.length > i) anrei = split[i++];
            if (split.length > i) abrei = split[i++];
            if (split.length > i) ausst = split[i++];

            System.out.println("OK - wir suchen nach Wohnungen mit folgenden Kriterien:");
            System.out.println("Land: " + land);
            System.out.println("Anzahl Zimmer: " + anzzi);
            System.out.println("Anreise: " + anrei);
            System.out.println("Abreise: " + abrei);
            System.out.println("Ausstattung: " + ausst);
            System.out.println("");

            query(split);

            //Teststring:
            //  Spanien,3,'1.1.2000','3.3.2000'

            if (input.equals("q")) return;
        }
    }

    private static void query(String[] params) {
        String query = String.format("Select NAMEL, ANZAHLZIMMER " +
                    "FROM dbsys38.FERIENWOHNUNG WHERE NAMEL = '%s' " +
                    "and ANZAHLZIMMER >= '%s'", params[0], params[1]);

        System.out.println("Generierter SQL query: " + query);


        ResultSet result = null;
        try {
            result = stmt.executeQuery(query);

            int count = 0;
            while (result.next()) {
                String land = result.getString("namel");
                String anzzi = result.getString("ANZAHLZIMMER");

                System.out.printf("--- Ergebnis %d: ---\n", ++count);
                System.out.println("Land: " + land);
                System.out.println("Anzahl Zimmer: " + anzzi);
                System.out.println("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getInput() {
        Scanner in = new Scanner(System.in);
        String input = in.next().trim();
        return input;
    }

    static void init() {
        try {
            // Treiber laden
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        } catch (SQLException se) {
            System.out.println("SQL Exception occurred while establishing connection to DBS: \n"
                    + se.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.exit(-1);
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

    static void testQuery() {
        // INSERT-Query
        StringBuilder sb = new StringBuilder();
        //sb.append("SELECT namef FROM DBSYS38.FERIENWOHNUNG");

        sb.append("Select NAMEL, count(ANZAHLZIMMER) FROM dbsys38.FERIENWOHNUNG WHERE NAMEL = 'Spanien' and ANZAHLZIMMER >='3' group by namel");

        // Query ausführen - einfügen
        String query = sb.toString();

        // ############ EINFÜGEN ###########
        //stmt.executeUpdate(myInsertQuery);

        ResultSet result = null;
        try {
            result = stmt.executeQuery(query);

            while (result.next()) {
                System.out.println(result.getString("namel"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void displayMenu() {
        System.out.println(welcome);
        /*
        for (String option : options) {
            System.out.println(option);
        }
        */
        System.out.println("");
    }
}
