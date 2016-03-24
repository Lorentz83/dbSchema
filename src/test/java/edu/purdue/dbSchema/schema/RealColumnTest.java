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
            col = new RealColumn(new Name("a"), "", false, false, null);
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

    @Test
    public void toString_Sql() {
        RealColumn col;
        col = new RealColumn(new Name("colName"), "t", false, false, null);
        assertThat(col.toString(), is("colName t"));

        col = new RealColumn(new Name("colName"), "t", false, true, null);
        assertThat(col.toString(), is("colName t UNIQUE"));

        col = new RealColumn(new Name("colName"), "t", true, false, null);
        assertThat(col.toString(), is("colName t NOT NULL"));

        col = new RealColumn(new Name("colName"), "t", true, true, null);
        assertThat(col.toString(), is("colName t NOT NULL UNIQUE"));
    }

    @Test
    public void getter() {
        RealColumn col;
        col = new RealColumn(new Name("colName"), "t", false, false, null);
        assertThat(col.isNotNull(), is(false));
        assertThat(col.isUnique(), is(false));
        assertThat(col.isVirtual(), is(false));

        col = new RealColumn(new Name("colName"), "t", false, true, null);
        assertThat(col.isNotNull(), is(false));
        assertThat(col.isUnique(), is(true));
        assertThat(col.isVirtual(), is(false));

        col = new RealColumn(new Name("colName"), "t", true, false, null);
        assertThat(col.isNotNull(), is(true));
        assertThat(col.isUnique(), is(false));
        assertThat(col.isVirtual(), is(false));
    }
}
