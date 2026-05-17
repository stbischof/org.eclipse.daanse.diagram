/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*/
package org.eclipse.daanse.diagram.kind.ecore;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

public final class EPackageBody implements NodeBody {

    /** Tab height with both nsPrefix and nsURI rendered. */
    public static final double TITLE_BAR = 42.0;

    private final String name;
    private final String nsPrefix;
    private final String nsURI;

    public EPackageBody(String name) {
        this(name, null, null);
    }

    public EPackageBody(String name, String nsPrefix) {
        this(name, nsPrefix, null);
    }

    public EPackageBody(String name, String nsPrefix, String nsURI) {
        this.name = name;
        this.nsPrefix = nsPrefix;
        this.nsURI = nsURI;
    }

    public double[] sizeHint(DNode node) {
        return new double[] { 220.0, TITLE_BAR + 40.0 };
    }

    public String elkPadding() {
        return "[top=" + (TITLE_BAR + 16) + ",left=22,bottom=22,right=22]";
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0)
                .attr("width", w).attr("height", h)
                .attr("rx", 6).attr("ry", 6)
                .style("fill:#fafafa;stroke:#9ca3af;stroke-width:1;stroke-dasharray:5 3"));
        // Tab grows to fit the longest of name / nsPrefix / nsURI line so
        // the URI doesn't poke beyond the tab outline on most schemas.
        double widest = (double) this.name.length() * 7.5 + 36.0;
        if (this.nsURI != null) {
            widest = Math.max(widest, (double) this.nsURI.length() * 5.5 + 36.0);
        }
        double tabW = Math.min(w, Math.max(180.0, widest));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0)
                .attr("width", tabW).attr("height", TITLE_BAR)
                .attr("rx", 6).attr("ry", 6)
                .style("fill:#ddd6fe;stroke:#7c3aed;stroke-width:1"));
        // Three-line tab: kind caption / package name / nsURI.
        g.add(new SvgElem("text").attr("x", 10).attr("y", 12)
                .style("font:400 10px sans-serif;fill:#4c1d95;letter-spacing:0.06em")
                .text("Package"));
        g.add(new SvgElem("text").attr("x", 10).attr("y", 25)
                .style("font:600 12px sans-serif;fill:#4c1d95").text(this.name));
        if (this.nsURI != null) {
            g.add(new SvgElem("text").attr("x", 10).attr("y", 38)
                    .style("font:400 9px monospace;fill:#6b21a8")
                    .text(this.nsURI));
        }
    }
}
