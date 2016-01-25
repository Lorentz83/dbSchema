package edu.purdue.dbSchema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.parser.DlmQueryType;
import edu.purdue.dbSchema.schema.DatabaseEngine;
import edu.purdue.dbSchema.schema.QueryFeature;
import edu.purdue.dbSchema.schema.Table;
import gudusoft.gsqlparser.EDbVendor;
import java.util.List;
import java.util.Scanner;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertThat;
import org.junit.Ignore;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class IntegrationTest {

    @Test
    public void oltpSeats() throws Exception {
        String username = "usr";
        DatabaseEngine db = loadDb();
        grantAll(db, username);

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

    @Test
    public void testError() throws Exception {
        String username = "usr";
        DatabaseEngine db = loadDb();
        grantAll(db, username);

        String sql = "select f_id from \"RESERVATION\", \"FLIGHT\"  "
                + "where  r_f_id = f_id AND f_id IN ("
                + "  select f_id"
                + "  from  \"RESERVATION\", \"FLIGHT\", \"AIRPORT\" as dap, \"AIRPORT\" as aap"
                + "  where f_depart_ap_id=dap.ap_id AND f_arrive_ap_id = aap.ap_id AND r_f_id=f_id AND extract(\"month\" from f_arrive_time) = 1"
                + "  group by f_id"
                + "  order by count(r_id) DESC limit 10"
                + ")";

        System.out.println(sql);
        List<QueryFeature> res = db.parse(sql, username);

    }

    @Test
    public void subQueries() throws Exception {
        String username = "usr";
        DatabaseEngine db = loadDb();
        grantAll(db, username);

        Table airline = db.getTable("airline");
        Table flight = db.getTable("flight");

        String sql = "";

        sql += "select al_name from \"AIRLINE\" WHERE al_id in (SELECT f_al_id from \"FLIGHT\" where f_arrive_time = f_depart_time ); ";
        sql += "select al_name, (select count(f_id) from \"FLIGHT\" where f_al_id = al_id ) from \"AIRLINE\"; ";
        sql += "select sub.name from (select al_name as name, count(al_id) as num from \"AIRLINE\" group by al_name) as sub; ";

        List<QueryFeature> res = db.parse(sql, username);
        QueryFeature feature;

        assertThat(res, hasSize(3));
        feature = res.get(0);
        assertThat(feature.getType(), is(DlmQueryType.SELECT));
        assertThat(feature.getUsedCols(), containsInAnyOrder(airline.getColumn("al_name"), flight.getColumn("f_al_id")));
        assertThat(feature.getFilteredCols(), containsInAnyOrder(airline.getColumn("al_id"), flight.getColumn("f_arrive_time"), flight.getColumn("f_depart_time")));

        feature = res.get(1);
        assertThat(feature.getType(), is(DlmQueryType.SELECT));
        assertThat(feature.getUsedCols(), containsInAnyOrder(airline.getColumn("al_name"), flight.getColumn("f_id")));
        assertThat(feature.getFilteredCols(), containsInAnyOrder(flight.getColumn("f_al_id"), airline.getColumn("al_id")));

        feature = res.get(2);
        assertThat(feature.getType(), is(DlmQueryType.SELECT));
        assertThat(feature.getUsedCols(), containsInAnyOrder(is(airline.getColumn("al_name")), is(airline.getColumn("al_id")), hasToString("name")));
        assertThat(feature.getFilteredCols(), empty());

    }

    @Test
    @Ignore("todo")
    public void defineVirtualColumn() {
        /**
         * TODO here we should decide how to define and deal with abstract
         * columns:
         *
         * "select a, sum (b+c), -a" are -a and sum(b+c) abstracts? currently
         * they are not
         *
         * but "select a, s from tbl, (select b+c from tbl) as t2"
         *
         * returns 3 real columns (a, b, c) and the virtual s.
         *
         */
    }

    @Test
    public void union() throws Exception {
        String username = "usr";
        DatabaseEngine db = loadDb();
        grantAll(db, username);

        Table airline = db.getTable("airline");
        Table flight = db.getTable("flight");

        String sql = "";

        sql += "select al_name from \"AIRLINE\" where al_iata_code='IT' UNION select al_id from \"AIRLINE\" where al_icao_code='AKX'; ";

        List<QueryFeature> res = db.parse(sql, username);
        QueryFeature feature;

        assertThat(res, hasSize(1));

        feature = res.get(0);
        assertThat(feature.getType(), is(DlmQueryType.SELECT));
        assertThat(feature.getUsedCols(), containsInAnyOrder(airline.getColumn("al_name"), airline.getColumn("al_id")));
        assertThat(feature.getFilteredCols(), containsInAnyOrder(airline.getColumn("al_iata_code"), airline.getColumn("al_icao_code")));
    }

    @Test
    @Ignore("todo")
    public void mixedUpdateAndSelect() throws Exception {

    }

    private DatabaseEngine loadDb() throws SqlSemanticException, UnsupportedSqlException, SqlParseException {
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
        return db;
    }

    private void grantAll(DatabaseEngine db, String username) throws SqlParseException, SqlSemanticException, UnsupportedSqlException {
        for (Table t : db.getTables()) {
            db.parse(String.format("GRANT SELECT ON %s TO %s", t.getName().getName(), username));
            db.parse(String.format("GRANT INSERT ON %s TO %s", t.getName().getName(), username));
        }
    }

}
