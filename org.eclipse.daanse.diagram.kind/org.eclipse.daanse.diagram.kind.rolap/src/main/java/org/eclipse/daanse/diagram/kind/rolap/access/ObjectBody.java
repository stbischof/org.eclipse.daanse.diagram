package org.eclipse.daanse.diagram.kind.rolap.access;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

public final class ObjectBody
implements NodeBody {
    public static final double ROW_HEIGHT = 24.0;
    private final String kindLabel;
    private final String name;
    private final String headerBg;
    private final String accent;

    public ObjectBody(String kindLabel, String name, String headerBg, String accent) {
        this.kindLabel = kindLabel;
        this.name = name;
        this.headerBg = headerBg;
        this.accent = accent;
    }

    public double[] sizeHint(DNode node) {
        double w = Math.max(140.0, (double)this.name.length() * 7.5 + 36.0);
        double h = 40.0;
        return new double[]{w, h};
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").cls("dv-frame").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 6).attr("ry", 6).style("fill:#ffffff;stroke:" + this.accent + ";stroke-width:1.4"));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", 14).attr("rx", 6).attr("ry", 6).style("fill:" + this.headerBg));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 8).attr("width", w).attr("height", 6).style("fill:" + this.headerBg));
        g.add(new SvgElem("text").attr("x", 8).attr("y", 11).style("font:600 9px sans-serif;fill:#ffffff;letter-spacing:0.5px").text(this.kindLabel));
        g.add(new SvgElem("text").attr("x", 8).attr("y", 30).style("font:600 12px sans-serif;fill:#111827").text(this.name));
    }
}

