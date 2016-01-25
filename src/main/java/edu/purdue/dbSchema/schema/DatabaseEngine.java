package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnauthorizedSqlException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.parser.DlmQueryType;
import edu.purdue.dbSchema.parser.Grant;
import edu.purdue.dbSchema.parser.ParsedQuery;
import edu.purdue.dbSchema.parser.SqlParser;
import edu.purdue.dbSchema.parser.StringPair;
import edu.purdue.dbSchema.utils.DbGrants;
import edu.purdue.dbSchema.utils.IDbGrants;
import gudusoft.gsqlparser.EDbVendor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains the database engine. This class implements the logic to handle
 * queries and to keep a representation of the database in memory.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DatabaseEngine implements Serializable {

    static final Logger LOGGER = Logger.getLogger(DatabaseEngine.class.getName());

    private final Map<Name, Table> _tables;
    private final IDbGrants _grants;
    private final EDbVendor _dbVendor;

    /**
     * Creates a DatabaseEngine specifying the database vendor. The database
     * vendor is required to enable the parser to recognize the SQL dialect.
     *
     * @param dbVendor the SQL dialect.
     */
    public DatabaseEngine(EDbVendor dbVendor) {
        if (dbVendor == null) {
            throw new NullPointerException("dbVendor");
        }
        _dbVendor = dbVendor;
        _tables = new TreeMap<>();
        _grants = new DbGrants();
    }

    /**
     * Extracts the features of a given SQL query. This method can handle only
     * SELECT/UPDATE/DELETE/INSERT queries and is intended to extract a set of
     * features to identify the user behavior. It can handle multiple queries.
     * Note that a single query like
     * <code>INSERT INTO tbl1(c) (SELECT -a FROM tbl2)</code> may return
     * multiple features (in this example, one insert and one select).
     *
     * @param sql the SQL query or multiple queries separated by a semicolon.
     * @param username the user who issued the query.
     * @return a list of query features.
     * @throws SqlParseException in case of parse errors.
     * @throws UnsupportedSqlException in case a statement is not supported by
     * this parser.
     * @throws SqlSemanticException in case of semantic errors, i.e. a table
     * that do not exist is referenced.
     * @throws UnauthorizedSqlException if the user is not authorized to execute
     * such kind of queries.
     */
    public List<QueryFeature> parse(String sql, String username) throws SqlParseException, UnsupportedSqlException, SqlSemanticException, UnauthorizedSqlException {
        Name normalizedUsername = new Name(username);
        SqlParser parser = new SqlParser(_dbVendor);
        List<QueryFeature> features = new ArrayList<>();

        int parsedQueriesNum = parser.parse(sql);
        List<ParsedQuery> parsedQueries = parser.getDmlQueries();
        if (parsedQueriesNum != parsedQueries.size()) {
            throw new UnauthorizedSqlException("the query is not a SELECT/UPDATE/DELETE/INSERT");
        }

        for (ParsedQuery q : parsedQueries) {
            QueryFeature qf = evaluateDlmQuery(q, normalizedUsername, Collections.<Name, Table>emptyMap());
            features.add(qf);
        }
        return features;
    }

    /**
     * Parses an SQL statement. This method should be used only to create the
     * schema and to add the grants, because there is no way to retrieve the
     * query features. Queries are submitted as a database administrator.
     *
     * @param sql the query or the queries separated by a semicolon.
     * @return the number of statements parsed.
     * @throws SqlParseException in case of parse errors.
     * @throws UnsupportedSqlException in case a statement is not supported by
     * this parser.
     * @throws SqlSemanticException in case of semantic errors.
     */
    public int parse(String sql) throws SqlParseException, UnsupportedSqlException, SqlSemanticException {
        SqlParser parser = new SqlParser(_dbVendor);
        int ret = parser.parse(sql);
        for (Table t : parser.getTables()) {
            if (_tables.put(t.getName(), t) != null) {
                throw new SqlSemanticException("relation '%s' already exists", t.getName());
            }
        }
        for (Grant g : parser.getGrants()) {
            evaluateGrant(g);
        }
        for (ParsedQuery parsed : parser.getDmlQueries()) {
            try {
                evaluateDlmQuery(parsed, null, Collections.<Name, Table>emptyMap());
            } catch (UnauthorizedSqlException ex) {
                LOGGER.log(Level.SEVERE, "This exception should never been thrown in this context", ex);
                throw new AssertionError(ex);
            }
        }
        return ret;
    }

    /**
     *
     * @param parsed the parsed query to evaluate.
     * @param userName the user name who issued the query.
     * @param additionalTables additional tables to use to resolve the name.
     * I.e. tables used in the outer query.
     * @return the query feature set.
     * @throws SqlSemanticException if the sql contains semantic errors or
     * cannot be evaluated against the given schema.
     * @throws UnauthorizedSqlException if the user does not have the required
     * grants.
     */
    protected QueryFeature evaluateDlmQuery(ParsedQuery parsed, Name userName, Map<Name, Table> additionalTables) throws SqlSemanticException, UnauthorizedSqlException {
        HashMap<Name, Table> usedTables = filterTables(parsed.from, additionalTables);
        HashMap<Name, Table> virtualTables = new HashMap<>();

        List<QueryFeature> features = new ArrayList<>();

        // sub queries
        for (Map.Entry<String, ParsedQuery> set : parsed.subQueriesFrom.entrySet()) {
            Name alias = new Name(set.getKey());
            ParsedQuery sub = set.getValue();
            QueryFeature qf = evaluateDlmQuery(sub, userName, usedTables);
            features.add(qf);

            Table virtualTable = new Table(alias, qf.getUsedCols());
            for (String virtualCol : sub.virtualColumns.keySet()) {
                Set<AbstractColumn> mappedTo = new HashSet<>();
                for (StringPair subCol : sub.virtualColumns.getSet(virtualCol)) {
                    addSelectedColumn(usedTables, parsed.whereColumns, mappedTo);
                }
                virtualTable.addVirtualColumn(virtualCol, mappedTo);
            }
            if (virtualTables.put(alias, virtualTable) != null) {
                throw new SqlSemanticException("table name '%s' specified more than once", alias);
            }
        }

        for (Map.Entry<Name, Table> vt : virtualTables.entrySet()) {
            if (usedTables.put(vt.getKey(), vt.getValue()) != null) {
                throw new SqlSemanticException("table name '%s' specified more than once", vt.getKey());
            }
        }

        for (ParsedQuery sub : parsed.subQueriesSelect) {
            features.add(evaluateDlmQuery(sub, userName, usedTables));
        }
        for (ParsedQuery sub : parsed.subQueriesWhere) {
            features.add(evaluateDlmQuery(sub, userName, usedTables));
        }

        // current query
        ArrayList<AbstractColumn> select = new ArrayList<>();
        addSelectedColumn(usedTables, parsed.mainColumns, select);
        ArrayList<AbstractColumn> where = new ArrayList<>();
        addSelectedColumn(usedTables, parsed.whereColumns, where);
        Set<Name> usedRoles = enforceRoles(userName, parsed.type, select, where);
        features.add(new QueryFeature(parsed.type, select, where, usedRoles));

        // next query
        if (parsed.nextCombinedQuery != null) {
            features.add(evaluateDlmQuery(parsed.nextCombinedQuery, userName, Collections.<Name, Table>emptyMap()));
        }

        // combine the result
        return new QueryFeature(features);
    }

    /**
     * Checks if the user has the right privileges on the selected columns. the
     * mainCols are the main columns involved in the queries, that may be the
     * selected or the updated. Depending on the query type these columns are
     * checked for write or read permission.
     *
     * @param userName the user who sent the query.
     * @param type the query type.
     * @param mainCols the main columns used in the query.
     * @param filteredCols the columns used to filter the query.
     * @return a set with the roles used by the user.
     * @throws UnauthorizedSqlException if the user misses any required right.
     * @throws NullPointerException
     */
    private Set<Name> enforceRoles(Name userName, DlmQueryType type, ArrayList<AbstractColumn> mainCols, ArrayList<AbstractColumn> filteredCols) throws UnauthorizedSqlException, NullPointerException {
        Set<Name> usedRoles = new HashSet<>();
        if (userName != null) {
            if (type == DlmQueryType.SELECT) {
                usedRoles.addAll(_grants.enforceRead(userName, mainCols));
            } else {
                usedRoles.addAll(_grants.enforceWrite(userName, mainCols));
            }
            usedRoles.addAll(_grants.enforceRead(userName, filteredCols));
        }
        return usedRoles;
    }

    /**
     * Adds the selected mainColumns to a provided list.
     *
     * @param usedTables the tables (with aliases) to search for mainColumns.
     * @param selectedCols a pair of (table name, column name) where the table
     * name may be empty.
     * @param retVal the collection where the new columns are going to be added.
     * @return the number of column added.
     * @throws SqlSemanticException if a column is referenced more than once, if
     * a column name is ambiguous or if a column reference a missing table
     */
    protected static int addSelectedColumn(final HashMap<Name, Table> usedTables, final List<StringPair> selectedCols, Collection<AbstractColumn> retVal) throws SqlSemanticException {
        int initialSize = retVal.size();
        for (StringPair select : selectedCols) {
            String tblName = select.first; //may be empty
            String colName = select.second;
            Table selectedTable;
            AbstractColumn selectedCol = null;

            if (tblName.isEmpty()) { // we miss the table name
                if (colName.equals("*")) { //select *
                    for (Table t : usedTables.values()) {
                        retVal.addAll(t.getColumns());
                    }
                    continue;
                }
                int counter = 0;
                // we search for a table which contains that column name
                for (Table t : usedTables.values()) {
                    AbstractColumn tmpCol = t.getColumn(colName);
                    if (tmpCol != null) {
                        selectedTable = t;
                        counter++;
                        selectedCol = tmpCol;
                    }
                }
                if (counter > 1) {
                    throw new SqlSemanticException("column reference '%s' is ambiguous", colName);
                }
            } else { // we know the table to search
                selectedTable = usedTables.get(new Name(tblName));
                if (selectedTable == null) {
                    throw new SqlSemanticException("missing FROM-clause entry for table '%s'", tblName);
                }
                if (colName.equals("*")) { //select *
                    retVal.addAll(selectedTable.getColumns());
                    continue;
                }
                selectedCol = selectedTable.getColumn(colName);
            }
            if (selectedCol == null) {
                throw new SqlSemanticException("column '%s' does not exist", colName);
            }
            retVal.add(selectedCol);
        }
        return retVal.size() - initialSize;
    }

    /**
     * Filters the tables used by the query.
     *
     * @param tableNames a list of pairs (table name, table alias).
     * @param additionalTables additional tables to be used to resolve the
     * names, i.e. the parent query tables.
     * @return a Map that associates names to tables and aliases to tables.
     * @throws SqlSemanticException if a table does not exists in the db or a
     * table is specified more than once.
     */
    protected HashMap<Name, Table> filterTables(List<StringPair> tableNames, Map<Name, Table> additionalTables) throws SqlSemanticException {
        HashMap<Name, Table> usedTables = new HashMap<>(additionalTables); // we want to copy it to don't change the parent's mapping
        HashSet<Name> tablesWithAlias = new HashSet<>();
        for (StringPair from : tableNames) {
            if (from.first.isEmpty()) {
                // this is a subquery
                continue;
            }
            final Name name = new Name(from.first);
            final String alias = from.second;

            Table t = _tables.get(name);
            if (t == null) {
                throw new SqlSemanticException("relation '%s' does not exist", name);
            }
            if (usedTables.put(name, t) != null) {
                if (tablesWithAlias.contains(name)) {
                    tablesWithAlias.remove(name);
                    usedTables.remove(name);
                } else {
                    if (!additionalTables.containsKey(name)) { //subqueries override tables in the parent query
                        throw new SqlSemanticException("table name '%s' specified more than once", name);
                    }
                }
            }
            if (!alias.isEmpty()) { // add the alias
                if (usedTables.put(new Name(alias), t) != null) {
                    throw new SqlSemanticException("table name '%s' specified more than once", alias);
                }
                tablesWithAlias.add(name);
            }
        }
        return usedTables;
    }

    protected void evaluateGrantToRole(Grant g) throws UnsupportedSqlException, SqlSemanticException {
        if (g.getType() != Grant.Type.ROLE) {
            throw new IllegalArgumentException("not grant to role");
        }
        _grants.grantRole(g.getRole(), g.getTo());
    }

    protected void evaluateGrantToTable(Grant grant) throws SqlSemanticException {
        BiConsumer<AbstractColumn, Name> addGrant;
        switch (grant.getType()) {
            case READ:
                addGrant = (col, role) -> _grants.grantRead(col, role);
                break;
            case WRITE:
                addGrant = (col, role) -> _grants.grantWrite(col, role);
                break;
            default:
                throw new IllegalArgumentException("not a grant to table");
        }

        Table table = _tables.get(grant.getTable());
        if (table == null) {
            throw new SqlSemanticException("relation '%s' does not exist", grant.getTable());
        }

        final Name colName = grant.getColumn();
        Collection<AbstractColumn> colsToGrant;
        if (colName == null) {
            colsToGrant = table.getColumns();
        } else {
            AbstractColumn col = table.getColumn(colName);
            if (col == null) {
                throw new SqlSemanticException("column '%s' does not exist in table '%s'", colName, grant.getTable());
            }
            colsToGrant = new ArrayList<>();
            colsToGrant.add(col);
        }
        for (AbstractColumn col : colsToGrant) {
            addGrant.accept(col, grant.getTo());
        }
    }

    protected void evaluateGrant(Grant g) throws UnsupportedSqlException, SqlSemanticException {
        if (g.getType() == Grant.Type.ROLE) {
            evaluateGrantToRole(g);
        } else {
            evaluateGrantToTable(g);
        }
    }

    /**
     * Gets an immutable collection of tables. Use this method to know the
     * tables contained in this database representation.
     *
     * @return a collection of tables.
     */
    public Collection<Table> getTables() {
        return Collections.unmodifiableCollection(_tables.values());
    }

    /**
     * Gets a table by its name.
     *
     * @param name the table name.
     * @return the table or null if there is no table with the given name.
     * @throws NullPointerException if name is null.
     * @throws IllegalArgumentException if name is empty.
     */
    public Table getTable(String name) {
        return _tables.get(new Name(name));
    }
}
