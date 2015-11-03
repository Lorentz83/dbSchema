package edu.purdue.dbSchema.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of MapSet which uses {@link HashMap} and {@link {HashSet}.
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 *
 * @param <K> the type of the key.
 * @param <V> the type of the values.
 */
public class HashMapSet<K, V> implements IMapSet<K, V> {

    private final Map<K, HashSet<V>> _mem = new HashMap<>();

    @Override
    public boolean put(K key, V value) throws NullPointerException {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        HashSet<V> set = _mem.get(key);
        if (set == null) {
            set = new HashSet<>();
            _mem.put(key, set);
        }
        return set.add(value);
    }

    @Override
    public boolean contains(K key, V value) throws NullPointerException {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        Set<V> set = _mem.get(key);
        return (set == null) ? false : set.contains(value);
    }

    @Override
    public Set<V> getSet(K key) throws NullPointerException {
        if (key == null) {
            throw new NullPointerException();
        }
        Set<V> set = _mem.get(key);
        return set == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(set);
    }

}
