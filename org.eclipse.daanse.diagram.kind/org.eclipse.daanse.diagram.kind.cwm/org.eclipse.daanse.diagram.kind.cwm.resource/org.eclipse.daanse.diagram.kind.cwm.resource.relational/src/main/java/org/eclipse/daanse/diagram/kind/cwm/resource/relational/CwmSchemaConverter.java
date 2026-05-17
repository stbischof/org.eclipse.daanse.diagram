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
package org.eclipse.daanse.diagram.kind.cwm.resource.relational;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.daanse.cwm.model.cwm.foundation.keysindexes.Index;
import org.eclipse.daanse.cwm.model.cwm.foundation.keysindexes.IndexedFeature;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.Parameter;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Feature;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.StructuralFeature;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.CheckConstraint;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Column;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.ColumnSet;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.ForeignKey;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.PrimaryKey;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Procedure;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.QueryColumnSet;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLDistinctType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLSimpleType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLStructuredType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Schema;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Table;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Trigger;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.UniqueConstraint;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.View;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.ActionOrientationType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.NullableType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.ProcedureType;
import org.eclipse.daanse.diagram.notation.edge.Edges;
import org.eclipse.daanse.diagram.notation.layout.Areas;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;
import org.eclipse.daanse.diagram.kind.schema.TableBody;
import org.eclipse.emf.common.util.EList;

public final class CwmSchemaConverter {

    /** Positive-list filter applied during conversion. The default
     *  {@link #all()} keeps the legacy behaviour (everything in scope is
     *  emitted); narrower filters cause the converter to skip whole schemas,
     *  column sets, columns, FK edges, types and procedures up-front instead
     *  of emitting them and pruning afterwards. */
    public record Filter(
            Predicate<ColumnSet> includeColumnSet,
            BiPredicate<ColumnSet, Column> includeColumn,
            boolean includeFkEdges,
            boolean includeTypes,
            boolean includeProcedures) {

        public static Filter all() {
            return new Filter(cs -> true, (cs, c) -> true, true, true, true);
        }
    }

    private static final String ID_SCHEMA       = "schema";
    private static final String ID_AREA_FUNCS   = "area_funcs";
    private static final String ID_AREA_PROCS   = "area_procs";
    private static final String ID_TABLE  = "t";
    private static final String ID_PROC   = "proc";
    private static final String ID_FN     = "fn";
    private static final String ID_DT     = "dt";
    private static final String ID_ST     = "st";
    private static final String ID_SIMPLE = "smp";
    private static final String ID_FK     = "fk";
    private static final String ID_TYPEREF = "typeref";
    private static final String ID_INHERIT = "typeinherit";

    private static final Stereotype FUNCTION_S        = new Stereotype("FUNCTION",        Palette.TEAL_700,   Icons.PROCEDURE);
    private static final Stereotype PROCEDURE_S       = new Stereotype("PROCEDURE",       Palette.PURPLE_900, Icons.PROCEDURE);
    private static final Stereotype DISTINCT_TYPE_S   = new Stereotype("DISTINCT TYPE",   Palette.CYAN_700,   Icons.DATATYPE);
    private static final Stereotype STRUCTURED_TYPE_S = new Stereotype("STRUCTURED TYPE", Palette.VIOLET_500, Icons.CLASS);
    private static final Stereotype SIMPLE_TYPE_S     = new Stereotype("SIMPLE TYPE",     Palette.TEAL_700,   Icons.DATATYPE);

    public Diagram convert(Schema cwmSchema) {
        return convert(List.of(cwmSchema), Filter.all());
    }

    public Diagram convert(List<Schema> cwmSchemas) {
        return convert(cwmSchemas, Filter.all());
    }

