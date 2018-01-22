import java.util.Scanner;
import java.sql.*;

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
            "email,Name der Ferienwohnung, Anreisedatum, Anzahl der Tage";

    //static String[] options = {"-la [land]", "-zi [mindestanzahl zimmer]", "-an [anreisetermin]", "-ab [abreisetermin]", "-au [ausstattung]"};


    public static void main(String[] args) {

        init();
        openCon();
        getMaxBN();
        loop();
        closeCon();

        //testQuery();
    }

    private static void getMaxBN() {
        String[] dummy = {};
        query(dummy, 'n');
    }

    private static void loop() {
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

                //Teststring:
                //  Spanien,3,'1.1.2000','3.3.2000'

                if (input.equals("q")) return;
            } else if (wahl == 'b'){
                String input = getInput();
                String[] split = input.split(",");
                if (split.length != 4){
                    System.out.println("Fehlende Angaben");
                } else {
                    query(split, 'b');
                }

            }
            /*
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

            System.out.println("Land: " + land);
            System.out.println("Anzahl Zimmer: " + anzzi);
            System.out.println("Anreise: " + anrei);
            System.out.println("Abreise: " + abrei);
            System.out.println("Ausstattung: " + ausst);

            query(split);

            //Teststring:
            //  Spanien,3,'1.1.2000','3.3.2000'

            if (input.equals("q")) return;
            */
        }

    }

    private static void query(String[] params, char wahl) {
        if (wahl == 's') {

            /*String query = String.format("Select NAMEL, ANZAHLZIMMER " +
                    "FROM dbsys38.FERIENWOHNUNG WHERE NAMEL = '%s' " +
                    "and ANZAHLZIMMER >= '%s'", params[0], params[1]);
            */

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
        } else if (wahl == 'b'){
            maxBN++;
            String query = String.format("insert into dbsys38.buchung values('%l', to_date(%s), to_date(%s), null, %s, null," +
                    "null,null, null, null, %s )", maxBN, params[2], params[2] + params[3], params[1], params[0]);

            ResultSet result = null;
            try  {
                result =stmt.executeQuery(query);
            } catch (SQLException e){
                e.printStackTrace();
            }
        } else if (wahl == 'n'){
            String query = String.format("select max(buchungsnr) as max from dbsys38.buchung");
            ResultSet result = null;
            try {
                result = stmt.executeQuery(query);
                while (result.next()) {
                    String maxbn = result.getString("max");
                    maxBN = Long.parseLong(maxbn);
                }
            } catch (SQLException e){
                e.printStackTrace();
            }


        }
    }

    private static String getInput() {
        Scanner in = new Scanner(System.in);
        String input = in.next().trim();
        System.out.println("Debug: Input received: " + input);
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

    static char displayMenu(){
        System.out.println("Ferien wohnung suchen: s");
        System.out.println("Ferien wohnung buchen: b");
        String input = getInput();
        if (input.equals("s")) {
            System.out.println(welcome);
            return 's';
        } else if (input.equals("b")){
            System.out.println(buchen);
            return 'b';
        }
        /*
        for (String option : options) {
            System.out.println(option);
        }
        */
        System.out.println("");
        return 'x';
    }
}
