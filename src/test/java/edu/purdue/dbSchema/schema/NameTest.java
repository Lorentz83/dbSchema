package edu.purdue.dbSchema.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class NameTest {

    @Test
    public void ctor_exception() {
        try {
            new Name(null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
            assertThat(ex.getMessage(), is("Missing name"));
        }
        try {
            new Name("");
            fail("missing IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is("Empty name"));
        }
        try {
            new Name("''");
            fail("missing IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage(), is("Empty name"));
        }
    }

    @Test
    public void equals_ignoreCase() {
        Name n1 = new Name("name");
        Name n2 = new Name("NaMe");

        assertThat(n1.equals(n2), is(true));
        assertThat(n1.compareTo(n2), is(0));
        assertThat(n1.hashCode(), is(n2.hashCode()));
    }

    @Test
    public void equals_ignoreQuotes() {
        Name n1 = new Name("na me");
        Name n2 = new Name("'na me'");
        Name n3 = new Name("\"na me\"");

        assertThat(n1.equals(n2), is(true));
        assertThat(n1.compareTo(n2), is(0));
        assertThat(n1.hashCode(), is(n2.hashCode()));

        assertThat(n1.equals(n3), is(true));
        assertThat(n1.compareTo(n3), is(0));
        assertThat(n1.hashCode(), is(n3.hashCode()));
    }

    @Test
    public void compare_ignoreCase() {
        Name n1 = new Name("a_name");
        Name n2 = new Name("B_name");

        assertThat(n1.compareTo(n2), is(lessThan(0)));
        assertThat(n1.equals(n2), is(false));

    }
}
