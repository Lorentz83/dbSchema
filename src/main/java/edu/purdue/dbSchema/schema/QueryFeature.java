package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.parser.DlmQueryType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class QueryFeature {

    private final DlmQueryType _type;
    private final Collection<AbstractColumn> _usedCols;
    private final Collection<AbstractColumn> _filteredCols;
    private final Collection<Name> _roles;

    public QueryFeature(DlmQueryType type, Collection<AbstractColumn> used, Collection<AbstractColumn> where, Collection<Name> roles) {
        if (type == null || used == null || where == null || roles == null) {
            throw new NullPointerException();
        }
        _type = type;
        _usedCols = Collections.unmodifiableCollection(used);
        _filteredCols = Collections.unmodifiableCollection(where);
        _roles = Collections.unmodifiableCollection(roles);
    }

    /**
     * Creates a QueryFeature merging all the features provided.
     *
     * @param features the features to merge.
     * @throws UnsupportedOperationException if the type of the features does
     * not match.
     * @throws NullPointerException if features or any of its element is null.
     * @throws NoSuchElementException if there is no feature
     */
    QueryFeature(Collection<QueryFeature> features) throws NullPointerException, UnsupportedOperationException, NoSuchElementException {
        if (features.isEmpty()) {
            throw new NoSuchElementException("no features to merge");
        }
        Set<AbstractColumn> usedCols = new HashSet<>();
        Set<AbstractColumn> filteredCols = new HashSet<>();
        Set<Name> roles = new TreeSet<>();
        DlmQueryType type = null;

        for (QueryFeature other : features) {
            if (type != null && type != other._type) {
                throw new UnsupportedOperationException("Cannot merge different type of features yet");
            }
            type = other._type;
            usedCols.addAll(other._usedCols);
            filteredCols.addAll(other._filteredCols);
            roles.addAll(other._roles);
        }
        _type = type;
        _usedCols = Collections.unmodifiableCollection(usedCols);
        _filteredCols = Collections.unmodifiableCollection(filteredCols);
        _roles = Collections.unmodifiableCollection(roles);
    }

    public DlmQueryType getType() {
        return _type;
    }

    public Collection<AbstractColumn> getUsedCols() {
        return _usedCols;
    }

    public Collection<AbstractColumn> getFilteredCols() {
        return _filteredCols;
    }

    public Collection<Name> getRoles() {
        return _roles;
    }
}
