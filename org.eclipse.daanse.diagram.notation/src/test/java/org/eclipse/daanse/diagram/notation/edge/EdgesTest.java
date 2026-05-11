/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*/
package org.eclipse.daanse.diagram.notation.edge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.daanse.diagram.notation.style.Cardinality;
import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.EndpointDecoration;
import org.eclipse.daanse.diagram.core.style.LineStyle;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.junit.jupiter.api.Test;

final class EdgesTest {

    private static final Stereotype S = new Stereotype("CLASS", Palette.BLUE_900, "");

    private DiagramBuilder builderWithTwoNodes(Object a, Object b) {
        DiagramBuilder db = DiagramBuilder.of("test");
        db.labelled(a, S, "A", "n");
        db.labelled(b, S, "B", "n");
        return db;
    }

    @Test
    void inheritance_setsTriangleHollowAndSolidLine() {
        Object a = new Object(); Object b = new Object();
        DEdge e = Edges.inheritance(builderWithTwoNodes(a, b), a, b).done();
        assertEquals(DEdge.Kind.INHERITANCE, e.kind());
        assertEquals(LineStyle.SOLID, e.lineStyle());
        assertEquals(EndpointDecoration.NONE, e.sourceDecoration());
        assertEquals(EndpointDecoration.TRIANGLE_HOLLOW, e.targetDecoration());
    }

    @Test
    void realization_isDashedTriangleHollow() {
        Object a = new Object(); Object b = new Object();
        DEdge e = Edges.realization(builderWithTwoNodes(a, b), a, b).done();
        assertEquals(LineStyle.DASHED, e.lineStyle());
        assertEquals(EndpointDecoration.TRIANGLE_HOLLOW, e.targetDecoration());
    }

    @Test
    void composition_setsFilledDiamondAtSource() {
        Object a = new Object(); Object b = new Object();
        DEdge e = Edges.composition(builderWithTwoNodes(a, b), a, b, Cardinality.ONE_TO_MANY).done();
        assertEquals(EndpointDecoration.DIAMOND_FILLED, e.sourceDecoration());
        assertEquals(EndpointDecoration.OPEN_ARROW, e.targetDecoration());
        assertEquals("1..*", e.targetCardinality());
    }

    @Test
    void aggregation_setsHollowDiamondAtSource() {
        Object a = new Object(); Object b = new Object();
        DEdge e = Edges.aggregation(builderWithTwoNodes(a, b), a, b, Cardinality.ZERO_TO_MANY).done();
        assertEquals(EndpointDecoration.DIAMOND_HOLLOW, e.sourceDecoration());
        assertEquals("0..*", e.targetCardinality());
    }

    @Test
    void association_setsBothCardinalities() {
        Object a = new Object(); Object b = new Object();
        DEdge e = Edges.association(builderWithTwoNodes(a, b), a, b,
                Cardinality.ZERO_TO_MANY, Cardinality.ONE).done();
        assertEquals(DEdge.Kind.ASSOCIATION, e.kind());
        assertEquals(EndpointDecoration.OPEN_ARROW, e.targetDecoration());
        assertEquals("0..*", e.sourceCardinality());
        assertEquals("1", e.targetCardinality());
    }

    @Test
    void dependency_isDashedClosedArrow() {
        Object a = new Object(); Object b = new Object();
        DEdge e = Edges.dependency(builderWithTwoNodes(a, b), a, b).done();
        assertEquals(DEdge.Kind.DEPENDENCY, e.kind());
        assertEquals(LineStyle.DASHED, e.lineStyle());
        assertEquals(EndpointDecoration.CLOSED_ARROW, e.targetDecoration());
    }

    @Test
    void trigger_isDashedOpenArrowDependency() {
        Object a = new Object(); Object b = new Object();
        DEdge e = Edges.trigger(builderWithTwoNodes(a, b), a, b).done();
        assertEquals(DEdge.Kind.DEPENDENCY, e.kind());
        assertEquals(LineStyle.DASHED, e.lineStyle());
        assertEquals(EndpointDecoration.OPEN_ARROW, e.targetDecoration());
    }

    @Test
    void unknownKey_returnsNullEdgeWithoutThrowing() {
        DiagramBuilder db = DiagramBuilder.of("test");
        Object a = new Object(); Object b = new Object();
        // Neither node registered.
        DEdge e = Edges.inheritance(db, a, b).done();
        assertEquals(null, e);
    }

    /** sanity check that Stereotype with empty icon does not blow up the
     *  builder's labelled node creation in our setup. */
    @Test
    void labelledBoxBodyAccepts() {
        Object a = new Object();
        DiagramBuilder db = DiagramBuilder.of("test");
        LabelledBoxBody body = db.labelled(a, S, "A", "n");
        assertEquals(LabelledBoxBody.class, body.getClass());
    }
}
