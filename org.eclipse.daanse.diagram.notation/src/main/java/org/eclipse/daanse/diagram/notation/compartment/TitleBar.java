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

/** Header band at the top of a {@link CompartmentBody}: stereotype line
 *  (optional) above the title, on a tinted background. */
public final class TitleBar {

    private static final double STEREOTYPE_HEIGHT = 12.0;
    private static final double TITLE_HEIGHT = 18.0;

    private final String title;
    private final String stereotype;
    private final String backgroundColor;
    private final String titleColor;

    public TitleBar(String title, String stereotype,
                    String backgroundColor, String titleColor) {
        this.title = title == null ? "" : title;
        this.stereotype = stereotype;
        this.backgroundColor = backgroundColor;
        this.titleColor = titleColor;
    }

    public double height() {
        return (stereotype != null ? STEREOTYPE_HEIGHT : 0) + TITLE_HEIGHT;
    }

    public void render(SvgElem g, double y, double width) {
        double h = height();
        if (backgroundColor != null) {
            g.add(new SvgElem("rect")
                    .attr("x", 0).attr("y", y)
                    .attr("width", width).attr("height", h)
                    .attr("fill", backgroundColor));
        }
        double cursor = y;
        if (stereotype != null) {
            g.add(new SvgElem("text")
                    .attr("x", width / 2)
                    .attr("y", cursor + STEREOTYPE_HEIGHT - 2)
                    .attr("text-anchor", "middle")
                    .style("font:600 9px sans-serif;letter-spacing:0.08em;fill:#4b5563")
                    .text("«" + stereotype + "»"));
            cursor += STEREOTYPE_HEIGHT;
        }
        g.add(new SvgElem("text")
                .attr("x", width / 2)
                .attr("y", cursor + TITLE_HEIGHT - 4)
                .attr("text-anchor", "middle")
                .style("font:700 13px sans-serif;fill:"
                        + (titleColor == null ? "#111827" : titleColor))
                .text(title));
    }
}
