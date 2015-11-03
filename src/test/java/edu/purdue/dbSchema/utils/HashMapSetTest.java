package edu.purdue.dbSchema.utils;

import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class HashMapSetTest {

    @Test
    public void putNullArgs() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();

        try {
            ms.put(null, 1);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            ms.put("", null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        ms.put("", 0);
    }

    @Test
    public void contains() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();

        assertThat(ms.contains("", 1), is(false));
        assertThat(ms.contains("", 2), is(false));
        assertThat(ms.contains("", 3), is(false));

        assertThat(ms.put("", 1), is(true));
        assertThat(ms.put("", 1), is(false));
        assertThat(ms.put("", 2), is(true));

        assertThat(ms.contains("", 1), is(true));
        assertThat(ms.contains("", 2), is(true));
        assertThat(ms.contains("", 3), is(false));
    }

    @Test
    public void containsNullArgs() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        try {
            ms.contains("", null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            ms.contains(null, 1);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void getSet() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        ms.put("", 1);
        ms.put("", 2);

        Set<Integer> set;
        set = ms.getSet("");
        assertThat(set, containsInAnyOrder(1, 2));

        set = ms.getSet("any");
        assertThat(set, hasSize(0));
    }

    @Test
    public void getSetNullArgs() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        try {
            ms.getSet(null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void getSetUnmodifiableSet() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        Set<Integer> set;
        set = ms.getSet("");
        try {
            set.add(2);
            fail("the set is not unmodifiable");
        } catch (UnsupportedOperationException ex) {
        }
        ms.put("", 3);
        set = ms.getSet("");
        try {
            set.add(2);
            fail("the set is not unmodifiable");
        } catch (UnsupportedOperationException ex) {
        }
    }
}
