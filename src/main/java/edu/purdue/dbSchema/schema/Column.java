package edu.purdue.dbSchema.schema;

import java.io.Serializable;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Column implements Serializable {

    private final String _name;
    private final String _type;
    private final boolean _notNull;
    private final boolean _unique;
    private final Table _table;

    Column(String name, String type, boolean notNull, boolean unique, Table table) {
        if (name == null || type == null) {
            throw new NullPointerException("name or type");
        }
        if (name.isEmpty() || type.isEmpty()) {
            throw new IllegalArgumentException("name or type");
        }
        _name = name;
        _type = type;
        _notNull = notNull;
        _unique = unique;
        _table = table;
    }

    public String getName() {
        return _name;
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
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + _name.hashCode();
        hash = 97 * hash + (_table == null ? 0 : _table.getName().hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

}
