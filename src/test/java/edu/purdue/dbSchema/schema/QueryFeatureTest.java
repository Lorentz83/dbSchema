package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.parser.DlmQueryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class QueryFeatureTest {

    @Test
    public void testImmutableCollections() {
        List<Column> used = new ArrayList<>();
        List<Column> where = new ArrayList<>();
        Set<Name> roles = new TreeSet<>();

        QueryFeature feature = new QueryFeature(DlmQueryType.INSERT, used, where, roles);

        try {
            feature.getUsedCols().add(null);
            fail("missing exception");
        } catch (UnsupportedOperationException ex) {
        }
        try {
            feature.getFilteredCols().add(null);
            fail("missing exception");
        } catch (UnsupportedOperationException ex) {
        }
        try {
            feature.getRoles().add(null);
            fail("missing exception");
        } catch (UnsupportedOperationException ex) {
        }
    }

    @Test
    public void merge_noDuplicates() {
        DlmQueryType type = DlmQueryType.INSERT;

        Column col1 = new Column(new Name("col1"), "type", true, true, null);
        Column col2 = new Column(new Name("col2"), "type", true, true, null);
        Column col3 = new Column(new Name("col3"), "type", true, true, null);
        Name user1 = new Name("username1");
        Name user2 = new Name("username2");

        ArrayList<QueryFeature> features = new ArrayList<>();

        List<Column> used1 = new ArrayList<>();
        List<Column> where1 = new ArrayList<>();
        Set<Name> roles1 = new TreeSet<>();

        used1.add(col1);
        used1.add(col2);
        roles1.add(user1);

        features.add(new QueryFeature(type, used1, where1, roles1));

        List<Column> used2 = new ArrayList<>();
        List<Column> where2 = new ArrayList<>();
        Set<Name> roles2 = new TreeSet<>();

        used2.add(col2);
        used2.add(col3);
        where2.add(col3);

        roles2.add(user2);

        features.add(new QueryFeature(type, used2, where2, roles2));

        QueryFeature merged = new QueryFeature(features);

        assertThat(merged.getType(), is(type));
        assertThat(merged.getUsedCols(), containsInAnyOrder(col1, col2, col3));
        assertThat(merged.getFilteredCols(), containsInAnyOrder(col3));
        assertThat(merged.getRoles(), containsInAnyOrder(user1, user2));
    }
}
