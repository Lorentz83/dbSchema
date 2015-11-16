package edu.purdue.dbSchema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.DatabaseEngine;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class App {

    public static void main(String[] args) throws SqlParseException, UnsupportedSqlException, SqlSemanticException, IOException, ClassNotFoundException {
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
            case "-db":
            case "--parse":
                db = readDb(dbFileName);
                in = (args.length < 3) ? System.in : new FileInputStream(args[2]);
                parseLine(db, in);
                break;
            case "-i":
            case "--info":
                db = readDb(dbFileName);
                for (Table t : db.getTables()) {
                    System.out.println(t.toString());
                }
                break;
            default:
                System.err.println("Unknown parameter " + args[1]);
        }

    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static DatabaseEngine initDb(String dbStorage, InputStream in) throws IOException {
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
                }
            }
            o.writeObject(db);
            return db;
        }
    }

    private static DatabaseEngine readDb(String dbStorage) throws IOException, ClassNotFoundException {
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(dbStorage))) {
            return (DatabaseEngine) o.readObject();
        }
    }

    private static void parseLine(DatabaseEngine db, InputStream in) throws IOException {
        String line;
        try (BufferedReader bin = new BufferedReader(new InputStreamReader(in))) {
            while ((line = bin.readLine()) != null) {
                try {
                    db.parse(line);
                    System.out.println("Parsing " + line + "ok");
                } catch (SqlParseException | UnsupportedSqlException | SqlSemanticException ex) {
                    System.err.println("Error parsing " + line);
                    ex.printStackTrace(System.err);
                }
            }
        }
    }

}
