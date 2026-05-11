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

import org.eclipse.daanse.diagram.core.render.SchemaContainerOptions;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.text.TextMetrics;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

/**
 * Container node holding child nodes (tables in a schema, machines in a
 * site, record-defs in a file, ...). The header is a solid tab on the
 * left of the title bar with an icon badge, an uppercase kind caption,
 * and the title underneath. The body itself is a dashed-bordered frame
 * inside which child nodes are rendered.
 */
public final class SchemaBody implements NodeBody {

    public static final double TITLE_BAR = 32;

    private final String title;
    private final String kind;
    private final String icon;
    private SchemaContainerOptions options = SchemaContainerOptions.defaults();

    public SchemaBody(String title) {
        this(title, "SCHEMA", Icons.SCHEMA);
    }

    public SchemaBody(String title, String kind, String icon) {
        this.title = title;
        this.kind = kind;
        this.icon = icon;
    }

    public SchemaBody options(SchemaContainerOptions opts) {
        this.options = opts != null ? opts : SchemaContainerOptions.defaults();
        return this;
    }

    public SchemaContainerOptions options() { return options; }

    public String kind() { return kind; }
    public String title() { return title; }

    @Override
    public double[] sizeHint(DNode node) {
        return new double[] { 200, TITLE_BAR + 40 };
    }

    @Override
    public String elkPadding() {
        return "[top=" + (int) (TITLE_BAR + 14) + ",left=16,bottom=16,right=16]";
    }

    @Override
    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        boolean iconVisible = icon != null;
        boolean kindVisible = kind != null;
        boolean titleVisible = title != null;

        if (options.showFrame) {
            g.add(new SvgElem("rect")
                    .attr("x", 0).attr("y", 0).attr("width", w).attr("height", h)
                    .attr("rx", 6).attr("ry", 6)
                    .style("fill:" + Palette.OFF_WHITE
                            + ";stroke:" + Palette.GRAY_400
                            + ";stroke-width:1;stroke-dasharray:5 3"));
        }

        if (options.showHeaderTab) {
            double tabWidth = Math.min(220, Math.max(
                    titleVisible ? TextMetrics.title(title) + 60 : 0,
                    kindVisible ? TextMetrics.row(kind) + 50 : 0));
            if (tabWidth < 60) tabWidth = 60;
            g.add(new SvgElem("rect")
                    .attr("x", 0).attr("y", 0)
                    .attr("width", tabWidth).attr("height", TITLE_BAR)
                    .attr("rx", 6).attr("ry", 6)
                    .style("fill:" + Palette.GRAY_800 + ";stroke:none"));

            if (iconVisible) {
                g.add(new SvgElem("circle")
                        .attr("cx", 16).attr("cy", TITLE_BAR / 2.0).attr("r", 10)
                        .style("fill:" + Palette.WHITE + ";fill-opacity:0.18;"
                                + "stroke:" + Palette.WHITE + ";stroke-opacity:0.6;stroke-width:1"));
                g.add(new SvgElem("g")
                        .attr("transform", "translate("
                                + (16 - 6) + "," + (TITLE_BAR / 2.0 - 6) + ")")
                        .add(new SvgElem("path")
                                .attr("d", icon)
                                .style("fill:" + Palette.WHITE + ";stroke:none")));
            }

            double textX = iconVisible ? 32 : 10;
            if (kindVisible) {
                g.add(new SvgElem("text")
                        .attr("x", textX).attr("y", 13)
                        .style("font:600 9px sans-serif;fill:" + Palette.WHITE
                                + ";letter-spacing:0.5px;opacity:0.85")
                        .text(kind));
            }
            if (titleVisible) {
                g.add(new SvgElem("text")
                        .attr("x", textX).attr("y", kindVisible ? 25 : 20)
                        .style("font:600 13px sans-serif;fill:" + Palette.GRAY_50)
                        .text(title));
            }
        }
    }
}
