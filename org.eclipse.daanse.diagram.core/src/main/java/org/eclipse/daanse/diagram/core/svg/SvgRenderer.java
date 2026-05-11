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
package org.eclipse.daanse.diagram.core.svg;

import java.util.StringJoiner;

import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.style.EndpointDecoration;
import org.eclipse.daanse.diagram.core.style.LineStyle;

/**
 * Renders a {@link Diagram} that has already been laid out into SVG. Each
 * node is drawn in its own translated group, then each edge — including
 * multi-section hyperedges — is stroked as an orthogonal polyline with the
 * appropriate marker.
 */
public final class SvgRenderer {

    /** Muted dark-blue-to-slate for every PK/FK relation — simple or composite.
     *  Deliberately subdued so the edges read as connectors, not accents. */
    private static final String FK_COLOR = "#334155"; // slate-700
    private static final String DEFAULT_EDGE_COLOR = "#374151"; // gray-700

    private static final String DEFAULT_CSS = """
            .dv-node rect.dv-frame { fill: #fff; stroke: #374151; stroke-width: 1; }
            .dv-node .dv-title { font: 600 13px sans-serif; fill: #111827; }
            .dv-node .dv-sub { font: 400 10px sans-serif; fill: #6b7280; }
            .dv-edge { fill: none; stroke: #374151; stroke-width: 1.2; }
            .dv-edge.fk { stroke: #334155; }
            .dv-edge.inherit { stroke: #16a34a; }
            .dv-edge.dep { stroke: #6b7280; stroke-dasharray: 4 3; }
            .dv-label { font: 400 10px sans-serif; fill: #334155; }
            .dv-cardinality { font: 400 10px sans-serif; fill: #1e293b; }
            .dv-cardinality-bg { fill: #ffffff; }
            """;

    public String render(Diagram diagram) {
        double[] bb = boundingBox(diagram);
        double pad = 16;
        double w = bb[2] + pad * 2;
        double h = bb[3] + pad * 2;
        SvgDoc doc = new SvgDoc(w, h);
        doc.css((diagram.css() != null ? diagram.css() : DEFAULT_CSS));

        // Legacy markers used by the Kind-based render path.
        doc.defs().add(arrowMarker("dv-arrow", DEFAULT_EDGE_COLOR));
        doc.defs().add(arrowMarker("dv-arrow-fk", FK_COLOR));
        doc.defs().add(triangleMarker("dv-tri", "#fff", "#16a34a"));

        // Decoration markers used by the EdgeStyle-driven render path. Each
        // decoration value produces a marker in the default edge color so
        // any edge that opts into explicit styling renders consistently.
        for (EndpointDecoration d : EndpointDecoration.values()) {
            if (d == EndpointDecoration.NONE) continue;
            doc.defs().add(decorationMarker(d, DEFAULT_EDGE_COLOR));
        }

        SvgElem body = doc.body().attr("transform", "translate(" + (pad - bb[0])
                + "," + (pad - bb[1]) + ")");
        for (DNode n : diagram.topLevelNodes()) {
            renderNode(body, n, 0, 0);
        }
        for (DEdge e : diagram.edges()) {
            renderEdge(body, e);
        }
        return doc.render();
    }

    private void renderNode(SvgElem parent, DNode n, double offX, double offY) {
        double ax = offX + n.x();
        double ay = offY + n.y();
        SvgElem g = new SvgElem("g").cls("dv-node dv-" + sanitize(n.id()))
                .attr("transform", "translate(" + SvgElem.fmt(ax) + "," + SvgElem.fmt(ay) + ")");
        n.body().render(g, n);
        for (DNode child : n.children()) {
            renderNode(g, child, 0, 0);
        }
        parent.add(g);
    }

    private void renderEdge(SvgElem parent, DEdge e) {
        if (hasExplicitStyle(e)) {
            renderEdgeExplicit(parent, e);
        } else {
            renderEdgeByKind(parent, e);
        }
        renderEdgeLabelAndCardinalities(parent, e);
    }

    private static boolean hasExplicitStyle(DEdge e) {
        return e.lineStyle() != null
                || e.sourceDecoration() != null
                || e.targetDecoration() != null;
    }

