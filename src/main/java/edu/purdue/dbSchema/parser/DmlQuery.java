package edu.purdue.dbSchema.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class DmlQuery {

    private final DlmQueryType _type;
    private Map<String, List<String>> _selected;
    private Map<String, List<String>> _filtered;
    private Map<String, List<String>> _changed;

    DmlQuery(DlmQueryType type) {
        _type = type;
        _selected = new TreeMap<>();
        _filtered = new TreeMap<>();
        _changed = new TreeMap<>();
    }

    public void addSelect(String table, String colName) {
        add(_selected, table, colName);
    }

    public void addFrom(String table, String alias) {
        add(_selected, table, alias);
    }

    public void addWhere() {
    }

    public DlmQueryType getType() {
        return _type;
    }

    public Map<String, List<String>> getSelected() {
        return _selected;
    }

    public Map<String, List<String>> getFiltered() {
        return _filtered;
    }

    public Map<String, List<String>> getChanged() {
        return _changed;
    }

    private static void add(Map<String, List<String>> map, String first, String second) {
        if (first == null || second == null) {
            throw new NullPointerException();
        }
        List<String> list = map.get(first);
        if (list == null) {
            list = new ArrayList<>();
            map.put(first, list);
        }
        list.add(second);
    }

}
