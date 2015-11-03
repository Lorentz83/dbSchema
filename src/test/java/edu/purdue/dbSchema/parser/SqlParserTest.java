package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Table;
import edu.purdue.dbSchema.util.TuneLogger;
import gudusoft.gsqlparser.EDbVendor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class SqlParserTest {

    @BeforeClass
    public static void tuneLogger() throws IOException {
        TuneLogger.init();
    }

    @Test
    public void grant() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("GRANT roleName TO userName; GRANT SELECT ON tbl.col TO usr");
        List<Grant> grants = p.getGrants();
        assertThat(grants, contains(new Grant("roleName", "userName"), new Grant(Grant.Type.READ, "usr", "tbl", "col")));
    }

    @Test
    public void grantCaseInsensitive() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("gRanT sEleCt on tbl.col tO usr");
        List<Grant> grants = p.getGrants();
        assertThat(grants, contains(new Grant(Grant.Type.READ, "usr", "tbl", "col")));
    }

    @Test
    public void grantWholeTableNoStar() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("GRANT select ON tbl TO usr");
        List<Grant> grants = p.getGrants();
        assertThat(grants, contains(new Grant(Grant.Type.READ, "usr", "tbl", "")));
    }

    @Test
    public void createTable() throws Exception {
        List<Table> tables;
        Table expectedTable = new Table("tbl1");
        final Column id = new Column("id", "integer", true, true);
        final Column name = new Column("name", "varchar(50)", false, true);
        final Column other = new Column("other", "boolean", true, false);
        final Column useless = new Column("useless", "boolean", false, false);
        expectedTable.addColumn(id)
                .addColumn(name)
                .addColumn(other)
                .addColumn(useless);

        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("CREATE TABLE tbl1(id integer primary key, name varchar(50) UNIQUE, other boolean NOT NULL, useless boolean)");

        tables = p.getTables();
        assertThat(tables.size(), is(1));
        Table actualTable = tables.get(0);

        testEquality(actualTable, expectedTable);
    }

    @Test
    public void createMultipleTable() throws Exception {
        List<Table> tables;
        List<Table> expectedTables = new ArrayList<Table>();
        Column col = new Column("id", "integer", false, false);
        expectedTables.add(new Table("tbl1").addColumn(col));
        expectedTables.add(new Table("tbl2").addColumn(col));

        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("CREATE TABLE tbl1(id integer); CREATE TABLE tbl2(id integer)");

        tables = p.getTables();
        assertThat(tables.size(), is(2));
        testEquality(tables.get(0), expectedTables.get(0));
        testEquality(tables.get(1), expectedTables.get(1));
        assertThat(tables, is(expectedTables));
    }

    @Test
    public void parseFunctionInSelect() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("SELECT sum(a), abs(avg(b)), sum(tbl.c, 5), (2 * d) + e as fixed FROM tbl");

        ParsedQuery query = p.getDmlQueries().get(0);
        assertThat(query.select, contains(new StringPair("", "a"),
                new StringPair("", "b"),
                new StringPair("tbl", "c"),
                new StringPair("", "d"),
                new StringPair("", "e")));
    }

    @Test
    public void parseSelect() throws Exception {
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
        assertThat(query.select, contains(new StringPair("", "*"), new StringPair("a", "f1")));
        assertThat(query.from, contains(new StringPair("tbl1", ""), new StringPair("tbl2", "t2"), new StringPair("tbl3", "")));

        query = queries.get(1);
        assertThat(query.type, is(DlmQueryType.SELECT));
        assertThat(query.where, is(1));
        assertThat(query.select, contains(new StringPair("", "*"), new StringPair("tblA", "col1")));
        assertThat(query.from, contains(new StringPair("tbl1", "tb1"), new StringPair("tbl2", "tblA")));

    }

    private void testEquality(Table actualTable, Table expectedTable) {
        assertThat(actualTable.getName(), is(expectedTable.getName()));
        final Collection<Column> expectedCols = expectedTable.getColumns();
        for (Column col : expectedCols) {
            assertThat(actualTable.getColumns(), hasItem(col));
        }
        assertThat(actualTable.getColumns(), hasSize(expectedCols.size()));

        //only this line is necessary, but a finer check makes the code easier to debug
        assertThat(actualTable, is(expectedTable));
    }
}
