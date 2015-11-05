package edu.purdue.dbSchema.parser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lorenzo Bossi
 */
public class ParsedQuery {

    public final DlmQueryType type;
    /**
     * Contains the main columns of a query. The columns returned by a select,
     * updated by an update or inserted by an insert.
     */
    public List<StringPair> mainColumns = new ArrayList<StringPair>();
    public List<StringPair> from = new ArrayList<StringPair>();
    public int where = 0;

    public ParsedQuery(DlmQueryType type) {
        this.type = type;
    }

    /**
     * Adds the main columns of a query. The columns returned by a select,
     * updated by an update or inserted by an insert.
     *
     * @param table the table name or view if explicitly specified in the query
     * or empty string.
     * @param colName the column name.
     */
    public void addMainColumn(String table, String colName) {
        mainColumns.add(new StringPair(table, colName));
    }

    /**
     * Adds a table used in the query name.
     *
     * @param table the table name.
     * @param alias the table alias or empty string if no alias is specified.
     */
    public void addFrom(String table, String alias) {
        from.add(new StringPair(table, alias));
    }

    public void addWhere() {
        where++;
    }
}
