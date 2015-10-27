package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class TableTest {

    @Test
    public void ctorException() {
        Table tbl;
        try {
            tbl = new Table(null);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }
        try {
            tbl = new Table("");
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void equals() throws Exception {
        Table tbl = new Table("name1");
        tbl.addColumn(new Column("name", "type", true, true));
        Table sameTbl = new Table("name1");
        sameTbl.addColumn(new Column("name", "type", true, true));
        Table anotherName = new Table("name2");
        anotherName.addColumn(new Column("name", "type", true, true));
        Table anotherCols = new Table("name1");
        anotherCols.addColumn(new Column("name2", "type", true, true));

        assertThat(tbl, is(sameTbl));
        assertThat(tbl.hashCode(), is(sameTbl.hashCode()));

        assertThat(tbl, is(not(anotherName)));
        assertThat(tbl.hashCode(), is(not(anotherName.hashCode())));

        assertThat(tbl, is(not(anotherCols)));
        //assertThat(tbl.hashCode(), is(not(anotherCols.hashCode())));
    }

    @Test
    public void addColsThroswOnEqualCols() throws SqlSemanticException {
        Column col = new Column("name1", "type", true, true);
        Column sameCol = new Column("name1", "anotherType", true, true);
        Column anotherCol = new Column("name2", "type", true, true);

        Table tbl = new Table("name1");

        tbl.addColumn(col);
        try {
            tbl.addColumn(sameCol);
            fail("Missing SqlSemanticException on duplicated cols");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("column \"name1\" specified more than once"));
        }
        tbl.addColumn(anotherCol);

        Collection<Column> cols = tbl.getColumns();
        assertThat(cols, hasSize(2));
        assertThat(cols, contains(col, anotherCol));
        assertThat(cols, not(hasItem(sameCol)));
    }

    @Test
    public void addColsReturnsThis() throws SqlSemanticException {
        Column col = new Column("name2", "type", true, true);
        Table tbl = new Table("name1");

        Table newTbl = tbl.addColumn(col);
        assertThat(newTbl, sameInstance(tbl));
    }

    @Test
    public void getColumnsReturnsUnmodifiableCollection() throws SqlSemanticException {
        Column col = new Column("name2", "type", true, true);
        Table tbl = new Table("name1");
        tbl.addColumn(col);

        Collection<Column> cols = tbl.getColumns();
        try {
            cols.clear();
            fail("Collection is not unmodifiable");
        } catch (UnsupportedOperationException ex) {
        }
        try {
            cols.add(col);
            fail("Collection is not unmodifiable");
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    public void getColumnNullOnMissing() throws SqlSemanticException {
        Column col = new Column("name2", "type", true, true);
        Table tbl = new Table("name1");
        tbl.addColumn(col);

        Column res;
        res = tbl.getColumn("name2");
        assertThat(res, sameInstance(col));

        res = tbl.getColumn("noName");
        assertThat(res, nullValue());
    }

    @Test
    public void getColumnThrowOnInvalidArg() throws SqlSemanticException {
        Column col = new Column("name2", "type", true, true);
        Table tbl = new Table("name1");
        tbl.addColumn(col);
        try {
            tbl.getColumn(null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            tbl.getColumn("");
            fail("missing IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }

    }
}
