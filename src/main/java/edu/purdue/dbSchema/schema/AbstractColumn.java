package edu.purdue.dbSchema.schema;

import java.io.Serializable;

/**
 * This abstract class represents a column. In a database there are both actual
 * columns and virtual columns. The former are container directly related to the
 * table, the latter are projection of actual columns or functions. This class
 * abstracts from this difference.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public abstract class AbstractColumn implements Serializable {

    protected final Table _table;
    protected final Name _name;

    /**
     * Creates a column.
     *
     * @param table the table this column belong (may be null).
     * @param name the column name.
     * @throws NullPointerException if name is null.
     */
    public AbstractColumn(Table table, Name name) {
        if (name == null) {
            throw new NullPointerException("table");
        }
        _name = name;
        _table = table;
    }

    @Override
    public String toString() {
        return _name.getName();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + _name.hashCode();
        hash = 97 * hash + (_table == null ? 0 : _table.getName().hashCode());
        return hash;
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

    /**
     * Returns the column name.
     *
     * @return the column name
     */
    public Name getName() {
        return _name;
    }

    /**
     * Returns the table this column belongs. Note that this value can be null.
     *
     * @return a table.
     */
    public Table getTable() {
        return _table;
    }

    /**
     * Returns if this column is virtual.
     *
     * @return true if it is a virtual column.
     */
    abstract public boolean isVirtual();
}
