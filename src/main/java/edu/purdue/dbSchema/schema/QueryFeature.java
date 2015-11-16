package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.parser.DlmQueryType;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class QueryFeature {

    private final DlmQueryType _type;
    private final Collection<Column> _usedCols;
    private final Collection<Column> _filteredCols;
    private final Set<Name> _roles;

    public QueryFeature(DlmQueryType type, List<Column> used, List<Column> where, Set<Name> roles) {
        if (type == null || used == null || where == null || roles == null) {
            throw new NullPointerException();
        }
        _type = type;
        _usedCols = used;
        _filteredCols = where;
        _roles = roles;
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

    public Set<Name> getRoles() {
        return _roles;
    }
}
