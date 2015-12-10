package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.parser.DlmQueryType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class QueryFeature {

    private final DlmQueryType _type;
    private final Collection<Column> _usedCols;
    private final Collection<Column> _filteredCols;
    private final Collection<Name> _roles;

    public QueryFeature(DlmQueryType type, Collection<Column> used, Collection<Column> where, Collection<Name> roles) {
        if (type == null || used == null || where == null || roles == null) {
            throw new NullPointerException();
        }
        _type = type;
        _usedCols = Collections.unmodifiableCollection(used);
        _filteredCols = Collections.unmodifiableCollection(where);
        _roles = Collections.unmodifiableCollection(roles);
    }

    QueryFeature() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DlmQueryType getType() {
        return _type;
    }

    public Collection<Column> getUsedCols() {
        return _usedCols;
    }

    public Collection<Column> getFilteredCols() {
        return _filteredCols;
    }

    public Collection<Name> getRoles() {
        return _roles;
    }

    QueryFeature merge(QueryFeature other) {
        if (_type != other._type) {
            throw new UnsupportedOperationException("Cannot merge different type of features yet");
        }
        Set<Column> usedCols = new HashSet<>(_usedCols);
        usedCols.addAll(other._usedCols);

        Set<Column> filteredCols = new HashSet<>(_filteredCols);
        filteredCols.addAll(other._filteredCols);

        Set<Name> roles = new TreeSet<>(_roles);
        roles.addAll(other._roles);

        return new QueryFeature(_type, usedCols, filteredCols, roles);
    }
}
