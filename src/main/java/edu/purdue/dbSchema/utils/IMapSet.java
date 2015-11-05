package edu.purdue.dbSchema.utils;

import java.io.Serializable;
import java.util.Set;

/**
 * An object that maps keys to sets of values.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 * @param <K> the type of the key.
 * @param <V> the type of the values.
 */
public interface IMapSet<K, V> extends Serializable {

    /**
     * Returns true if value is contained in the key set.
     *
     * @param key the key.
     * @param value the value to check if present in the key set.
     * @return true if value is contained in the key set.
     * @throws NullPointerException if key or value is null;
     */
    boolean contains(K key, V value) throws NullPointerException;

    /**
     * Returns the unmodifiable set associated to the key.
     *
     * @param key the key.
     * @return an unmodifiable set.
     * @throws NullPointerException if key is null;
     */
    Set<V> getSet(K key) throws NullPointerException;

    /**
     * Adds a value to the associated key.
     *
     * @param key the key.
     * @param value the value to add to the key set.
     * @return true if the element was added (not already present in the set).
     * @throws NullPointerException if key or value is null;
     */
    boolean put(K key, V value);

}
