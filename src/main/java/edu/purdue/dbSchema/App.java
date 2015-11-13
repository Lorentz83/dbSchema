package edu.purdue.dbSchema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.DatabaseEngine;
import gudusoft.gsqlparser.EDbVendor;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) throws SqlParseException, UnsupportedSqlException, SqlSemanticException, IOException, ClassNotFoundException {
        DatabaseEngine db;
        switch (args[0]) {
            case "-c":
                String schemaFile = args[1];
                String dbStorage = args[2];
                initDb(schemaFile, dbStorage);
                break;
            case "-db":
                db = readDb(args[1]);
                parseLine(db, new BufferedReader(new InputStreamReader(System.in)));
                break;
            case "-info":
                db = readDb(args[1]);
                db.dumpInfo(System.out);
                break;
        }

    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static DatabaseEngine initDb(String schemaFile, String dbStorage) throws IOException, SqlParseException, UnsupportedSqlException, SqlSemanticException {
        DatabaseEngine db = new DatabaseEngine(EDbVendor.dbvoracle);
        String createTables = readFile(schemaFile, StandardCharsets.UTF_8).toLowerCase();
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(dbStorage))) {
            db.parse(createTables);
            o.writeObject(db);
        }
        return db;
    }

    private static DatabaseEngine readDb(String dbStorage) throws IOException, ClassNotFoundException {
        try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(dbStorage))) {
            return (DatabaseEngine) o.readObject();
        }
    }

    private static void parseLine(DatabaseEngine db, BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            try {
                line = line.toLowerCase();
                db.parse(line);
                System.out.println("Parsing " + line + "ok");
            } catch (SqlParseException | UnsupportedSqlException | SqlSemanticException ex) {
                System.err.println("Error parsing " + line);
                ex.printStackTrace(System.err);
            }
        }
    }

}
