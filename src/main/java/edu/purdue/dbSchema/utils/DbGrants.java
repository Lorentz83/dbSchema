package edu.purdue.dbSchema.utils;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnauthorizedSqlException;
import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Name;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DbGrants implements IDbGrants {

    private final IMapSet<Column, Name> _grantRead;
    private final IMapSet<Column, Name> _grantWrite;
    private final IDirectedAcyclicGraph<Name> _roleGraph;

    DbGrants(IDirectedAcyclicGraph<Name> roleGraph, IMapSet<Column, Name> read, IMapSet<Column, Name> write) {
        _grantRead = read;
        _grantWrite = write;
        _roleGraph = roleGraph;
    }

    public DbGrants() {
        this(new DirectedAcyclicGraph<Name>(), new HashMapSet<Column, Name>(), new HashMapSet<Column, Name>());
    }

    @Override
    public void grantRole(Name role, Name to) throws SqlSemanticException {
        if (!_roleGraph.add(to, role)) {
            throw new SqlSemanticException("role cycle detected");
        }
    }

    @Override
    public boolean grantRead(Column col, Name to) {
        return _grantRead.put(col, to);
    }

    @Override
    public boolean grantWrite(Column col, Name to) {
        return _grantWrite.put(col, to);
    }

    private Name hasGrant(final IMapSet<Column, Name> grant, Name username, Column c) throws NullPointerException {
        for (Name role : _roleGraph.followNodeAndSelef(username)) {
            if (grant.contains(c, role)) {
                return role;
            }
        }
        return null;
    }

    private Set<Name> enforce(IMapSet<Column, Name> grant, Name username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException {
        Set<Name> usedRoles = new HashSet<>();
        for (Column col : columns) {
            Name role = hasGrant(grant, username, col);
            if (role == null) {
                throw new UnauthorizedSqlException("the user '%s' has no right to read '%s'", username, col.getName());
            }
            usedRoles.add(role);
        }
        return usedRoles;
    }

    @Override
    public Set<Name> enforceWrite(Name username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException {
        return enforce(_grantWrite, username, columns);
    }

    @Override
    public Set<Name> enforceRead(Name username, Collection<Column> columns) throws NullPointerException, UnauthorizedSqlException {
        return enforce(_grantRead, username, columns);
    }

}
