package edu.purdue.dbSchema.utils;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnauthorizedSqlException;
import edu.purdue.dbSchema.schema.Column;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class DbGrants implements IDbGrants {

    private final IMapSet<Column, String> _grantRead;
    private final IMapSet<Column, String> _grantWrite;
    private final IDirectedAcyclicGraph<String> _roleGraph;

    DbGrants(IDirectedAcyclicGraph<String> roleGraph, IMapSet<Column, String> read, IMapSet<Column, String> write) {
        _grantRead = read;
        _grantWrite = write;
        _roleGraph = roleGraph;
    }

    public DbGrants() {
        this(new DirectedAcyclicGraph<String>(), new HashMapSet<Column, String>(), new HashMapSet<Column, String>());
    }

    @Override
    public void grantRole(String role, String to) throws SqlSemanticException {
        if (!_roleGraph.add(to, role)) {
            throw new SqlSemanticException("role cycle detected");
        }
    }

    @Override
    public boolean grantRead(Column col, String to) {
        return _grantRead.put(col, to);
    }

    @Override
    public boolean grantWrite(Column col, String to) {
        return _grantWrite.put(col, to);
    }

    private String hasGrant(final IMapSet<Column, String> grant, String username, Column c) throws NullPointerException {
        for (String role : _roleGraph.followNodeAndSelef(username)) {
            if (grant.contains(c, role)) {
                return role;
            }
        }
        return null;
    }

    private Set<String> enforce(IMapSet<Column, String> grant, String username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException {
        Set<String> usedRoles = new HashSet<>();
        for (Column col : columns) {
            String role = hasGrant(grant, username, col);
            if (role == null) {
                throw new UnauthorizedSqlException("the user '%s' has no right to read '%s'", username, col.getName());
            }
            usedRoles.add(role);
        }
        return usedRoles;
    }

    @Override
    public Set<String> enforceWrite(String username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException {
        return enforce(_grantWrite, username, columns);
    }

    @Override
    public Set<String> enforceRead(String username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException {
        return enforce(_grantRead, username, columns);
    }

}
