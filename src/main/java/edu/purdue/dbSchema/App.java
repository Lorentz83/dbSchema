package edu.purdue.dbSchema;

import edu.purdue.dbSchema.schema.Database;

public class App {

    public static void main(String[] args) {

        Database db = new Database();
        db.parse("CREATE TABLE tbl1 (id integer primary key, name varchar(255) )");

    }
}
