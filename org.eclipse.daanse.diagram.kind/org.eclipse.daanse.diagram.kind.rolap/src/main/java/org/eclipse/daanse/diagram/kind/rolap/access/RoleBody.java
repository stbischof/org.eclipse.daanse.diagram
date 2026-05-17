package org.eclipse.daanse.diagram.kind.rolap.access;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.daanse.diagram.kind.rolap.access.AccessColors;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

public final class RoleBody
implements NodeBody {
    public static final double ROW_HEIGHT = 22.0;
    public static final double SUB_ROW_HEIGHT = 18.0;
    public static final double TITLE_HEIGHT = 30.0;
    public static final double SECTION_HEADER_HEIGHT = 16.0;
    public static final double PAD_X = 10.0;
    public static final double MIN_WIDTH = 280.0;
    private static final String STYLE_ROW_NAME = "font:500 11px sans-serif;fill:#111827";
    private static final String STYLE_META = "font:400 10px monospace;fill:#6b7280";
    private static final String TITLE_BG = "#5b21b6";
    private final String name;
    private final List<Row> rows = new ArrayList<Row>();
    private final Map<Row, DPort> ports = new LinkedHashMap<Row, DPort>();

    public RoleBody(String name) {
        this.name = name;
    }

    public Row addRow(GrantKind kind, String label, String access) {
        Row r = new Row(kind, label, access, false);
        this.rows.add(r);
        return r;
    }

    public Row addMemberRow(String label, String access) {
        Row r = new Row(GrantKind.MEMBER, label, access, true);
        this.rows.add(r);
        return r;
    }

    public List<Row> rows() {
        return this.rows;
    }

    public DPort port(Row row) {
        DPort p = this.ports.get(row);
        if (p == null) {
            throw new IllegalStateException("no port for row " + row.label);
        }
        return p;
    }

    public double[] sizeHint(DNode node) {
        double w = Math.max(280.0, this.textWidth(this.name) + 80.0);
        for (Row r : this.rows) {
            w = Math.max(w, this.textWidth(r.displayLine()) + 80.0);
        }
        double h = 30.0;
        boolean inSection = false;
        GrantKind currentSection = null;
        for (Row r : this.rows) {
            if (!r.subRow && r.kind != currentSection) {
                h += 16.0;
                currentSection = r.kind;
                inSection = true;
            }
            h += r.subRow ? 18.0 : 22.0;
        }
        if (!inSection) {
            h += 8.0;
        }
        return new double[]{w, h};
    }

    public List<DPort> createPorts(DNode node) {
        ArrayList<DPort> out = new ArrayList<DPort>();
        int i = 0;
        for (Row r : this.rows) {
            DPort p = new DPort(node.id() + ".row" + i + ".e", node).side(PortSide.EAST);
            this.ports.put(r, p);
            out.add(p);
            ++i;
        }
        return out;
    }

    public void layoutPorts(DNode node) {
        double w = node.width();
        double y = 30.0;
        GrantKind currentSection = null;
        for (Row r : this.rows) {
            if (!r.subRow && r.kind != currentSection) {
                y += 16.0;
                currentSection = r.kind;
            }
            double rh = r.subRow ? 18.0 : 22.0;
            DPort p = this.ports.get(r);
            p.position(w - p.width(), y + rh / 2.0 - 2.0);
            y += rh;
        }
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").cls("dv-frame").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4));
        String clipId = "dv-clip-" + RoleBody.sanitize(node.id());
        g.add(new SvgElem("defs").add(new SvgElem("clipPath").attr("id", clipId).add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4))));
        SvgElem inner = new SvgElem("g").attr("clip-path", "url(#" + clipId + ")");
        g.add(inner);
        g = inner;
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", 30.0).attr("rx", 4).attr("ry", 4).style("fill:#5b21b6"));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 26.0).attr("width", w).attr("height", 4).style("fill:#5b21b6"));
        g.add(new SvgElem("text").attr("x", 10.0).attr("y", 12).style("font:400 9px sans-serif;fill:#ddd6fe;letter-spacing:0.5px").text("ACCESS ROLE"));
        g.add(new SvgElem("text").cls("dv-title").attr("x", 10.0).attr("y", 24).style("font:600 13px sans-serif;fill:#ffffff").text(this.name));
        double y = 30.0;
        GrantKind currentSection = null;
        for (Row r : this.rows) {
            if (!r.subRow && r.kind != currentSection) {
                this.renderSectionHeader(g, RoleBody.sectionLabel(r.kind), y, w);
                y += 16.0;
                currentSection = r.kind;
            }
            double rh = r.subRow ? 18.0 : 22.0;
            this.renderRow(g, r, y, w, rh);
            y += rh;
        }
    }

    private void renderSectionHeader(SvgElem g, String label, double y, double w) {
        g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", 16.0).style("fill:#ede9fe"));
        g.add(new SvgElem("text").attr("x", 10.0).attr("y", y + 11.0).style("font:600 10px sans-serif;fill:#5b21b6;letter-spacing:0.5px").text(label));
    }

    private void renderRow(SvgElem g, Row r, double y, double w, double rh) {
        double textX;
        g.add(new SvgElem("rect").attr("x", 1).attr("y", y).attr("width", w - 2.0).attr("height", rh).style("fill:" + (r.subRow ? "#faf5ff" : "#f5f3ff")));
        g.add(new SvgElem("line").attr("x1", 1).attr("x2", w - 1.0).attr("y1", y + rh).attr("y2", y + rh).style("stroke:#ddd6fe;stroke-width:0.8"));
        double d = textX = r.subRow ? 28.0 : 14.0;
        if (r.subRow) {
            g.add(new SvgElem("text").attr("x", 16.0).attr("y", y + rh / 2.0 + 3.0).style("font:700 10px sans-serif;fill:#7c3aed").text("\u2022"));
        }
        g.add(new SvgElem("text").attr("x", textX).attr("y", y + rh / 2.0 + 3.0).style(r.subRow ? STYLE_META : STYLE_ROW_NAME).text(r.label));
        if (r.access != null && !r.access.isEmpty()) {
            this.renderAccessPill(g, r.access, w - 10.0, y + rh / 2.0);
        }
    }

    private void renderAccessPill(SvgElem g, String access, double rightX, double cy) {
        String bg = AccessColors.bg(access);
        String fg = AccessColors.fg(access);
        String label = access.toUpperCase();
        double pillW = (double)label.length() * 6.2 + 12.0;
        double pillH = 14.0;
        double x = rightX - pillW;
        double y = cy - pillH / 2.0;
        g.add(new SvgElem("rect").attr("x", x).attr("y", y).attr("width", pillW).attr("height", pillH).attr("rx", pillH / 2.0).attr("ry", pillH / 2.0).style("fill:" + bg));
        g.add(new SvgElem("text").attr("x", x + pillW / 2.0).attr("y", y + 10.0).attr("text-anchor", "middle").style("font:700 9px sans-serif;letter-spacing:0.5px;fill:" + fg).text(label));
    }

    private static String sectionLabel(GrantKind kind) {
        return switch (kind.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "CATALOG GRANTS";
            case 1 -> "CUBE GRANTS";
            case 2 -> "HIERARCHY GRANTS";
            case 3 -> "MEMBER GRANTS";
            case 4 -> "DATABASE SCHEMA GRANTS";
        };
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }

    private double textWidth(String s) {
        return s == null ? 0.0 : (double)s.length() * 6.5;
    }

    public static final class Row {
        public final GrantKind kind;
        public final String label;
        public final String access;
        public final boolean subRow;
        public String targetId;

        Row(GrantKind kind, String label, String access, boolean subRow) {
            this.kind = kind;
            this.label = label;
            this.access = access;
            this.subRow = subRow;
        }

        public Row target(String id) {
            this.targetId = id;
            return this;
        }

        String displayLine() {
            return this.label + (String)(this.access != null ? "  " + this.access : "");
        }
    }

    public static enum GrantKind {
        CATALOG,
        CUBE,
        HIERARCHY,
        MEMBER,
        SCHEMA;

    }
}

