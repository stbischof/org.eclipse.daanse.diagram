/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.diagram.notation.edge;

import java.util.List;

import org.eclipse.daanse.diagram.notation.style.Cardinality;
import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DEndpoint;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.style.EndpointDecoration;
import org.eclipse.daanse.diagram.core.style.LineStyle;

/**
 * Static factories for the common UML / ER edge semantics. Each factory
 * creates a {@link DEdge} via the supplied {@link DiagramBuilder}, applies
 * the canonical {@link DEdge.Kind}, the matching {@link LineStyle}, and the
 * canonical {@link EndpointDecoration} pair. The returned
 * {@link EdgeBuilder} lets the caller add a label, override decorations,
 * or attach cardinalities.
 *
 * <p>Each semantic factory has both a node-to-node form (taking object keys
 * registered with the builder) and a port-to-port form (taking
 * {@link DPort} handles directly), so column-exact docking — common in DB
 * schemas and Ecore reference diagrams — is a one-liner.</p>
 *
 * <p>If either endpoint is unknown, the factory returns an
 * {@link EdgeBuilder} wrapping {@code null}; callers can still chain setters
 * safely and {@link EdgeBuilder#done()} returns {@code null} just like
 * {@link DiagramBuilder#edge(Object, Object, String)} does today.</p>
 */
public final class Edges {

    private Edges() {}

    /** Default id prefix used by every factory unless overridden via the
     *  module-specific overload. Kept short to keep edge ids compact. */
    private static final String DEFAULT_PREFIX = "e";

    public static EdgeBuilder inheritance(DiagramBuilder b, Object child, Object parent) {
        return inheritance(b, child, parent, DEFAULT_PREFIX);
    }
    public static EdgeBuilder inheritance(DiagramBuilder b, Object child, Object parent, String idPrefix) {
        return styleInheritance(b.edge(child, parent, idPrefix));
    }
    public static EdgeBuilder inheritance(DiagramBuilder b, DPort child, DPort parent) {
        return inheritance(b, child, parent, DEFAULT_PREFIX);
    }
    public static EdgeBuilder inheritance(DiagramBuilder b, DPort child, DPort parent, String idPrefix) {
        return styleInheritance(b.portEdge(child, parent, idPrefix));
    }
    private static EdgeBuilder styleInheritance(DEdge e) {
        if (e != null) {
            e.kind(DEdge.Kind.INHERITANCE).lineStyle(LineStyle.SOLID)
             .sourceDecoration(EndpointDecoration.NONE)
             .targetDecoration(EndpointDecoration.TRIANGLE_HOLLOW);
        }
        return new EdgeBuilder(e);
    }

    public static EdgeBuilder realization(DiagramBuilder b, Object impl, Object iface) {
        return realization(b, impl, iface, DEFAULT_PREFIX);
    }
    public static EdgeBuilder realization(DiagramBuilder b, Object impl, Object iface, String idPrefix) {
        return styleRealization(b.edge(impl, iface, idPrefix));
    }
    public static EdgeBuilder realization(DiagramBuilder b, DPort impl, DPort iface) {
        return realization(b, impl, iface, DEFAULT_PREFIX);
    }
    public static EdgeBuilder realization(DiagramBuilder b, DPort impl, DPort iface, String idPrefix) {
        return styleRealization(b.portEdge(impl, iface, idPrefix));
    }
    private static EdgeBuilder styleRealization(DEdge e) {
        if (e != null) {
            e.kind(DEdge.Kind.INHERITANCE).lineStyle(LineStyle.DASHED)
             .sourceDecoration(EndpointDecoration.NONE)
             .targetDecoration(EndpointDecoration.TRIANGLE_HOLLOW);
        }
        return new EdgeBuilder(e);
    }

