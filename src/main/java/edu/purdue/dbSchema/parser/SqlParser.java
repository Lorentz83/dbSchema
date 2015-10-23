package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.Column;
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
import gudusoft.gsqlparser.nodes.TResultColumn;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import gudusoft.gsqlparser.stmt.TSelectSqlStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/analyzing-ddl-statement/
 * http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/decoding-sql-grammar-select-statement/
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class SqlParser {

    private final static Logger LOGGER = Logger.getLogger(SqlParser.class.getName());
    private final EDbVendor _dbVendor;
    private List<Table> _tables;
    private List<ParsedQuery> _queries;

    public List<Table> getTables() {
        return _tables;
    }

    public List<ParsedQuery> getDmlQueries() {
        return _queries;
    }

    public SqlParser(EDbVendor dbVendor) {
        if (dbVendor == null) {
            throw new NullPointerException("dbVendor");
        }
        _dbVendor = dbVendor;
    }

    public int parse(String sql) throws SqlParseException, UnsupportedSqlException, SqlSemanticException {
        TGSqlParser sqlparser = new TGSqlParser(_dbVendor);
        sqlparser.setSqltext(sql);
        if (sqlparser.parse() != 0) {
            throw new SqlParseException(sqlparser.getErrormessage());
        }

        _tables = new ArrayList<Table>();
        _queries = new ArrayList<ParsedQuery>();

        int stmNum = sqlparser.sqlstatements.size();
        for (int i = 0; i < stmNum; i++) {
            analyzeStmt(sqlparser.sqlstatements.get(i));
        }

        return stmNum;
    }

    protected void analyzeStmt(TCustomSqlStatement stmt) throws UnsupportedSqlException, SqlSemanticException {
        switch (stmt.sqlstatementtype) {
            case sstselect:
                ParsedQuery q = analyzeSelectStmt((TSelectSqlStatement) stmt);
                _queries.add(q);
                break;
//            case sstupdate:
//                analyzeUpdateStmt((TUpdateSqlStatement) stmt);
//                break;
            case sstcreatetable:
                Table t = analyzeCreateTableStmt((TCreateTableSqlStatement) stmt);
                _tables.add(t);
                break;
//            case sstaltertable:
//                analyzeAlterTableStmt((TAlterTableStatement) stmt);
//                break;
//            case sstcreateview:
//                analyzeCreateViewStmt((TCreateViewSqlStatement) stmt);
//                break;
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
            tbl.addColumn(new Column(colName, colType, isNotNull, isUnique));
        }
        return tbl;
    }

    private void addWhereExpression(TExpression expression, ParsedQuery query) {
        if (expression == null) {
            return;
        }
        TObjectName complexName = expression.getObjectOperand();
        if (complexName != null) { // this is a col name i.e. schema.tbl.col as alias
            // we currently ignore the schema
            String colName = complexName.getColumnNameOnly();
            String tblName = complexName.getTableString();
            query.addSelect(tblName, colName);
        } else { // if it is not a col name, go recursively
            TFunctionCall functionCall = expression.getFunctionCall();
            if (functionCall != null) { //this is a function call i.e. sum(tbl)
                TExpressionList args = functionCall.getArgs();
                for (int j = 0; j < args.size(); j++) {
                    addWhereExpression(args.getExpression(j), query);
                }
            }
            // in case of unary or binay expressions i.e. -col or col1 + col2
            addWhereExpression(expression.getLeftOperand(), query);
            addWhereExpression(expression.getRightOperand(), query);
        }
    }

    protected ParsedQuery analyzeSelectStmt(TSelectSqlStatement pStmt) throws UnsupportedSqlException {
        LOGGER.log(Level.INFO, "select");
        ParsedQuery query = new ParsedQuery(DlmQueryType.SELECT);
        if (pStmt.isCombinedQuery()) {
            throw new UnsupportedSqlException("cobined queries are not supported yet");
//            // pStmt.getSetOperator() to know type of combined query (i.e. union, intersect...)
//            analyzeSelectStmt(pStmt.getLeftStmt());
//            analyzeSelectStmt(pStmt.getRightStmt());
//            if (pStmt.getOrderbyClause() != null) {
//                System.out.printf("order by clause %s\n", pStmt.getOrderbyClause().toString());
//            }
        } else {
            //select list
            for (int i = 0; i < pStmt.getResultColumnList().size(); i++) {
                TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
                addWhereExpression(resultColumn.getExpr(), query);
            }

            //from clause, check this document for detailed information
            //http://www.sqlparser.com/sql-parser-query-join-table.php
            for (int i = 0; i < pStmt.joins.size(); i++) {
                TJoin join = pStmt.joins.getJoin(i);
                String tableAlias;
                String tableName;
                switch (join.getKind()) {
                    case TBaseType.join_source_fake:
                        tableAlias = (join.getTable().getAliasClause() != null) ? join.getTable().getAliasClause().toString() : "";
                        tableName = join.getTable().toString();
                        query.addFrom(tableName, tableAlias);
                        System.out.printf("table: %s, alias: %s\n", tableName, tableAlias);
                        break;
                    case TBaseType.join_source_table:
                        tableAlias = (join.getTable().getAliasClause() != null) ? join.getTable().getAliasClause().toString() : "";
                        tableName = join.getTable().toString();
                        query.addFrom(tableName, tableAlias);
                        System.out.printf("table: %s, alias: %s\n", join.getTable().toString(), (join.getTable().getAliasClause() != null) ? join.getTable().getAliasClause().toString() : "");
                        for (int j = 0; j < join.getJoinItems().size(); j++) {
                            TJoinItem joinItem = join.getJoinItems().getJoinItem(j);
                            System.out.printf("Join type: %s\n", joinItem.getJoinType().toString());
                            tableAlias = (joinItem.getTable().getAliasClause() != null) ? joinItem.getTable().getAliasClause().toString() : "";
                            tableName = joinItem.getTable().toString();
                            query.addFrom(tableName, tableAlias);
                            System.out.printf("table: %s, alias: %s\n", tableName, tableAlias);

                            if (joinItem.getOnCondition() != null) {
                                System.out.printf("On: %s\n", joinItem.getOnCondition().toString());
                                query.addWhere();
                            } else if (joinItem.getUsingColumns() != null) {
                                System.out.printf("using: %s\n", joinItem.getUsingColumns().toString());
                                query.addWhere();
                            }
                        }
                        break;
                    case TBaseType.join_source_join:
                        //select from another query
                        throw new UnsupportedSqlException("unsuppported neasted query");
//                        TJoin source_join = join.getJoin();
//                        System.out.printf("table: %s, alias: %s\n", source_join.getTable().toString(), (source_join.getTable().getAliasClause() != null) ? source_join.getTable().getAliasClause().toString() : "");
//
//                        for (int j = 0; j < source_join.getJoinItems().size(); j++) {
//                            TJoinItem joinItem = source_join.getJoinItems().getJoinItem(j);
//                            System.out.printf("source_join type: %s\n", joinItem.getJoinType().toString());
//                            System.out.printf("table: %s, alias: %s\n", joinItem.getTable().toString(), (joinItem.getTable().getAliasClause() != null) ? joinItem.getTable().getAliasClause().toString() : "");
//                            if (joinItem.getOnCondition() != null) {
//                                System.out.printf("On: %s\n", joinItem.getOnCondition().toString());
//                            } else if (joinItem.getUsingColumns() != null) {
//                                System.out.printf("using: %s\n", joinItem.getUsingColumns().toString());
//                            }
//                        }
//
//                        for (int j = 0; j < join.getJoinItems().size(); j++) {
//                            TJoinItem joinItem = join.getJoinItems().getJoinItem(j);
//                            System.out.printf("Join type: %s\n", joinItem.getJoinType().toString());
//                            System.out.printf("table: %s, alias: %s\n", joinItem.getTable().toString(), (joinItem.getTable().getAliasClause() != null) ? joinItem.getTable().getAliasClause().toString() : "");
//                            if (joinItem.getOnCondition() != null) {
//                                System.out.printf("On: %s\n", joinItem.getOnCondition().toString());
//                            } else if (joinItem.getUsingColumns() != null) {
//                                System.out.printf("using: %s\n", joinItem.getUsingColumns().toString());
//                            }
//                        }
//
//                        break;
                    default:
                        throw new UnsupportedSqlException("unknown type in join!");
                }
            }

            //where clause
            if (pStmt.getWhereClause() != null) {
                TExpression conds = pStmt.getWhereClause().getCondition();
                countWhereConditions(conds, query);
                System.out.printf("where clause: \n%s\n", pStmt.getWhereClause().toString());
            }

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
        }
        return query;
    }

    /*



     protected static void printObjectNameList(TObjectNameList objList) {
     for (int i = 0; i < objList.size(); i++) {
     System.out.println(objList.getObjectName(i).toString());
     }

     }

     protected static void printColumnDefinitionList(TColumnDefinitionList cdl) {
     for (int i = 0; i < cdl.size(); i++) {
     System.out.println(cdl.getColumn(i).getColumnName());
     }
     }

     protected static void printConstraintList(TConstraintList cnl) {
     for (int i = 0; i < cnl.size(); i++) {
     printConstraint(cnl.getConstraint(i), true);
     }
     }

     protected static void printAlterTableOption(TAlterTableOption ato) {
     System.out.println(ato.getOptionType());
     switch (ato.getOptionType()) {
     case AddColumn:
     printColumnDefinitionList(ato.getColumnDefinitionList());
     break;
     case ModifyColumn:
     printColumnDefinitionList(ato.getColumnDefinitionList());
     break;
     case AlterColumn:
     System.out.println(ato.getColumnName().toString());
     break;
     case DropColumn:
     System.out.println(ato.getColumnName().toString());
     break;
     case SetUnUsedColumn:  //oracle
     printObjectNameList(ato.getColumnNameList());
     break;
     case DropUnUsedColumn:
     break;
     case DropColumnsContinue:
     break;
     case RenameColumn:
     System.out.println("rename " + ato.getColumnName().toString() + " to " + ato.getNewColumnName().toString());
     break;
     case ChangeColumn:   //MySQL
     System.out.println(ato.getColumnName().toString());
     printColumnDefinitionList(ato.getColumnDefinitionList());
     break;
     case RenameTable:   //MySQL
     System.out.println(ato.getColumnName().toString());
     break;
     case AddConstraint:
     printConstraintList(ato.getConstraintList());
     break;
     case AddConstraintIndex:    //MySQL
     if (ato.getColumnName() != null) {
     System.out.println(ato.getColumnName().toString());
     }
     printObjectNameList(ato.getColumnNameList());
     break;
     case AddConstraintPK:
     case AddConstraintUnique:
     case AddConstraintFK:
     if (ato.getConstraintName() != null) {
     System.out.println(ato.getConstraintName().toString());
     }
     printObjectNameList(ato.getColumnNameList());
     break;
     case ModifyConstraint:
     System.out.println(ato.getConstraintName().toString());
     break;
     case RenameConstraint:
     System.out.println("rename " + ato.getConstraintName().toString() + " to " + ato.getNewConstraintName().toString());
     break;
     case DropConstraint:
     System.out.println(ato.getConstraintName().toString());
     break;
     case DropConstraintPK:
     break;
     case DropConstraintFK:
     System.out.println(ato.getConstraintName().toString());
     break;
     case DropConstraintUnique:
     if (ato.getConstraintName() != null) { //db2
     System.out.println(ato.getConstraintName());
     }

     if (ato.getColumnNameList() != null) {//oracle
     printObjectNameList(ato.getColumnNameList());
     }
     break;
     case DropConstraintCheck: //db2
     System.out.println(ato.getConstraintName());
     break;
     case DropConstraintPartitioningKey:
     break;
     case DropConstraintRestrict:
     break;
     case DropConstraintIndex:
     System.out.println(ato.getConstraintName());
     break;
     case DropConstraintKey:
     System.out.println(ato.getConstraintName());
     break;
     case AlterConstraintFK:
     System.out.println(ato.getConstraintName());
     break;
     case AlterConstraintCheck:
     System.out.println(ato.getConstraintName());
     break;
     case CheckConstraint:
     break;
     case OraclePhysicalAttrs:
     case toOracleLogClause:
     case OracleTableP:
     case MssqlEnableTrigger:
     case MySQLTableOptons:
     case Db2PartitioningKeyDef:
     case Db2RestrictOnDrop:
     case Db2Misc:
     case Unknown:
     break;
     }

     }

     protected static void analyzeCreateViewStmt(TCreateViewSqlStatement pStmt) {
     TCreateViewSqlStatement createView = pStmt;
     System.out.println("View name:" + createView.getViewName().toString());
     TViewAliasClause aliasClause = createView.getViewAliasClause();
     for (int i = 0; i < aliasClause.getViewAliasItemList().size(); i++) {
     System.out.println("View alias:" + aliasClause.getViewAliasItemList().getViewAliasItem(i).toString());
     }

     System.out.println("View subquery: \n" + createView.getSubquery().toString());
     }

     protected static void analyzeUpdateStmt(TUpdateSqlStatement pStmt) {
     System.out.println("Table Name:" + pStmt.getTargetTable().toString());
     System.out.println("set clause:");
     for (int i = 0; i < pStmt.getResultColumnList().size(); i++) {
     TResultColumn resultColumn = pStmt.getResultColumnList().getResultColumn(i);
     TExpression expression = resultColumn.getExpr();
     System.out.println("\tcolumn:" + expression.getLeftOperand().toString() + "\tvalue:" + expression.getRightOperand().toString());
     }
     if (pStmt.getWhereClause() != null) {
     System.out.println("where clause:\n" + pStmt.getWhereClause().getCondition().toString());
     }
     }

     protected static void analyzeAlterTableStmt(TAlterTableStatement pStmt) {
     System.out.println("Table Name:" + pStmt.getTableName().toString());
     System.out.println("Alter table options:");
     for (int i = 0; i < pStmt.getAlterTableOptionList().size(); i++) {
     printAlterTableOption(pStmt.getAlterTableOptionList().getAlterTableOption(i));
     }
     }

     */
    private void countWhereConditions(TExpression conds, ParsedQuery query) throws UnsupportedSqlException {
        EExpressionType expressionType = conds.getExpressionType();
        switch (expressionType) {
            case subquery_t:
                throw new UnsupportedSqlException("unsupported neasted query " + conds.toString());
            case logical_not_t:
            case parenthesis_t:
                countWhereConditions(conds.getLeftOperand(), query);
                break;
            case logical_and_t:
            case logical_or_t:
            case logical_xor_t:
                countWhereConditions(conds.getLeftOperand(), query);
                countWhereConditions(conds.getRightOperand(), query);
                break;
            default:
                LOGGER.log(Level.WARNING, "Unknown condition {0}", expressionType.toString());
                query.addWhere();
        }
    }
}
