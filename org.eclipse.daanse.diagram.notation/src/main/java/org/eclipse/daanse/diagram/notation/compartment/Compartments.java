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
package org.eclipse.daanse.diagram.notation.compartment;

import org.eclipse.daanse.diagram.core.svg.SvgElem;

/**
 * Stateless rendering helpers shared by every body that draws a stack of
 * sub-compartments — currently {@code TableBody} (CWM Relational) and
 * {@code EClassBody} (Ecore). Each independently encoded the same
 * "header strip + tinted row + thin bottom divider" pattern; this helper
 * collapses both onto one implementation, parameterised by row height
 * (TableBody uses 20 px rows, EClassBody uses 18 px).
 */
public final class Compartments {

    private Compartments() {}

    /**
     * Header strip for a sub-compartment: a coloured background spanning
     * the body width with a small uppercase caption pinned to the left.
     * Convenience overload using the common 9-px caption.
     */
    public static void renderSectionHeader(SvgElem g, String label, double y,
                                             double w, double height, String bg) {
        renderSectionHeader(g, label, y, w, height, bg, 9, 10);
    }

    /** Header strip with explicit caption font size and left-padding so
     *  individual bodies can keep their existing typography. */
    public static void renderSectionHeader(SvgElem g, String label, double y,
                                             double w, double height, String bg,
                                             double captionFontPx, double captionX) {
        g.add(new SvgElem("rect")
                .attr("x", 1).attr("y", y)
                .attr("width", w - 2).attr("height", height)
                .style("fill:" + bg));
        g.add(new SvgElem("text").attr("x", captionX).attr("y", y + height - 5)
                .style("font:600 " + captionFontPx + "px sans-serif;"
                        + "fill:#374151;letter-spacing:0.5px")
                .text(label.toUpperCase()));
    }

    /**
     * Row background plus the thin separator line at the row's bottom edge.
     * The divider stroke colour is typically the matching header colour so
     * a stack of rows reads as one section.
     */
    public static void renderRowBackground(SvgElem g, double y, double w,
                                             double rowHeight, String fill,
                                             String divider) {
        g.add(new SvgElem("rect")
                .attr("x", 1).attr("y", y)
                .attr("width", w - 2).attr("height", rowHeight)
                .style("fill:" + fill));
        g.add(new SvgElem("line")
                .attr("x1", 1).attr("x2", w - 1)
                .attr("y1", y + rowHeight).attr("y2", y + rowHeight)
                .style("stroke:" + divider + ";stroke-width:1"));
    }
}