    public static EdgeBuilder composition(DiagramBuilder b, Object whole, Object part, Cardinality partCard) {
        return composition(b, whole, part, partCard, DEFAULT_PREFIX);
    }
    public static EdgeBuilder composition(DiagramBuilder b, Object whole, Object part, Cardinality partCard, String idPrefix) {
        return styleComposition(b.edge(whole, part, idPrefix), partCard);
    }
    public static EdgeBuilder composition(DiagramBuilder b, DPort whole, DPort part, Cardinality partCard) {
        return composition(b, whole, part, partCard, DEFAULT_PREFIX);
    }
    public static EdgeBuilder composition(DiagramBuilder b, DPort whole, DPort part, Cardinality partCard, String idPrefix) {
        return styleComposition(b.portEdge(whole, part, idPrefix), partCard);
    }
    private static EdgeBuilder styleComposition(DEdge e, Cardinality partCard) {
        EdgeBuilder eb = new EdgeBuilder(e);
        if (e != null) {
            e.kind(DEdge.Kind.ASSOCIATION).lineStyle(LineStyle.SOLID)
             .sourceDecoration(EndpointDecoration.DIAMOND_FILLED)
             .targetDecoration(EndpointDecoration.OPEN_ARROW);
            if (partCard != null) eb.targetCardinality(partCard);
        }
        return eb;
    }

    public static EdgeBuilder aggregation(DiagramBuilder b, Object whole, Object part, Cardinality partCard) {
        return aggregation(b, whole, part, partCard, DEFAULT_PREFIX);
    }
    public static EdgeBuilder aggregation(DiagramBuilder b, Object whole, Object part, Cardinality partCard, String idPrefix) {
        return styleAggregation(b.edge(whole, part, idPrefix), partCard);
    }
    public static EdgeBuilder aggregation(DiagramBuilder b, DPort whole, DPort part, Cardinality partCard) {
        return aggregation(b, whole, part, partCard, DEFAULT_PREFIX);
    }
    public static EdgeBuilder aggregation(DiagramBuilder b, DPort whole, DPort part, Cardinality partCard, String idPrefix) {
        return styleAggregation(b.portEdge(whole, part, idPrefix), partCard);
    }
    private static EdgeBuilder styleAggregation(DEdge e, Cardinality partCard) {
        EdgeBuilder eb = new EdgeBuilder(e);
        if (e != null) {
            e.kind(DEdge.Kind.ASSOCIATION).lineStyle(LineStyle.SOLID)
             .sourceDecoration(EndpointDecoration.DIAMOND_HOLLOW)
             .targetDecoration(EndpointDecoration.OPEN_ARROW);
            if (partCard != null) eb.targetCardinality(partCard);
        }
        return eb;
    }

    public static EdgeBuilder association(DiagramBuilder b, Object a, Object c,
                                           Cardinality srcCard, Cardinality tgtCard) {
        return association(b, a, c, srcCard, tgtCard, DEFAULT_PREFIX);
    }
    public static EdgeBuilder association(DiagramBuilder b, Object a, Object c,
                                           Cardinality srcCard, Cardinality tgtCard, String idPrefix) {
        return styleAssociation(b.edge(a, c, idPrefix), srcCard, tgtCard);
    }
    public static EdgeBuilder association(DiagramBuilder b, DPort a, DPort c,
                                           Cardinality srcCard, Cardinality tgtCard) {
        return association(b, a, c, srcCard, tgtCard, DEFAULT_PREFIX);
    }
    public static EdgeBuilder association(DiagramBuilder b, DPort a, DPort c,
                                           Cardinality srcCard, Cardinality tgtCard, String idPrefix) {
        return styleAssociation(b.portEdge(a, c, idPrefix), srcCard, tgtCard);
    }
    private static EdgeBuilder styleAssociation(DEdge e, Cardinality srcCard, Cardinality tgtCard) {
        EdgeBuilder eb = new EdgeBuilder(e);
        if (e != null) {
            e.kind(DEdge.Kind.ASSOCIATION).lineStyle(LineStyle.SOLID)
             .sourceDecoration(EndpointDecoration.NONE)
             .targetDecoration(EndpointDecoration.OPEN_ARROW);
            eb.cardinalities(srcCard, tgtCard);
        }
        return eb;
    }