    /** Render path using the new EndpointDecoration / LineStyle fields. */
    private void renderEdgeExplicit(SvgElem parent, DEdge e) {
        EndpointDecoration src = e.sourceDecoration();
        EndpointDecoration tgt = e.targetDecoration();
        LineStyle ls = e.lineStyle() != null ? e.lineStyle() : LineStyle.SOLID;
        String dashArray = switch (ls) {
            case SOLID -> null;
            case DASHED -> "4 3";
            case DOTTED -> "1 3";
        };
        String cls = "dv-edge";
        if (e.cssClass() != null) {
            cls += " " + e.cssClass();
        }
        StringBuilder inline = new StringBuilder();
        if (dashArray != null) {
            inline.append("stroke-dasharray:").append(dashArray).append(";");
        }
        if (e.color() != null) {
            inline.append("stroke:").append(e.color()).append(";");
        }
        int total = e.sections().size();
        for (int i = 0; i < total; i++) {
            DEdge.Section s = e.sections().get(i);
            SvgElem path = new SvgElem("path").cls(cls.trim())
                    .attr("d", pathFor(s));
            if (inline.length() > 0) {
                path.style(inline.toString());
            }
            if (i == 0 && src != null && src != EndpointDecoration.NONE) {
                path.attr("marker-start", "url(#" + decorationMarkerId(src) + ")");
            }
            if (i == total - 1 && tgt != null && tgt != EndpointDecoration.NONE) {
                path.attr("marker-end", "url(#" + decorationMarkerId(tgt) + ")");
            }
            parent.add(path);
        }
    }

    /** Legacy render path: pick marker + CSS class from {@link DEdge.Kind}. */
    private void renderEdgeByKind(SvgElem parent, DEdge e) {
        boolean fkLike = e.kind() == DEdge.Kind.FOREIGN_KEY
                || e.kind() == DEdge.Kind.COMPOSITE_FK;
        String marker = switch (e.kind()) {
            case FOREIGN_KEY, COMPOSITE_FK -> "url(#dv-arrow-fk)";
            case INHERITANCE -> "url(#dv-tri)";
            case DEPENDENCY -> "url(#dv-arrow)";
            default -> "url(#dv-arrow)";
        };
        String cls = "dv-edge " + switch (e.kind()) {
            case FOREIGN_KEY, COMPOSITE_FK -> "fk";
            case INHERITANCE -> "inherit";
            case DEPENDENCY -> "dep";
            default -> "";
        };
        if (e.cssClass() != null) {
            cls += " " + e.cssClass();
        }

        // For FK edges we rely on the cardinality pills ("1" vs "n") to
        // communicate direction, so no arrowheads are drawn. Other edge
        // kinds (inheritance, dependency) still carry their marker at the
        // end of the last section.
        int total = e.sections().size();
        for (int i = 0; i < total; i++) {
            DEdge.Section s = e.sections().get(i);
            SvgElem path = new SvgElem("path").cls(cls.trim())
                    .attr("d", pathFor(s));
            if (!fkLike && i == total - 1) {
                path.attr("marker-end", marker);
            }
            parent.add(path);
        }
    }

    private void renderEdgeLabelAndCardinalities(SvgElem parent, DEdge e) {
        boolean fkLike = e.kind() == DEdge.Kind.FOREIGN_KEY
                || e.kind() == DEdge.Kind.COMPOSITE_FK;
        String edgeColor = edgeColor(e);
        // Cardinality pills are drawn for any edge that declares at least
        // one — FK edges default to 0..* / 1, reference edges get whatever
        // the converter stored.
        if (fkLike || e.sourceCardinality() != null || e.targetCardinality() != null) {
            renderCardinalities(parent, e);
        }
        if (e.label() != null) {
            double lx, ly, lw, lh;
            if (e.labelLaidOut() && e.labelWidth() > 0) {
                // ELK found a non-overlapping spot along the route.
                lx = e.labelX();
                ly = e.labelY();
                lw = e.labelWidth();
                lh = e.labelHeight();
            } else if (!e.sections().isEmpty()) {
                // Fallback: midpoint of the first section, with estimated size.
                DEdge.Section first = e.sections().get(0);
                lw = e.label().length() * 5.8 + 4;
                lh = 12;
                lx = (first.startX + first.endX) / 2 - lw / 2;
                ly = (first.startY + first.endY) / 2 - lh - 2;
            } else {
                return;
            }
            // For edges that already carry cardinality pills (every FK; any
            // reference edge with explicit cardinalities), ELK doesn't know
            // about the pills, so a tiny perpendicular padding lifts the
            // label clear of the pill outline without floating it visibly
            // away from the route.
            boolean hasCardinalityPills = e.kind() == DEdge.Kind.FOREIGN_KEY
                    || e.kind() == DEdge.Kind.COMPOSITE_FK
                    || e.sourceCardinality() != null
                    || e.targetCardinality() != null;
            if (hasCardinalityPills) {
                ly -= 4;
            }
            parent.add(new SvgElem("text").cls("dv-label")
                    .attr("x", lx + 2)
                    .attr("y", ly + lh - 2)
                    .style("fill:" + edgeColor)
                    .text(e.label()));
        }
    }

