package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.Table;
import edu.purdue.dbSchema.testUtil.SoftEqual;
import edu.purdue.dbSchema.testUtil.TuneLogger;
import gudusoft.gsqlparser.EDbVendor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class SqlParserTest {

    @BeforeClass
    public static void tuneLogger() throws IOException {
        TuneLogger.init();
    }

    @Test
    public void ctor_exception() {
        try {
            SqlParser p = new SqlParser(null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void parse_parseError() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        try {
            p.parse("select * froma table");
            fail("missing SqlParseException");
        } catch (SqlParseException ex) {
            assertThat(ex.getMessage(), containsString("syntax error"));
        }
    }

    @Test
    public void parse_emptyStringNoError() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        assertThat(p.parse(""), is(0));
        assertThat(p.parse(" \t"), is(0));
        assertThat(p.parse(" ;  ;"), is(0));
    }

    @Test
    public void parse_grantExceptions() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        try {
            p.parse("GRANT read ON something");
            fail("missing SqlParseException");
        } catch (UnsupportedSqlException ex) {
        }
        try {
            p.parse("GRANT sElEcT ON namespace.table.col TO user");
            fail("missing SqlParseException");
        } catch (SqlParseException ex) {
            assertThat(ex.getMessage(), containsString("the object 'namespace.table.col' is not in the form of 'table.coloumn'"));
        }
        try {
            p.parse("GRANT something ON table TO user");
            fail("missing SqlParseException");
        } catch (SqlParseException ex) {
            assertThat(ex.getMessage(), containsString("Cannot recognize the privilege 'something'"));
        }
    }

    @Test
    public void parse_grant() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("GRANT roleName TO userName; GRANT SELECT ON tbl.col TO usr; GRANT UPDATE ON tbl.col2 TO usr2");
        List<Grant> grants = p.getGrants();
        assertThat(grants, contains(new Grant("roleName", "userName"), new Grant(Grant.Type.READ, "usr", "tbl", "col"), new Grant(Grant.Type.WRITE, "usr2", "tbl", "col2")));
    }

    @Test
    public void parse_grantCaseInsensitive() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("gRanT sEleCt on tbl.col tO usr");
        List<Grant> grants = p.getGrants();
        assertThat(grants, contains(new Grant(Grant.Type.READ, "usr", "tbl", "col")));
    }

    @Test
    public void parse_grantWholeTableNoStar() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("GRANT select ON tbl TO usr");
        List<Grant> grants = p.getGrants();
        assertThat(grants, contains(new Grant(Grant.Type.READ, "usr", "tbl", "")));
    }

    @Test
    public void parse_createTable() throws Exception {
        List<Table> tables;
        Table expectedTable = new Table("tbl1");
        expectedTable.addColumn("id", "integer", true, true);
        expectedTable.addColumn("name", "varchar(50)", false, true);
        expectedTable.addColumn("other", "boolean", true, false);
        expectedTable.addColumn("useless", "boolean", false, false);

        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("CREATE TABLE tbl1(id integer primary key, name varchar(50) UNIQUE, other boolean NOT NULL, useless boolean)");

        tables = p.getTables();
        assertThat(tables.size(), is(1));
        Table actualTable = tables.get(0);

        SoftEqual.table(actualTable, expectedTable);
    }

    @Test
    public void parse_createMultipleTable() throws Exception {
        List<Table> tables;
        List<Table> expectedTables = new ArrayList<Table>();
        expectedTables.add(new Table("tbl1").addColumn("id", "integer", false, false));
        expectedTables.add(new Table("tbl2").addColumn("id", "integer", false, false));

        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("CREATE TABLE tbl1(id integer); CREATE TABLE tbl2(id integer)");

        tables = p.getTables();
        assertThat(tables.size(), is(2));
        SoftEqual.tables(tables, expectedTables);
        SoftEqual.table(tables.get(0), expectedTables.get(0));
        SoftEqual.table(tables.get(1), expectedTables.get(1));
    }

    @Test
    public void parse_FunctionInSelect() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("SELECT sum(a), abs(avg(b)), sum(tbl.c, 5), (2 * d) + e, now()  as fixed FROM tbl");

        ParsedQuery query = p.getDmlQueries().get(0);
        assertThat(query.mainColumns, contains(new StringPair("", "a"),
                new StringPair("", "b"),
                new StringPair("tbl", "c"),
                new StringPair("", "d"),
                new StringPair("", "e")));
    }

    @Test
    public void parse_joinUsing() throws Exception {
        SqlParser p;
        p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("select * from t0, t1 join t2 using(a,b), t3");
        ParsedQuery query;

        query = p.getDmlQueries().get(0);
        assertThat(query.from, hasSize(4));
        assertThat(query.whereColumns, containsInAnyOrder(
                new StringPair("t1", "a"),
                new StringPair("t1", "b"),
                new StringPair("t2", "a"),
                new StringPair("t2", "b")
        ));

        p.parse("select * from v1 join v2 using(a)");
        query = p.getDmlQueries().get(0);
        assertThat(query.from, hasSize(2));
        assertThat(query.whereColumns, containsInAnyOrder(
                new StringPair("v1", "a"),
                new StringPair("v2", "a")
        ));
    }

    @Test
    public void parse_Select() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("SELECT * , a.f1 from tbl1, tbl2 as t2 join tbl3 on a=b WHERE tbl1.a = tbl2.b ;"
                + "select * , tblA.col1 as c1 from tbl1 tb1, tbl2 as tblA where a = 'hello'");

        List<ParsedQuery> queries = p.getDmlQueries();
        ParsedQuery query;

        assertThat(p.getTables(), hasSize(0));
        assertThat(queries, hasSize(2));

        query = queries.get(0);
        assertThat(query.type, is(DlmQueryType.SELECT));
        assertThat(query.whereColumns, contains(new StringPair("", "a"), new StringPair("", "b"), new StringPair("tbl1", "a"), new StringPair("tbl2", "b")));
        assertThat(query.mainColumns, contains(new StringPair("", "*"), new StringPair("a", "f1")));
        assertThat(query.from, contains(new StringPair("tbl1", ""), new StringPair("tbl2", "t2"), new StringPair("tbl3", "")));
        assert_noSubQueries_noCombinedQueries_noVirtualColumns(query);

        query = queries.get(1);
        assertThat(query.type, is(DlmQueryType.SELECT));
        assertThat(query.whereColumns, contains(new StringPair("", "a")));
        assertThat(query.mainColumns, contains(new StringPair("", "*"), new StringPair("tblA", "col1")));
        assertThat(query.from, contains(new StringPair("tbl1", "tb1"), new StringPair("tbl2", "tblA")));
        assert_noSubQueries_noCombinedQueries(query);
    }

    @Test
    public void parse_Insert() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("INSERT INTO tbl1(c1, c2) VALUES (4, '5')");
        List<ParsedQuery> queries = p.getDmlQueries();
        assertThat(queries, hasSize(1));
        ParsedQuery query = queries.get(0);

        assertThat(query.type, is(DlmQueryType.INSERT));
        assertThat(query.from, contains(new StringPair("tbl1", "")));

        assertThat(query.mainColumns, contains(new StringPair("", "c1"), new StringPair("", "c2")));

        assert_noSubQueries_noCombinedQueries_noVirtualColumns(query);
    }

    @Test
    @Ignore("to define, should we consider this a pair select insert?")
    public void parse_InsertWithQuery() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("INSERT INTO tbl1(c1, c2) (SELECT a, b FROM tbl2 WHERE b>5)");
        List<ParsedQuery> queries = p.getDmlQueries();
        assertThat(queries, hasSize(1));
        ParsedQuery query = queries.get(0);

        assertThat(query.type, is(DlmQueryType.INSERT));
        assertThat(query.from, hasSize(0));

        assertThat(query.from, contains(new StringPair("tbl1", "")));
        assertThat(query.mainColumns, contains(new StringPair("", "c1"), new StringPair("", "c2")));

        assert_noSubQueries_noCombinedQueries_noVirtualColumns(query);
    }

    @Test
    public void parse_Delete() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("DELETE from tbl1 WHERE tbl1.c > 10");
        List<ParsedQuery> queries = p.getDmlQueries();

        assertThat(queries, hasSize(1));
        ParsedQuery query = queries.get(0);

        assertThat(query.type, is(DlmQueryType.DELETE));
        assertThat(query.from, contains(new StringPair("tbl1", "")));
        assertThat(query.whereColumns, contains(new StringPair("tbl1", "c")));
        assertThat(query.mainColumns, hasSize(0));

        assert_noSubQueries_noCombinedQueries_noVirtualColumns(query);
    }

    @Test
    public void parse_Update() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("UPDATE tbl1 SET c1=4, c2 = 3 WHERE c2>3");
        List<ParsedQuery> queries = p.getDmlQueries();

        assertThat(queries, hasSize(1));
        ParsedQuery query = queries.get(0);

        assertThat(query.type, is(DlmQueryType.UPDATE));
        assertThat(query.mainColumns, contains(new StringPair("", "c1"), new StringPair("", "c2")));
        assertThat(query.from, contains(new StringPair("tbl1", "")));
        assertThat(query.whereColumns, contains(new StringPair("", "c2")));

        assert_noSubQueries_noCombinedQueries_noVirtualColumns(query);
    }

    @Test
    public void parse_DbSpecialFunctions() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvpostgresql);
        p.parse("select cast(F_SEATS_LEFT as float)/F_SEATS_TOTAL*100 as seats, extract(\"month\" from F_ARRIVE_TIME) as month, -NEGATIVE_FIELD FROM table");

        List<ParsedQuery> queries = p.getDmlQueries();

        ParsedQuery query = queries.get(0);

        assertThat(query.mainColumns, contains(
                new StringPair("", "F_SEATS_LEFT"),
                new StringPair("", "F_SEATS_TOTAL"),
                new StringPair("", "F_ARRIVE_TIME"),
                new StringPair("", "NEGATIVE_FIELD")
        ));
    }

    @Test
    public void parse_combinedQueries() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("SELECT f1 FROM t1 UNION SELECT f2 FROM t2 UNION SELECT f3 FROM t3");
        List<ParsedQuery> queries = p.getDmlQueries();

        assertThat(queries, hasSize(1));
        ParsedQuery query = queries.get(0);

        for (int n = 1; n <= 3; n++) {
            assertThat(query.type, is(DlmQueryType.SELECT));
            assertThat(query.mainColumns, contains(new StringPair("", "f" + n)));
            assertThat(query.from, contains(new StringPair("t" + n, "")));
            assertThat(query.whereColumns, empty());
            assertThat(query.subQueriesFrom, empty());
            assertThat(query.subQueriesSelect, empty());
            assertThat(query.subQueriesWhere, empty());
            query = query.nextCombinedQuery;
        }
        assertThat(query, nullValue());
    }

    @Test
    public void parse_aliasInSelect_Star() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvpostgresql);
        p.parse("select a.* from airline a");

        ParsedQuery query = p.getDmlQueries().get(0);
        assertThat(query.mainColumns, containsInAnyOrder(new StringPair("a", "*")));
        assertThat(query.virtualColumns.keySet(), hasSize(0));
    }

    @Test
    public void parse_aliasInSelect() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvpostgresql);
        p.parse("select airline.al_name as name, (AL_IATTR01 + AL_IATTR02) as s, -AL_IATTR03 as neg, now() as time  from airline");

        ParsedQuery query = p.getDmlQueries().get(0);
        assertThat(query.mainColumns, containsInAnyOrder(
                new StringPair("airline", "al_name"),
                new StringPair("", "AL_IATTR01"),
                new StringPair("", "AL_IATTR02"),
                new StringPair("", "AL_IATTR03")
        ));
        assertThat(query.virtualColumns.keySet(), hasSize(4));
        assertThat(query.virtualColumns.getSet("name"), containsInAnyOrder(new StringPair("airline", "al_name")));
        assertThat(query.virtualColumns.getSet("s"), containsInAnyOrder(new StringPair("", "AL_IATTR01"), new StringPair("", "AL_IATTR02")));
        assertThat(query.virtualColumns.getSet("neg"), containsInAnyOrder(new StringPair("", "AL_IATTR03")));
        assertThat(query.virtualColumns.getSet("time"), hasSize(0));
    }

    @Test
    public void parse_subQueries() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("SELECT f1, t3.f, (select count(*) from t1 where f1 = t2.f) as c FROM t2, (select * from tbl3) as t3 where f1 in (select l from t4)");
        List<ParsedQuery> queries = p.getDmlQueries();
        assertThat(queries, hasSize(1));

        ParsedQuery query = queries.get(0);
        assertThat(query.type, is(DlmQueryType.SELECT));
        assertThat(query.mainColumns, contains(new StringPair("", "f1"), new StringPair("t3", "f")));
        assertThat(query.from, contains(new StringPair("t2", ""), new StringPair("", "t3")));
        assertThat(query.whereColumns, contains(new StringPair("", "f1")));
        assertThat(query.nextCombinedQuery, nullValue());

        assertThat(query.subQueriesFrom, hasSize(1));
        assertThat(query.subQueriesSelect, hasSize(1));
        assertThat(query.subQueriesWhere, hasSize(1));

        ParsedQuery sub;

        sub = query.subQueriesSelect.get(0);
        assertThat(sub.type, is(DlmQueryType.SELECT));
        assertThat(sub.mainColumns, contains(new StringPair("", "*")));
        assertThat(sub.from, contains(new StringPair("t1", "")));
        assertThat(sub.whereColumns, contains(new StringPair("", "f1"), new StringPair("t2", "f")));
        assert_noSubQueries_noCombinedQueries(sub);

        sub = query.subQueriesFrom.get(0).getSecond();
        assertThat(sub.type, is(DlmQueryType.SELECT));
        assertThat(sub.mainColumns, contains(new StringPair("", "*")));
        assertThat(sub.from, contains(new StringPair("tbl3", "")));
        assertThat(sub.whereColumns, empty());
        assert_noSubQueries_noCombinedQueries_noVirtualColumns(sub);

        sub = query.subQueriesWhere.get(0);
        assertThat(sub.type, is(DlmQueryType.SELECT));
        assertThat(sub.mainColumns, contains(new StringPair("", "l")));
        assertThat(sub.from, contains(new StringPair("t4", "")));
        assertThat(sub.whereColumns, empty());
        assert_noSubQueries_noCombinedQueries_noVirtualColumns(sub);
    }

    @Test
    public void parse_timeFunction() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvpostgresql);
        p.parse("select f_id from \"FLIGHT\" where extract(\"month\" from f_arrive_time)=1");
        ParsedQuery ret = p.getDmlQueries().get(0);
        assertThat(ret.mainColumns, contains(new StringPair("", "f_id")));
        assertThat(ret.whereColumns, contains(new StringPair("", "f_arrive_time")));
    }

    @Test
    public void parse_WhereConditions() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvpostgresql);
        p.parse("select * from t1, t2 where f(a)>1 and f(t1.b, t2.c) or d=e");
        ParsedQuery q = p.getDmlQueries().get(0);
        assertThat(q.whereColumns, contains(
                new StringPair("", "a"),
                new StringPair("t1", "b"),
                new StringPair("t2", "c"),
                new StringPair("", "d"),
                new StringPair("", "e")
        ));
    }

    @Test
    public void parse_Limit() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvpostgresql);
        p.parse("select * from tbl LIMIT 10");
        ParsedQuery ret = p.getDmlQueries().get(0);
        assertThat(ret.mainColumns, contains(new StringPair("", "*")));
        assertThat(ret.whereColumns, empty());
        //TODO, should we report it?
    }

    private void assert_noSubQueries_noCombinedQueries(ParsedQuery query) {
        assertThat(query.subQueriesFrom, empty());
        assertThat(query.subQueriesSelect, empty());
        assertThat(query.subQueriesWhere, empty());
        assertThat(query.nextCombinedQuery, nullValue());
    }

    private void assert_noSubQueries_noCombinedQueries_noVirtualColumns(ParsedQuery query) {
        assert_noSubQueries_noCombinedQueries(query);
        assertThat(query.virtualColumns.isEmpty(), is(true));
    }
}
