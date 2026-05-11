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

/** Plain "name : type" row: a single line of text laid out left-aligned
 *  with a default 16px row height and 8px left padding. */
public final class TextRow implements Row {

    private static final double DEFAULT_HEIGHT = 16.0;
    private static final double LEFT_PADDING = 8.0;

    private final String text;
    private final String style;

    public TextRow(String text) {
        this(text, "font:400 11px sans-serif;fill:#1f2937");
    }

    public TextRow(String text, String style) {
        this.text = text == null ? "" : text;
        this.style = style;
    }

    @Override
    public double height() {
        return DEFAULT_HEIGHT;
    }

    @Override
    public void render(SvgElem g, double y, double width) {
        g.add(new SvgElem("text")
                .attr("x", LEFT_PADDING)
                .attr("y", y + DEFAULT_HEIGHT - 4)
                .style(style)
                .text(text));
    }
}
