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

import org.eclipse.daanse.diagram.core.render.LabelledBoxOptions;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

/**
 * Generic, domain-agnostic node body: a coloured title bar with a
 * "kind" caption and a name, an optional stereotype, and a flat list
 * of row strings rendered below as plain text. Used by every CWM
 * non-relational converter where a full UML-style class compartment
 * (with attributes / operations) would be overkill.
 */
public final class LabelledBoxBody implements NodeBody {

    public static final double TITLE_HEIGHT = 28;
    public static final double ROW_HEIGHT = 16;
    public static final double PAD_X = 10;
    public static final double MIN_WIDTH = 200;

    private final List<String> kindChain;
    private final String name;
    private String stereotype;
    private String titleBg = "#1e3a8a"; // blue-900 default
    private String titleFg = "#ffffff";
    private String rowFg = "#111827";
    private String icon;
    private final List<String> rows = new ArrayList<>();
    private LabelledBoxOptions options = LabelledBoxOptions.defaults();

    public LabelledBoxBody(String kind, String name) {
        this(kind == null ? List.<String>of() : List.of(kind), name);
    }

    public LabelledBoxBody(List<String> kindChain, String name) {
        this.kindChain = kindChain == null ? List.of() : List.copyOf(kindChain);
        this.name = name;
    }

    public LabelledBoxBody stereotype(String s) { this.stereotype = s; return this; }
    public LabelledBoxBody titleBg(String c) { this.titleBg = c; return this; }
    public LabelledBoxBody titleFg(String c) { this.titleFg = c; return this; }
    public LabelledBoxBody rowFg(String c) { this.rowFg = c; return this; }
    public LabelledBoxBody icon(String svgPath) { this.icon = svgPath; return this; }
    public LabelledBoxBody addRow(String text) { rows.add(text); return this; }
    public LabelledBoxBody options(LabelledBoxOptions opts) {
        this.options = opts != null ? opts : LabelledBoxOptions.defaults();
        return this;
    }

    public List<String> rows() { return rows; }
    public String name() { return name; }
    public LabelledBoxOptions options() { return options; }
    public List<String> kindChain() { return kindChain; }

    private String renderedKind() {
        if (kindChain.isEmpty()) return null;
        if (options.showKindAncestors) return String.join(" ▸ ", kindChain);
        return kindChain.get(kindChain.size() - 1);
    }

    @Override
    public double[] sizeHint(DNode node) {
        boolean iconVisible = icon != null;
        double leftPad = iconVisible ? 28 : PAD_X;
        double w = Math.max(MIN_WIDTH, name.length() * 7.5 + 60 + (iconVisible ? 18 : 0));
        if (options.showRows) {
            for (String r : rows) {
                w = Math.max(w, r.length() * 6.5 + PAD_X * 2);
            }
        }
        String kind = renderedKind();
        if (kind != null) {
            w = Math.max(w, kind.length() * 6 + 20 + leftPad);
        }
        int visibleRows = options.showRows ? rows.size() : 0;
        double h = TITLE_HEIGHT + 4 + visibleRows * ROW_HEIGHT;
        if (visibleRows == 0) h += 4;
        return new double[] { w, h };
    }

    @Override
    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        boolean iconVisible = icon != null;
        String kind = renderedKind();
        boolean kindVisible = kind != null;
        boolean stereoVisible = stereotype != null && options.showStereotype;
        double leftPad = iconVisible ? 28 : PAD_X;
        g.add(new SvgElem("rect").cls("dv-frame")
                .attr("x", 0).attr("y", 0).attr("width", w).attr("height", h)
                .attr("rx", 4).attr("ry", 4));
        g.add(new SvgElem("rect")
                .attr("x", 0).attr("y", 0).attr("width", w).attr("height", TITLE_HEIGHT)
                .attr("rx", 4).attr("ry", 4)
                .style("fill:" + titleBg));
        g.add(new SvgElem("rect")
                .attr("x", 0).attr("y", TITLE_HEIGHT - 4).attr("width", w).attr("height", 4)
                .style("fill:" + titleBg));
        if (iconVisible) {
            // Circular badge on the left of the title bar with the SVG glyph.
            g.add(new SvgElem("circle")
                    .attr("cx", 14).attr("cy", TITLE_HEIGHT / 2.0).attr("r", 9)
                    .style("fill:#ffffff;fill-opacity:0.18;stroke:#ffffff;stroke-opacity:0.6;stroke-width:1"));
            g.add(new SvgElem("g")
                    .attr("transform",
                            "translate(" + (14 - 6) + "," + (TITLE_HEIGHT / 2.0 - 6) + ")")
                    .add(new SvgElem("path")
                            .attr("d", icon)
                            .style("fill:" + titleFg + ";stroke:none")));
        }
        if (kindVisible) {
            g.add(new SvgElem("text")
                    .attr("x", leftPad).attr("y", 12)
                    .style("font:600 9px sans-serif;fill:" + titleFg
                            + ";letter-spacing:0.5px;opacity:0.85")
                    .text(kind.toUpperCase()));
        }
        if (stereoVisible) {
            g.add(new SvgElem("text")
                    .attr("x", leftPad).attr("y", 23)
                    .style("font:400 10px sans-serif;fill:" + titleFg + ";opacity:0.7")
                    .text(stereotype));
        }
        g.add(new SvgElem("text")
                .attr("x", leftPad).attr("y", stereoVisible ? 24 : 23)
                .style("font:600 13px sans-serif;fill:" + titleFg)
                .text(name));

        if (options.showRows) {
            double y = TITLE_HEIGHT + 4;
            boolean alt = false;
            for (String r : rows) {
                if (alt) {
                    g.add(new SvgElem("rect")
                            .attr("x", 1).attr("y", y).attr("width", w - 2).attr("height", ROW_HEIGHT)
                            .style("fill:#f9fafb"));
                }
                g.add(new SvgElem("text")
                        .attr("x", PAD_X).attr("y", y + 11)
                        .style("font:400 11px sans-serif;fill:" + rowFg)
                        .text(r));
                y += ROW_HEIGHT;
                alt = !alt;
            }
        }
    }
}
