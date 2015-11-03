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
public class DirectedAcyclicGraph<T> implements IDirectedAcyclicGraph<T> {

    private final IMapSet<T, T> _edges;

    DirectedAcyclicGraph(IMapSet<T, T> edges) {
        _edges = edges;
    }

    public DirectedAcyclicGraph() {
        this(new HashMapSet<T, T>());
    }

    @Override
    public boolean add(T from, T to) throws NullPointerException {
        if (from == null || to == null) {
            throw new NullPointerException();
        }
        for (T ancestor : followNodeAndSelef(to)) {
            if (ancestor.equals(from)) {
                return false;
            }
        }
        _edges.put(from, to);
        return true;
    }

    @Override
    public Iterable<T> followNode(T start) throws NullPointerException {
        if (start == null) {
            throw new NullPointerException();
        }
        return new DagIterable(_edges, start, false);
    }

    @Override
    public Iterable<T> followNodeAndSelef(T start) throws NullPointerException {
        if (start == null) {
            throw new NullPointerException();
        }
        return new DagIterable(_edges, start, true);
    }
}

class DagIterable<T> implements Iterable<T> {

    final T _start;
    final IMapSet<T, T> _edges;
    final boolean _includeStart;

    public DagIterable(IMapSet<T, T> edges, T start, boolean includeStart) {
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

    private final IMapSet<T, T> _edges;
    private final Set<T> _visited;
    private final Queue<T> _toReturn;

    public DagIterator(IMapSet<T, T> edges, T start) {
        _edges = edges;
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
