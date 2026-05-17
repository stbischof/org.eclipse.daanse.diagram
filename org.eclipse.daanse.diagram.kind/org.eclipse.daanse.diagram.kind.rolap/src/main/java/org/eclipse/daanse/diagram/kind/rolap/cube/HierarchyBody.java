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

public final class HierarchyBody
implements NodeBody {
    public static final double TITLE_H = 28.0;
    public static final double LEVEL_ROW_H = 18.0;
    public static final double PROP_ROW_H = 14.0;
    public static final double SEC_H = 14.0;
    public static final double PAD_X = 10.0;
    public static final double MIN_W = 240.0;
    private static final String TITLE_BG = "#b45309";
    private final String name;
    private final String pkLabel;
    private final boolean parentChild;
    private final List<LevelRow> levelRows = new ArrayList<LevelRow>();
    private DPort queryPort;
    private final Map<String, DPort> propertyPorts = new LinkedHashMap<String, DPort>();

    public HierarchyBody(String name, String pkLabel, boolean parentChild) {
        this.name = name;
        this.pkLabel = pkLabel;
        this.parentChild = parentChild;
    }

    public LevelRow addLevel(String id, String name) {
        LevelRow r = new LevelRow(id, name);
        this.levelRows.add(r);
        return r;
    }

    public DPort queryPort() {
        return this.queryPort;
    }

    public DPort propertyPort(String levelId, String propName) {
        return this.propertyPorts.get(levelId + "." + propName);
    }

    public double[] sizeHint(DNode node) {
        double w = Math.max(240.0, (double)this.name.length() * 7.5 + 60.0);
        for (LevelRow lr : this.levelRows) {
            w = Math.max(w, (double)(lr.name.length() * 7 + 80));
            for (LevelProp p : lr.props) {
                w = Math.max(w, (double)(p.propName + ": " + p.columnName).length() * 6.5 + 60.0);
            }
        }
        double h = 34.0;
        if (!this.levelRows.isEmpty()) {
            h += 14.0;
            for (LevelRow lr : this.levelRows) {
                h += 18.0 + (double)lr.props.size() * 14.0;
            }
        }
        return new double[]{w, h};
    }

    public List<DPort> createPorts(DNode node) {
        ArrayList<DPort> out = new ArrayList<DPort>();
        this.queryPort = new DPort(node.id() + ".query.w", node).side(PortSide.WEST);
        out.add(this.queryPort);
        for (LevelRow lr : this.levelRows) {
            for (LevelProp p : lr.props) {
                String key = lr.id + "." + p.propName;
                DPort port = new DPort(node.id() + "." + key + ".e", node).side(PortSide.EAST);
                this.propertyPorts.put(key, port);
                out.add(port);
            }
        }
        return out;
    }

    public void layoutPorts(DNode node) {
        double w = node.width();
        this.queryPort.position(0.0, 12.0);
        double y = 34.0;
        if (!this.levelRows.isEmpty()) {
            y += 14.0;
            for (LevelRow lr : this.levelRows) {
                y += 18.0;
                for (LevelProp p : lr.props) {
                    DPort port = this.propertyPorts.get(lr.id + "." + p.propName);
                    port.position(w - port.width(), y + 7.0 - 2.0);
                    y += 14.0;
                }
            }
        }
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").cls("dv-frame").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", 28.0).attr("rx", 4).attr("ry", 4).style("fill:#b45309"));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 24.0).attr("width", w).attr("height", 4).style("fill:#b45309"));
        g.add(new SvgElem("text").attr("x", 10.0).attr("y", 11).style("font:600 9px sans-serif;fill:#fde68a;letter-spacing:0.5px").text(this.parentChild ? "PARENT/CHILD HIERARCHY" : "HIERARCHY"));
        g.add(new SvgElem("text").attr("x", 10.0).attr("y", 23).style("font:600 12px sans-serif;fill:#ffffff").text(this.name));
        double y = 30.0;
        if (this.pkLabel != null) {
            g.add(new SvgElem("text").attr("x", 10.0).attr("y", y + 8.0).style("font:400 9px monospace;fill:#6b7280").text("pk: " + this.pkLabel));
        }
        y = 34.0;
        if (!this.levelRows.isEmpty()) {
            g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", 14.0).style("fill:#fef3c7"));
            g.add(new SvgElem("text").attr("x", 10.0).attr("y", y + 10.0).style("font:600 9px sans-serif;fill:#7c2d12;letter-spacing:0.5px").text("LEVELS"));
            y += 14.0;
            for (LevelRow lr : this.levelRows) {
                g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", 18.0).style("fill:#fef9c3"));
                g.add(new SvgElem("text").attr("x", 10.0).attr("y", y + 12.0).style("font:600 11px sans-serif;fill:#92400e").text(lr.name + (lr.uniqueMembers ? "  \u2605" : "")));
                y += 18.0;
                for (LevelProp p : lr.props) {
                    g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", 14.0).style("fill:#fffbeb"));
                    g.add(new SvgElem("text").attr("x", 22.0).attr("y", y + 10.0).style("font:400 10px sans-serif;fill:#374151").text(p.propName));
                    g.add(new SvgElem("text").attr("x", w - 10.0).attr("y", y + 10.0).attr("text-anchor", "end").style("font:400 10px monospace;fill:#6b7280").text(p.columnName));
                    y += 14.0;
                }
            }
        }
    }

    public static final class LevelRow {
        public final String id;
        public final String name;
        public boolean uniqueMembers;
        public final List<LevelProp> props = new ArrayList<LevelProp>();

        LevelRow(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public LevelRow uniqueMembers(boolean v) {
            this.uniqueMembers = v;
            return this;
        }

        public LevelRow addProp(String propName, String columnName) {
            this.props.add(new LevelProp(propName, columnName));
            return this;
        }
    }

    public static final class LevelProp {
        public final String propName;
        public final String columnName;

        LevelProp(String propName, String columnName) {
            this.propName = propName;
            this.columnName = columnName;
        }
    }
}

