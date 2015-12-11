package edu.purdue.dbSchema.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class RealColumnTest {

    @Test
    public void ctorException() {
        RealColumn col;
        try {
            col = new RealColumn(null, "asd", false, false, null);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }
        try {
            col = new RealColumn(new Name("asd"), null, false, false, null);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }

        try {
            col = new RealColumn(new Name(""), "", false, false, null);
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void equalsChecksInstances() {
        RealColumn col = new RealColumn(new Name("colName"), "type", true, true, null);
        RealColumn sameCol = new RealColumn(new Name("colName"), "type", true, true, null);

        assertThat(col.equals(sameCol), is(false));
        assertThat(col.equals(col), is(true));
    }
}
