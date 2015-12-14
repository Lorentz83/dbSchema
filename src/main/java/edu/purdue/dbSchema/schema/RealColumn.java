package edu.purdue.dbSchema.schema;

import java.io.Serializable;

/**
 * Represents a table column. This is an immutable object linked to the table
 * which created it. Equality on columns considers also the table they belong
 * to.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class RealColumn extends AbstractColumn implements Serializable {

    private final String _type;
    private final boolean _notNull;
    private final boolean _unique;

    /**
     * Creates a column.
     *
     * @param name the column name.
     * @param type the colum type.
     * @param notNull if the column is not not null.
     * @param unique if the column is unique or primary key.
     * @param table the table this column belongs.
     * @throws NullPointerException if name or type are null.
     * @throws IllegalArgumentException if name or type are empty.
     */
    RealColumn(Name name, String type, boolean notNull, boolean unique, Table table) throws NullPointerException, IllegalArgumentException {
        super(table, name);
        if (name == null || type == null) {
            throw new NullPointerException("name or type");
        }
        if (type.isEmpty()) {
            throw new IllegalArgumentException("name or type");
        }
        _type = type;
        _notNull = notNull;
        _unique = unique;
    }

    public String getType() {
        return _type;
    }

    public boolean isNotNull() {
        return _notNull;
    }

    public boolean isUnique() {
        return _unique;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_name).append(' ').append(_type);
        if (_notNull) {
            sb.append(' ').append("NOT NULL");
        }
        if (_unique) {
            sb.append(' ').append("UNIQUE");
        }
        return sb.toString();
    }

    @Override
    public boolean isVirtual() {
        return false;
    }
}
