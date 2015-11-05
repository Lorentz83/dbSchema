package edu.purdue.dbSchema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnsupportedSqlException;
import edu.purdue.dbSchema.schema.DatabaseEngine;
import gudusoft.gsqlparser.EDbVendor;

public class App {

    public static void main(String[] args) throws SqlParseException, UnsupportedSqlException, SqlSemanticException {

        DatabaseEngine db = new DatabaseEngine(EDbVendor.dbvmysql);
        int parse = db.parse("CREATE TABLE tbl1 (id integer primary key, name varchar(255) )");

    }
}