    /** UML default cardinality for an FK-like edge: "0..*" at the FK
     *  (child) side, "1" at the PK (parent) side. For reference edges the
     *  converter supplies explicit values; anything null is skipped. */
    private void renderCardinalities(SvgElem parent, DEdge e) {
        boolean fkLike = e.kind() == DEdge.Kind.FOREIGN_KEY
                || e.kind() == DEdge.Kind.COMPOSITE_FK;
        String srcCard = e.sourceCardinality();
        String tgtCard = e.targetCardinality();
        if (fkLike) {
            if (srcCard == null) srcCard = "0..*";
            if (tgtCard == null) tgtCard = "1";
        }
        if ((srcCard == null || srcCard.isEmpty())
                && (tgtCard == null || tgtCard.isEmpty())) {
            return;
        }
        if (e.sections().isEmpty()) {
            return;
        }
        String color = edgeColor(e);
        int srcCount = e.sources().size();
        int tgtCount = e.targets().size();
        int total = e.sections().size();
        // Hyperedge layout: src-legs | junctionA-interior | middle | junctionB-interior | tgt-legs
        boolean hyper = total == srcCount + 3 + tgtCount && srcCount + tgtCount > 2;

        if (srcCard != null && !srcCard.isEmpty()) {
            int srcSectEnd = hyper ? srcCount : 1;
            for (int i = 0; i < srcSectEnd; i++) {
                DEdge.Section s = e.sections().get(i);
                addCardinality(parent, s.startX, s.startY, secondPoint(s), srcCard, color);
            }
        }
        if (tgtCard != null && !tgtCard.isEmpty()) {
            int tgtSectStart = hyper ? srcCount + 3 : 0;
            for (int i = tgtSectStart; i < total; i++) {
                DEdge.Section s = e.sections().get(i);
                addCardinality(parent, s.endX, s.endY, secondLastPoint(s), tgtCard, color);
            }
        }
    }

    /**
     * Draws the cardinality as a pill sitting next to the endpoint: white
     * fill, bold black text, edge-coloured border. Placed along the first/
     * last segment direction with a perpendicular offset so it never lands
     * on the line itself.
     */
    private void addCardinality(SvgElem parent, double ax, double ay,
                                 double[] next, String text, String color) {
        double dx = next[0] - ax;
        double dy = next[1] - ay;
        double m = Math.hypot(dx, dy);
        if (m == 0) {
            m = 1;
            dx = 1;
        }
        double ux = dx / m, uy = dy / m;
        double px = -uy, py = ux;

        // Pill dimensions scale with the 10px text so one-character and
        // longer values like "0..1" both stay comfortably framed.
        double w = Math.max(14, text.length() * 5.8 + 6);
        double h = 13;

        // Pin the pill right next to the port, at the column row's height,
        // so the cardinality reads as "this column, this side". No
        // perpendicular offset — the pill sits on the same row as the port
        // and its white fill masks the short overlap with the edge line.
        double cx = ax + ux * 12;
        double cy = ay + uy * 12;

        parent.add(new SvgElem("rect").cls("dv-cardinality-bg")
                .attr("x", cx - w / 2).attr("y", cy - h / 2)
                .attr("width", w).attr("height", h)
                .attr("rx", h / 2).attr("ry", h / 2)
                .style("fill:#ffffff;stroke:" + color + ";stroke-width:1.3"));
        parent.add(new SvgElem("text").cls("dv-cardinality")
                .attr("x", cx).attr("y", cy + 3)
                .attr("text-anchor", "middle")
                .text(text));
    }

