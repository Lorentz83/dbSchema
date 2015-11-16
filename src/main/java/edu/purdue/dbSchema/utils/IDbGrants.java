package edu.purdue.dbSchema.utils;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnauthorizedSqlException;
import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Name;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Contains and checks the permissions of a database. Note that there is no
 * distinction between roles and users, the two words may be used
 * interchangeably. The roles are stored in a direct acyclic graph that is
 * visited to check the permissions.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public interface IDbGrants extends Serializable {

    /**
     * Grants one role to another role or user.
     *
     * @param role the role to grant.
     * @param to the user or role to be granted.
     * @throws NullPointerException if a parameter is null.
     * @throws edu.purdue.dbSchema.erros.SqlSemanticException if the new role
     * would add a circular reference.
     */
    public void grantRole(Name role, Name to) throws NullPointerException, SqlSemanticException;

    /**
     * Grants the read permission of one column to a role.
     *
     * @param column the column associated to the current permission.
     * @param to the user or role to grant the current permission.
     * @return true if the grant was not already present.
     * @throws NullPointerException if a parameter is null.
     */
    public boolean grantRead(Column column, Name to) throws NullPointerException;

    /**
     * Grants the write permission of one column to a role.
     *
     * @param column the column associated to the current permission.
     * @param to the user or role to grant the current permission.
     * @return true if the grant was not already present.
     * @throws NullPointerException if a parameter is null.
     */
    public boolean grantWrite(Column column, Name to) throws NullPointerException;

    /**
     * Checks if a user can read a set of columns.
     *
     * @param username the name of the user or role.
     * @param columns a collection of columns to check if the user can read.
     * @return the set of roles required to read the columns.
     * @throws NullPointerException if a parameter is null.
     * @throws UnauthorizedSqlException if the user has no permission to read
     * all the columns.
     */
    public Set<Name> enforceRead(Name username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException;

    /**
     * Checks if a user can write a set of columns.
     *
     * @param username the name of the user or role.
     * @param columns a collection of columns to check if the user can write.
     * @return the set of roles required to write the columns.
     * @throws NullPointerException if a parameter is null.
     * @throws UnauthorizedSqlException if the user has no permission to write
     * all the columns.
     */
    public Set<Name> enforceWrite(Name username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException;

}
