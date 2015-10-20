package edu.purdue.dbSchema.schema;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Column implements Comparable<Column> {

    private final String _name;
    private final String _type;
    private final boolean _notNull;
    private final boolean _unique;

    public Column(String name, String type, boolean notNull, boolean unique) {
        if (name == null || type == null) {
            throw new NullPointerException("name or type");
        }
        _name = name;
        _type = type;
        _notNull = notNull;
        _unique = unique;
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
        hash = 97 * hash + (this._name != null ? this._name.hashCode() : 0);
        hash = 97 * hash + (this._type != null ? this._type.hashCode() : 0);
        hash = 97 * hash + (this._notNull ? 1 : 0);
        hash = 97 * hash + (this._unique ? 1 : 0);
        return hash;
    }

    @Override
    public int compareTo(Column o) {
        if (this.equals(o)) {
            return 0;
        }
        return this._name.compareTo(o._name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Column other = (Column) obj;
        if ((this._name == null) ? (other._name != null) : !this._name.equals(other._name)) {
            return false;
        }
        if ((this._type == null) ? (other._type != null) : !this._type.equals(other._type)) {
            return false;
        }
        if (this._notNull != other._notNull) {
            return false;
        }
        if (this._unique != other._unique) {
            return false;
        }
        return true;
    }

}