    public static EdgeBuilder dependency(DiagramBuilder b, Object a, Object c) {
        return dependency(b, a, c, DEFAULT_PREFIX);
    }
    public static EdgeBuilder dependency(DiagramBuilder b, Object a, Object c, String idPrefix) {
        return styleDependency(b.edge(a, c, idPrefix));
    }
    public static EdgeBuilder dependency(DiagramBuilder b, DPort a, DPort c) {
        return dependency(b, a, c, DEFAULT_PREFIX);
    }
    public static EdgeBuilder dependency(DiagramBuilder b, DPort a, DPort c, String idPrefix) {
        return styleDependency(b.portEdge(a, c, idPrefix));
    }
    /** Port-to-node dependency edge — useful when only one side has a port
     *  (e.g. column references a SimpleType node). */
    public static EdgeBuilder dependency(DiagramBuilder b, DPort src, Object tgtKey, String idPrefix) {
        DEdge e = b.portToNodeEdge(src, b.node(tgtKey), idPrefix);
        return styleDependency(e);
    }
    private static EdgeBuilder styleDependency(DEdge e) {
        if (e != null) {
            e.kind(DEdge.Kind.DEPENDENCY).lineStyle(LineStyle.DASHED)
             .sourceDecoration(EndpointDecoration.NONE)
             .targetDecoration(EndpointDecoration.CLOSED_ARROW);
        }
        return new EdgeBuilder(e);
    }

    public static EdgeBuilder foreignKey(DiagramBuilder b, DPort fkCol, DPort pkCol) {
        return foreignKey(b, fkCol, pkCol, DEFAULT_PREFIX);
    }
    public static EdgeBuilder foreignKey(DiagramBuilder b, DPort fkCol, DPort pkCol, String idPrefix) {
        return styleForeignKey(b.portEdge(fkCol, pkCol, idPrefix), false);
    }

    /**
     * Composite / multi-port foreign key: a single hyperedge whose source
     * endpoints are all the FK columns on the referring table and whose
     * target endpoints are all the matching PK columns on the referenced
     * table. ELK Layered routes the trunk + per-leg splines and the renderer
     * draws the union of pieces.
     *
     * <p>If both lists have exactly one entry the result is a normal
     * {@link DEdge.Kind#FOREIGN_KEY simple FK}; otherwise it is a
     * {@link DEdge.Kind#COMPOSITE_FK} hyperedge.</p>
     */
    public static EdgeBuilder foreignKey(DiagramBuilder b, List<DPort> fkCols, List<DPort> pkCols) {
        return foreignKey(b, fkCols, pkCols, DEFAULT_PREFIX);
    }
    public static EdgeBuilder foreignKey(DiagramBuilder b, List<DPort> fkCols, List<DPort> pkCols, String idPrefix) {
        if (fkCols == null || pkCols == null || fkCols.isEmpty() || pkCols.isEmpty()) {
            return new EdgeBuilder(null);
        }
        if (fkCols.size() == 1 && pkCols.size() == 1) {
            return foreignKey(b, fkCols.get(0), pkCols.get(0), idPrefix);
        }
        // Hyperedge: stable id derived from the first source/target port pair.
        String id = idPrefix + "_" + fkCols.get(0).id() + "_" + pkCols.get(0).id();
        DEdge e = b.edge(id);
        for (DPort p : fkCols) e.addSource(new DEndpoint.PortEndpoint(p));
        for (DPort p : pkCols) e.addTarget(new DEndpoint.PortEndpoint(p));
        return styleForeignKey(e, true);
    }

    private static EdgeBuilder styleForeignKey(DEdge e, boolean composite) {
        if (e != null) {
            e.kind(composite ? DEdge.Kind.COMPOSITE_FK : DEdge.Kind.FOREIGN_KEY)
             .lineStyle(LineStyle.SOLID)
             .sourceDecoration(EndpointDecoration.NONE)
             .targetDecoration(EndpointDecoration.NONE);
        }
        return new EdgeBuilder(e);
    }

    /** Trigger edge — an event that fires on a target. Modeled as a dashed
     *  dependency with an open arrowhead so it reads "this event triggers
     *  that". Replaces ad-hoc reuse of {@link DEdge.Kind#FOREIGN_KEY} for
     *  trigger-style relationships. */
    public static EdgeBuilder trigger(DiagramBuilder b, Object event, Object target) {
        return trigger(b, event, target, DEFAULT_PREFIX);
    }
    public static EdgeBuilder trigger(DiagramBuilder b, Object event, Object target, String idPrefix) {
        DEdge e = b.edge(event, target, idPrefix);
        if (e != null) {
            e.kind(DEdge.Kind.DEPENDENCY).lineStyle(LineStyle.DASHED)
             .sourceDecoration(EndpointDecoration.NONE)
             .targetDecoration(EndpointDecoration.OPEN_ARROW);
        }
        return new EdgeBuilder(e);
    }
}
