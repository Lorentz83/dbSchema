package edu.purdue.dbSchema.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class MapSet<K, V> {

    private final Map<K, Set<V>> _mem = new HashMap<>();

    public boolean put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        Set<V> set = _mem.get(key);
        if (set == null) {
            set = new HashSet<>();
            _mem.put(key, set);
        }
        return set.add(value);
    }

    public boolean contains(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        Set<V> set = _mem.get(key);
        return (set == null) ? false : set.contains(value);
    }

    public Set<V> getSet(K key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Set<V> set = _mem.get(key);
        return set == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(set);
    }

}