    private double[] secondPoint(DEdge.Section s) {
        if (!s.bendPoints.isEmpty()) {
            return s.bendPoints.get(0);
        }
        return new double[] { s.endX, s.endY };
    }

    private double[] secondLastPoint(DEdge.Section s) {
        if (!s.bendPoints.isEmpty()) {
            return s.bendPoints.get(s.bendPoints.size() - 1);
        }
        return new double[] { s.startX, s.startY };
    }

    private String edgeColor(DEdge e) {
        if (e.color() != null) {
            return e.color();
        }
        return switch (e.kind()) {
            case FOREIGN_KEY, COMPOSITE_FK -> FK_COLOR;
            case INHERITANCE -> "#16a34a";
            case DEPENDENCY -> "#6b7280";
            default -> DEFAULT_EDGE_COLOR;
        };
    }

    private String pathFor(DEdge.Section s) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("M" + SvgElem.fmt(s.startX) + "," + SvgElem.fmt(s.startY));
        for (double[] bp : s.bendPoints) {
            sj.add("L" + SvgElem.fmt(bp[0]) + "," + SvgElem.fmt(bp[1]));
        }
        sj.add("L" + SvgElem.fmt(s.endX) + "," + SvgElem.fmt(s.endY));
        return sj.toString();
    }

    private double[] boundingBox(Diagram diagram) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (DNode n : diagram.topLevelNodes()) {
            minX = Math.min(minX, n.x());
            minY = Math.min(minY, n.y());
            maxX = Math.max(maxX, n.x() + n.width());
            maxY = Math.max(maxY, n.y() + n.height());
        }
        for (DEdge e : diagram.edges()) {
            for (DEdge.Section s : e.sections()) {
                minX = Math.min(minX, Math.min(s.startX, s.endX));
                minY = Math.min(minY, Math.min(s.startY, s.endY));
                maxX = Math.max(maxX, Math.max(s.startX, s.endX));
                maxY = Math.max(maxY, Math.max(s.startY, s.endY));
                for (double[] bp : s.bendPoints) {
                    minX = Math.min(minX, bp[0]);
                    minY = Math.min(minY, bp[1]);
                    maxX = Math.max(maxX, bp[0]);
                    maxY = Math.max(maxY, bp[1]);
                }
            }
        }
        if (Double.isInfinite(minX)) {
            return new double[] { 0, 0, 0, 0 };
        }
        return new double[] { minX, minY, maxX - minX, maxY - minY };
    }

    private String sanitize(String id) {
        return id.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }

    private static SvgElem arrowMarker(String id, String color) {
        return new SvgElem("marker")
                .attr("id", id).attr("viewBox", "0 0 10 10")
                .attr("refX", 9).attr("refY", 5)
                .attr("markerWidth", 7).attr("markerHeight", 7)
                .attr("orient", "auto-start-reverse")
                .add(new SvgElem("path").attr("d", "M0,0 L10,5 L0,10 z")
                        .attr("fill", color));
    }

    private static SvgElem triangleMarker(String id, String fill, String stroke) {
        return new SvgElem("marker")
                .attr("id", id).attr("viewBox", "0 0 12 12")
                .attr("refX", 11).attr("refY", 6)
                .attr("markerWidth", 10).attr("markerHeight", 10)
                .attr("orient", "auto-start-reverse")
                .add(new SvgElem("path").attr("d", "M0,0 L12,6 L0,12 z")
                        .attr("fill", fill).attr("stroke", stroke));
    }

    /** Stable id derived from the decoration enum value. */
    static String decorationMarkerId(EndpointDecoration d) {
        return "dv-deco-" + d.name().toLowerCase().replace('_', '-');
    }

    /** Build a single {@code <marker>} element for the given decoration in
     *  the requested color. The marker viewBox is {@code 0 0 12 12} and is
     *  oriented with {@code auto-start-reverse} so a single definition can
     *  be reused for both source and target ends of an edge. */
    private static SvgElem decorationMarker(EndpointDecoration d, String color) {
        String id = decorationMarkerId(d);
        return switch (d) {
            case NONE -> throw new IllegalArgumentException("NONE has no marker");
            case OPEN_ARROW -> marker(id, 11, 6, 10, 10,
                    new SvgElem("path").attr("d", "M0,0 L12,6 M0,12 L12,6")
                            .attr("fill", "none").attr("stroke", color)
                            .attr("stroke-width", 1.4));
            case CLOSED_ARROW -> marker(id, 11, 6, 10, 10,
                    new SvgElem("path").attr("d", "M0,0 L12,6 L0,12 z")
                            .attr("fill", color));
            // Compact inheritance triangle. {@code refX=12} aligns the
            // triangle's APEX with the path endpoint and pushes the entire
            // marker forward of it (the base sits a viewBox-width back, but
            // with markerWidth=6 the actual backward coverage is only
            // ~6 pixels — leaving a comfortable visible run on every
            // perpendicular entry segment).
            case TRIANGLE_HOLLOW -> marker(id, 12, 6, 6, 6,
                    new SvgElem("path").attr("d", "M0,0 L12,6 L0,12 z")
                            .attr("fill", "#fff").attr("stroke", color)
                            .attr("stroke-width", 1.4));
            case TRIANGLE_FILLED -> marker(id, 12, 6, 6, 6,
                    new SvgElem("path").attr("d", "M0,0 L12,6 L0,12 z")
                            .attr("fill", color));
            case DIAMOND_HOLLOW -> marker(id, 11, 6, 12, 12,
                    new SvgElem("path").attr("d", "M0,6 L6,0 L12,6 L6,12 z")
                            .attr("fill", "#fff").attr("stroke", color)
                            .attr("stroke-width", 1.2));
            case DIAMOND_FILLED -> marker(id, 11, 6, 12, 12,
                    new SvgElem("path").attr("d", "M0,6 L6,0 L12,6 L6,12 z")
                            .attr("fill", color));
            case CROWS_FOOT_MANY -> marker(id, 11, 6, 11, 11,
                    new SvgElem("path")
                            .attr("d", "M12,6 L0,0 M12,6 L0,12 M12,6 L0,6")
                            .attr("fill", "none").attr("stroke", color)
                            .attr("stroke-width", 1.3));
            case CROWS_FOOT_ONE -> marker(id, 11, 6, 8, 11,
                    new SvgElem("path").attr("d", "M6,0 L6,12")
                            .attr("fill", "none").attr("stroke", color)
                            .attr("stroke-width", 1.4));
            case CROWS_FOOT_MANY_OPT -> marker(id, 11, 6, 14, 11,
                    new SvgElem("g")
                            .add(new SvgElem("circle").attr("cx", 9.5).attr("cy", 6).attr("r", 2)
                                    .attr("fill", "#fff").attr("stroke", color).attr("stroke-width", 1.2))
                            .add(new SvgElem("path")
                                    .attr("d", "M7,6 L0,0 M7,6 L0,12 M7,6 L0,6")
                                    .attr("fill", "none").attr("stroke", color)
                                    .attr("stroke-width", 1.3)));
            case CROWS_FOOT_ONE_OPT -> marker(id, 11, 6, 12, 11,
                    new SvgElem("g")
                            .add(new SvgElem("circle").attr("cx", 9.5).attr("cy", 6).attr("r", 2)
                                    .attr("fill", "#fff").attr("stroke", color).attr("stroke-width", 1.2))
                            .add(new SvgElem("path").attr("d", "M5,0 L5,12")
                                    .attr("fill", "none").attr("stroke", color)
                                    .attr("stroke-width", 1.4)));
        };
    }

    private static SvgElem marker(String id, double refX, double refY,
                                   double width, double height, SvgElem child) {
        return new SvgElem("marker")
                .attr("id", id).attr("viewBox", "0 0 12 12")
                .attr("refX", refX).attr("refY", refY)
                .attr("markerWidth", width).attr("markerHeight", height)
                .attr("orient", "auto-start-reverse")
                .add(child);
    }

}
