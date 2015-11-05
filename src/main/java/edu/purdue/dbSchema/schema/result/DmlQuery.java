package edu.purdue.dbSchema.schema.result;

import edu.purdue.dbSchema.parser.DlmQueryType;
import edu.purdue.dbSchema.schema.Column;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DmlQuery {

    private final DlmQueryType _type;
    private final List<Column> _selected;
    private final List<Column> _filtered;
    private final List<Column> _changed;

    DmlQuery(DlmQueryType type, List<Column> selected, List<Column> filtered, List<Column> changed) {
        _type = type;
        _selected = Collections.unmodifiableList(selected);
        _filtered = Collections.unmodifiableList(filtered);
        _changed = Collections.unmodifiableList(changed);
    }

    public DlmQueryType getType() {
        return _type;
    }

    public List<Column> getSelected() {
        return _selected;
    }

    public List<Column> getFiltered() {
        return _filtered;
    }

    public List<Column> getChanged() {
        return _changed;
    }

}
