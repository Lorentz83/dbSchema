package edu.purdue.dbSchema.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a virtual column. Something that is handled like a column but is
 * created on fly aggregating other column values or function.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class VirtualColumn extends AbstractColumn {

    Set<AbstractColumn> _mappedTo;

    public VirtualColumn(Table table, Name name) {
        super(table, name);
    }

    VirtualColumn(Table table, Name name, Collection<AbstractColumn> mappedTo) {
        super(table, name);
        if (mappedTo == null) {
            throw new NullPointerException("mappedTo");
        }
        Set<AbstractColumn> _mappedTo = Collections.unmodifiableSet(new HashSet<>(mappedTo));
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

}
