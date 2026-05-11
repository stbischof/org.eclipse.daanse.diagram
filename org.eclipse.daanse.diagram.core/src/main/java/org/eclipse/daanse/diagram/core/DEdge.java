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
package org.eclipse.daanse.diagram.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.daanse.diagram.core.style.EndpointDecoration;
import org.eclipse.daanse.diagram.core.style.LineStyle;

/**
 * A diagram edge, optionally a hyperedge (multiple sources, multiple targets).
 * <p>
 * A composite foreign key is expressed as a single hyperedge whose source
 * endpoints are the FK columns on the referring table and whose target
 * endpoints are the PK columns on the referenced table. ELK Layered will
 * route the hyperedge — the shared trunk plus the per-source/per-target
 * splines — and the renderer then draws the union of the pieces.
 */
public final class DEdge {

    public enum Kind { ASSOCIATION, FOREIGN_KEY, COMPOSITE_FK, INHERITANCE, DEPENDENCY, CUSTOM }

    private final String id;
    private final List<DEndpoint> sources = new ArrayList<>();
    private final List<DEndpoint> targets = new ArrayList<>();
    private final List<Section> sections = new ArrayList<>();
    private Kind kind = Kind.ASSOCIATION;
    private String cssClass;
    private String label;
    private String sourceCardinality;
    private String targetCardinality;
    private LineStyle lineStyle;
    private EndpointDecoration sourceDecoration;
    private EndpointDecoration targetDecoration;
    private String color;
    private double labelX, labelY, labelWidth, labelHeight;
    private boolean labelLaidOut;

    public DEdge(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public static DEdge between(DPort source, DPort target) {
        DEdge e = new DEdge("e_" + source.id() + "_" + target.id());
        e.addSource(new DEndpoint.PortEndpoint(source));
        e.addTarget(new DEndpoint.PortEndpoint(target));
        return e;
    }

    public static DEdge between(DNode source, DNode target) {
        DEdge e = new DEdge("e_" + source.id() + "_" + target.id());
        e.addSource(new DEndpoint.NodeEndpoint(source));
        e.addTarget(new DEndpoint.NodeEndpoint(target));
        return e;
    }

    public String id() { return id; }
    public List<DEndpoint> sources() { return sources; }
    public List<DEndpoint> targets() { return targets; }

    public DEdge addSource(DEndpoint e) { sources.add(e); return this; }
    public DEdge addTarget(DEndpoint e) { targets.add(e); return this; }

    public Kind kind() { return kind; }
    public DEdge kind(Kind k) { this.kind = k; return this; }

    public String cssClass() { return cssClass; }
    public DEdge cssClass(String c) { this.cssClass = c; return this; }

    public String label() { return label; }
    public DEdge label(String l) { this.label = l; return this; }

    public String sourceCardinality() { return sourceCardinality; }
    public DEdge sourceCardinality(String c) { this.sourceCardinality = c; return this; }

    public String targetCardinality() { return targetCardinality; }
    public DEdge targetCardinality(String c) { this.targetCardinality = c; return this; }

    /**
     * Optional line style. When non-null, takes precedence over the default
     * line style derived from {@link #kind()} during rendering.
     */
    public LineStyle lineStyle() { return lineStyle; }
    public DEdge lineStyle(LineStyle ls) { this.lineStyle = ls; return this; }

    /**
     * Optional decoration drawn at the source endpoint. When non-null, the
     * renderer draws a {@code marker-start} for the corresponding decoration
     * (skipped for {@link EndpointDecoration#NONE}). When null, the renderer
     * falls back to the marker derived from {@link #kind()}.
     */
    public EndpointDecoration sourceDecoration() { return sourceDecoration; }
    public DEdge sourceDecoration(EndpointDecoration d) { this.sourceDecoration = d; return this; }

    /**
     * Optional decoration drawn at the target endpoint. See
     * {@link #sourceDecoration()} for fallback behavior.
     */
    public EndpointDecoration targetDecoration() { return targetDecoration; }
    public DEdge targetDecoration(EndpointDecoration d) { this.targetDecoration = d; return this; }

    /** Optional CSS colour override for the edge stroke. When non-null,
     *  the renderer applies it as an inline {@code stroke} style on the
     *  path, overriding both the kind-derived colour and the CSS class
     *  default. {@code null} keeps the existing colour selection. */
    public String color() { return color; }
    public DEdge color(String c) { this.color = c; return this; }

    public double labelX() { return labelX; }
    public double labelY() { return labelY; }
    public double labelWidth() { return labelWidth; }
    public double labelHeight() { return labelHeight; }
    public boolean labelLaidOut() { return labelLaidOut; }

    public void setLabelLayout(double x, double y, double w, double h) {
        this.labelX = x;
        this.labelY = y;
        this.labelWidth = w;
        this.labelHeight = h;
        this.labelLaidOut = true;
    }

    public List<Section> sections() { return sections; }

    /**
     * A routed section of the edge. For a simple edge there is usually one
     * section. Hyperedges emit multiple sections that share junction points.
     */
    public static final class Section {
        public double startX, startY, endX, endY;
        public final List<double[]> bendPoints = new ArrayList<>();
        public final List<double[]> junctionPoints = new ArrayList<>();
    }
}
