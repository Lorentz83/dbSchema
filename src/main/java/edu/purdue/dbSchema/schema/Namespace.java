package edu.purdue.dbSchema.schema;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Namespace {

    private final String _name;
    private final Set<Table> _tables;

    public Namespace(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        _name = name;
        _tables = new TreeSet<Table>();
    }

    void addTable(Table tbl) {
        if (tbl == null) {
            throw new NullPointerException("table");
        }
        _tables.add(tbl);
    }

    public String getName() {
        return _name;
    }

    public Set<Table> getTables() {
        return Collections.unmodifiableSet(_tables);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this._name != null ? this._name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Namespace other = (Namespace) obj;
        if ((this._name == null) ? (other._name != null) : !this._name.equals(other._name)) {
            return false;
        }
        if (this._tables != other._tables && (this._tables == null || !this._tables.equals(other._tables))) {
            return false;
        }
        return true;
    }

}
