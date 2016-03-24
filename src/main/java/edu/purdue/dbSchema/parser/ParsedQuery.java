package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.utils.HashMapSet;
import edu.purdue.dbSchema.utils.IMapSet;
import edu.purdue.dbSchema.utils.Pair;
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
     * Contains the aliases in the select. Every alias is mapped to a set of
     * columns used to generate it. The set may be empty in case no real columns
     * are used for this alias (i.e. select now() as "time")
     */
    public IMapSet<String, StringPair> virtualColumns = new HashMapSet<>();

    /**
     * Contains the main columns of a query. The columns returned by a select,
     * updated by an update or inserted by an insert. The format is (table name,
     * column name), whenever the table name is not specified it is an empty
     * string.
     */
    public List<StringPair> mainColumns = new ArrayList<>();

    /**
     * The tables that appear in the from clause, or the one from which rows are
     * removed, inserted or updated. The format is (table name, table alias)
     * where if alias misses is an empty string.
     */
    public List<StringPair> from = new ArrayList<>();

    /**
     * Contains the columns touched by a where or join clause. The format is
     * (table name, column name), whenever the table name is not specified it is
     * an empty string.
     */
    public List<StringPair> whereColumns = new ArrayList<>();

    /**
     * Contains the next query in case of combined queries. I.e. if the parsed
     * query is "SQL1 union SQL2 except SQL3 intersect SQL4" and this element
     * contains SQL2, nextCombinedQuery contains SQL3. This value is null if
     * there is no next query.
     */
    public ParsedQuery nextCombinedQuery = null;

    /**
     * Contains a list of sub queries in the SELECT clause. Empty if no
     * sub-query exists.
     */
    public List<ParsedQuery> subQueriesSelect = new ArrayList<>();

    /**
     * Contains a list of pairs [alias, ParsedQuery] of sub queries used in the
     * FROM clause. Empty if no sub-query exists.
     */
    public List<Pair<String, ParsedQuery>> subQueriesFrom = new ArrayList<>();

    /**
     * Contains a list of sub queries in the WHERE clause. Empty if no sub-query
     * exists.
     */
    public List<ParsedQuery> subQueriesWhere = new ArrayList<>();

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
     * @return the StringPair representing the added column
     * @throws NullPointerException if colName or table is null.
     * @throws IllegalArgumentException if colName is empty.
     */
    public StringPair addMainColumn(String table, String colName) throws IllegalArgumentException, NullPointerException {
        if (table == null || colName == null) {
            throw new NullPointerException();
        }
        if (colName.isEmpty()) {
            throw new IllegalArgumentException("Missing column name");
        }
        StringPair pair = new StringPair(table, colName);
        mainColumns.add(pair);
        return pair;
    }

    /**
     * Adds a columns used to filter the query. Essentially all the columns that
     * are in the WHERE clause.
     *
     * @param table the table name or view if explicitly specified in the query
     * or empty string.
     * @param colName the column name.
     * @throws NullPointerException if colName or table is null.
     * @throws IllegalArgumentException if colName is empty.
     */
    public void addWhereColumn(String table, String colName) throws IllegalArgumentException, NullPointerException {
        if (table == null || colName == null) {
            throw new NullPointerException();
        }
        if (colName.isEmpty()) {
            throw new IllegalArgumentException("Missing column name");
        }
        whereColumns.add(new StringPair(table, colName));
    }

    /**
     * Adds a table used in the query name.
     *
     * @param table the table name, empty if it is a sub-query.
     * @param alias the table alias or empty string if no alias is specified.
     * @throws NullPointerException if table or alias is null.
     * @throws IllegalArgumentException if both table and alias are empty.
     */
    public void addFrom(String table, String alias) throws IllegalArgumentException, NullPointerException {
        if (table == null || alias == null) {
            throw new NullPointerException();
        }
        if (table.isEmpty() && alias.isEmpty()) {
            throw new IllegalArgumentException("Missing both table and alias name");
        }
        from.add(new StringPair(table, alias));
    }

}
