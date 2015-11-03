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
public class MapSetTest {

    @Test
    public void putNullArgs() {
        MapSet<String, Integer> ms = new MapSet<>();

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
        MapSet<String, Integer> ms = new MapSet<>();

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
        MapSet<String, Integer> ms = new MapSet<>();
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
        MapSet<String, Integer> ms = new MapSet<>();
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
        MapSet<String, Integer> ms = new MapSet<>();
        try {
            ms.getSet(null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void getSetUnmodifiableSet() {
        MapSet<String, Integer> ms = new MapSet<>();
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
