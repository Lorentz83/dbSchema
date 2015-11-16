package edu.purdue.dbSchema;

import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.QueryFeature;
import edu.purdue.dbSchema.schema.Table;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class FeatureFormatter {

    private final PrintStream _out;
    private final List<Column> _columns;
    private final String _separator = ", ";

    FeatureFormatter(Collection<Table> tables, PrintStream out) {
        _out = out;
        _columns = new ArrayList<>();
        for (Table t : tables) {
            for (Column c : t.getColumns()) {
                _columns.add(c);
            }
        }
    }

    void header() {
        _out.print("type" + _separator);
        for (Column c : _columns) {
            _out.print(c.getTable().getName().normalize() + "." + c.getName().normalize() + _separator);
        }
        _out.println();
    }

    void format(QueryFeature feature) {
        _out.print(feature.getType().toString().charAt(0) + _separator);
        for (Column c : _columns) {
            HashSet<Column> usedCols = new HashSet<>();
            usedCols.addAll(feature.getUsedCols());
            usedCols.addAll(feature.getFilteredCols());

            String val = usedCols.contains(c) ? "1" : "0";
            _out.print(val + _separator);
        }
        _out.println();
    }
}
