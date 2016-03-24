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
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class HashMapSetTest {

    @Test
    public void put() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        try {
            ms.put(null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }

        assertThat(ms.put("a"), is(true));
        assertThat(ms.put("a"), is(false));
    }

    @Test
    public void putPair() {
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
        assertThat(ms.put("", 0), is(true));
        assertThat(ms.put("", 0), is(false));
        assertThat(ms.put("", 1), is(true));
        assertThat(ms.put("a", 0), is(true));
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

    @Test
    public void keySet() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        ms.put("s1");
        ms.put("s1", 1);
        ms.put("s2", 1);
        // these must not be returned
        ms.contains("s3", 2);
        ms.getSet("s4");

        assertThat(ms.keySet(), containsInAnyOrder("s1", "s2"));
    }

    @Test
    public void keySet_returnsUnmodifiable() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        try {
            ms.keySet().add("hello");
            fail("missing exception");
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    public void isEmpty() {
        HashMapSet<String, Integer> ms = new HashMapSet<>();
        assertThat(ms.isEmpty(), is(true));
        ms.put("key");
        assertThat(ms.isEmpty(), is(false));
    }
}
