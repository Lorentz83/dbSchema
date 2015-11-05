package edu.purdue.dbSchema.utils;

import java.util.ArrayList;
import mockit.Deencapsulation;
import mockit.Mocked;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class DirectedAcyclicGraphTest {

    @Test
    public void addFailSameNodes() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();
        String el = "start";
        assertThat(dag.add(el, el), is(false));
    }

    @Test
    public void addNullPointer(@Mocked final IMapSet<String, String> edges) {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>(edges);
        try {
            dag.add(null, "");
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
        try {
            dag.add("", null);
            fail("missing NullPointerException");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void followNode(@Mocked final IMapSet<String, String> edges) {
        String start = "start";
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>(edges);
        Iterable<String> it = dag.followNode(start);
        assertThat(it, instanceOf(DagIterable.class));
        DagIterable<String> actualIt = (DagIterable<String>) it;

        assertThat(Deencapsulation.getField(actualIt, String.class), is(start));
        assertThat(Deencapsulation.getField(actualIt, "_edges"), sameInstance(edges));
        assertThat(Deencapsulation.getField(actualIt, boolean.class), is(false));
    }

    @Test
    public void followNodeAndSelef(@Mocked final IMapSet<String, String> edges) {
        String start = "start";
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>(edges);
        Iterable<String> it = dag.followNodeAndSelef(start);
        assertThat(it, instanceOf(DagIterable.class));
        DagIterable<String> actualIt = (DagIterable<String>) it;
        assertThat(Deencapsulation.getField(actualIt, String.class), is(start));
        assertThat(Deencapsulation.getField(actualIt, "_edges"), sameInstance(edges));
        assertThat(Deencapsulation.getField(actualIt, boolean.class), is(true));
    }

    @Test
    public void followNodeNullPointer(@Mocked final IMapSet<String, String> edges) {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>(edges);
        try {
            dag.followNode(null);
            fail("missing null pointer exception");
        } catch (NullPointerException ex) {

        }
        try {
            dag.followNodeAndSelef(null);
            fail("missing null pointer exception");
        } catch (NullPointerException ex) {

        }
    }

    @Test
    public void followNode_AndSelef() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();
        // rol1 -- rolA         /- rolE
        //      `- rolB -- rolC -- rolD
        dag.add("rol1", "rolA");
        dag.add("rol1", "rolB");
        dag.add("rolB", "rolC");
        dag.add("rolC", "rolE");
        dag.add("rolC", "rolD");

        assertThat(dag.followNode("none"), emptyIterable());
        assertThat(dag.followNodeAndSelef("none"), containsInAnyOrder("none"));

        assertThat(dag.followNode("rolC"), containsInAnyOrder("rolE", "rolD"));
        assertThat(dag.followNodeAndSelef("rolC"), containsInAnyOrder("rolC", "rolE", "rolD"));

        // rol1 -- rolA --------/- rolE
        //      `- rolB -- rolC -- rolD
        dag.add("rolA", "rolE");

        assertThat(dag.followNode("rol1"), containsInAnyOrder("rolA", "rolB", "rolC", "rolE", "rolD"));
        ArrayList<String> ordered = toList(dag.followNode("rol1"));
        assertThat(ordered.indexOf("rolA"), lessThan(ordered.indexOf("rolC")));
        assertThat(ordered.indexOf("rolE"), lessThan(ordered.indexOf("rolD")));
    }

    @Test
    public void addDetectsCycles() {
        DirectedAcyclicGraph<String> dag = new DirectedAcyclicGraph<>();
        assertThat(dag.add("A", "B"), is(true));
        assertThat(dag.add("B", "C"), is(true));
        assertThat(dag.add("C", "A"), is(false));
        assertThat(dag.followNode("A"), contains("B", "C"));
    }

    private ArrayList<String> toList(Iterable<String> iterable) {
        ArrayList<String> list = new ArrayList<>();
        for (String item : iterable) {
            list.add(item);
        }
        return list;
    }
}
