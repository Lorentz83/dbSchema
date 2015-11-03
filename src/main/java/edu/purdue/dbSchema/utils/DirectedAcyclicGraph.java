package edu.purdue.dbSchema.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class DirectedAcyclicGraph<T> {

    private final MapSet<T, T> _edges = new MapSet();

    public boolean add(T from, T to) {
        if (from == null || to == null) {
            throw new NullPointerException();
        }
        for (T ancestor : getAnchestors(to)) {
            if (ancestor.equals(from)) {
                return false;
            }
        }
        _edges.put(from, to);
        return true;
    }

    private Iterable<T> getAnchestors(T start) {
        return new DagIterable(_edges, start, false);
    }

    private Iterable<T> getAnchestorsAndSelf(T start) {
        return new DagIterable(_edges, start, true);
    }
}

class DagIterable<T> implements Iterable<T> {

    private final T _start;
    private final MapSet<T, T> _edges;
    private final boolean _includeStart;

    public DagIterable(MapSet<T, T> edges, T start, boolean includeStart) {
        _edges = edges;
        _start = start;
        _includeStart = includeStart;
    }

    @Override
    public Iterator<T> iterator() {
        DagIterator it = new DagIterator(_edges, _start);
        if (!_includeStart) {
            it.next();
        }
        return it;
    }
}

class DagIterator<T> implements Iterator<T> {

    private final T _start;
    private final MapSet<T, T> _edges;
    private final Set<T> _visited;
    private final Queue<T> _toReturn;

    public DagIterator(MapSet<T, T> edges, T start) {
        _edges = edges;
        _start = start;
        _visited = new HashSet<>();
        _toReturn = new LinkedList<>();

        _visited.add(start);
        _toReturn.add(start);
    }

    @Override
    public boolean hasNext() {
        return !_toReturn.isEmpty();
    }

    @Override
    public T next() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        T head = _toReturn.poll();
        for (T next : _edges.getSet(head)) {
            if (_visited.add(next)) {
                _toReturn.add(next);
            }
        }
        return head;
    }
}
