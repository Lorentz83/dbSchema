package edu.purdue.dbSchema.testUtil;

import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Table;
import java.util.Collection;
import java.util.Iterator;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Checks if two objects are equal. Equals on db entities checks the reference
 * equality, this class contains helper methods to check if all the fields are
 * equals.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class SoftEqual {

    public static void table(Table actualTable, Table expectedTable) {
        assertThat(actualTable.getName(), is(expectedTable.getName()));

        columns(actualTable.getColumns(), expectedTable.getColumns());
    }

    public static void column(Column actualCol, Column expectedCol) {
        assertThat(actualCol.getName(), is(expectedCol.getName()));
        assertThat(actualCol.getType(), is(expectedCol.getType()));
        assertThat(actualCol.isNotNull(), is(expectedCol.isNotNull()));
        assertThat(actualCol.isUnique(), is(expectedCol.isUnique()));
    }

    public static void columns(final Collection<Column> actualCols, final Collection<Column> expectedCols) {
        assertThat(actualCols, hasSize(expectedCols.size()));
        Iterator<Column> actualIt = actualCols.iterator();
        Iterator<Column> expectedIt = expectedCols.iterator();

        while (expectedIt.hasNext() & actualIt.hasNext()) {
            column(actualIt.next(), expectedIt.next());
        }
    }

    public static void tables(final Collection<Table> actualTables, final Collection<Table> expectedTables) {
        assertThat(actualTables, hasSize(expectedTables.size()));
        Iterator<Table> actualIt = actualTables.iterator();
        Iterator<Table> expectedIt = expectedTables.iterator();

        while (expectedIt.hasNext() & actualIt.hasNext()) {
            table(actualIt.next(), expectedIt.next());
        }
    }
}
