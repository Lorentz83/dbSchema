package edu.purdue.dbSchema.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class VirtualColumnTest {

    @Test
    public void ctorException() {
        VirtualColumn col;
        try {
            col = new VirtualColumn(null, new Name("n"), null);
            fail("Missing NullPointerExceptio");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void isVirtual() {
        VirtualColumn col = new VirtualColumn(null, new Name("n"));
        assertThat(col.isVirtual(), is(true));
    }
}
