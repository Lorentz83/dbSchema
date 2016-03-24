package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.schema.Name;
import java.util.Objects;

/**
 * Contains the representation of a grant statement as seen by the parser.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class Grant {

    /**
     * Represents the type of a grant.
     */
    public enum Type {

        /**
         * Grants a read privilege.
         */
        READ,
        /**
         * Grants a write privilege.
         */
        WRITE,
        /**
         * Grants a role.
         */
        ROLE
    };

    private final Name _to;
    private final Name _role;
    private final Name _table;
    private final Name _column;
    private final Type _type;

    /**
     * Creates a new role grant. I.e. the representation of GRANT admin TO user.
     *
     * @param role the role to be granted.
     * @param to the user or role that receives the grant.
     * @throws NullPointerException if role or to are null.
     * @throws IllegalArgumentException if role or to are empty.
     */
    public Grant(String role, String to) throws NullPointerException, IllegalArgumentException {
        _to = new Name(to);
        _role = new Name(role);
        _type = Type.ROLE;
        _table = null;
        _column = null;
    }

    /**
     * Creates a grant to a table.
     *
     * @param type READ or WRITE.
     * @param to the user or role who receive the grant.
     * @param table the table which is granted.
     * @param column the column which is granted. An empty value means grant all
     * the columns.
     * @throws IllegalArgumentException if type is ROLE or to or table are
     * empty.
     */
    public Grant(Type type, String to, String table, String column) throws IllegalArgumentException, NullPointerException {
        if (type == null) {
            throw new NullPointerException("Null type");
        }
        _to = new Name(to);
        _role = null;
        if (type == Type.ROLE || to.isEmpty() || table.isEmpty()) {
            throw new IllegalArgumentException("Cannot grant both to role and table");
        }
        _type = type;
        _table = new Name(table);
        _column = column.isEmpty() ? null : new Name(column);
    }

    /**
     * Returns the user or role who receive the grant.
     *
     * @return a never empty string.
     */
    public Name getTo() {
        return _to;
    }

    /**
     * Returns the role that is granted.
     *
     * @return the role of null if type is not ROLE.
     */
    public Name getRole() {
        return _role;
    }

    /**
     * Returns the table name which is granted.
     *
     * @return the table name or null if type is ROLE.
     */
    public Name getTable() {
        return _table;
    }

    /**
     * Returns the column name which is granted.
     *
     * @return the column name, null if all the columns are granted or type is
     * ROLE.
     */
    public Name getColumn() {
        return _column;
    }

    /**
     * Returns the type of the grant.
     *
     * @return the type.
     */
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
