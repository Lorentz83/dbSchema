package edu.purdue.dbSchema.parser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lorenzo Bossi
 */
public class ParsedQuery {

    public final DlmQueryType type;
    public List<StringPair> select = new ArrayList<StringPair>();
    public List<StringPair> from = new ArrayList<StringPair>();
    public int where = 0;

    public ParsedQuery(DlmQueryType type) {
        this.type = type;
    }

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
