package edu.purdue.dbSchema.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class ColumnTest {

    @Test
    public void ctorException() {
        Column col;
        try {
            col = new Column(null, "asd", false, false, null);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }
        try {
            col = new Column(new Name("asd"), null, false, false, null);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }

        try {
            col = new Column(new Name(""), "", false, false, null);
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void equalsChecksInstances() {
        Column col = new Column(new Name("colName"), "type", true, true, null);
        Column sameCol = new Column(new Name("colName"), "type", true, true, null);

        assertThat(col.equals(sameCol), is(false));
        assertThat(col.equals(col), is(true));
    }
}
