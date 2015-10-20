package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Table;
import gudusoft.gsqlparser.EDbVendor;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class SqlParserTest {

    @Test
    public void createTable() throws SqlParseException, UnsupportedSqlException {
        List<Table> tables;
        Table expectedTable = new Table("tbl1");
        expectedTable.addColumn(new Column("id", "integer", false, true));
        expectedTable.addColumn(new Column("name", "varchar(50)", true, false));
        expectedTable.addColumn(new Column("other", "boolean", false, true));
        expectedTable.addColumn(new Column("useless", "boolean", false, false));

        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("CREATE TABLE tbl1(id integer primary key, name varchar(50) UNIQUE, other boolean NOT NULL, useless boolean)");

        tables = p.getTables();
        assertThat(tables.size(), is(1));
        assertThat(tables.get(0), is(expectedTable));
    }

    @Test
    public void createMultipleTable() throws SqlParseException, UnsupportedSqlException {
        List<Table> tables;
        List<Table> expectedTables = new ArrayList<Table>();
        expectedTables.add(new Table("tbl1"));
        expectedTables.add(new Table("tbl2"));

        SqlParser p = new SqlParser(EDbVendor.dbvoracle);
        p.parse("CREATE TABLE tbl1(); CREATE TABLE tbl2();");

        tables = p.getTables();
        assertThat(tables.size(), is(2));
        assertThat(tables, is(expectedTables));
    }
}
