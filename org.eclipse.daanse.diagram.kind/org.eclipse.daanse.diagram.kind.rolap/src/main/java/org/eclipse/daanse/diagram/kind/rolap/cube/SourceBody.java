package org.eclipse.daanse.diagram.kind.rolap.cube;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

public final class SourceBody
implements NodeBody {
    private static final double TITLE_H = 22.0;
    private static final double KEY_ROW_H = 18.0;
    private static final double PAD_X = 8.0;
    private static final String TITLE_BG = "#374151";
    private final String kindLabel;
    private final List<KeyRow> rows = new ArrayList<KeyRow>();
    private DPort inputPort;
    private final List<DPort> rowEastPorts = new ArrayList<DPort>();
    private final List<DPort> rowWestPorts = new ArrayList<DPort>();

    public SourceBody(String kindLabel) {
        this.kindLabel = kindLabel;
    }

    public SourceBody addRow(String label) {
        this.rows.add(new KeyRow(label));
        return this;
    }

    public DPort inputPort() {
        return this.inputPort;
    }

    public DPort rowEastPort(int i) {
        return this.rowEastPorts.get(i);
    }

    public DPort rowWestPort(int i) {
        return this.rowWestPorts.get(i);
    }

    public double[] sizeHint(DNode node) {
        double w = 160.0;
        for (KeyRow r : this.rows) {
            w = Math.max(w, (double)(r.label.length() * 7 + 40));
        }
        double h = 22.0 + (double)Math.max(this.rows.size(), 1) * 18.0 + 4.0;
        return new double[]{w, h};
    }

    public List<DPort> createPorts(DNode node) {
        ArrayList<DPort> out = new ArrayList<DPort>();
        this.inputPort = new DPort(node.id() + ".in.w", node).side(PortSide.WEST);
        out.add(this.inputPort);
        for (int i = 0; i < this.rows.size(); ++i) {
            DPort east = new DPort(node.id() + ".r" + i + ".e", node).side(PortSide.EAST);
            DPort west = new DPort(node.id() + ".r" + i + ".w", node).side(PortSide.WEST);
            this.rowEastPorts.add(east);
            this.rowWestPorts.add(west);
            out.add(east);
            out.add(west);
        }
        return out;
    }

    public void layoutPorts(DNode node) {
        double w = node.width();
        this.inputPort.position(0.0, 9.0);
        double y = 24.0;
        for (int i = 0; i < this.rows.size(); ++i) {
            double cy = y + 9.0 - 2.0;
            this.rowWestPorts.get(i).position(0.0, cy);
            this.rowEastPorts.get(i).position(w - this.rowEastPorts.get(i).width(), cy);
            y += 18.0;
        }
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").cls("dv-frame").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", 22.0).attr("rx", 4).attr("ry", 4).style("fill:#374151"));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 18.0).attr("width", w).attr("height", 4).style("fill:#374151"));
        g.add(new SvgElem("text").attr("x", 8.0).attr("y", 14).style("font:600 11px sans-serif;fill:#ffffff;letter-spacing:0.5px").text(this.kindLabel));
        double y = 24.0;
        for (KeyRow r : this.rows) {
            g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", 18.0).style("fill:#f3f4f6"));
            g.add(new SvgElem("text").attr("x", 8.0).attr("y", y + 12.0).style("font:400 10px monospace;fill:#1f2937").text(r.label));
            y += 18.0;
        }
    }

    public record KeyRow(String label) {
    }
}

