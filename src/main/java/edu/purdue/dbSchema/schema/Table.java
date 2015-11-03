package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Table {

    private final String _name;
    private final Map<String, Column> _cols;

    public Table(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name");
        }
        _name = name;
        _cols = new TreeMap<String, Column>();
    }

    public Table addColumn(String name, String type, boolean notNull, boolean unique) throws SqlSemanticException {
        if (_cols.containsKey(name)) {
            throw new SqlSemanticException("column '%s' specified more than once", name);
        }
        Column col = new Column(name, type, notNull, unique, this);
        _cols.put(name, col);
        return this;
    }

    public String getName() {
        return _name;
    }

    public Collection<Column> getColumns() {
        return Collections.unmodifiableCollection(_cols.values());
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(_name).append(" (\n");
        for (Column col : _cols.values()) {
            sb.append(col.toString()).append(",\n");
        }
        sb.append(");");
        return sb.toString();
    }

    /**
     * Gets the column by name.
     *
     * @param name the column name.
     * @return the selected column or null if it does not exists.
     * @throws NullPointerException if name is null.
     * @throws IllegalArgumentException if name is empty.
     */
    Column getColumn(String name) throws IllegalArgumentException {
        if (name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return _cols.get(name);
    }

}