    public Diagram convert(List<Schema> cwmSchemas, Filter filter) {
        DiagramBuilder b = DiagramBuilder.of(cwmSchemas.isEmpty() ? "" : cwmSchemas.get(0).getName());
        Map<UniqueConstraint, ColumnSet> pkOwner = new HashMap<>();
        List<ColumnSet> tableOrder = new ArrayList<>();
        // Per-schema sentinel keys for FUNCTIONS and PROCEDURES sub-areas —
        // separated so the eye can group routines by kind. Tables sit directly
        // under the schema container with no extra box.
        Map<Schema, Object> funcsAreaKey = new java.util.IdentityHashMap<>();
        Map<Schema, Object> procsAreaKey = new java.util.IdentityHashMap<>();

        for (Schema cwmSchema : cwmSchemas) {
            // Skip schemas whose entire content is filtered out — no empty
            // chrome left to clean up later.
            if (!schemaHasContent(cwmSchema, filter)) continue;
            b.container(cwmSchema, Names.n(cwmSchema.getName()), "SCHEMA", Icons.SCHEMA, ID_SCHEMA);
            boolean hasFuncs = false;
            boolean hasProcs = false;
            if (filter.includeProcedures()) {
                for (ModelElement me : cwmSchema.getOwnedElement()) {
                    if (me instanceof Procedure p) {
                        if (p.getType() == ProcedureType.FUNCTION) hasFuncs = true;
                        else hasProcs = true;
                    }
                }
            }
            if (hasFuncs) {
                funcsAreaKey.put(cwmSchema, Areas.subArea(b, cwmSchema, "functions",
                        "FUNCTIONS", Icons.PROCEDURE, ID_AREA_FUNCS));
            }
            if (hasProcs) {
                procsAreaKey.put(cwmSchema, Areas.subArea(b, cwmSchema, "procedures",
                        "PROCEDURES", Icons.PROCEDURE, ID_AREA_PROCS));
            }
            buildSchema(b, cwmSchema, pkOwner, tableOrder,
                    funcsAreaKey.get(cwmSchema), procsAreaKey.get(cwmSchema), filter);
        }
        if (filter.includeFkEdges()) {
            for (ColumnSet cs : tableOrder) {
                for (ModelElement child : cs.getOwnedElement()) {
                    if (!(child instanceof ForeignKey fk)) continue;
                    if (!(fk.getUniqueKey() instanceof UniqueConstraint ref)) continue;
                    ColumnSet refSet = pkOwner.get(ref);
                    if (refSet == null || !b.has(refSet)) continue;
                    addFkEdge(b, fk, cs, refSet);
                }
            }
        }
        if (filter.includeTypes()) {
            for (Schema cwmSchema : cwmSchemas) {
                for (ModelElement me : cwmSchema.getOwnedElement()) {
                    if (me instanceof ColumnSet cs && b.has(cs)) {
                        for (Feature f : cs.getFeature()) {
                            if (!(f instanceof Column col)) continue;
                            if (!filter.includeColumn().test(cs, col)) continue;
                            if (col.getType() instanceof SQLSimpleType sst) {
                                ensureSimpleTypeNode(b, sst, cwmSchema);
                            }
                        }
                    }
                    if (me instanceof SQLDistinctType dt && dt.getSqlSimpleType() != null) {
                        ensureSimpleTypeNode(b, dt.getSqlSimpleType(), cwmSchema);
                    }
                }
            }

            for (ColumnSet cs : tableOrder) {
                TableBody tb = (TableBody) b.node(cs).body();
                for (Feature f : cs.getFeature()) {
                    if (!(f instanceof Column col)) continue;
                    if (!filter.includeColumn().test(cs, col)) continue;
                    Classifier t = col.getType();
                    if (t == null || !b.has(t)) continue;
                    if (!(t instanceof SQLDistinctType) && !(t instanceof SQLStructuredType)
                            && !(t instanceof SQLSimpleType)) continue;
                    Edges.dependency(b, tb.port(col.getName(), PortSide.EAST), t, ID_TYPEREF)
                            .label(col.getName())
                            .done();
                }
            }

            for (Schema cwmSchema : cwmSchemas) {
                for (ModelElement me : cwmSchema.getOwnedElement()) {
                    if (me instanceof SQLDistinctType dt
                            && dt.getSqlSimpleType() != null
                            && b.has(dt) && b.has(dt.getSqlSimpleType())) {
                        Edges.inheritance(b, dt, dt.getSqlSimpleType(), ID_INHERIT).done();
                    }
                }
            }
        }
        return b.diagram();
    }

