package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.Table;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.EExpressionType;
import gudusoft.gsqlparser.TBaseType;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TColumnDefinition;
import gudusoft.gsqlparser.nodes.TConstraint;
import gudusoft.gsqlparser.nodes.TExpression;
import gudusoft.gsqlparser.nodes.TExpressionList;
import gudusoft.gsqlparser.nodes.TFunctionCall;
import gudusoft.gsqlparser.nodes.TJoin;
import gudusoft.gsqlparser.nodes.TJoinItem;
import gudusoft.gsqlparser.nodes.TObjectName;
import gudusoft.gsqlparser.nodes.TObjectNameList;
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.nodes.TResultColumnList;
import gudusoft.gsqlparser.nodes.TTable;
import gudusoft.gsqlparser.nodes.TWhereClause;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import gudusoft.gsqlparser.stmt.TDeleteSqlStatement;
import gudusoft.gsqlparser.stmt.TInsertSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import gudusoft.gsqlparser.stmt.TUpdateSqlStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Is an high level parser. This class wraps the real SQL parser exposing a
 * higher level APIs which parse only a subset of SQL returning a summary of the
 * features required by this project. Note: this class is not thread safe and it
 * is not meant to be, even if it can be reused to parse multiple queries, a
 * better approach is to create a new instance for every query to parse.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class SqlParser {
    /*
     From these examples.
     http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/analyzing-ddl-statement/
     http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/decoding-sql-grammar-select-statement/
     */

    private final static Logger LOGGER = Logger.getLogger(SqlParser.class.getName());
    private final EDbVendor _dbVendor;
    private List<Table> _tables;
    private List<ParsedQuery> _queries;
    private List<Grant> _grants;

    /**
     * Gets the grants parsed by the last invocation of {@link #parse(java.lang.String)
     * }.
     *
     * @return the list of grant statements parsed or an empty list.
     */
    public List<Grant> getGrants() {
        return _grants;
    }

    /**
     * Gets the create table statements parsed by the last invocation of {@link #parse(java.lang.String)
     * }.
     *
     * @return the list of create table statements parsed or an empty list.
     */
    public List<Table> getTables() {
        return _tables;
    }

    /**
     * Gets the SQL queries parsed by the last invocation of {@link #parse(java.lang.String)
     * }.
     *
     * @return the list of queries parsed or an empty list.
     */
    public List<ParsedQuery> getDmlQueries() {
        return _queries;
    }

    /**
     * Creates a new parser instance.
     *
     * @param dbVendor the SQL dialect used by this parser.
     * @throws NullPointerException if dbVendor is null.
     */
    public SqlParser(EDbVendor dbVendor) {
        if (dbVendor == null) {
            throw new NullPointerException("dbVendor");
        }
        _dbVendor = dbVendor;
    }

    /**
     * Parse a string. The results will be stored and can be accessed using {@link #getDmlQueries()}, {@link #getGrants()
     * } and {@link #getTables()}.
     *
     * @param sql the string to parse.
     * @return the number of parsed SQL statements.
     * @throws SqlParseException in case of parse error.
     * @throws UnsupportedSqlException if the SQL is not supported by this
     * abstraction level.
     * @throws SqlSemanticException if the SQL is semantically incorrect (i.e.
     * creates one table with two columns with the same name).
     * @throws NullPointerException if sql is null.
     */
    public int parse(String sql) throws SqlParseException, UnsupportedSqlException, SqlSemanticException {
        sql = sql.trim();
        _tables = new ArrayList<>();
        _queries = new ArrayList<>();
        _grants = new ArrayList<>();

        if (sql.isEmpty()) {
            return 0;
        }

        TGSqlParser sqlparser = new TGSqlParser(_dbVendor);
        sqlparser.setSqltext(sql);
        if (sqlparser.parse() != 0) {
            throw new SqlParseException("Errors: %s; Error message: %s", sqlparser.getErrorCount(), sqlparser.getErrormessage());
        }

        int stmNum = sqlparser.sqlstatements.size();
        for (int i = 0; i < stmNum; i++) {
            analyzeStmt(sqlparser.sqlstatements.get(i));
        }

        return stmNum;
    }

    protected void analyzeStmt(TCustomSqlStatement stmt) throws UnsupportedSqlException, SqlSemanticException, SqlParseException {
        ParsedQuery q;
        switch (stmt.sqlstatementtype) {
            case sstselect:
                q = analyzeSelectStmt((TSelectSqlStatement) stmt);
                _queries.add(q);
                break;
            case sstinsert:
                q = analyzeInsertStmt((TInsertSqlStatement) stmt);
                _queries.add(q);
                break;
            case sstupdate:
                q = analyzeUpdateStmt((TUpdateSqlStatement) stmt);
                _queries.add(q);
                break;
            case sstdelete:
                q = analyzeDeleteStmt((TDeleteSqlStatement) stmt);
                _queries.add(q);
                break;
            case sstcreatetable:
                Table t = analyzeCreateTableStmt((TCreateTableSqlStatement) stmt);
                _tables.add(t);
                break;
            case sstGrant:
            case sstoraclegrant:
            case sstpostgresqlGrant:
                Grant grant = analyzeGrantStmt(stmt);
                _grants.add(grant);
                break;
            default:
                throw new UnsupportedSqlException(stmt.sqlstatementtype.toString());
        }
    }

    protected Table analyzeCreateTableStmt(TCreateTableSqlStatement pStmt) throws SqlSemanticException {
        Table tbl = new Table(pStmt.getTargetTable().toString());
        TColumnDefinition column;
        for (int i = 0; i < pStmt.getColumnList().size(); i++) {
            column = pStmt.getColumnList().getColumn(i);

            String colName = column.getColumnName().toString();
            String colType = column.getDatatype().toString();
            // column.getDefaultExpression().toString()
            boolean isNotNull = false;
            boolean isUnique = false;
            if (column.getConstraints() != null) {
                for (int j = 0; j < column.getConstraints().size(); j++) {
                    TConstraint constraint = column.getConstraints().getConstraint(j);

                    switch (constraint.getConstraint_type()) {
                        case notnull:
                            isNotNull = true;
                            break;
                        case primary_key:
                            isNotNull = true;
                            isUnique = true;
                            break;
                        case unique:
                            isUnique = true;
                            break;
                    }
                }
            }
            tbl.addColumn(colName, colType, isNotNull, isUnique);
        }
        return tbl;
    }

    /**
     * Analyzes the SELECT clause and adds the columns into the ParsedQuery.
     * Whenever a sub-query is encountered, it is recursively parsed.
     *
     * @param expression the expression in the SELECT clause.
     * @param query the ParsedQuery to be constructed.
     * @throws UnsupportedSqlException in case of error in the sub-query.
     */
    private void addColumnsName(TExpression expression, ParsedQuery query) throws UnsupportedSqlException {
        if (expression == null) {
            return;
        }
        TObjectName complexName = expression.getObjectOperand();
        if (complexName != null) { // this is a col name i.e. schema.tbl.col as alias
            // we currently ignore the schema
            String colName = complexName.getColumnNameOnly();
            String tblName = complexName.getTableString();
            query.addMainColumn(tblName, colName);
        } else { // if it is not a col name, go recursively
            TFunctionCall functionCall = expression.getFunctionCall();
            if (functionCall != null) { //this is a function call i.e. sum(tbl)
                switch (functionCall.getFunctionType()) {
                    case cast_t:
                    case extract_t:
                        addColumnsName(functionCall.getExpr1(), query);
                        break;
                    default:
                        TExpressionList args = functionCall.getArgs();
                        for (int j = 0; j < args.size(); j++) {
                            addColumnsName(args.getExpression(j), query);
                        }
                }
            }
            // in case of unary or binay expressions i.e. -col or col1 + col2
            addColumnsName(expression.getLeftOperand(), query);
            addColumnsName(expression.getRightOperand(), query);

            TSelectSqlStatement subQuery = expression.getSubQuery();
            if (subQuery != null) {
                query.subQueries.add(analyzeSelectStmt(subQuery));
            }
        }
    }

    protected ParsedQuery analyzeSelectStmt(TSelectSqlStatement pStmt) throws UnsupportedSqlException {
        LOGGER.log(Level.INFO, "select");
        if (pStmt.isCombinedQuery()) {
            // pStmt.getSetOperator() //to know type of combined query (i.e. union, intersect...)
            // here the problem is that, considering
            // select f from tbl1 UNION select f from tbl2
            // the two queries refer to two different fields "f", therefore I cannot return just one ParsedQuery
            ParsedQuery q = analyzeSelectStmt(pStmt.getLeftStmt());
            ParsedQuery last = q;
            // the parser splits the queries like ( (Q1 UNION Q2) UNION Q3 )
            while (last.nextCombinedQuery != null) {
                last = last.nextCombinedQuery;
            }
            last.nextCombinedQuery = analyzeSelectStmt(pStmt.getRightStmt());
            return q;
        } else {
            ParsedQuery query = new ParsedQuery(DlmQueryType.SELECT);
            //select list
            for (int i = 0; i < pStmt.getResultColumnList().size(); i++) {
                TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
                addColumnsName(resultColumn.getExpr(), query);
            }

            for (int i = 0; i < pStmt.joins.size(); i++) {
                TJoin join = pStmt.joins.getJoin(i);
                addTable(join.getTable(), query);
                switch (join.getKind()) {
                    case TBaseType.join_source_fake:
                        // nothing to do, this is the first table in the list
                        break;
                    case TBaseType.join_source_table:
                        for (int j = 0; j < join.getJoinItems().size(); j++) {
                            TJoinItem joinItem = join.getJoinItems().getJoinItem(j);
                            addTable(joinItem.getTable(), query);

                            if (joinItem.getOnCondition() != null) {
                                evaluateWhereConditions(joinItem.getOnCondition(), query);
                            } else if (joinItem.getUsingColumns() != null) {
                                TObjectNameList colList = joinItem.getUsingColumns();
                                for (int n = 0; n < colList.size(); n++) {
                                    TObjectName colName = colList.getObjectName(n);
                                    query.addWhereColumn(colName.getTableString(), colName.getColumnNameOnly());
                                }
                            }
                        }
                        break;
                    case TBaseType.join_source_join:
                        //select from another query
                        throw new UnsupportedSqlException("unsuppported neasted query");
                    default:
                        throw new UnsupportedSqlException("unknown type in join!");
                }
            }
            parseWhere(query, pStmt.getWhereClause());

            // group by pStmt.getGroupByClause()
            // order by pStmt.getOrderbyClause()
            // for update
            if (pStmt.getForUpdateClause() != null) {
                throw new UnsupportedSqlException("for update: " + pStmt.getForUpdateClause().toString());
            }

            // top clause
            if (pStmt.getTopClause() != null) {
                throw new UnsupportedSqlException("top clause: " + pStmt.getTopClause().toString());
            }

            // limit clause
            if (pStmt.getLimitClause() != null) {
                throw new UnsupportedSqlException("limit clause: " + pStmt.getLimitClause().toString());
            }
            return query;
        }
    }

    /**
     * Parses the portion of the FROM clause to add the table to the
     * ParsedQuery. Considering that the form clause can contain sub queries,
     * this method recursively call the parse whenever it finds any of them.
     *
     * @param table the "virtual" table.
     * @param query the ParsedQuery that is going to be constructed.
     * @throws NullPointerException
     * @throws UnsupportedSqlException
     * @throws IllegalArgumentException
     */
    private void addTable(TTable table, ParsedQuery query) throws NullPointerException, UnsupportedSqlException, IllegalArgumentException {
        String tableAlias = table.getAliasName();
        String tableName;
        switch (table.getTableType()) {
            case objectname:
                tableName = table.getName();
                break;
            case subquery:
                tableName = "";
                query.subQueries.add(analyzeSelectStmt(table.getSubquery()));
                break;
            default:
                throw new UnsupportedSqlException("unsuppported table '%s' in from clause", table.toString());
        }
        query.addFrom(tableName, tableAlias);
    }

    private void parseWhere(ParsedQuery query, TWhereClause where) throws UnsupportedSqlException {
        if (where != null) {
            TExpression conds = where.getCondition();
            evaluateWhereConditions(conds, query);
        }
    }

    private void evaluateWhereConditions(TExpression conds, ParsedQuery query) throws UnsupportedSqlException {
        EExpressionType expressionType = conds.getExpressionType();
        switch (expressionType) {
            case subquery_t:
                ParsedQuery sub = analyzeSelectStmt(conds.getSubQuery());
                query.subQueries.add(sub);
                break;
            case logical_not_t:
            case parenthesis_t:
                evaluateWhereConditions(conds.getLeftOperand(), query);
                break;
            case logical_and_t:
            case logical_or_t:
            case logical_xor_t:
            case arithmetic_minus_t:
            case arithmetic_plus_t:
            case arithmetic_modulo_t:
            case arithmetic_divide_t:
            case arithmetic_times_t:
            case simple_comparison_t: // f_id [ = > < ... ] '340867051503681675'
            case in_t: // f_arrive_ap_id in ('211')
                evaluateWhereConditions(conds.getLeftOperand(), query);
                evaluateWhereConditions(conds.getRightOperand(), query);
                break;
            case simple_object_name_t:
                TObjectName colName = conds.getObjectOperand();
                query.addWhereColumn(colName.getTableString(), colName.getColumnNameOnly());
                break;
            case simple_constant_t: // nothing to do, it is a constant
            case list_t: //nothing to do, it is a list
                break;
            default:
                LOGGER.log(Level.WARNING, "Unknown expression type: {0}, found in: {1}", new Object[]{expressionType.toString(), conds.toString()});
        }
    }

    private final Pattern _grantRegex = Pattern.compile("GRANT +(?<what>\\w+)( +ON +(?<where>[\\w.]+))? +TO +(?<to>\\w+) *;?", Pattern.CASE_INSENSITIVE);

    private Grant analyzeGrantStmt(TCustomSqlStatement stmt) throws UnsupportedSqlException, SqlParseException {
        String grantStr = stmt.toString();
        // GRANT what ON col TO role
        // GRANT role TO role
        Matcher matcher = _grantRegex.matcher(grantStr);
        if (!matcher.matches()) {
            throw new UnsupportedSqlException("Cannot parse grant statement" + grantStr);
        }
        String what = matcher.group("what");
        String to = matcher.group("to");
        String where = matcher.group("where");
        if (where != null) {
            String[] w = where.split("\\.");
            String table = w[0];
            String col = "";
            if (w.length > 1) {
                col = w[1];
            }
            if (w.length > 2) {
                throw new SqlParseException("In the grant '%s', the object '%s' is not in the form of 'table.coloumn'", grantStr, where);
            }
            Grant.Type type = null;
            switch (what.toUpperCase()) {
                case "SELECT":
                    type = Grant.Type.READ;
                    break;
                case "INSERT":
                case "UPDATE":
                case "DELETE":
                case "REFERENCES":
                    type = Grant.Type.WRITE;
                    break;
                default:
                    throw new SqlParseException("Cannot recognize the privilege '%s' in the grant statement '%s'", what, grantStr);

            }
            return new Grant(type, to, table, col);
        }
        return new Grant(what, to);
    }

    private ParsedQuery analyzeInsertStmt(TInsertSqlStatement stmt) {
        ParsedQuery ret = new ParsedQuery(DlmQueryType.INSERT);
        ret.addFrom(stmt.getTargetTable().getName(), "");
        TObjectNameList cols = stmt.getColumnList();
        for (int n = 0; n < cols.size(); n++) {
            String col = cols.getObjectName(n).getColumnNameOnly();
            ret.addMainColumn("", col);
        }
        return ret;
    }

    private ParsedQuery analyzeDeleteStmt(TDeleteSqlStatement stmt) throws UnsupportedSqlException {
        ParsedQuery ret = new ParsedQuery(DlmQueryType.DELETE);
        ret.addFrom(stmt.getTargetTable().getName(), "");
        parseWhere(ret, stmt.getWhereClause());
        return ret;
    }

    private ParsedQuery analyzeUpdateStmt(TUpdateSqlStatement stmt) throws UnsupportedSqlException {
        ParsedQuery ret = new ParsedQuery(DlmQueryType.UPDATE);
        ret.addFrom(stmt.getTargetTable().getName(), "");
        TResultColumnList cols = stmt.getResultColumnList();
        for (int i = 0; i < cols.size(); i++) {
            ret.addMainColumn("", cols.getResultColumn(i).getExpr().getLeftOperand().toString());
        }
        parseWhere(ret, stmt.getWhereClause());
        return ret;
    }

}
