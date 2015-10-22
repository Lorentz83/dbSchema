package edu.purdue.dbSchema.parser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lorenzo Bossi
 */
public class QueryInfo {

    class StringPair {

        public StringPair(String first, String second) {
            this.first = first;
            this.second = second;
        }
        public final String first;
        public final String second;

        @Override
        public String toString() {
            return String.format("%s.%s", first, second);
        }
    }
    private List<StringPair> select = new ArrayList<QueryInfo.StringPair>();
    private List<StringPair> from = new ArrayList<StringPair>();
    private int where = 0;

    public void addSelect(String table, String colName) {
        select.add(new StringPair(table, colName));
    }

    public void addFrom(String table, String alias) {
        from.add(new StringPair(table, alias));
    }

    public void addWhere() {
        where++;
    }
}
