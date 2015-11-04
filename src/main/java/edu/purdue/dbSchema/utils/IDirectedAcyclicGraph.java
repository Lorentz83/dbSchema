package edu.purdue.dbSchema.utils;

import java.io.Serializable;

/**
 * An object to contain a direct acyclic graph.
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 * @param <T> the type of the nodes.
 */
public interface IDirectedAcyclicGraph<T> extends Serializable {

    /**
     * Adds an edge to the graph.
     *
     * @param from the starting point.
     * @param to the ending point.
     * @return true if the edge was actually added (i.e. not present and didn't
     * create a cycle)
     * @throws NullPointerException if from or to is null
     */
    boolean add(T from, T to) throws NullPointerException;

    /**
     * Get all the ancestors of the selected node. It is guaranteed that the
     * elements are partially ordered by increasing number of edges. In case of
     * the starting point is not a node in the graph, an empty iterable is
     * returned.
     *
     * @param start the starting point.
     * @return an iterable with the list of ancestors.
     * @throws NullPointerException if start is null.
     */
    Iterable<T> followNode(T start) throws NullPointerException;

    /**
     * Get all the ancestors of the selected node, starting with the node
     * itself. It is guaranteed that the elements are partially ordered by
     * increasing number of edges. In case of the starting point is not a node
     * in the graph, an iterator with only the selected node is returned.
     *
     * @param start the starting point.
     * @return an iterable which starts with the selected node and followed by
     * the list of ancestors.
     * @throws NullPointerException if start is null.
     */
    Iterable<T> followNodeAndSelef(T start) throws NullPointerException;

}
