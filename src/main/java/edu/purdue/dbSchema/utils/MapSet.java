package edu.purdue.dbSchema.utils;

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

    public void put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        Set<V> set = _mem.get(key);
        if (set == null) {
            set = new HashSet<>();
            _mem.put(key, set);
        }
        set.add(value);
    }

    public boolean contains(K key, V value) {
        Set<V> set = _mem.get(key);
        return (set == null) ? false : set.contains(value);
    }

}
