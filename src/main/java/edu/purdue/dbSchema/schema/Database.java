package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.parser.DlmQueryType;
import edu.purdue.dbSchema.parser.ParsedQuery;
import edu.purdue.dbSchema.parser.SqlParser;
import edu.purdue.dbSchema.parser.StringPair;
import gudusoft.gsqlparser.EDbVendor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Database {

    private final Map<String, Table> _tables;
    private final EDbVendor _dbVendor;

    public Database(EDbVendor dbVendor) {
        if (dbVendor == null) {
            throw new NullPointerException("dbVendor");
        }
        _dbVendor = dbVendor;
        _tables = new TreeMap<>();
    }

    public int parse(String sql) throws SqlParseException, UnsupportedSqlException, SqlSemanticException {
        SqlParser parser = new SqlParser(_dbVendor);
        int ret = parser.parse(sql);
        for (Table t : parser.getTables()) {
            if (_tables.put(t.getName(), t) != null) {
                throw new SqlSemanticException("relation '" + t.getName() + "' already exists");
            }
        }
        for (ParsedQuery parsed : parser.getDmlQueries()) {
            switch (parsed.type) {
                case SELECT:
                    evaluateSelect(parsed);
                    break;
                default:
                    throw new UnsupportedSqlException(parsed.type.toString());
            }
        }
        return ret;
    }

    protected List<Column> evaluateSelect(ParsedQuery parsed) throws SqlSemanticException {
        if (parsed.type != DlmQueryType.SELECT) {
            throw new IllegalArgumentException("expected select got " + parsed.type);
        }
        HashMap<String, Table> usedTables = filterTables(parsed.from);
        return getSelectedColumns(usedTables, parsed.select);
    }

    /**
     * Returns a list of selected columns.
     *
     * @param usedTables the tables (with aliases) to search for columns.
     * @param selectedCols a pair of (table name, column name) where the table
     * name may be empty.
     * @return a list of Columns used.
     * @throws SqlSemanticException if a column is referenced more than once, if
     * a column name is ambiguous or if a column reference a missing table
     */
    protected static ArrayList<Column> getSelectedColumns(final HashMap<String, Table> usedTables, final List<StringPair> selectedCols) throws SqlSemanticException {
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
                selectedTable = usedTables.get(tblName);
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
    protected HashMap<String, Table> filterTables(List<StringPair> tableNames) throws SqlSemanticException {
        HashMap<String, Table> usedTables = new HashMap<>();
        for (StringPair from : tableNames) {
            final String name = from.first;
            final String alias = from.second;

            Table t = _tables.get(name);
            if (t == null) {
                throw new SqlSemanticException("relation '%s' does not exist", name);
            }
            if (usedTables.put(name, t) != null) {
                throw new SqlSemanticException("table name '%s' specified more than once", name);
            }
            if (!alias.isEmpty()) { // add the alias
                if (usedTables.put(alias, t) != null) {
                    throw new SqlSemanticException("table name '%s' specified more than once", alias);
                }
            }
        }
        return usedTables;
    }

    public Table getTable(String name) throws NoSuchElementException {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        return _tables.get(name);
    }
}
