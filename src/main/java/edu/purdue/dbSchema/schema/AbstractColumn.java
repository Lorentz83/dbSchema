package edu.purdue.dbSchema.schema;

/**
 * This is the base class to represent a column. In a database are present
 * actual columns and virtual columns. The former are container directly related
 * to the table, the latter are projection of actual columns or functions.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class AbstractColumn {

    protected final Table _table;
    protected final Name _name;

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

    public Name getName() {
        return _name;
    }

    /**
     * Returns the table this column belongs.
     *
     * @return a table.
     */
    public Table getTable() {
        return _table;
    }
}
