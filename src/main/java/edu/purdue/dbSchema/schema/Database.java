package edu.purdue.dbSchema.schema;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Database {

    Namespace defaultNS = new Namespace("default");

    public void parse(String sql) {

        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvoracle);
        sqlparser.setSqltext(sql);

        int ret = sqlparser.parse();
        if (ret == 0) {
            System.out.println("Check syntax ok!");
        } else {
            System.out.println(sqlparser.getErrormessage());
        }

    }

}
