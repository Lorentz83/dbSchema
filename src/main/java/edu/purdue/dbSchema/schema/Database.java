package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlParseException;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Database {

    private Namespace _defaultNS = new Namespace("default");
    private final EDbVendor _dbVendor;

    public Database(EDbVendor dbVendor) {
        if (dbVendor == null) {
            throw new NullPointerException("dbVendor");
        }
        _dbVendor = dbVendor;
    }

    public void parse(String sql) throws SqlParseException {

        TGSqlParser sqlparser = new TGSqlParser(_dbVendor);
        sqlparser.setSqltext(sql);

        if (sqlparser.parse() != 0) {
            throw new SqlParseException(sqlparser.getErrormessage());
        }

    }

}

/**
 * http://www.dpriver.com/blog/list-of-demos-illustrate-how-to-use-general-sql-parser/get-columns-and-tables-in-sql-script/
 *
 *
 *
 */
