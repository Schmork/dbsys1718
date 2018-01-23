import java.sql.*;
import java.io.*;

import static java.lang.System.exit;

public class Main {

    // for displaying the menu
    static String suchenMenuText = "Hallo! Die Abfrage von Ferienwohnungen muss folgendem Format folgen: \n" +
            "Spanien,3,'1.1.2000','3.3.2000'\n" +
            "[Land],[Anz.Zimmer],[Anreise],[Abreise]\n" +
            "\n" +
            "oder optional mit Ausstattung:\n" +
            "Spanien,3,'1.1.2000','3.3.2000',Sauna\n" +
            "[Land],[Anz.Zimmer],[Anreise],[Abreise],[Ausstattung]";

    static String buchenMenuText = "Zum Buchen Ihre Daten bitte im folgendem Format eingeben:\n" +
            "siegmund.döring@gmx.de,Haus Peter,01/01/1990,01/01/1999\n" +
            "[email], [Name der Ferienwohnung], [Anreisedatum], [Abreisedatum]";

    static String rootMenuText = "Ferienwohnung suchen: s\n" +
                            "Ferienwohnung buchen: b\n" +
                            "Programm beenden: x\n";

    public static void main(String[] args) throws IOException {
        DataBaseController.init();
        DataBaseController.openCon();
        loop();
        DataBaseController.closeCon();
    }

    private static long getMaxBN() {
        String[] dummy = {};
        query(dummy, 'n');

        String maxBN = "";
        String query = String.format("select max(buchungsnr) as max from dbsys38.buchung");
        try {
            ResultSet result = DataBaseController.stmt.executeQuery(query);

            while (result.next()) {
                maxBN = result.getString("max");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Long.parseLong(maxBN);
    }

    private static void loop() throws IOException {
        while (true) {

            char wahl = displayMenu();
            if (wahl == 's') {
                String input = getInput();

                String[] split = input.split(",");

                int i = 0;
                if (split.length > i) System.out.println("Land: " + split[i++]);
                if (split.length > i) System.out.println("Anzahl Zimmer: " + split[i++]);
                if (split.length > i) System.out.println("Anreise: " + split[i++]);
                if (split.length > i) System.out.println("Abreise: " + split[i++]);
                if (split.length > i) System.out.println("Ausstattung: " + split[i++]);

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

        if (wahl == 's') {
            suchen(params);
        } else if (wahl == 'b') {
            buchen(params);
        }
    }

    private static void suchen(String[] params) {

        String fromBeinhaltet = "";
        String checkBeinhaltet = "";

        if (params.length == 5) {
            fromBeinhaltet = ", dbsys38.Beinhaltet ";
            checkBeinhaltet = String.format("AND Beinhaltet.NameF = Ferienwohnung.NameF " +
                                            "AND Beinhaltet.Art = '%s'", params[4]);
        }

        String query = "SELECT DISTINCT Ferienwohnung.NameF, Anzahlzimmer " +
                        "FROM dbsys38.Ferienwohnung " + fromBeinhaltet;
        query += String.format("WHERE Ferienwohnung.NameF NOT IN (SELECT BUCHUNG.NameF FROM dbsys38.Buchung " +
                        "WHERE Buchung.Anreisedatum BETWEEN TO_DATE(%s) AND TO_DATE(%s) " +
                        "OR Buchung.Abreisedatum BETWEEN TO_DATE(%s) AND TO_DATE(%s)) " +
                        "AND Ferienwohnung.NameL = '%s' " +
                        "AND Anzahlzimmer >= '%s' ", params[2], params[3], params[2], params[3], params[0], params[1]);
        query += checkBeinhaltet;
        System.out.println("Debug: " + query);

        try {
            ResultSet result = DataBaseController.stmt.executeQuery(query);

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
        String query = String.format("insert into dbsys38.buchung(buchungsnr, buchungsdatum, email, namef, anreisedatum, abreisedatum) " +
                "values(%d, sysdate, '%s', '%s', to_date('%s'), to_date('%s'))", getMaxBN() + 1, params[0], params[1], params[2], params[3]);
        System.out.println("debug query: " + query);
        try {
            DataBaseController.stmt.executeUpdate(query);
            DataBaseController.conn.commit();
            System.out.println("Buchung wurde der Datenbank erfolgreich hinzugefügt.");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                DataBaseController.conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static String getInput() throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String input = br.readLine();
        return input;
    }

    static char displayMenu() throws IOException {
        System.out.println(rootMenuText);
        String input = getInput();
        if (input.equals("s")) {
            System.out.println(suchenMenuText);
            return 's';
        } else if (input.equals("b")) {
            System.out.println(buchenMenuText);
            return 'b';
        } else if (input.equals("x")) {
            return 'x';
        }
        System.out.println("");
        return 'y';
    }
}
