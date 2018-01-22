import java.sql.*;
import java.io.*;

import static java.lang.System.exit;

public class Main {
    static String name = "dbsys49";
    static String passwd = "geheim";
    static Connection conn = null;
    static Statement stmt = null;
    static long maxBN = 0;

    // for displaying the menu
    static String welcome = "Hallo! Die Abfrage von Ferienwohnungen muss folgendem Format folgen: \n" +
            "Spanien,3,'1.1.2000','3.3.2000'\n" +
            "[Land],[Anz.Zimmer],[Anreise],[Abreise]\n" +
            "\n" +
            "oder optional mit Ausstattung:\n" +
            "Spanien,3,'1.1.2000','3.3.2000',Sauna\n" +
            "[Land],[Anz.Zimmer],[Anreise],[Abreise],[Ausstattung]";

    static String buchen = "Zum Buchen Ihre Daten bitte im folgendem Format eingeben:\n" +
            "siegmund.döring@gmx.de,Haus Peter,01/01/1990,01/01/1999\n" +
            "[email], [Name der Ferienwohnung], [Anreisedatum], [Abreisedatum]";

    public static void main(String[] args) throws IOException {
        init();
        openCon();
        loop();
        closeCon();
    }

    private static long getMaxBN() {
        String[] dummy = {};
        query(dummy, 'n');
        return maxBN;
    }

    private static void loop() throws IOException {
        while (true) {

            char wahl = displayMenu();
            if (wahl == 's') {
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

                System.out.println("Land: " + land);
                System.out.println("Anzahl Zimmer: " + anzzi);
                System.out.println("Anreise: " + anrei);
                System.out.println("Abreise: " + abrei);
                System.out.println("Ausstattung: " + ausst);

                query(split, 's');

                if (input.equals("q")) return;
            } else if (wahl == 'b') {
                String input = getInput();
                String[] split = input.split(",");
                System.out.println("Anzahl Parameter: " + split.length);
                if (split.length != 4) {
                    System.out.println("Erwarte genau 4 Parameter");
                } else {
                    query(split, 'b');
                }

            } else if (wahl == 'x') {
                exit(0);
            }
        }

    }

    private static void query(String[] params, char wahl) {
        if (wahl == 's') { suchen();
        } else if (wahl == 'b') { buchen(params);
        } else if (wahl == 'n') {
            String query = String.format("select max(buchungsnr) as max from dbsys38.buchung");
            ResultSet result = null;
            try {
                result = stmt.executeQuery(query);
                while (result.next()) {
                    String maxbn = result.getString("max");
                    maxBN = Long.parseLong(maxbn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void suchen() {
        String query = String.format("SELECT DISTINCT Ferienwohnung.NameF, Anzahlzimmer " +
                "FROM dbsys38.Ferienwohnung, dbsys38.Beinhaltet " +
                "WHERE Ferienwohnung.NameL = 'Spanien' " +
                "AND Anzahlzimmer >= 2 " +
                "AND Beinhaltet.NameF = Ferienwohnung.NameF " +
                "AND Beinhaltet.Art = 'Sauna'");
        System.out.println("Debug: " + query);

        ResultSet result = null;
        try {
            result = stmt.executeQuery(query);

            int count = 0;
            while (result.next()) {
                String nameF = result.getString("NameF");
                String anzzi = result.getString("Anzahlzimmer");

                System.out.printf("--- Ergebnis %d: ---\n", count++);
                System.out.println("Name: " + nameF);
                System.out.println("Anzahl Zimmer: " + anzzi);
                System.out.println("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void buchen(String[] params) {
        maxBN++;
        String query = String.format("insert into dbsys38.buchung(buchungsnr, email, namef, anreisedatum, abreisedatum) " +
                "values(%d, '%s', '%s', to_date('%s'), to_date('%s'))", getMaxBN() + 1, params[0], params[1], params[2], params[3]);
        System.out.println("debug query: " + query);
        try {
            stmt.executeUpdate(query);
            conn.commit();
            System.out.println("Buchung wurde der Datenbank erfolgreich hinzugefügt.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getInput() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String input = br.readLine();
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
            stmt.executeUpdate("ALTER SESSION SET nls_date_format = 'DD.MM.YYYY'");
        } catch (SQLException se) {
            System.out.println("SQL Exception occurred while establishing connection to DBS: \n"
                    + se.getMessage());
            try {
                conn.rollback();
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

    static char displayMenu() throws IOException {
        System.out.println("Ferien wohnung suchen: s");
        System.out.println("Ferien wohnung buchen: b");
        System.out.println("Beenden: x");
        String input = getInput();
        if (input.equals("s")) {
            System.out.println(welcome);
            return 's';
        } else if (input.equals("b")) {
            System.out.println(buchen);
            return 'b';
        } else if (input.equals("x")) {
            return 'x';
        }
        System.out.println("");
        return 'y';
    }
}
