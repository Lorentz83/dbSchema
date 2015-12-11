package edu.purdue.dbSchema.schema.result;

import edu.purdue.dbSchema.parser.DlmQueryType;
import edu.purdue.dbSchema.schema.RealColumn;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DmlQuery {

    private final DlmQueryType _type;
    private final List<RealColumn> _selected;
    private final List<RealColumn> _filtered;
    private final List<RealColumn> _changed;

    DmlQuery(DlmQueryType type, List<RealColumn> selected, List<RealColumn> filtered, List<RealColumn> changed) {
        _type = type;
        _selected = Collections.unmodifiableList(selected);
        _filtered = Collections.unmodifiableList(filtered);
        _changed = Collections.unmodifiableList(changed);
    }

    public DlmQueryType getType() {
        return _type;
    }

    public List<RealColumn> getSelected() {
        return _selected;
    }

    public List<RealColumn> getFiltered() {
        return _filtered;
    }

    public List<RealColumn> getChanged() {
        return _changed;
    }

}
