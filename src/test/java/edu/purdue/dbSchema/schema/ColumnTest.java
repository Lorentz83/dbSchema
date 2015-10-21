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
public class ColumnTest {

    @Test
    public void ctorException() {
        Column col;
        try {
            col = new Column(null, "asd", false, false);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }
        try {
            col = new Column("asd", null, false, false);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }
        try {
            col = new Column("", "asd", false, false);
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            col = new Column("asd", "", false, false);
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void equals() {
        Column col = new Column("colName", "type", true, true);
        Column sameCol = new Column("colName", "type", true, true);
        Column anotherCol = new Column("anotherName", "type", true, true);

        assertThat(col, is(sameCol));
        assertThat(col.hashCode(), is(sameCol.hashCode()));

        assertThat(col, is(not(anotherCol)));
        assertThat(col.hashCode(), is(not(anotherCol.hashCode())));
    }
}
