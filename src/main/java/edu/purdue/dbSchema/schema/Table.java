package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a table in the database.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class Table implements Serializable {

    private final String _name;
    private final Map<String, Column> _cols;

    /**
     * Creates a table with the specified name.
     *
     * @param name the table name.
     */
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

    /**
     * Adds a column to the table.
     *
     * @param name the column name.
     * @param type the colum type.
     * @param notNull if the column is not not null.
     * @param unique if the column is unique or primary key.
     * @return the current table.
     * @throws SqlSemanticException if the current table contains already a
     * column with the specified name.
     * @throws NullPointerException if name or type are null.
     * @throws IllegalArgumentException if name or type are empty.
     */
    public Table addColumn(String name, String type, boolean notNull, boolean unique) throws SqlSemanticException, NullPointerException, IllegalAccessError {
        if (_cols.containsKey(name)) {
            throw new SqlSemanticException("column '%s' specified more than once", name);
        }
        Column col = new Column(name, type, notNull, unique, this);
        _cols.put(name, col);
        return this;
    }

    /**
     * Returns the name of the table.
     *
     * @return the table name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns an unmodifiable collection containing all the columns in the
     * table.
     *
     * @return the columns.
     */
    public Collection<Column> getColumns() {
        return Collections.unmodifiableCollection(_cols.values());
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }

    /**
     * Returns if another object is actually the same object of this.
     *
     * @param obj the object to compare.
     * @return if obj == this.
     */
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
     * Gets a column by name.
     *
     * @param name the column name.
     * @return the selected column or null if it does not exists.
     * @throws NullPointerException if name is null.
     * @throws IllegalArgumentException if name is empty.
     */
    public Column getColumn(String name) throws IllegalArgumentException, NullPointerException {
        if (name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return _cols.get(name);
    }

}
