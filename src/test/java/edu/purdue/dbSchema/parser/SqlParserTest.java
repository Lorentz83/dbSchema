package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Table;
import gudusoft.gsqlparser.EDbVendor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class SqlParserTest {

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
    public void parseSelect() throws Exception {
        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("SELECT * from tbl1, tbl2 WHERE tbl1.a = tbl2.b");

        List<DmlQuery> queries = p.getDmlQueries();
        assertThat(p.getTables(), hasSize(0));
        assertThat(queries, hasSize(1));
        DmlQuery query = queries.get(0);
        assertThat(query.getType(), is(DmlQuery.Type.SELECT));

        assertThat(query.getChanged(), anEmptyMap());
        assertThat(query.getFiltered(), hasEntry(equalTo("tbl1"), contains("a")));
        assertThat(query.getFiltered(), hasEntry(equalTo("tbl2"), contains("b")));

        assertThat(query.getSelected(), hasEntry(equalTo("tbl1"), contains("*")));
        assertThat(query.getSelected(), hasEntry(equalTo("tbl2"), contains("*")));

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
