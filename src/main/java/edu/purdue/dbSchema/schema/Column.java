package edu.purdue.dbSchema.schema;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Column {

    private final String _name;
    private final String _type;
    private final boolean _notNull;
    private final boolean _unique;

    public Column(String name, String type, boolean notNull, boolean unique) {
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
        hash = 97 * hash + _type.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Column other = (Column) obj;
        return _name.equals(other._name)
                && _type.equals(other._type)
                && _notNull == other._notNull
                && _unique == other._unique;
    }

}
