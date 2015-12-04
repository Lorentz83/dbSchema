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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DatabaseEngine implements Serializable {

    static final Logger LOGGER = Logger.getLogger(DatabaseEngine.class.getName());

    private final Map<Name, Table> _tables;
    private final IDbGrants _grants;
    private final EDbVendor _dbVendor;

    public DatabaseEngine(EDbVendor dbVendor) {
        if (dbVendor == null) {
            throw new NullPointerException("dbVendor");
        }
        _dbVendor = dbVendor;
        _tables = new TreeMap<>();
        _grants = new DbGrants();
    }

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
            QueryFeature qf = evaluateDlmQuery(q, normalizedUsername);
            features.add(qf);
        }
        return features;
    }

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
                evaluateDlmQuery(parsed, null);
            } catch (UnauthorizedSqlException ex) {
                LOGGER.log(Level.SEVERE, "This exception should never been thrown in this context", ex);
            }
        }
        return ret;
    }

    protected QueryFeature evaluateDlmQuery(ParsedQuery parsed, Name name) throws SqlSemanticException, NullPointerException, UnauthorizedSqlException {
        HashMap<Name, Table> usedTables = filterTables(parsed.from);
        ArrayList<Column> select = getSelectedColumns(usedTables, parsed.mainColumns);
        ArrayList<Column> where = getSelectedColumns(usedTables, parsed.whereColumns);
        Set<Name> usedRoles = new HashSet<>();

        if (name != null) {
            if (parsed.type == DlmQueryType.SELECT) {
                usedRoles.addAll(_grants.enforceRead(name, select));
            } else {
                usedRoles.addAll(_grants.enforceWrite(name, select));
            }
            usedRoles.addAll(_grants.enforceRead(name, where));
        }
        return new QueryFeature(parsed.type, select, where, usedRoles);
    }

    /**
     * Returns a list of selected mainColumns.
     *
     * @param usedTables the tables (with aliases) to search for mainColumns.
     * @param selectedCols a pair of (table name, column name) where the table
     * name may be empty.
     * @return a list of Columns used.
     * @throws SqlSemanticException if a column is referenced more than once, if
     * a column name is ambiguous or if a column reference a missing table
     */
    protected static ArrayList<Column> getSelectedColumns(final HashMap<Name, Table> usedTables, final List<StringPair> selectedCols) throws SqlSemanticException {
        ArrayList<Column> usedCols = new ArrayList<>();
        for (StringPair select : selectedCols) {
            String tblName = select.first; //may be empty
            String colName = select.second;
            Table selectedTable;
            Column selectedCol = null;

            if (tblName.isEmpty()) { // we miss the table name
                if (colName.equals("*")) { //select *
                    for (Table t : usedTables.values()) {
                        usedCols.addAll(t.getColumns());
                    }
                    continue;
                }
                int counter = 0;
                // we search for a table which contains that column name
                for (Table t : usedTables.values()) {
                    Column tmpCol = t.getColumn(colName);
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
                    usedCols.addAll(selectedTable.getColumns());
                    continue;
                }
                selectedCol = selectedTable.getColumn(colName);
            }
            if (selectedCol == null) {
                throw new SqlSemanticException("column '%s' does not exist", colName);
            }
            usedCols.add(selectedCol);
        }
        return usedCols;
    }

    /**
     * Filter the tables used by the query.
     *
     * @param tableNames a list of pairs (table name, table alias).
     * @return a Map that associates names to tables and aliases to tables.
     * @throws SqlSemanticException if a table does not exists in the db or a
     * table is specified more than once.
     */
    protected HashMap<Name, Table> filterTables(List<StringPair> tableNames) throws SqlSemanticException {
        HashMap<Name, Table> usedTables = new HashMap<>();
        HashSet<Name> tablesWithAlias = new HashSet<>();
        for (StringPair from : tableNames) {
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
                    throw new SqlSemanticException("table name '%s' specified more than once", name);
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
        BiConsumer<Column, Name> addGrant;
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
        Collection<Column> colsToGrant;
        if (colName == null) {
            colsToGrant = table.getColumns();
        } else {
            Column col = table.getColumn(colName);
            if (col == null) {
                throw new SqlSemanticException("column '%s' does not exist in table '%s'", colName, grant.getTable());
            }
            colsToGrant = new ArrayList<>();
            colsToGrant.add(col);
        }
        for (Column col : colsToGrant) {
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

    public Collection<Table> getTables() {
        return Collections.unmodifiableCollection(_tables.values());
    }

    public Table getTable(String name) throws NoSuchElementException {
        return _tables.get(new Name(name));
    }
}