    private static boolean schemaHasContent(Schema cwmSchema, Filter filter) {
        for (ModelElement me : cwmSchema.getOwnedElement()) {
            if (me instanceof Procedure && filter.includeProcedures()) return true;
            if ((me instanceof SQLStructuredType || me instanceof SQLDistinctType)
                    && filter.includeTypes()) return true;
            if (me instanceof ColumnSet cs && filter.includeColumnSet().test(cs)) return true;
        }
        return false;
    }

    private void ensureSimpleTypeNode(DiagramBuilder b, SQLSimpleType sst, Schema owningSchema) {
        if (b.has(sst)) return;
        var body = b.labelled(sst, SIMPLE_TYPE_S, Names.n(sst.getName()), ID_SIMPLE);
        String detail = formatType(sst);
        if (detail != null && !detail.equals(Names.n(sst.getName()))) {
            body.addRow(detail);
        }
        if (owningSchema != null && b.has(owningSchema)) {
            b.nest(owningSchema, sst);
        }
    }

    private void buildSchema(DiagramBuilder b, Schema cwmSchema,
                             Map<UniqueConstraint, ColumnSet> pkOwner,
                             List<ColumnSet> tableOrder,
                             Object funcsArea,
                             Object procsArea,
                             Filter filter) {
        for (ModelElement me : cwmSchema.getOwnedElement()) {
            if (me instanceof Procedure proc) {
                if (!filter.includeProcedures()) continue;
                buildProcedure(b, proc);
                Object area = proc.getType() == ProcedureType.FUNCTION
                        ? (funcsArea != null ? funcsArea : cwmSchema)
                        : (procsArea != null ? procsArea : cwmSchema);
                b.nest(area, proc);
                continue;
            }
            // SQLStructuredType is also a ColumnSet (extends Class), so check
            // it before the generic ColumnSet branch.
            if (me instanceof SQLStructuredType st) {
                if (!filter.includeTypes()) continue;
                buildStructuredType(b, st);
                b.nest(cwmSchema, st);
                continue;
            }
            if (me instanceof SQLDistinctType dt) {
                if (!filter.includeTypes()) continue;
                buildDistinctType(b, dt);
                b.nest(cwmSchema, dt);
                continue;
            }
            if (!(me instanceof ColumnSet cs)) continue;
            if (!filter.includeColumnSet().test(cs)) continue;
            TableBody.HeaderKind kind;
            if (cs instanceof View) kind = TableBody.HeaderKind.VIEW;
            else if (cs instanceof QueryColumnSet) kind = TableBody.HeaderKind.QUERY_COLUMN_SET;
            else if (cs instanceof Table) kind = TableBody.HeaderKind.TABLE;
            else continue;

            TableBody tb = new TableBody(cs.getName()).headerKind(kind);
            if (cs instanceof Table table && table.isIsSystem()) tb.system(true);
            for (Feature f : cs.getFeature()) {
                if (f instanceof Column col && filter.includeColumn().test(cs, col)) {
                    tb.addColumn(toColumn(col, cs));
                }
            }
            for (ModelElement me2 : cwmSchema.getOwnedElement()) {
                if (me2 instanceof Index idx && idx.getSpannedClass() == cs) {
                    tb.addIndex(toIndex(idx));
                }
            }
            if (cs instanceof Table table) {
                for (ModelElement me2 : cwmSchema.getOwnedElement()) {
                    if (me2 instanceof Trigger tr && tr.getTable() == table) {
                        tb.addTrigger(toTrigger(tr));
                    }
                }
            }
            for (ModelElement child : cs.getOwnedElement()) {
                if (child instanceof UniqueConstraint uc && !(child instanceof PrimaryKey)) {
                    TableBody.UniqueConstraint u = new TableBody.UniqueConstraint(
                            uc.getName() != null ? uc.getName() : "unique");
                    for (StructuralFeature f : uc.getFeature()) u.columns.add(f.getName());
                    tb.addUniqueConstraint(u);
                }
                if (child instanceof CheckConstraint cc) {
                    TableBody.CheckConstraint ck = new TableBody.CheckConstraint(
                            cc.getName() != null ? cc.getName() : "check");
                    if (cc.getBody() != null) ck.body(cc.getBody().getBody());
                    for (ModelElement ce : cc.getConstrainedElement()) {
                        if (ce instanceof Column col) ck.columns.add(col.getName());
                    }
                    tb.addCheckConstraint(ck);
                }
            }
            b.node(cs, ID_TABLE, tb);
            b.nest(cwmSchema, cs);
            tableOrder.add(cs);
            for (ModelElement child : cs.getOwnedElement()) {
                if (child instanceof UniqueConstraint uc) pkOwner.put(uc, cs);
            }
        }
    }

