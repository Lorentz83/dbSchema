package edu.purdue.dbSchema.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class PairTest {

    @Test
    public void equals_otherTypes() {
        Pair<String, Integer> p = new Pair<String, Integer>("s", 1);
        assertThat(p.equals(""), is(false));
        assertThat(p.equals(null), is(false));
    }

    @Test
    public void testEqualsAndHash() {
        Pair<String, Integer> p = new Pair<String, Integer>("s", 1);
        Pair<String, Integer> equal = new Pair<String, Integer>("s", 1);

        Pair<String, Integer> other1 = new Pair<String, Integer>("", 1);
        Pair<String, Integer> other2 = new Pair<String, Integer>("s", 2);

        assertThat(p, is(equal));
        assertThat(p.hashCode(), is(equal.hashCode()));

        assertThat(p, is(not(other1)));
        assertThat(p.hashCode(), is(not(other1.hashCode())));
        assertThat(p, is(not(other2)));
        assertThat(p.hashCode(), is(not(other2.hashCode())));
    }

    @Test
    public void getter() {
        String first = "s";
        Integer second = 2;
        Pair<String, Integer> p = new Pair<String, Integer>(first, second);

        assertThat(p.getFirst(), sameInstance(first));
        assertThat(p.getSecond(), sameInstance(second));
    }
}
