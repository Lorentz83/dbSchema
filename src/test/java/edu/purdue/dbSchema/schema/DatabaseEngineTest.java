package edu.purdue.dbSchema.schema;

import edu.purdue.dbSchema.erros.SqlSemanticException;
import edu.purdue.dbSchema.erros.UnauthorizedSqlException;
import edu.purdue.dbSchema.parser.DlmQueryType;
import edu.purdue.dbSchema.parser.Grant;
import edu.purdue.dbSchema.parser.ParsedQuery;
import edu.purdue.dbSchema.parser.StringPair;
import gudusoft.gsqlparser.EDbVendor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DatabaseEngineTest {

    DatabaseEngine _testDb;
    ParsedQuery _select;

    @Before
    public void initTestDb() throws Exception {
        _testDb = new DatabaseEngine(EDbVendor.dbvoracle);
        _testDb.parse("create table tbl1(id integer, f1 varchar)");
        _testDb.parse("create table tbl2(id integer, f2 varchar)");
        _select = new ParsedQuery(DlmQueryType.SELECT);
    }

    @Test
    public void filterTablesMissingTable() throws Exception {
        try {
            _select.addFrom("tblX", "");
            _testDb.filterTables(_select.from, Collections.<Name, Table>emptyMap());
            fail("missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("relation 'tblX' does not exist"));
        }
    }

    @Test
    public void filterTablesDuplicatedTable() throws Exception {
        try {
            _select.addFrom("tbl1", "");
            _select.addFrom("tbl1", "");
            _testDb.filterTables(_select.from, Collections.<Name, Table>emptyMap());
            fail("missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("table name 'tbl1' specified more than once"));
        }
    }

    @Test
    public void filterTables_DuplicatedTableAlias() throws Exception {
        try {
            _select.addFrom("tbl1", "t");
            _select.addFrom("tbl2", "t");
            _testDb.filterTables(_select.from, Collections.<Name, Table>emptyMap());
            fail("missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("table name 't' specified more than once"));
        }
    }

    @Test
    public void filterTables_SkipsSameTableDifferentAliases() throws Exception {
        Table tbl1 = _testDb.getTable("tbl1");
        _select.addFrom("tbl1", "t1");
        _select.addFrom("tbl1", "t2");
        HashMap<Name, Table> tables = _testDb.filterTables(_select.from, Collections.<Name, Table>emptyMap());
        assertThat(tables, hasEntry(new Name("t1"), tbl1));
        assertThat(tables, hasEntry(new Name("t2"), tbl1));
        assertThat(tables, aMapWithSize(2));
    }

    @Test
    public void filterTables_AliasCannotOverrideTable() throws Exception {
        _select.addFrom("tbl1", "");
        _select.addFrom("tbl2", "tbl1");
        try {
            _testDb.filterTables(_select.from, Collections.<Name, Table>emptyMap());
            fail("Missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("table name 'tbl1' specified more than once"));
        }
    }

    @Test
    public void filterTables() throws Exception {
        Table tbl1 = _testDb.getTable("tbl1");
        Table tbl2 = _testDb.getTable("tbl2");
        _select.addFrom("tbl1", "");
        _select.addFrom("tbl2", "t2");
        HashMap<Name, Table> filtered = _testDb.filterTables(_select.from, Collections.<Name, Table>emptyMap());
        assertThat(filtered, hasEntry(new Name("tbl1"), tbl1));
        assertThat(filtered, hasEntry(new Name("tbl2"), tbl2));
        assertThat(filtered, hasEntry(new Name("t2"), tbl2));
        assertThat(filtered, aMapWithSize(3));
    }

    @Test
    public void getSelectedColumnsMissingColumn() throws Exception {
        HashMap<Name, Table> usedTables = new HashMap<>();
        Table tbl1 = new Table("tbl1").addColumn("A1", "string", false, false)
                .addColumn("id", "int", true, true);
        usedTables.put(new Name("tbl1"), tbl1);
        List<StringPair> selectedCols;

        try {
            selectedCols = new ArrayList<>();
            selectedCols.add(new StringPair("", "C"));
            DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
            fail("missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("column 'C' does not exist"));
        }
        try {
            selectedCols = new ArrayList<>();
            selectedCols.add(new StringPair("tbl1", "C"));
            DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
            fail("missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("column 'C' does not exist"));
        }
        try {
            selectedCols = new ArrayList<>();
            selectedCols.add(new StringPair("tblX", "C"));
            DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
            fail("missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("missing FROM-clause entry for table 'tblX'"));
        }
    }

    @Test
    public void getSelectedColumnsAmbiguousColumn() throws Exception {
        HashMap<Name, Table> usedTables = new HashMap<>();
        Table tbl1 = new Table("tbl1").addColumn("A1", "string", false, false)
                .addColumn("id", "int", true, true);
        Table tbl2 = new Table("tbl2").addColumn("B1", "string", false, false)
                .addColumn("id", "int", true, true);
        usedTables.put(tbl1.getName(), tbl1);
        usedTables.put(tbl2.getName(), tbl2);
        List<StringPair> selectedCols;
        try {
            selectedCols = new ArrayList<>();
            selectedCols.add(new StringPair("", "id"));
            DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
            fail("missing exception");
        } catch (SqlSemanticException ex) {
            assertThat(ex.getMessage(), is("column reference 'id' is ambiguous"));
        }
    }

    @Test
    public void getSelectedColumns() throws Exception {
        HashMap<Name, Table> usedTables = new HashMap<>();
        Table tbl1 = new Table("tbl1")
                .addColumn("A1", "string", false, false)
                .addColumn("id", "int", true, true);
        Table tbl2 = new Table("tbl2")
                .addColumn("B1", "string", false, false)
                .addColumn("id", "int", true, true);
        usedTables.put(new Name("tbl1"), tbl1);
        usedTables.put(new Name("tbl"), tbl1);
        usedTables.put(new Name("tbl2"), tbl2);
        List<StringPair> selectedCols;
        ArrayList<AbstractColumn> cols;

        selectedCols = new ArrayList<>();
        selectedCols.add(new StringPair("tbl2", "id"));
        cols = DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
        assertThat(cols, hasSize(1));
        assertThat(cols.get(0), sameInstance(tbl2.getColumn("id")));

        selectedCols = new ArrayList<>();
        selectedCols.add(new StringPair("", "B1"));
        cols = DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
        assertThat(cols, hasSize(1));
        assertThat(cols.get(0), sameInstance(tbl2.getColumn("B1")));

        selectedCols = new ArrayList<>();
        selectedCols.add(new StringPair("tbl", "A1"));
        cols = DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
        assertThat(cols, hasSize(1));
        assertThat(cols.get(0), sameInstance(tbl1.getColumn("A1")));
    }

    @Test
    public void getSelectedColumnsStar() throws Exception {
        HashMap<Name, Table> usedTables = new HashMap<>();
        Table tbl1 = new Table("tbl1")
                .addColumn("A1", "string", false, false)
                .addColumn("id", "int", true, true);
        Table tbl2 = new Table("tbl2")
                .addColumn("B1", "string", false, false)
                .addColumn("id", "int", true, true);
        usedTables.put(new Name("tbl"), tbl1);
        usedTables.put(new Name("tbl2"), tbl2);

        ArrayList<AbstractColumn> cols;
        ArrayList<StringPair> selectedCols;
        selectedCols = new ArrayList<>();
        selectedCols.add(new StringPair("tbl", "*"));

        cols = DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
        assertThat(cols, containsInAnyOrder(tbl1.getColumns().toArray()));

        ArrayList<AbstractColumn> expectedCols;

        selectedCols = new ArrayList<>();
        selectedCols.add(new StringPair("", "*"));
        cols = DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
        expectedCols = new ArrayList<AbstractColumn>();
        expectedCols.addAll(tbl1.getColumns());
        expectedCols.addAll(tbl2.getColumns());
        assertThat(cols, containsInAnyOrder(expectedCols.toArray()));

        selectedCols = new ArrayList<>();
        selectedCols.add(new StringPair("tbl", "id"));
        selectedCols.add(new StringPair("tbl", "*"));
        cols = DatabaseEngine.getSelectedColumns(usedTables, selectedCols);
        expectedCols = new ArrayList<AbstractColumn>();
        expectedCols.addAll(tbl1.getColumns());
        expectedCols.add(tbl1.getColumn("id"));
        assertThat(cols, containsInAnyOrder(expectedCols.toArray()));
    }

    @Test
    public void evaluateSelectGetColumns() throws Exception {
        _select.addFrom("tbl1", "");
        _select.addFrom("tbl2", "t2");
        _select.addMainColumn("tbl1", "id");
        _select.addMainColumn("t2", "f2");
        QueryFeature res = _testDb.evaluateDlmQuery(_select, null, Collections.<Name, Table>emptyMap());

        AbstractColumn c1 = _testDb.getTable("tbl1").getColumn("id");
        AbstractColumn c2 = _testDb.getTable("tbl2").getColumn("f2");
        assertThat(res.getRoles(), empty());
        assertThat(res.getFilteredCols(), empty());
        assertThat(res.getUsedCols(), containsInAnyOrder(c1, c2));
    }

    @Test
    public void evaluateGrantCyclicRole() throws Exception {
        _testDb.evaluateGrant(new Grant("usr1", "usr2"));

        try {
            _testDb.evaluateGrant(new Grant("usr2", "usr1"));
            fail("missing exception on circular grant");
        } catch (SqlSemanticException ex) {

        }
    }

    @Test
    public void evaluateGrant_ToTable() throws Exception {
        try {
            _testDb.parse("select id from tbl1", "user1");
            fail("missing exception on circular grant");
        } catch (UnauthorizedSqlException ex) {
            assertThat(ex.getMessage(), is("the user 'user1' has no right to read 'id'"));
        }
        _testDb.evaluateGrant(new Grant(Grant.Type.READ, "user1", "tbl1", "id"));

        _testDb.parse("select id from tbl1", "user1");
    }

    @Test
    public void isSerializable() throws Exception {
        _testDb.parse("create table tblnew(id int); grant select on tblnew to usernew");
        ObjectOutputStream oos = new ObjectOutputStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {

            }
        });

        oos.writeObject(_testDb);
    }

    @Test
    public void parse_subQueryInFrom() throws Exception {
        _testDb.parse("GRANT SELECT ON tbl1 TO user");
        _testDb.parse("GRANT SELECT ON tbl2 TO user");

        AbstractColumn tbl2id = _testDb.getTable("tbl2").getColumn("id");
        AbstractColumn tbl1id = _testDb.getTable("tbl1").getColumn("id");

        QueryFeature feature = _testDb.parse("select tbl1.id, t2.s from (select sum(tbl2.id) as s from tbl2 where tbl2.id>0) as t2, tbl1", "user").get(0);
        assertThat(feature.getRoles(), contains("user"));
        assertThat(feature.getFilteredCols(), contains(tbl2id));
        assertThat(feature.getType(), is(DlmQueryType.SELECT));
        assertThat(feature.getUsedCols(), containsInAnyOrder(tbl1id, tbl2id));
    }
}
