package edu.purdue.dbSchema.schema;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Table {

    private final String _name;
    private final Set<Column> _cols;

    public Table(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name");
        }
        _name = name;
        _cols = new TreeSet<Column>();
    }

    public boolean addColumn(Column col) {
        if (col == null) {
            throw new NullPointerException("column");
        }
        return _cols.add(col);
    }

    public String getName() {
        return _name;
    }

    public Set<Column> getColumns() {
        return Collections.unmodifiableSet(_cols);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this._name != null ? this._name.hashCode() : 0);
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
        final Table other = (Table) obj;
        if ((this._name == null) ? (other._name != null) : !this._name.equals(other._name)) {
            return false;
        }
        if (this._cols != other._cols && (this._cols == null || !this._cols.equals(other._cols))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(_name).append(" (\n");
        for (Column col : _cols) {
            sb.append(col.toString()).append(",\n");
        }
        sb.append(");");
        return sb.toString();
    }

}
