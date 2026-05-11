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

/** Row composed of a leading {@link IconBadge}, a name and an optional
 *  right-aligned text (e.g. type, multiplicity, default value). */
public final class IconRow implements Row {

    private static final double DEFAULT_HEIGHT = 18.0;
    private static final double LEFT_PADDING = 8.0;
    private static final double ICON_SIZE = 12.0;
    private static final double GAP = 6.0;
    private static final double RIGHT_PADDING = 8.0;

    private final IconBadge badge;
    private final String name;
    private final String rightText;

    public IconRow(IconBadge badge, String name, String rightText) {
        this.badge = badge;
        this.name = name == null ? "" : name;
        this.rightText = rightText;
    }

    @Override
    public double height() {
        return DEFAULT_HEIGHT;
    }

    @Override
    public void render(SvgElem g, double y, double width) {
        double iconY = y + (DEFAULT_HEIGHT - ICON_SIZE) / 2;
        double textY = y + DEFAULT_HEIGHT - 5;
        double nameX = LEFT_PADDING;
        if (badge != null) {
            renderBadge(g, LEFT_PADDING, iconY);
            nameX = LEFT_PADDING + ICON_SIZE + GAP;
        }
        g.add(new SvgElem("text")
                .attr("x", nameX)
                .attr("y", textY)
                .style("font:400 11px sans-serif;fill:#1f2937")
                .text(name));
        if (rightText != null && !rightText.isEmpty()) {
            g.add(new SvgElem("text")
                    .attr("x", width - RIGHT_PADDING)
                    .attr("y", textY)
                    .attr("text-anchor", "end")
                    .style("font:400 10px monospace;fill:#6b7280")
                    .text(rightText));
        }
    }

    private void renderBadge(SvgElem g, double x, double y) {
        SvgElem inner = new SvgElem("g")
                .attr("transform", "translate(" + SvgElem.fmt(x) + "," + SvgElem.fmt(y) + ")");
        if (badge.svgPath() != null) {
            SvgElem path = new SvgElem("path").attr("d", badge.svgPath());
            if (badge.fillColor() != null) path.attr("fill", badge.fillColor());
            if (badge.strokeColor() != null) path.attr("stroke", badge.strokeColor());
            inner.add(path);
        } else if (badge.label() != null) {
            inner.add(new SvgElem("text")
                    .attr("x", ICON_SIZE / 2)
                    .attr("y", ICON_SIZE - 2)
                    .attr("text-anchor", "middle")
                    .style("font:700 10px sans-serif;fill:"
                            + (badge.fillColor() == null ? "#1f2937" : badge.fillColor()))
                    .text(badge.label()));
        }
        if (badge.tooltip() != null) {
            inner.add(new SvgElem("title").text(badge.tooltip()));
        }
        g.add(inner);
    }
}
