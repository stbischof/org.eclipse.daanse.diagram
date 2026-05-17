package org.eclipse.daanse.diagram.kind.ecore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.diagram.notation.compartment.Badges;
import org.eclipse.daanse.diagram.notation.compartment.Compartments;
import org.eclipse.daanse.diagram.notation.compartment.SectionConnectors;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

public final class EClassBody
implements NodeBody {
    public static final double ROW_HEIGHT = 18.0;
    public static final double SECTION_HEADER_HEIGHT = 16.0;
    private static final String ATTR_ROW_BG = "#eff6ff";
    private static final String ATTR_HEAD_BG = "#dbeafe";
    private static final String REF_ROW_BG = "#f0fdf4";
    private static final String REF_HEAD_BG = "#bbf7d0";
    private static final String OP_ROW_BG = "#fef3c7";
    private static final String OP_HEAD_BG = "#fde68a";
    private static final String LIT_ROW_BG = "#faf5ff";
    private static final String LIT_HEAD_BG = "#e9d5ff";
    /** Pastel pair for the DEFAULTS section, matching the CWM Relational
     *  table's defaults section so the eye reads them as the same concept. */
    private static final String DEF_ROW_BG = "#f9fafb";
    private static final String DEF_HEAD_BG = "#f3f4f6";
    /** Right-side gutter reserved for the {@code D} default-value badge —
     *  attribute and reference type text are always laid out clear of it. */
    private static final double DEF_COL_WIDTH = 16.0;
    /** Width of each one-letter flag column (derived / transient / volatile). */
    private static final double FLAG_COL_WIDTH = 14.0;
    /** Width of the dedicated lower..upper bounds column to the right of
     *  type, sized for {@code 0..*} / {@code 1..1} text. */
    private static final double BOUNDS_COL_WIDTH = 38.0;
    /** Connector colour used in the defaults gutter. Same gray-500 as the
     *  table-default accent. */
    private static final String DEF_CONNECTOR_COLOR = "#6b7280";
    /** Horizontal spacing between adjacent connector lanes in the right
     *  gutter — same {@code 5}px pitch the CWM relational table uses for
     *  index / unique / check / default lanes. */
    private static final double DEF_LANE_SPACING = 5.0;
    private static final String STYLE_META = "font:400 10px monospace;fill:#6b7280";
    private static final String STYLE_ROW_NAME = "font:400 11px sans-serif";
    private static final String CLASS_TITLE = "#1e3a8a";
    /** Slate, deliberately muted relative to the saturated class blue, so
     *  the abstract-vs-concrete distinction reads at a glance even when
     *  the italic name styling and the {@code abstract class} stereotype
     *  are missed. */
    private static final String ABS_TITLE = "#475569";
    private static final String INTERFACE_TITLE = "#5b21b6";
    private static final String DATATYPE_TITLE = "#14532d";
    private static final String ENUM_TITLE = "#7c2d12";
    private static final String ANNOT_TITLE = "#374151";
    private final String name;
    private Kind kind = Kind.CLASS;
    private String instanceClassName;
    private final List<Attribute> attributes = new ArrayList<Attribute>();
    private final List<Reference> references = new ArrayList<Reference>();
    private final List<Operation> operations = new ArrayList<Operation>();
    private final List<Literal> literals = new ArrayList<Literal>();
    private final Map<String, DPort[]> refPorts = new LinkedHashMap<String, DPort[]>();
    private DPort classPortWest;
    private DPort classPortEast;
    private DPort classPortNorth;

    public EClassBody(String name) {
        this.name = name;
    }

    public EClassBody kind(Kind k) {
        this.kind = k;
        return this;
    }

    public EClassBody instanceClassName(String s) {
        this.instanceClassName = s;
        return this;
    }

    public EClassBody addAttribute(Attribute a) {
        this.attributes.add(a);
        return this;
    }

    public EClassBody addReference(Reference r) {
        this.references.add(r);
        return this;
    }

    public EClassBody addOperation(Operation o) {
        this.operations.add(o);
        return this;
    }

    public EClassBody addLiteral(Literal l) {
        this.literals.add(l);
        return this;
    }

    public String name() {
        return this.name;
    }

    public Kind kind() {
        return this.kind;
    }

    public DPort refPort(String name, PortSide side) {
        DPort[] pair = this.refPorts.get(name);
        if (pair == null) {
            throw new IllegalArgumentException("No such reference: " + name);
        }
        return side == PortSide.WEST ? pair[0] : pair[1];
    }

    /** True iff this body has a reference row registered under the given
     *  name. Useful when wiring bidirectional edges that need to look up
     *  the opposite side's feature-row port without throwing. */
    public boolean hasRefPort(String name) {
        return this.refPorts.containsKey(name);
    }

    public DPort classPort(PortSide side) {
        return switch (side) {
            case NORTH -> this.classPortNorth;
            case WEST -> this.classPortWest;
            case EAST -> this.classPortEast;
            default -> this.classPortEast;
        };
    }

    /** Extra clearance above each class so the inheritance edge routed
     *  into the {@code NORTH} port has a long perpendicular run before the
     *  arrow marker — the marker covers ~7 px back from the endpoint, so
     *  the run before it has to be visibly longer than that. */
    @Override
    public String elkPadding() {
        return "[top=24,left=12,bottom=12,right=12]";
    }

    /** Per-parameter sub-row height beneath an operation header row. */
    private static final double PARAM_ROW_HEIGHT = 14.0;

    /** True iff at least one attribute carries a default value, i.e. the
     *  body needs to reserve a right-gutter column for the D badge and
     *  emit a DEFAULTS section listing the values. */
    private boolean hasAnyDefault() {
        for (Attribute a : this.attributes) {
            if (a.defaultValue != null) return true;
        }
        return false;
    }

    private int defaultCount() {
        int n = 0;
        for (Attribute a : this.attributes) {
            if (a.defaultValue != null) n++;
        }
        return n;
    }

    /** Width of the right gutter reserved for the D badge column plus one
     *  connector lane per defaulted attribute. */
    private double rightGutterWidth() {
        int n = this.defaultCount();
        if (n == 0) return 0;
        return DEF_COL_WIDTH + 4.0 + n * DEF_LANE_SPACING;
    }

    private boolean hasAnyDerivedFlag() {
        for (Attribute a : this.attributes) if (a.derived) return true;
        for (Reference r : this.references) if (r.derived) return true;
        return false;
    }

    private boolean hasAnyTransient() {
        for (Attribute a : this.attributes) if (a.transientFlag) return true;
        for (Reference r : this.references) if (r.transientFlag) return true;
        return false;
    }

    private boolean hasAnyVolatile() {
        for (Attribute a : this.attributes) if (a.volatileFlag) return true;
        for (Reference r : this.references) if (r.volatileFlag) return true;
        return false;
    }

    /** Right edge x of the bounds column, computed by walking the right
     *  gutter inward column-by-column. Layout right→left:
     *  containment/reference shape (rightmost), then D + connector lanes,
     *  then the per-flag pillars (V / T / /). */
    private double boundsRightX(double w) {
        double x = w - 4.0;
        // Containment / reference icon column — rightmost, only when this
        // class has any references.
        if (!this.references.isEmpty()) x -= FLAG_COL_WIDTH;
        if (this.hasAnyDefault())     x -= rightGutterWidth();          // D + connector lanes
        if (this.hasAnyVolatile())    x -= FLAG_COL_WIDTH;
        if (this.hasAnyTransient())   x -= FLAG_COL_WIDTH;
        if (this.hasAnyDerivedFlag()) x -= FLAG_COL_WIDTH;
        return x;
    }

    /** Width of the right-most containment/reference shape column. */
    private double shapeColumnWidth() {
        return this.references.isEmpty() ? 0 : FLAG_COL_WIDTH;
    }

    /** Right edge x of the type column, immediately to the left of the
     *  bounds column. */
    private double typeRightX(double w) {
        return this.boundsRightX(w) - BOUNDS_COL_WIDTH;
    }

    private SvgElem letterBadge(double x, double y, String letter, String fillColor, String tooltip) {
        return Badges.letter(x, y, letter, fillColor, tooltip);
    }

    /** Render the cluster of right-gutter flag badges for a row at
     *  {@code (y, w)} with the given derived / transient / volatile
     *  flags, and (when applicable) a containment/reference shape on
     *  the very right. The D badge for default-value attributes is
     *  rendered separately by {@link #renderAttribute}. */
    private void renderFlagColumns(SvgElem g, double y, double w, double iconY,
                                    boolean derived, boolean transientFlag,
                                    boolean volatileFlag) {
        double x = w - 4.0;
        // Shape column (containment/reference) is the right-most slot when
        // present; flag columns sit to its left.
        x -= this.shapeColumnWidth();
        if (this.hasAnyDefault()) x -= rightGutterWidth();
        if (this.hasAnyVolatile()) {
            x -= FLAG_COL_WIDTH;
            if (volatileFlag) g.add(this.letterBadge(x + 2.0, iconY, "V", "#f97316", "volatile"));
        }
        if (this.hasAnyTransient()) {
            x -= FLAG_COL_WIDTH;
            if (transientFlag) g.add(this.letterBadge(x + 2.0, iconY, "T", "#0891b2", "transient"));
        }
        if (this.hasAnyDerivedFlag()) {
            x -= FLAG_COL_WIDTH;
            if (derived) g.add(this.letterBadge(x + 2.0, iconY, "/", "#6b7280", "derived"));
        }
    }

    public double[] sizeHint(DNode node) {
        double w = Math.max(220.0, this.textWidth(this.name) + 60.0);
        // Total width of the right gutter columns: D + flag pillars +
        // bounds + reference shape + connector lanes.
        double rightCols = BOUNDS_COL_WIDTH + 8.0;
        if (this.hasAnyDefault()) rightCols += rightGutterWidth();
        if (this.hasAnyVolatile()) rightCols += FLAG_COL_WIDTH;
        if (this.hasAnyTransient()) rightCols += FLAG_COL_WIDTH;
        if (this.hasAnyDerivedFlag()) rightCols += FLAG_COL_WIDTH;
        if (!this.references.isEmpty()) rightCols += FLAG_COL_WIDTH;
        for (Attribute a : this.attributes) {
            // name + type + bounds + flags must all fit horizontally.
            w = Math.max(w, this.textWidth(a.name) + 30.0
                    + this.textWidth(a.type != null ? a.type : "?") + rightCols);
        }
        for (Reference r : this.references) {
            w = Math.max(w, this.textWidth(r.name) + 30.0
                    + this.textWidth(r.type != null ? r.type : "?") + rightCols);
        }
        for (Operation o : this.operations) {
            w = Math.max(w, this.textWidth(o.headerLine()) + 20.0 + 20.0);
            for (String[] p : o.params) {
                w = Math.max(w, this.textWidth(p[0] + " : " + p[1]) + 36.0);
            }
        }
        for (Literal l : this.literals) {
            w = Math.max(w, this.textWidth(l.displayLine()) + 20.0 + 20.0);
        }
        if (this.instanceClassName != null) {
            w = Math.max(w, this.textWidth(this.instanceClassName) + 20.0 + 30.0);
        }
        double h = 28.0;
        if (!this.attributes.isEmpty()) {
            h += 16.0 + (double)this.attributes.size() * 18.0;
        }
        if (!this.references.isEmpty()) {
            h += 16.0 + (double)this.references.size() * 18.0;
        }
        if (!this.operations.isEmpty()) {
            h += 16.0;
            for (Operation o : this.operations) {
                h += operationHeight(o);
            }
        }
        if (!this.literals.isEmpty()) {
            h += 16.0 + (double)this.literals.size() * 18.0;
        }
        if (this.hasAnyDefault()) {
            int defaultCount = 0;
            for (Attribute a : this.attributes) {
                if (a.defaultValue != null) {
                    defaultCount++;
                    // Ensure the body is wide enough for the longest default
                    // value plus the left badge gutter and the right
                    // connector gutter — same shape DB's TableBody uses.
                    w = Math.max(w, this.textWidth(a.defaultValue)
                            + 20.0 + DEF_COL_WIDTH + 12.0);
                }
            }
            h += 16.0 + (double) defaultCount * 18.0;
        }
        if (this.instanceClassName != null) {
            h += 34.0;
        }
        if (h == 28.0) {
            h += 8.0;
        }
        return new double[]{w, h};
    }

    private static double operationHeight(Operation o) {
        return 18.0 + (double) o.params.size() * PARAM_ROW_HEIGHT;
    }

    public List<DPort> createPorts(DNode node) {
        ArrayList<DPort> ports = new ArrayList<DPort>();
        this.classPortWest = new DPort(node.id() + ".__class.w", node).side(PortSide.WEST);
        this.classPortEast = new DPort(node.id() + ".__class.e", node).side(PortSide.EAST);
        this.classPortNorth = new DPort(node.id() + ".__class.n", node).side(PortSide.NORTH);
        ports.add(this.classPortWest);
        ports.add(this.classPortEast);
        ports.add(this.classPortNorth);
        for (Reference r : this.references) {
            DPort w = new DPort(node.id() + "." + r.name + ".w", node).side(PortSide.WEST);
            DPort e = new DPort(node.id() + "." + r.name + ".e", node).side(PortSide.EAST);
            this.refPorts.put(r.name, new DPort[]{w, e});
            ports.add(w);
            ports.add(e);
        }
        return ports;
    }

    public void layoutPorts(DNode node) {
        double w = node.width();
        this.classPortWest.position(0.0, 12.0);
        this.classPortEast.position(w - this.classPortEast.width(), 12.0);
        // Inheritance edges anchor at the top centre of the class. The
        // port is placed *above* the node by 18 px so ELK has to draw a
        // long perpendicular run before the arrowhead — the entire arrow
        // marker plus its inbound run land outside the title bar.
        this.classPortNorth.position(
                w / 2.0 - this.classPortNorth.width() / 2.0, -18.0);
        double y = 28.0;
        if (!this.attributes.isEmpty()) {
            y += 16.0 + (double)this.attributes.size() * 18.0;
        }
        if (!this.references.isEmpty()) {
            y += 16.0;
            int i = 0;
            for (Reference r : this.references) {
                double cy = y + (double)i * 18.0 + 9.0 - 2.0;
                DPort[] pair = this.refPorts.get(r.name);
                pair[0].position(0.0, cy);
                pair[1].position(w - pair[1].width(), cy);
                ++i;
            }
        }
    }

    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").cls("dv-frame").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4));
        String clipId = "dv-clip-" + EClassBody.sanitize(node.id());
        g.add(new SvgElem("defs").add(new SvgElem("clipPath").attr("id", clipId).add(new SvgElem("rect").attr("x", 0).attr("y", 0).attr("width", w).attr("height", h).attr("rx", 4).attr("ry", 4))));
        SvgElem inner = new SvgElem("g").attr("clip-path", "url(#" + clipId + ")");
        g.add(inner);
        g = inner;
        String titleBg = this.titleColor();
        g.add(new SvgElem("rect").cls("dv-title-bar").attr("x", 0).attr("y", 0).attr("width", w).attr("height", 28.0).attr("rx", 4).attr("ry", 4).style("fill:" + titleBg));
        g.add(new SvgElem("rect").attr("x", 0).attr("y", 24.0).attr("width", w).attr("height", 4).style("fill:" + titleBg));
        g.add(this.kindIcon(10.0, 8.0));
        double titleX = 28.0;
        // Plain class / interface need no extra label — the kind icon and
        // the italic-for-interface name already say it. Abstract class,
        // datatype, enumeration and annotation get a small caption above
        // the name.
        String kindLabel = this.kindLabel();
        if (kindLabel != null) {
            g.add(new SvgElem("text").attr("x", titleX).attr("y", 12).style("font:400 10px sans-serif;fill:#e5e7eb").text(kindLabel));
        }
        g.add(new SvgElem("text").cls("dv-title").attr("x", titleX).attr("y", kindLabel != null ? 23 : 18).style("font:600 13px sans-serif;fill:#f9fafb" + (this.kind == Kind.ABSTRACT_CLASS || this.kind == Kind.INTERFACE ? ";font-style:italic" : "")).text(this.name));
        double y = 28.0;
        if (this.instanceClassName != null) {
            y = this.renderMetaInstance(g, y, w);
        }
        // Track each defaulted attribute's row centre so the connector lines
        // can run from the right-gutter D badge to the matching DEFAULTS row.
        boolean defs = this.hasAnyDefault();
        java.util.LinkedHashMap<String, Double> attrMidY = new java.util.LinkedHashMap<>();
        if (!this.attributes.isEmpty()) {
            this.renderSectionHeader(g, "attributes", y, w, ATTR_HEAD_BG);
            y += 16.0;
            for (Attribute a : this.attributes) {
                this.renderRowBg(g, y, w, ATTR_ROW_BG, ATTR_HEAD_BG);
                this.renderAttribute(g, a, y, w);
                if (a.defaultValue != null) attrMidY.put(a.name, y + 9.0);
                y += 18.0;
            }
        }
        if (!this.references.isEmpty()) {
            this.renderSectionHeader(g, "references", y, w, REF_HEAD_BG);
            y += 16.0;
            for (Reference r : this.references) {
                this.renderRowBg(g, y, w, REF_ROW_BG, REF_HEAD_BG);
                this.renderReference(g, r, y, w);
                y += 18.0;
            }
        }
        if (!this.operations.isEmpty()) {
            this.renderSectionHeader(g, "operations", y, w, OP_HEAD_BG);
            y += 16.0;
            for (Operation o : this.operations) {
                double rowH = operationHeight(o);
                this.renderRowBg(g, y, w, OP_ROW_BG, OP_HEAD_BG, rowH);
                this.renderOperation(g, o, y, w);
                y += rowH;
            }
        }
        if (!this.literals.isEmpty()) {
            this.renderSectionHeader(g, "literals", y, w, LIT_HEAD_BG);
            y += 16.0;
            for (Literal l : this.literals) {
                this.renderRowBg(g, y, w, LIT_ROW_BG, LIT_HEAD_BG);
                this.renderLiteral(g, l, y, w);
                y += 18.0;
            }
        }
        if (defs) {
            this.renderSectionHeader(g, "defaults", y, w, DEF_HEAD_BG);
            y += 16.0;
            java.util.LinkedHashMap<String, Double> defaultMidY = new java.util.LinkedHashMap<>();
            for (Attribute a : this.attributes) {
                if (a.defaultValue == null) continue;
                this.renderRowBg(g, y, w, DEF_ROW_BG, DEF_HEAD_BG);
                // D badge + default value text only — the connector running
                // through the right gutter says which attribute this default
                // belongs to, so duplicating the attribute name on the right
                // would just add noise (and overlap long default texts).
                g.add(this.defaultValueBadge(3.0, y + 4.0, a.defaultValue));
                g.add(new SvgElem("text").attr("x", 18.0).attr("y", y + 13.0)
                        .style(STYLE_META).text(a.defaultValue));
                defaultMidY.put(a.name, y + 9.0);
                y += 18.0;
            }
            // Per-default lane connector via the shared common helper.
            double anchorX = w - this.shapeColumnWidth() - rightGutterWidth() + DEF_COL_WIDTH;
            java.util.List<SectionConnectors.Group> groups
                    = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, Double> en : attrMidY.entrySet()) {
                Double dy = defaultMidY.get(en.getKey());
                if (dy == null) continue;
                groups.add(SectionConnectors
                        .Group.of(en.getValue(), dy));
            }
            SectionConnectors.render(
                    g, groups, anchorX, DEF_LANE_SPACING, DEF_CONNECTOR_COLOR, 0);
        }
    }

    private double renderMetaInstance(SvgElem g, double y, double w) {
        this.renderSectionHeader(g, "instance class", y, w, "#e5e7eb");
        this.renderRowBg(g, y += 16.0, w, "#f9fafb", "#e5e7eb");
        g.add(new SvgElem("text").attr("x", 14.0).attr("y", y + 13.0).style(STYLE_META).text(this.instanceClassName));
        return y + 18.0;
    }

    private void renderAttribute(SvgElem g, Attribute a, double y, double w) {
        double iconY = y + 4.0;
        // Left gutter keeps only the ID icon — derived/transient/volatile
        // moved to the right-side flag columns so all "boolean property"
        // markers live in one consistent strip on the right.
        if (a.id) {
            g.add(this.keyIcon(3.0, iconY));
        } else if (a.inheritedFrom != null) {
            g.add(this.inheritedIcon(3.0, iconY, a.inheritedFrom));
        }
        // Inherited rows also render in italic + dimmer colour so they read
        // as "from a parent class" without needing a separate compartment.
        String italic = a.inheritedFrom != null ? ";font-style:italic" : "";
        String dim = a.inheritedFrom != null ? ";opacity:0.7" : "";
        String nameStyle = a.id ? "font:700 11px sans-serif" + italic + dim
                : STYLE_ROW_NAME + italic + dim;
        SvgElem nameText = new SvgElem("text").attr("x", 18.0).attr("y", y + 13.0)
                .style(nameStyle).text(a.name);
        if (a.inheritedFrom != null) {
            nameText.add(new SvgElem("title").text("inherited from " + a.inheritedFrom));
        }
        g.add(nameText);
        // Type and bounds in their own right-aligned columns.
        g.add(new SvgElem("text").attr("x", this.typeRightX(w)).attr("y", y + 13.0)
                .attr("text-anchor", "end").style(STYLE_META + italic + dim)
                .text(a.type != null ? a.type : "?"));
        g.add(new SvgElem("text").attr("x", this.boundsRightX(w)).attr("y", y + 13.0)
                .attr("text-anchor", "end").style(STYLE_META + italic + dim)
                .text(a.multiplicity != null ? a.multiplicity : ""));
        // Flag badges (derived / transient / volatile).
        this.renderFlagColumns(g, y, w, iconY, a.derived, a.transientFlag, a.volatileFlag);
        // D badge sits in the D column, which is one slot left of the
        // (always-rightmost) containment/reference shape column. The 3-px
        // padding leaves room for the connector to exit cleanly.
        if (a.defaultValue != null) {
            double dx = w - this.shapeColumnWidth() - rightGutterWidth() + 3.0;
            g.add(this.defaultValueBadge(dx, iconY, a.defaultValue));
        }
    }

    private SvgElem defaultValueBadge(double x, double y, String value) {
        return Badges.defaultValue(x, y, value);
    }

    private void renderReference(SvgElem g, Reference r, double y, double w) {
        double iconY = y + 4.0;
        if (r.inheritedFrom != null) {
            g.add(this.inheritedIcon(3.0, iconY, r.inheritedFrom));
        }
        String italic = r.inheritedFrom != null ? ";font-style:italic" : "";
        String dim = r.inheritedFrom != null ? ";opacity:0.7" : "";
        SvgElem nameText = new SvgElem("text").attr("x", 18.0).attr("y", y + 13.0)
                .style(STYLE_ROW_NAME + italic + dim).text(r.name);
        if (r.inheritedFrom != null) {
            nameText.add(new SvgElem("title").text("inherited from " + r.inheritedFrom));
        }
        g.add(nameText);
        // Type and bounds columns mirror the attribute layout.
        g.add(new SvgElem("text").attr("x", this.typeRightX(w)).attr("y", y + 13.0)
                .attr("text-anchor", "end").style(STYLE_META + italic + dim)
                .text(r.type != null ? r.type : "?"));
        g.add(new SvgElem("text").attr("x", this.boundsRightX(w)).attr("y", y + 13.0)
                .attr("text-anchor", "end").style(STYLE_META + italic + dim)
                .text(r.multiplicity != null ? r.multiplicity : ""));
        this.renderFlagColumns(g, y, w, iconY, r.derived, r.transientFlag, r.volatileFlag);
        // Containment / reference shape sits in the very rightmost column —
        // past the D + connector gutter and the per-flag pillars — so it is
        // the last "kind tag" the eye lands on when scanning a row.
        double iconCol = w - FLAG_COL_WIDTH - 2.0;
        if (r.containment) {
            g.add(this.containmentIcon(iconCol, iconY));
        } else {
            g.add(this.referenceIcon(iconCol, iconY));
        }
    }

    /** UML composition marker: a small filled diamond. Distinct from the
     *  reference chevron at first glance — the eye reads "solid shape" =
     *  "owns the target". */
    private SvgElem containmentIcon(double x, double y) {
        return new SvgElem("g")
                .attr("transform", "translate(" + SvgElem.fmt(x) + "," + SvgElem.fmt(y) + ")")
                .attr("aria-label", "containment")
                .add(new SvgElem("path").attr("d", "M6 1 L11 5 L6 9 L1 5 Z")
                        .style("fill:#1f2937;stroke:none"));
    }

    /** Plain reference marker: an open chevron pointing toward the target.
     *  Same colour as the containment diamond so the whole body reads
     *  unified, but a different shape so the kind is unambiguous. */
    private SvgElem referenceIcon(double x, double y) {
        return new SvgElem("g")
                .attr("transform", "translate(" + SvgElem.fmt(x) + "," + SvgElem.fmt(y) + ")")
                .attr("aria-label", "reference")
                .add(new SvgElem("path").attr("d", "M3 1 L9 5 L3 9")
                        .style("fill:none;stroke:#1f2937;stroke-width:1.4;stroke-linecap:round;stroke-linejoin:round"));
    }

    private SvgElem keyIcon(double x, double y) {
        SvgElem g = new SvgElem("g").attr("transform", "translate(" + SvgElem.fmt((double)x) + "," + SvgElem.fmt((double)y) + ")").attr("aria-label", "id");
        g.add(new SvgElem("circle").attr("cx", 2.6).attr("cy", 5).attr("r", 2.4).style("fill:#f59e0b;stroke:#b45309;stroke-width:0.6"));
        g.add(new SvgElem("circle").attr("cx", 2.6).attr("cy", 5).attr("r", 0.9).style("fill:#b45309"));
        g.add(new SvgElem("path").attr("d", "M5 4.2 L10 4.2 L10 6 L9 6 L9 5 L8 5 L8 6 L5 6 Z").style("fill:#f59e0b;stroke:#b45309;stroke-width:0.6;stroke-linejoin:round"));
        return g;
    }

    private void renderOperation(SvgElem g, Operation o, double y, double w) {
        double iconY = y + 4.0;
        // Operation icon on the left, replaced by the inheritance arrow when
        // the operation comes from a supertype.
        if (o.inheritedFrom != null) {
            g.add(this.inheritedIcon(3.0, iconY, o.inheritedFrom));
        } else {
            g.add(this.operationIcon(3.0, iconY));
        }
        String italic = o.inheritedFrom != null ? ";font-style:italic" : "";
        String dim = o.inheritedFrom != null ? ";opacity:0.7" : "";
        // Operation header: name + parens on the left, return type
        // right-aligned in the same column attributes / references use, so
        // every type in the body lines up.
        String nameAndParens = o.name + "(" + (o.params.isEmpty() ? "" : "…") + ")";
        SvgElem nameText = new SvgElem("text").attr("x", 18.0).attr("y", y + 13.0)
                .style(STYLE_ROW_NAME + italic + dim).text(nameAndParens);
        if (o.inheritedFrom != null) {
            nameText.add(new SvgElem("title").text("inherited from " + o.inheritedFrom));
        }
        g.add(nameText);
        if (o.returnType != null) {
            g.add(new SvgElem("text").attr("x", this.boundsRightX(w)).attr("y", y + 13.0)
                    .attr("text-anchor", "end").style(STYLE_META + italic + dim).text(o.returnType));
        }
        // Each parameter on its own indented sub-row: bullet + name on the
        // left, type right-aligned to the same gutter as the operation's
        // return type and every other structural-feature type.
        double cursor = y + 18.0;
        for (String[] p : o.params) {
            g.add(new SvgElem("circle").attr("cx", 24).attr("cy", cursor + 7)
                    .attr("r", 1.6).style("fill:#92400e"));
            g.add(new SvgElem("text").attr("x", 32).attr("y", cursor + 11)
                    .style(STYLE_META).text(p[0]));
            g.add(new SvgElem("text").attr("x", this.boundsRightX(w)).attr("y", cursor + 11)
                    .attr("text-anchor", "end").style(STYLE_META).text(p[1]));
            cursor += PARAM_ROW_HEIGHT;
        }
    }

    /** Small upward chevron + tail marking a row that's inherited from a
     *  supertype. Indigo so it doesn't compete with the kind colours of
     *  the compartments below. */
    private SvgElem inheritedIcon(double x, double y, String supertypeName) {
        SvgElem g = new SvgElem("g")
                .attr("transform", "translate(" + SvgElem.fmt(x) + "," + SvgElem.fmt(y) + ")")
                .attr("aria-label", "inherited from " + supertypeName);
        g.add(new SvgElem("path").attr("d", "M6 1 L11 6 L8 6 L8 10 L4 10 L4 6 L1 6 Z")
                .style("fill:#6366f1;stroke:#4338ca;stroke-width:0.6"));
        g.add(new SvgElem("title").text("inherited from " + supertypeName));
        return g;
    }

    /** Symbol marking an operation row: a small italic {@code ƒ} matching
     *  the operation compartment's amber palette so the eye can scan
     *  operations apart from attributes / references at a glance. */
    private SvgElem operationIcon(double x, double y) {
        return new SvgElem("g")
                .attr("transform", "translate(" + SvgElem.fmt(x) + "," + SvgElem.fmt(y) + ")")
                .attr("aria-label", "operation")
                .add(new SvgElem("text").attr("x", 1).attr("y", 10)
                        .style("font:italic 700 12px serif;fill:#a16207")
                        .text("ƒ"));
    }

    private void renderLiteral(SvgElem g, Literal l, double y, double w) {
        g.add(new SvgElem("text").attr("x", 10.0).attr("y", y + 13.0).style(STYLE_ROW_NAME).text(l.name));
        g.add(new SvgElem("text").attr("x", w - 10.0).attr("y", y + 13.0).attr("text-anchor", "end").style(STYLE_META).text(l.displayRight()));
    }

    private void renderSectionHeader(SvgElem g, String label, double y, double w, String bg) {
        Compartments.renderSectionHeader(g, label, y, w, SECTION_HEADER_HEIGHT, bg);
    }

    private void renderRowBg(SvgElem g, double y, double w, String fill, String divider) {
        Compartments.renderRowBackground(g, y, w, ROW_HEIGHT, fill, divider);
    }

    private void renderRowBg(SvgElem g, double y, double w, String fill, String divider, double rowH) {
        Compartments.renderRowBackground(g, y, w, rowH, fill, divider);
    }

    private String titleColor() {
        return switch (this.kind.ordinal()) {
            case 1 -> ABS_TITLE;
            case 2 -> INTERFACE_TITLE;
            case 3 -> DATATYPE_TITLE;
            case 4 -> ENUM_TITLE;
            case 5 -> ANNOT_TITLE;
            default -> CLASS_TITLE;
        };
    }

    private String kindLabel() {
        return switch (this.kind) {
            case CLASS -> "Class";
            case INTERFACE -> "Interface";
            case ABSTRACT_CLASS -> "abstract Class";
            case DATATYPE -> "Datatype";
            case ENUM -> "Enumeration";
            case ANNOTATION -> "Annotation";
        };
    }

    private SvgElem kindIcon(double x, double y) {
        SvgElem g = new SvgElem("g").attr("transform", "translate(" + SvgElem.fmt((double)x) + "," + SvgElem.fmt((double)y) + ")");
        String stroke = "#f9fafb";
        switch (this.kind.ordinal()) {
            case 4: {
                g.add(new SvgElem("path").attr("d", "M6 0 L1 3 L1 9 L6 12 L11 9 L11 3 Z").style("fill:none;stroke:" + stroke + ";stroke-width:1"));
                g.add(new SvgElem("circle").attr("cx", 6).attr("cy", 6).attr("r", 2).style("fill:" + stroke));
                break;
            }
            case 3: {
                g.add(new SvgElem("rect").attr("x", 1).attr("y", 2).attr("width", 10).attr("height", 8).style("fill:none;stroke:" + stroke + ";stroke-width:1;stroke-dasharray:2 1"));
                break;
            }
            case 2: {
                g.add(new SvgElem("circle").attr("cx", 6).attr("cy", 6).attr("r", 5).style("fill:none;stroke:" + stroke + ";stroke-width:1.2"));
                break;
            }
            case 5: {
                g.add(new SvgElem("text").attr("x", 2).attr("y", 10).style("font:700 11px sans-serif;fill:" + stroke).text("@"));
                break;
            }
            default: {
                g.add(new SvgElem("rect").attr("x", 1).attr("y", 1).attr("width", 10).attr("height", 10).style("fill:none;stroke:" + stroke + ";stroke-width:1"));
                g.add(new SvgElem("line").attr("x1", 1).attr("y1", 5).attr("x2", 11).attr("y2", 5).style("stroke:" + stroke + ";stroke-width:0.8"));
            }
        }
        return g;
    }

    private static String sanitize(String id) {
        return id.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }

    private double textWidth(String s) {
        return s == null ? 0.0 : (double)s.length() * 6.5;
    }

    public static enum Kind {
        CLASS,
        ABSTRACT_CLASS,
        INTERFACE,
        DATATYPE,
        ENUM,
        ANNOTATION;

    }

    public static final class Attribute {
        public final String name;
        public String type;
        public String multiplicity = "1";
        public boolean id;
        public boolean derived;
        public boolean transientFlag;
        public boolean volatileFlag;
        public String defaultValue;
        /** When non-null, this attribute is shown in italic + dimmer colour
         *  and the value is the simple name of the declaring supertype
         *  (used by the renderer for an "inherited from <name>" tooltip). */
        public String inheritedFrom;

        public Attribute(String name) {
            this.name = name;
        }

        public Attribute type(String t) {
            this.type = t;
            return this;
        }

        public Attribute multiplicity(String m) {
            this.multiplicity = m;
            return this;
        }

        public Attribute id() {
            this.id = true;
            return this;
        }

        public Attribute derived() {
            this.derived = true;
            return this;
        }

        public Attribute transientFlag() {
            this.transientFlag = true;
            return this;
        }

        public Attribute volatileFlag() {
            this.volatileFlag = true;
            return this;
        }

        public Attribute defaultValue(String v) {
            this.defaultValue = v;
            return this;
        }

        public Attribute inheritedFrom(String supertypeName) {
            this.inheritedFrom = supertypeName;
            return this;
        }
    }

    public static final class Reference {
        public final String name;
        public String type;
        public String multiplicity = "1";
        public boolean containment;
        public boolean derived;
        public boolean transientFlag;
        public boolean volatileFlag;
        public String opposite;
        public String inheritedFrom;

        public Reference(String name) {
            this.name = name;
        }

        public Reference type(String t) {
            this.type = t;
            return this;
        }

        public Reference multiplicity(String m) {
            this.multiplicity = m;
            return this;
        }

        public Reference containment() {
            this.containment = true;
            return this;
        }

        public Reference derived() {
            this.derived = true;
            return this;
        }

        public Reference transientFlag() {
            this.transientFlag = true;
            return this;
        }

        public Reference volatileFlag() {
            this.volatileFlag = true;
            return this;
        }

        public Reference opposite(String o) {
            this.opposite = o;
            return this;
        }

        public Reference inheritedFrom(String supertypeName) {
            this.inheritedFrom = supertypeName;
            return this;
        }
    }

    public static final class Operation {
        public final String name;
        public final List<String[]> params = new ArrayList<String[]>();
        public String returnType;
        public String inheritedFrom;

        public Operation inheritedFrom(String supertypeName) {
            this.inheritedFrom = supertypeName;
            return this;
        }

        public Operation(String name) {
            this.name = name;
        }

        public Operation param(String name, String type) {
            this.params.add(new String[]{name, type});
            return this;
        }

        public Operation returnType(String t) {
            this.returnType = t;
            return this;
        }

        /** Width-estimation helper used by {@link EClassBody#sizeHint}: a
         *  rough single-line representation of the operation header so the
         *  body can compute a max width. The real renderer paints
         *  {@code name(...)} on the left and the return type as a separate
         *  right-aligned column, so this method's return value is never
         *  rendered verbatim. */
        String headerLine() {
            StringBuilder sb = new StringBuilder(this.name).append("(");
            if (!this.params.isEmpty()) sb.append("…");
            sb.append(")");
            if (this.returnType != null) {
                sb.append(" : ").append(this.returnType);
            }
            return sb.toString();
        }
    }

    public static final class Literal {
        public final String name;
        public int value;
        public String literal;

        public Literal(String name) {
            this.name = name;
        }

        public Literal value(int v) {
            this.value = v;
            return this;
        }

        public Literal literal(String l) {
            this.literal = l;
            return this;
        }

        /** Width-estimation helper for {@link EClassBody#sizeHint}; the
         *  literal row's actual rendering uses {@link #displayRight()}
         *  directly. */
        String displayLine() {
            return this.displayRight();
        }

        String displayRight() {
            StringBuilder sb = new StringBuilder().append(this.value);
            if (this.literal != null && !this.literal.equals(this.name)) {
                sb.append(" (").append(this.literal).append(")");
            }
            return sb.toString();
        }
    }
}

