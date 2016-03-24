package edu.purdue.dbSchema.parser;

import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class ParsedQueryTest {

    @Test
    public void addMainColumn_Exceptions() {
        ParsedQuery pq = new ParsedQuery(DlmQueryType.SELECT);
        try {
            pq.addMainColumn(null, "col");
            fail("Missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            pq.addMainColumn("tbl", null);
            fail("Missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            pq.addMainColumn("tbl", "");
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void addWhereColumn_Exceptions() {
        ParsedQuery pq = new ParsedQuery(DlmQueryType.SELECT);
        try {
            pq.addWhereColumn(null, "col");
            fail("Missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            pq.addWhereColumn("tbl", null);
            fail("Missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            pq.addWhereColumn("tbl", "");
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void addFrom_Exceptions() {
        ParsedQuery pq = new ParsedQuery(DlmQueryType.SELECT);
        try {
            pq.addFrom(null, "col");
            fail("Missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            pq.addFrom("tbl", null);
            fail("Missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            pq.addFrom("", "");
            fail("Missing IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }
}
