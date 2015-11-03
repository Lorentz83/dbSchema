package edu.purdue.dbSchema.parser;

import java.util.Objects;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Grant {

    public enum Type {

        READ, WRITE, ROLE
    };

    private final String _to;
    private final String _role;
    private final String _table;
    private final String _column;
    private final Type _type;

    public Grant(String role, String to) {
        _to = to;
        _role = role;
        _type = Type.ROLE;
        _table = null;
        _column = null;
    }

    public Grant(Type type, String to, String table, String column) {
        _to = to;
        _role = null;
        if (type == Type.ROLE) {
            throw new IllegalArgumentException("Cannot grant both to role and table");
        }
        _type = type;
        _table = table;
        _column = column;
    }

    public String getTo() {
        return _to;
    }

    public String getRole() {
        return _role;
    }

    public String getTable() {
        return _table;
    }

    public String getColumn() {
        return _column;
    }

    public Type getType() {
        return _type;
    }

    @Override
    public String toString() {
        if (_type == Type.ROLE) {
            return String.format("GRANT %s TO %s", _role, _to);
        }
        return String.format("GRANT %s ON %s.%s TO %s", _type, _table, _column, _to);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this._to);
        hash = 89 * hash + Objects.hashCode(this._role);
        hash = 89 * hash + Objects.hashCode(this._table);
        hash = 89 * hash + Objects.hashCode(this._column);
        hash = 89 * hash + Objects.hashCode(this._type);
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
        final Grant other = (Grant) obj;
        if (!Objects.equals(this._to, other._to)) {
            return false;
        }
        if (!Objects.equals(this._role, other._role)) {
            return false;
        }
        if (!Objects.equals(this._table, other._table)) {
            return false;
        }
        if (!Objects.equals(this._column, other._column)) {
            return false;
        }
        if (this._type != other._type) {
            return false;
        }
        return true;
    }
}
