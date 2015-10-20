package edu.purdue.dbSchema;

import edu.purdue.dbSchema.erros.SqlParseException;
import edu.purdue.dbSchema.schema.Database;
import gudusoft.gsqlparser.EDbVendor;

public class App {

    public static void main(String[] args) throws SqlParseException {

        Database db = new Database(EDbVendor.dbvmysql);
        db.parse("CREATE TABLE tbl1 (id integer primary key, name varchar(255) )");

    }
}
