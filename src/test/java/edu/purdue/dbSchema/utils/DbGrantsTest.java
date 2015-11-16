package edu.purdue.dbSchema.utils;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnauthorizedSqlException;
import edu.purdue.dbSchema.schema.Column;
import edu.purdue.dbSchema.schema.Name;
import edu.purdue.dbSchema.schema.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DbGrantsTest {

    DbGrants _grants = new DbGrants();
    Table _table1, _table2;
    private Column _col1;
    private Column _col2;

    @Before
    public void init() throws Exception {
        _grants.grantRole(new Name("roleA"), new Name("user1"));
        _grants.grantRole(new Name("roleB"), new Name("user2"));
        _grants.grantRole(new Name("roleC"), new Name("roleA"));

        _table1 = new Table("tbl1");
        _table1.addColumn("id", "type", true, true);
        _col1 = _table1.getColumn("id");

        _table2 = new Table("tbl2");
        _table2.addColumn("id", "type", true, true);
        _col2 = _table2.getColumn("id");

        _grants.grantRead(_col1, new Name("roleA"));
        _grants.grantWrite(_col2, new Name("roleB"));
    }

    @Test
    public void enforceWrite() throws Exception {
        Set<Name> roles = _grants.enforceWrite(new Name("user2"), collection(_col2));
        assertThat(roles, containsInAnyOrder(new Name("roleB")));
    }

    @Test
    public void enforceRead() throws Exception {
        Set<Name> roles = _grants.enforceRead(new Name("user1"), collection(_col1));
        assertThat(roles, containsInAnyOrder(new Name("roleA")));
    }

    @Test
    public void enforceRead_MultiplePermission() throws Exception {
        _grants.grantRead(_col2, new Name("roleC"));

        Set<Name> roles = _grants.enforceRead(new Name("user1"), collection(_col1, _col2));
        assertThat(roles, containsInAnyOrder(new Name("roleA"), new Name("roleC")));
    }

    @Test
    public void enforceRead_EmptyColumns() throws Exception {
        Set<Name> roles = _grants.enforceRead(new Name("nouser"), Collections.emptyList());
        assertThat(roles, empty());
    }

    @Test
    public void grantRole_Throw() throws Exception {
        try {
            _grants.grantRole(new Name("user1"), new Name("roleC"));
            fail("missing exception");
        } catch (SqlSemanticException ex) {
        }
    }

    @Test
    public void enforceWrite_Throw() throws Exception {
        try {
            _grants.enforceWrite(new Name("user1"), collection(_col1));
            fail("missing exception");
        } catch (UnauthorizedSqlException ex) {
            assertThat(ex.getMessage(), is("the user 'user1' has no right to write 'id'"));
        }
    }

    @Test
    public void enforceRead_Throw() throws Exception {
        try {
            _grants.enforceRead(new Name("user2"), collection(_col2));
            fail("missing exception");
        } catch (UnauthorizedSqlException ex) {
            assertThat(ex.getMessage(), is("the user 'user2' has no right to read 'id'"));
        }
    }

    private Collection<Column> collection(Column el) {
        ArrayList<Column> ret = new ArrayList<>();
        ret.add(el);
        return ret;
    }

    private Collection<Column> collection(Column el1, Column el2) {
        ArrayList<Column> ret = new ArrayList<>();
        ret.add(el1);
        ret.add(el2);
        return ret;
    }
}
