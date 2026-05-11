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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.diagram.core.svg.SvgElem;

/** Vertical strip on either edge of a node carrying one badge per row.
 *  Each slot is positioned at an absolute y; layout is decided by the
 *  caller (typically by walking the row heights of an adjacent
 *  {@link Compartment}). */
public final class IconGutter {

    private static final double ICON_SIZE = 12.0;

    private final double width;
    private final List<Slot> slots = new ArrayList<>();

    public IconGutter(double width) {
        this.width = width;
    }

    public IconGutter slot(IconBadge badge, double centerY) {
        if (badge != null) {
            slots.add(new Slot(badge, centerY));
        }
        return this;
    }

    public double width() {
        return width;
    }

    public void render(SvgElem g, double xLeftEdge) {
        double iconX = xLeftEdge + (width - ICON_SIZE) / 2;
        for (Slot s : slots) {
            double y = s.centerY - ICON_SIZE / 2;
            SvgElem inner = new SvgElem("g")
                    .attr("transform", "translate(" + SvgElem.fmt(iconX) + "," + SvgElem.fmt(y) + ")");
            IconBadge b = s.badge;
            if (b.svgPath() != null) {
                SvgElem path = new SvgElem("path").attr("d", b.svgPath());
                if (b.fillColor() != null) path.attr("fill", b.fillColor());
                if (b.strokeColor() != null) path.attr("stroke", b.strokeColor());
                inner.add(path);
            } else if (b.label() != null) {
                inner.add(new SvgElem("text")
                        .attr("x", ICON_SIZE / 2)
                        .attr("y", ICON_SIZE - 2)
                        .attr("text-anchor", "middle")
                        .style("font:700 10px sans-serif;fill:"
                                + (b.fillColor() == null ? "#1f2937" : b.fillColor()))
                        .text(b.label()));
            }
            if (b.tooltip() != null) {
                inner.add(new SvgElem("title").text(b.tooltip()));
            }
            g.add(inner);
        }
    }

    private record Slot(IconBadge badge, double centerY) {}
}
