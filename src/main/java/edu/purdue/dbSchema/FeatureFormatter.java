package edu.purdue.dbSchema;

import edu.purdue.dbSchema.schema.AbstractColumn;
import edu.purdue.dbSchema.schema.QueryFeature;
import edu.purdue.dbSchema.schema.Table;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class FeatureFormatter {

    private final PrintStream _out;
    private final List<AbstractColumn> _columns;
    private final String _separator = ", ";

    FeatureFormatter(Collection<Table> tables, PrintStream out) {
        _out = out;
        _columns = new ArrayList<>();
        for (Table t : tables) {
            for (AbstractColumn c : t.getColumns()) {
                _columns.add(c);
            }
        }
    }

    void header() {
        _out.print("role" + _separator);
        _out.print("type" + _separator);
        for (int n = 0; n < _columns.size(); n++) {
            AbstractColumn c = _columns.get(n);
            _out.print(c.getTable().getName().getName() + "." + c.getName().getName());
            if (n < _columns.size() - 1) {
                _out.print(_separator);
            }
        }
        _out.println();
    }

    void format(QueryFeature feature, String role) {
        _out.print(role);
        _out.print(_separator);
        _out.print(feature.getType().toString().charAt(0) + _separator);
        for (int n = 0; n < _columns.size(); n++) {
            AbstractColumn c = _columns.get(n);
            HashSet<AbstractColumn> usedCols = new HashSet<>();
            usedCols.addAll(feature.getUsedCols());
            usedCols.addAll(feature.getFilteredCols());

            String val = usedCols.contains(c) ? "1" : "0";
            _out.print(val);
            if (n < _columns.size() - 1) {
                _out.print(_separator);
            }
        }
        _out.println();
    }

}
