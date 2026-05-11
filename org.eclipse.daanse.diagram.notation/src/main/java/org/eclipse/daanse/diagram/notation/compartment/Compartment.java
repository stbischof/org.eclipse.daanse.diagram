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

/** A horizontal section within a {@link CompartmentBody}: optional header
 *  label with a tinted background, followed by a stack of rows on a row
 *  background. Width is supplied at render time; height is derived from
 *  the contained rows. */
public final class Compartment {

    private static final double HEADER_HEIGHT = 14.0;
    private static final double LEFT_PADDING = 8.0;

    private final String headerLabel;
    private final String headerBg;
    private final String rowBg;
    private final List<Row> rows = new ArrayList<>();

    public Compartment(String headerLabel, String headerBg, String rowBg) {
        this.headerLabel = headerLabel;
        this.headerBg = headerBg;
        this.rowBg = rowBg;
    }

    public Compartment add(Row r) {
        rows.add(r);
        return this;
    }

    public List<Row> rows() {
        return rows;
    }

    /** Header height contributed by this compartment (zero when unlabelled). */
    public double headerHeight() {
        return headerLabel != null ? HEADER_HEIGHT : 0;
    }

    /** Total height: header (when labelled) + sum of row heights. */
    public double height() {
        double rowsHeight = 0;
        for (Row r : rows) rowsHeight += r.height();
        return headerHeight() + rowsHeight;
    }

    /** Render the compartment into {@code g} starting at {@code y} with the
     *  given total {@code width}. Returns the y-coordinate just below this
     *  compartment so the caller can stack the next one. */
    public double render(SvgElem g, double y, double width) {
        double cursor = y;
        if (headerLabel != null) {
            if (headerBg != null) {
                g.add(new SvgElem("rect")
                        .attr("x", 0).attr("y", cursor)
                        .attr("width", width).attr("height", HEADER_HEIGHT)
                        .attr("fill", headerBg));
            }
            g.add(new SvgElem("text")
                    .attr("x", LEFT_PADDING)
                    .attr("y", cursor + HEADER_HEIGHT - 3)
                    .style("font:600 9px sans-serif;letter-spacing:0.06em;fill:#374151")
                    .text(headerLabel));
            cursor += HEADER_HEIGHT;
        }
        if (rowBg != null && !rows.isEmpty()) {
            double rowsTotal = 0;
            for (Row r : rows) rowsTotal += r.height();
            g.add(new SvgElem("rect")
                    .attr("x", 0).attr("y", cursor)
                    .attr("width", width).attr("height", rowsTotal)
                    .attr("fill", rowBg));
        }
        for (Row r : rows) {
            r.render(g, cursor, width);
            cursor += r.height();
        }
        return cursor;
    }
}