    private void addFkEdge(DiagramBuilder b, ForeignKey fk, ColumnSet src, ColumnSet tgt) {
        EList<StructuralFeature> srcCols = fk.getFeature();
        EList<StructuralFeature> tgtCols = fk.getUniqueKey().getFeature();
        if (srcCols.isEmpty() || tgtCols.isEmpty()) return;
        TableBody srcBody = (TableBody) b.node(src).body();
        TableBody tgtBody = (TableBody) b.node(tgt).body();
        List<DPort> srcPorts = new ArrayList<>(srcCols.size());
        for (StructuralFeature f : srcCols) {
            srcPorts.add(srcBody.port(f.getName(), PortSide.EAST));
        }
        List<DPort> tgtPorts = new ArrayList<>(tgtCols.size());
        for (StructuralFeature f : tgtCols) {
            tgtPorts.add(tgtBody.port(f.getName(), PortSide.WEST));
        }
        Edges.foreignKey(b, srcPorts, tgtPorts, ID_FK)
                .label(fk.getName() != null ? fk.getName() : "fk")
                .done();
    }

    private TableBody.Column toColumn(Column c, Classifier owner) {
        TableBody.Column col = new TableBody.Column(c.getName());
        if (c.getType() instanceof SQLSimpleType sst) col.type(formatType(sst));
        else if (c.getType() != null) col.type(c.getType().getName());
        for (ModelElement me : owner.getOwnedElement()) {
            if (me instanceof PrimaryKey pk && pk.getFeature().contains(c)) col.pk();
            if (me instanceof ForeignKey fk && fk.getFeature().contains(c)) col.fk();
        }
        if (c.getIsNullable() == NullableType.COLUMN_NO_NULLS) col.notNull();
        if (c.getInitialValue() != null && c.getInitialValue().getBody() != null
                && !c.getInitialValue().getBody().isBlank()) {
            col.defaultValue(c.getInitialValue().getBody());
        }
        return col;
    }

    private String formatType(SQLSimpleType sst) {
        String name = Names.n(sst.getName());
        if (sst.getCharacterMaximumLength() > 0L) {
            return name + "(" + sst.getCharacterMaximumLength() + ")";
        }
        if (sst.getNumericPrecision() > 0L) {
            return name + "(" + sst.getNumericPrecision()
                    + (sst.getNumericScale() > 0L ? "," + sst.getNumericScale() : "") + ")";
        }
        return name;
    }

    private TableBody.Index toIndex(Index idx) {
        TableBody.Index i = new TableBody.Index(idx.getName());
        if (idx.isIsUnique()) i.unique();
        for (IndexedFeature f : idx.getIndexedFeature()) {
            if (f.getFeature() != null) i.columns.add(f.getFeature().getName());
        }
        return i;
    }

    private TableBody.Trigger toTrigger(Trigger tr) {
        TableBody.Trigger t = new TableBody.Trigger(tr.getName()).on(describeEvent(tr));
        if (tr.getActionOrientation() == ActionOrientationType.STATEMENT) t.forEachStatement();
        else t.forEachRow();
        return t;
    }

