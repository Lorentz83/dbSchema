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
     * Returns the unmodifiable set associated to the key. If the key is not
     * contained in this MapSet, an empty set is returned.
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
    boolean put(K key, V value) throws NullPointerException;

    /**
     * Adds a key without any value. This is a simple placeholder to create an
     * empty set.
     *
     * @param key the key.
     * @return true if the key was not already present.
     * @throws NullPointerException if key is null;
     */
    boolean put(K key) throws NullPointerException;

    /**
     * Returns the unmodifiable set of keys contained in this MapSet.
     *
     * @return an unmodifiable set of keys.
     */
    Set<K> keySet();

    /**
     * Returns true if this MapSet is empty. This is a convenience method for
     * keySet().isEmpty();
     *
     * @return true if this MapSet is empty.
     */
    boolean isEmpty();

}
