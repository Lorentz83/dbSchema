package edu.purdue.dbSchema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnauthorizedSqlException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.AbstractColumn;
import edu.purdue.dbSchema.schema.DatabaseEngine;
import edu.purdue.dbSchema.schema.QueryFeature;
import edu.purdue.dbSchema.schema.Table;
import gudusoft.gsqlparser.EDbVendor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.logging.LogManager;

public class App {

    private final static String username = "user";

    public static void main(String[] args) throws SqlParseException, UnsupportedSqlException, SqlSemanticException, IOException, ClassNotFoundException, UnauthorizedSqlException {
        tuneLog();
        DatabaseEngine db;
        String dbFileName = args[0];
        InputStream in;
        switch (args[1]) {
            case "--create":
            case "-c":
                in = (args.length < 3) ? System.in : new FileInputStream(args[2]);
                db = initDb(dbFileName, in);
                System.out.println("Tables loaded: " + db.getTables().size());
                break;
            case "--parse":
                db = readDb(dbFileName);
                in = (args.length < 3) ? System.in : new FileInputStream(args[2]);
                parseLine(db, in);
                break;
            case "--feature":
                db = readDb(dbFileName);
                in = (args.length < 3) ? System.in : new FileInputStream(args[2]);
                parseFeatureLine(db, in);
                break;
            case "-i":
            case "--info":
                db = readDb(dbFileName);
                for (Table t : db.getTables()) {
                    System.out.print(t.getName().getName());
                    System.out.print(':');
                    Collection<AbstractColumn> cols = t.getColumns();
                    int n = 0;
                    for (AbstractColumn c : cols) {
                        System.out.print(c.getName().getName());
                        if (++n < cols.size()) {
                            System.out.print(',');
                        }
                    }
                    System.out.println();
                }
                break;
            default:
                System.err.println("Unknown parameter " + args[1]);
        }

    }

    private static DatabaseEngine initDb(String dbStorage, InputStream in) throws IOException, SqlParseException, UnsupportedSqlException, SqlSemanticException {
        DatabaseEngine db = new DatabaseEngine(EDbVendor.dbvpostgresql);
        try (Scanner scanner = new Scanner(in);
                ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(dbStorage))) {
            scanner.useDelimiter(";");
            while (scanner.hasNext()) {
                String createTable = scanner.next().trim();
                try {
                    db.parse(createTable);
                } catch (SqlParseException | UnsupportedSqlException | SqlSemanticException ex) {
                    System.err.println(" --- Error parsing ---");
                    System.err.println(createTable);
                    ex.printStackTrace(System.err);
                    System.err.println(" --- end error ---");
                    throw ex;
                }
            }

            for (Table t : db.getTables()) {
                db.parse(String.format("GRANT SELECT ON %s TO %s", t.getName().getName(), username));
                db.parse(String.format("GRANT INSERT ON %s TO %s", t.getName().getName(), username));
            }
            o.writeObject(db);
            return db;
        }
    }

    static DatabaseEngine readDb(String dbStorage) throws IOException, ClassNotFoundException {
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(dbStorage))) {
            return (DatabaseEngine) o.readObject();
        }
    }

    private static void parseFeatureLine(DatabaseEngine db, InputStream in) throws IOException {
        String line;
        FeatureFormatter featureFormatter = new FeatureFormatter(db.getTables(), System.out);
        featureFormatter.header();
        try (BufferedReader bin = new BufferedReader(new InputStreamReader(in))) {
            while ((line = bin.readLine()) != null) {
                String[] split = line.split(":");
                String role = split[0].trim();
                String query = split[1].trim().replace("?", "1");
                try {
                    List<QueryFeature> features = db.parse(query, username);
                    for (QueryFeature feature : features) {
                        featureFormatter.format(feature, role);
                    }
                } catch (SqlParseException | UnsupportedSqlException | UnauthorizedSqlException | SqlSemanticException ex) {
                    System.err.println(" --- Error parsing ---");
                    System.err.println(query);
                    ex.printStackTrace(System.err);
                    System.err.println(" --- end error ---");
                }
            }
        }
    }

    private static void parseLine(DatabaseEngine db, InputStream in) throws IOException {
        String line;
        FeatureFormatter featureFormatter = new FeatureFormatter(db.getTables(), System.out);
        featureFormatter.header();
        try (BufferedReader bin = new BufferedReader(new InputStreamReader(in))) {
            while ((line = bin.readLine()) != null) {
                try {
                    List<QueryFeature> features = db.parse(line, username);
                    for (QueryFeature feature : features) {
                        featureFormatter.format(feature);
                    }
                } catch (SqlParseException | UnsupportedSqlException | UnauthorizedSqlException | SqlSemanticException ex) {
                    System.err.println(" --- Error parsing ---");
                    System.err.println(line);
                    ex.printStackTrace(System.err);
                    System.err.println(" --- end error ---");
                }
            }
        }
    }

    public static void tuneLog() throws IOException {
        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
    }
}