    private String describeEvent(Trigger tr) {
        String timing = tr.getConditionTiming() != null ? tr.getConditionTiming().getLiteral() : "";
        String manip = tr.getEventManipulation() != null ? tr.getEventManipulation().getLiteral() : "";
        return (timing + " " + manip).trim().toUpperCase();
    }

    /**
     * Renders a stored procedure or function. {@link ProcedureType#FUNCTION}
     * picks a different stereotype and surfaces any {@code PDK_RETURN}
     * parameter as a {@code "returns: TYPE"} footer row. Parameters with a
     * default value get an extra row underneath their type row reading
     * {@code "= <expression>"} (mirrors how table column defaults appear).
     */
    private void buildProcedure(DiagramBuilder b, Procedure proc) {
        boolean isFunction = proc.getType() == ProcedureType.FUNCTION;
        Stereotype s = isFunction ? FUNCTION_S : PROCEDURE_S;
        LabelledBoxBody body = b.labelled(proc, s, Names.n(proc.getName()),
                isFunction ? ID_FN : ID_PROC);
        String returnType = null;
        for (Parameter p : proc.getParameter()) {
            String raw = p.getKind() != null ? p.getKind().getLiteral() : "";
            String dir = raw.startsWith("pdk_") ? raw.substring(4) : raw;
            if ("return".equalsIgnoreCase(dir)) {
                returnType = typeName(p.getType());
                continue;
            }
            String dirTag = dir.isEmpty() ? "" : dir.toUpperCase() + " ";
            String row = dirTag + Names.n(p.getName()) + " : " + typeName(p.getType());
            String def = parameterDefault(p);
            if (def != null) row += "  = " + def;
            body.addRow(row);
        }
        if (returnType != null) body.addRow("returns: " + returnType);
    }

    private static String parameterDefault(Parameter p) {
        if (p.getDefaultValue() == null) return null;
        String body = p.getDefaultValue().getBody();
        return body != null && !body.isBlank() ? body : null;
    }

    private static String typeName(Classifier c) {
        return c != null && c.getName() != null ? c.getName() : "?";
    }

    /** SQLDistinctType: small datatype box with the underlying SQL simple
     *  type (length/precision/scale) on the body row. Acts as a domain
     *  alias node that columns can connect to. */
    private void buildDistinctType(DiagramBuilder b, SQLDistinctType dt) {
        LabelledBoxBody body = b.labelled(dt, DISTINCT_TYPE_S, Names.n(dt.getName()), ID_DT);
        body.addRow(formatBase(dt, dt.getSqlSimpleType()));
    }

    private static String formatBase(SQLDistinctType dt, SQLSimpleType base) {
        String name = base != null && base.getName() != null ? base.getName() : "?";
        if (dt.getLength() > 0L) return name + "(" + dt.getLength() + ")";
        if (dt.getPrecision() > 0L) {
            return name + "(" + dt.getPrecision()
                    + (dt.getScale() > 0L ? "," + dt.getScale() : "") + ")";
        }
        if (base != null) {
            if (base.getCharacterMaximumLength() > 0L) {
                return name + "(" + base.getCharacterMaximumLength() + ")";
            }
            if (base.getNumericPrecision() > 0L) {
                return name + "(" + base.getNumericPrecision()
                        + (base.getNumericScale() > 0L ? "," + base.getNumericScale() : "") + ")";
            }
        }
        return name;
    }

    /** SQLStructuredType: Class-style box with one row per attribute
     *  (StructuralFeature). Iterate getFeature() (not getStructuralFeature()
     *  which is unreliable in this generated model). */
    private void buildStructuredType(DiagramBuilder b, SQLStructuredType st) {
        LabelledBoxBody body = b.labelled(st, STRUCTURED_TYPE_S, Names.n(st.getName()), ID_ST);
        for (Feature f : st.getFeature()) {
            if (!(f instanceof StructuralFeature sf)) continue;
            String tn = sf.getType() != null && sf.getType().getName() != null
                    ? sf.getType().getName() : "?";
            body.addRow(Names.n(sf.getName()) + " : " + tn);
        }
    }
}
