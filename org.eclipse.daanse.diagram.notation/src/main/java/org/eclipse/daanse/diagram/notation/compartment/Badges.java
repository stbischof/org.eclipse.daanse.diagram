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
 * Tiny single-letter pills used in row gutters: the {@code D} default-value
 * marker, the {@code T} (transient), {@code V} (volatile), {@code /}
 * (derived) flags on Ecore feature rows, and the {@code D} marker on the
 * CWM Relational defaults section. Same 9×9 rounded rect with white-on-fill
 * letter; the only differences are colour and the tooltip text.
 */
public final class Badges {

    private Badges() {}

    /**
     * 9×9 rounded rect with the given letter centred in white, plus a
     * {@code <title>} tooltip and an {@code aria-label} for accessibility.
     * The badge group is positioned at {@code (x, y)} via a transform.
     */
    public static SvgElem letter(double x, double y, String letter,
                                   String fillColor, String tooltip) {
        SvgElem g = new SvgElem("g")
                .attr("transform", "translate(" + SvgElem.fmt(x) + "," + SvgElem.fmt(y) + ")")
                .attr("aria-label", tooltip);
        g.add(new SvgElem("rect").attr("x", 0.5).attr("y", 0.5)
                .attr("width", 9).attr("height", 9)
                .attr("rx", 1.5).attr("ry", 1.5)
                .style("fill:" + fillColor + ";stroke:#374151;stroke-width:0.7"));
        g.add(new SvgElem("text").attr("x", 5).attr("y", 7.5)
                .attr("text-anchor", "middle")
                .style("font:700 7px sans-serif;fill:#fff").text(letter));
        if (tooltip != null) {
            g.add(new SvgElem("title").text(tooltip));
        }
        return g;
    }

    /**
     * Convenience for the {@code D} default-value pill — same shape every
     * body uses, with the value spelled out in the tooltip.
     */
    public static SvgElem defaultValue(double x, double y, String value) {
        return letter(x, y, "D", "#6b7280", "DEFAULT " + value);
    }
}
