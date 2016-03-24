package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Represents a table in the database.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class Table implements Serializable {

    private final Name _name;
    private final Map<Name, AbstractColumn> _cols;

    /**
     * Creates a table with the specified name.
     *
     * @param name the table name.
     */
    public Table(String name) {
        _name = new Name(name);
        _cols = new TreeMap<>();
    }

    /**
     * Creates a virtual table using other table columns. Useful for views or
     * sub-queries.
     *
     * @param alias the name of this virtual table.
     * @param columns the columns to use;
     */
    Table(Name alias, Collection<AbstractColumn> columns) {
        _name = alias;
        _cols = new TreeMap<>();
        for (AbstractColumn col : columns) {
            _cols.put(col.getName(), col);
        }
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
        Name normalizedName = new Name(name);
        if (_cols.containsKey(normalizedName)) {
            throw new SqlSemanticException("column '%s' specified more than once", name);
        }
        RealColumn col = new RealColumn(normalizedName, type, notNull, unique, this);
        _cols.put(normalizedName, col);
        return this;
    }

    public Table addVirtualColumn(String name, Set<AbstractColumn> mappedTo) {
        Name colName = new Name(name);
        _cols.put(colName, new VirtualColumn(this, colName, mappedTo));
        return this;
    }

    /**
     * Returns the name of the table.
     *
     * @return the table name.
     */
    public Name getName() {
        return _name;
    }

    /**
     * Returns an unmodifiable collection containing all the columns in the
     * table.
     *
     * @return the columns.
     */
    public Collection<AbstractColumn> getColumns() {
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
        int num = _cols.size();
        for (AbstractColumn col : _cols.values()) {
            sb.append(col.toString());
            if (--num > 0) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
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
    public AbstractColumn getColumn(Name name) throws IllegalArgumentException, NullPointerException {
        if (name == null) {
            throw new NullPointerException();
        }
        return _cols.get(name);
    }

    /**
     * Convenience method for {@link #getColumn(edu.purdue.dbSchema.schema.Name)
     * } which converts the string to the Name.
     *
     * @param name the column name.
     * @return the selected column or null if it does not exists.
     * @throws NullPointerException if name is null.
     * @throws IllegalArgumentException if name is empty.
     */
    public AbstractColumn getColumn(String name) throws IllegalArgumentException, NullPointerException {
        return getColumn(new Name(name));
    }

}
