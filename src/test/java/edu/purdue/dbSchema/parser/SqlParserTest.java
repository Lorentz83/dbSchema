package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.schema.Table;
import edu.purdue.dbSchema.testUtil.SoftEqual;
import edu.purdue.dbSchema.testUtil.TuneLogger;
import gudusoft.gsqlparser.EDbVendor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
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
    public void parse_grant() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("GRANT roleName TO userName; GRANT SELECT ON tbl.col TO usr");
        List<Grant> grants = p.getGrants();
        assertThat(grants, contains(new Grant("roleName", "userName"), new Grant(Grant.Type.READ, "usr", "tbl", "col")));
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
        p.parse("SELECT sum(a), abs(avg(b)), sum(tbl.c, 5), (2 * d) + e as fixed FROM tbl");

        ParsedQuery query = p.getDmlQueries().get(0);
        assertThat(query.mainColumns, contains(new StringPair("", "a"),
                new StringPair("", "b"),
                new StringPair("tbl", "c"),
                new StringPair("", "d"),
                new StringPair("", "e")));
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
        assertThat(query.where, is(2));
        assertThat(query.mainColumns, contains(new StringPair("", "*"), new StringPair("a", "f1")));
        assertThat(query.from, contains(new StringPair("tbl1", ""), new StringPair("tbl2", "t2"), new StringPair("tbl3", "")));

        query = queries.get(1);
        assertThat(query.type, is(DlmQueryType.SELECT));
        assertThat(query.where, is(1));
        assertThat(query.mainColumns, contains(new StringPair("", "*"), new StringPair("tblA", "col1")));
        assertThat(query.from, contains(new StringPair("tbl1", "tb1"), new StringPair("tbl2", "tblA")));

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
    }

    @Test
    public void parse_Delete() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("DELETE from tbl1 WHERE c > 10");
        List<ParsedQuery> queries = p.getDmlQueries();

        assertThat(queries, hasSize(1));
        ParsedQuery query = queries.get(0);

        assertThat(query.type, is(DlmQueryType.DELETE));
        assertThat(query.from, contains(new StringPair("tbl1", "")));
        assertThat(query.where, is(1));
        assertThat(query.mainColumns, hasSize(0));
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
        assertThat(query.where, is(1));
    }
}
