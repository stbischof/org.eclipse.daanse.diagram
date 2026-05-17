package org.eclipse.daanse.diagram.kind.rolap.cube;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

public final class CubeBody
implements NodeBody {
    private static final double TITLE_H = 28.0;
    private static final double ROW_H = 18.0;
    private static final double SEC_H = 14.0;
    private static final double PAD_X = 10.0;
    private static final double MIN_W = 200.0;
    private static final String TITLE_BG = "#1e3a8a";
    private static final String M_ROW_BG = "#eff6ff";
    private static final String M_HEAD_BG = "#dbeafe";
    private final String name;
    private final boolean virtual;
    private final List<String> measures = new ArrayList<String>();
    private DPort queryPort;
    private final Map<Integer, DPort> measureColumnPorts = new LinkedHashMap<Integer, DPort>();

    public CubeBody(String name, boolean virtual) {
        this.name = name;
        this.virtual = virtual;
    }

    public CubeBody addMeasure(String label) {
        this.measures.add(label);
        return this;
    }

    public DPort queryPort() {
        return this.queryPort;
    }

    public DPort measurePort(int index) {
        return this.measureColumnPorts.get(index);
    }

    public double[] sizeHint(DNode node) {
        double w = Math.max(200.0, (double)this.name.length() * 7.5 + 40.0);
        for (String m : this.measures) {
            w = Math.max(w, (double)m.length() * 6.5 + 20.0 + 24.0);
        }
        double h = 28.0;
        if (!this.measures.isEmpty()) {
            h += 14.0 + (double)this.measures.size() * 18.0;
        }
        return new double[]{w, h};
    }

    public List<DPort> createPorts(DNode node) {
        ArrayList<DPort> out = new ArrayList<DPort>();
        this.queryPort = new DPort(node.id() + ".query.e", node).side(PortSide.EAST);
        out.add(this.queryPort);
        for (int i = 0; i < this.measures.size(); ++i) {
            DPort p = new DPort(node.id() + ".m" + i + ".e", node).side(PortSide.EAST);
            this.measureColumnPorts.put(i, p);
            out.add(p);
        }
        return out;
    }

    public void layoutPorts(DNode node) {
        double w = node.width();
        this.queryPort.position(w - this.queryPort.width(), 12.0);
        double y = 42.0;
        for (int i = 0; i < this.measures.size(); ++i) {
            DPort p = this.measureColumnPorts.get(i);
            p.position(w - p.width(), y + 9.0 - 2.0);
            y += 18.0;
        }
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").cls("dv-frame").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", 28.0).attr("rx", 4).attr("ry", 4).style("fill:#1e3a8a"));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 24.0).attr("width", w).attr("height", 4).style("fill:#1e3a8a"));
        g.add(new SvgElem("text").attr("x", 10.0).attr("y", 12).style("font:600 9px sans-serif;fill:#bfdbfe;letter-spacing:0.5px").text(this.virtual ? "VIRTUAL CUBE" : "CUBE"));
        g.add(new SvgElem("text").attr("x", 10.0).attr("y", 23).style("font:600 13px sans-serif;fill:#ffffff").text(this.name));
        if (!this.measures.isEmpty()) {
            double y = 28.0;
            g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", 14.0).style("fill:#dbeafe"));
            g.add(new SvgElem("text").attr("x", 10.0).attr("y", y + 10.0).style("font:600 9px sans-serif;fill:#1e3a8a;letter-spacing:0.5px").text("MEASURES"));
            y += 14.0;
            for (String m : this.measures) {
                g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", 18.0).style("fill:#eff6ff"));
                g.add(new SvgElem("text").attr("x", 10.0).attr("y", y + 12.0).style("font:500 11px sans-serif;fill:#111827").text(m));
                y += 18.0;
            }
        }
    }
}

