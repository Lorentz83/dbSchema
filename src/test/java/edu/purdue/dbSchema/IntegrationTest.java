package edu.purdue.dbSchema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.DatabaseEngine;
import edu.purdue.dbSchema.schema.Table;
import gudusoft.gsqlparser.EDbVendor;
import java.util.Scanner;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class IntegrationTest {

    @Test
    public void oltpSeats() throws Exception {
        String username = "usr";

        DatabaseEngine db = new DatabaseEngine(EDbVendor.dbvpostgresql);

        try (Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream("edu/purdue/dbSchema/testData/seats.sql"))) {
            scanner.useDelimiter(";");
            while (scanner.hasNext()) {
                String createTable = scanner.next().trim();
                try {
                    db.parse(createTable);
                } catch (SqlParseException | UnsupportedSqlException | SqlSemanticException ex) {
                    System.err.println("Error on " + createTable);
                    ex.printStackTrace(System.err);
                    throw ex;
                }
            }
        }
        for (Table t : db.getTables()) {
            db.parse(String.format("GRANT SELECT ON %s TO %s", t.getName().getName(), username));
            db.parse(String.format("GRANT INSERT ON %s TO %s", t.getName().getName(), username));
        }
        try (Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream("edu/purdue/dbSchema/testData/queries.sql"))) {
            scanner.useDelimiter(";");
            while (scanner.hasNext()) {
                String sql = scanner.next().trim();
                try {
                    db.parse(sql, username);
                } catch (Exception ex) {
                    System.err.println("Error on " + sql);
                    ex.printStackTrace(System.err);
                    throw ex;
                }
            }
        }

    }
}
