package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
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
    public void equalsChecksReferences() throws Exception {
        Table tbl = new Table("name1");
        tbl.addColumn("name", "type", true, true);

        Table sameTbl = new Table("name1");
        sameTbl.addColumn("name", "type", true, true);

        assertThat(tbl.equals(sameTbl), is(false));
        assertThat(tbl.equals(tbl), is(true));
    }

    @Test
    public void addColsThroswOnEqualCols() throws SqlSemanticException {
        Table tbl = new Table("name1");

        tbl.addColumn("name1", "type", true, true);
        try {
            tbl.addColumn("name1", "anotherType", true, true);
            fail("Missing SqlSemanticException on duplicated cols");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("column 'name1' specified more than once"));
        }
        tbl.addColumn("name2", "type", true, true);

        List<RealColumn> cols = new ArrayList(tbl.getColumns());
        assertThat(cols, hasSize(2));
        assertThat(cols.get(0).getName(), is(new Name("name1")));
        assertThat(cols.get(0).getType(), is("type"));
        assertThat(cols.get(1).getName(), is(new Name("name2")));
    }

    @Test
    public void addColsReturnsThis() throws SqlSemanticException {
        Table tbl = new Table("name1");

        Table newTbl = tbl.addColumn("name2", "type", true, true);
        assertThat(newTbl, sameInstance(tbl));
    }

    @Test
    public void getColumnsReturnsUnmodifiableCollection() throws SqlSemanticException {
        Table tbl = new Table("name1");
        tbl.addColumn("name2", "type", true, true);

        Collection<AbstractColumn> cols = tbl.getColumns();
        try {
            cols.clear();
            fail("Collection is not unmodifiable");
        } catch (UnsupportedOperationException ex) {
        }
        try {
            cols.add(null);
            fail("Collection is not unmodifiable");
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    public void getColumnNullOnMissing() throws SqlSemanticException {
        Table tbl = new Table("name1");
        tbl.addColumn("name2", "type", true, true);

        AbstractColumn res;
        res = tbl.getColumn("name2");
        assertThat(res, not(nullValue()));

        res = tbl.getColumn("noName");
        assertThat(res, nullValue());
    }

    @Test
    public void getColumnThrowOnInvalidArg() throws SqlSemanticException {
        Table tbl = new Table("name1");
        tbl.addColumn("name2", "type", true, true);
        try {
            tbl.getColumn((String) null);
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
