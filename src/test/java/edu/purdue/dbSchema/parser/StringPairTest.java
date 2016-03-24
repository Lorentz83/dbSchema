package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.utils.Pair;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class StringPairTest {

    @Test
    public void toStringTest() {
        Pair<String, String> sp = new StringPair("f", "s");
        assertThat(sp.toString(), is("f.s"));
    }
}
