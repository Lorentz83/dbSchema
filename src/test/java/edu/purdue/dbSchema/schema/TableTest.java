package edu.purdue.dbSchema.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
            tbl = new Table(""); //empty string is valid
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void equalChecksName() {
        Table tbl = new Table("name1");
        Table sameTbl = new Table("name1");
        Table differentTbl = new Table("anotherName");

        assertThat(tbl, is(sameTbl));
        assertThat(tbl.hashCode(), is(sameTbl.hashCode()));

        assertThat(tbl, is(not(differentTbl)));
        assertThat(tbl.hashCode(), is(not(differentTbl.hashCode())));
    }

    @Test
    public void equalChecksCol() {
        Table tbl = new Table("tbl");
        Table sameTbl = new Table("tbl");
        Table differentTbl = new Table("tbl");

        Column col1 = new Column("col1", "type1", true, true);
        Column col2 = new Column("col2", "type2", true, true);

        tbl.addColumn(col1);
        tbl.addColumn(col2);
        sameTbl.addColumn(col1);
        sameTbl.addColumn(col2);

        differentTbl.addColumn(col2);

        assertThat(tbl, is(sameTbl));
        assertThat(tbl.hashCode(), is(sameTbl.hashCode()));

        assertThat(tbl, is(not(differentTbl)));
    }
}
