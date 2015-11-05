package edu.purdue.dbSchema.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains a Data Manipulation Statements query as returned by the parser. Note
 * that in this stage the information may be incomplete because there is no
 * knowledge of the current schema. I.e. in SELECT a, b FROM tbl1 JOIN tbl2 we
 * do not know what table the columns a and b belongs to.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class ParsedQuery {

    /**
     * The query type.
     */
    public final DlmQueryType type;
    /**
     * Contains the main columns of a query. The columns returned by a select,
     * updated by an update or inserted by an insert. The format is (table name,
     * column name), whenever the table name is not specified it is an empty
     * string.
     */
    public List<StringPair> mainColumns = new ArrayList<StringPair>();

    /**
     * The tables that appear in the from clause, or the one from which rows are
     * removed, inserted or updated. The format is (table name, table alias)
     * where if alias misses is an empty string.
     */
    public List<StringPair> from = new ArrayList<StringPair>();

    /**
     * Counts the where clause.
     *
     * TODO improve this.
     */
    public int where = 0;

    /**
     * Creates a ParsedQyery with the specific type.
     *
     * @param type the type of the query.
     */
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
     * @throws NullPointerException if colName or table is null.
     * @throws IllegalArgumentException if colName is empty.
     */
    public void addMainColumn(String table, String colName) throws IllegalArgumentException, NullPointerException {
        if (table == null || colName == null) {
            throw new NullPointerException();
        }
        if (colName.isEmpty()) {
            throw new IllegalArgumentException("Missing column name");
        }
        mainColumns.add(new StringPair(table, colName));
    }

    /**
     * Adds a table used in the query name.
     *
     * @param table the table name.
     * @param alias the table alias or empty string if no alias is specified.
     * @throws NullPointerException if table or alias is null.
     * @throws IllegalArgumentException if table is empty.
     */
    public void addFrom(String table, String alias) throws IllegalArgumentException, NullPointerException {
        if (table == null || alias == null) {
            throw new NullPointerException();
        }
        if (table.isEmpty()) {
            throw new IllegalArgumentException("Missing table name");
        }
        from.add(new StringPair(table, alias));
    }

    public void addWhere() {
        where++;
    }
}
