package org.eclipse.daanse.diagram.kind.rolap.cube;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

public final class DimensionBody
implements NodeBody {
    private final String name;
    private final String fkLabel;
    private final boolean time;

    public DimensionBody(String name, String fkLabel, boolean time) {
        this.name = name;
        this.fkLabel = fkLabel;
        this.time = time;
    }

    public double[] sizeHint(DNode node) {
        double w = Math.max(160, this.name.length() * 7 + 40);
        if (this.fkLabel != null) {
            w = Math.max(w, (double)(this.fkLabel.length() * 6 + 30));
        }
        return new double[]{w, this.fkLabel != null ? 38.0 : 28.0};
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        String accent = this.time ? "#7c2d12" : "#5b21b6";
        g.add(new SvgElem("rect").cls("dv-frame").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4).style("fill:#ffffff;stroke:" + accent + ";stroke-width:1.4"));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", 12).attr("rx", 4).attr("ry", 4).style("fill:" + accent));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 8).attr("width", w).attr("height", 4).style("fill:" + accent));
        g.add(new SvgElem("text").attr("x", 8).attr("y", 9).style("font:600 8px sans-serif;fill:#ffffff;letter-spacing:0.5px").text(this.time ? "TIME DIMENSION" : "DIMENSION"));
        g.add(new SvgElem("text").attr("x", 8).attr("y", 24).style("font:600 12px sans-serif;fill:#111827").text(this.name));
        if (this.fkLabel != null) {
            g.add(new SvgElem("text").attr("x", 8).attr("y", 35).style("font:400 9px monospace;fill:#6b7280").text("fk: " + this.fkLabel));
        }
    }
}

