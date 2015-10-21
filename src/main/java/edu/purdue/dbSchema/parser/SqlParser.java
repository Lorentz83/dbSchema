package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Table;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TCustomSqlStatement;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TColumnDefinition;
import gudusoft.gsqlparser.nodes.TConstraint;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/analyzing-ddl-statement/
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class SqlParser {

    private final EDbVendor _dbVendor;
    private List<Table> _tables;

    public List<Table> getTables() {
        return _tables;
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

        int stmNum = sqlparser.sqlstatements.size();
        for (int i = 0; i < stmNum; i++) {
            analyzeStmt(sqlparser.sqlstatements.get(i));
        }

        return stmNum;
    }

    protected void analyzeStmt(TCustomSqlStatement stmt) throws UnsupportedSqlException, SqlSemanticException {
        switch (stmt.sqlstatementtype) {
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

    protected static void printConstraint(TConstraint constraint, Boolean outline) {

        if (constraint.getConstraintName() != null) {
            System.out.println("\t\tconstraint name:" + constraint.getConstraintName().toString());
        }

        switch (constraint.getConstraint_type()) {
            case notnull:
                System.out.println("\t\tnot null");
                break;
            case primary_key:
                System.out.println("\t\tprimary key");
                if (outline) {
                    String lcstr = "";
                    if (constraint.getColumnList() != null) {
                        for (int k = 0; k < constraint.getColumnList().size(); k++) {
                            if (k != 0) {
                                lcstr = lcstr + ",";
                            }
                            lcstr = lcstr + constraint.getColumnList().getObjectName(k).toString();
                        }
                        System.out.println("\t\tprimary key columns:" + lcstr);
                    }
                }
                break;
            case unique:
                System.out.println("\t\tunique key");
                if (outline) {
                    String lcstr = "";
                    if (constraint.getColumnList() != null) {
                        for (int k = 0; k < constraint.getColumnList().size(); k++) {
                            if (k != 0) {
                                lcstr = lcstr + ",";
                            }
                            lcstr = lcstr + constraint.getColumnList().getObjectName(k).toString();
                        }
                    }
                    System.out.println("\t\tcolumns:" + lcstr);
                }
                break;
            case check:
                System.out.println("\t\tcheck:" + constraint.getCheckCondition().toString());
                break;
            case foreign_key:
            case reference:
                System.out.println("\t\tforeign key");
                if (outline) {
                    String lcstr = "";
                    if (constraint.getColumnList() != null) {
                        for (int k = 0; k < constraint.getColumnList().size(); k++) {
                            if (k != 0) {
                                lcstr = lcstr + ",";
                            }
                            lcstr = lcstr + constraint.getColumnList().getObjectName(k).toString();
                        }
                    }
                    System.out.println("\t\tcolumns:" + lcstr);
                }
                System.out.println("\t\treferenced table:" + constraint.getReferencedObject().toString());
                if (constraint.getReferencedColumnList() != null) {
                    String lcstr = "";
                    for (int k = 0; k < constraint.getReferencedColumnList().size(); k++) {
                        if (k != 0) {
                            lcstr = lcstr + ",";
                        }
                        lcstr = lcstr + constraint.getReferencedColumnList().getObjectName(k).toString();
                    }
                    System.out.println("\t\treferenced columns:" + lcstr);
                }
                break;
            default:
                break;
        }
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
}
