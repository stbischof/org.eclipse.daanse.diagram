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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Feature;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Column;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.ColumnSet;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.QueryColumnSet;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLDistinctType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLSimpleType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLStructuredType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Schema;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.View;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.NullableType;
import org.eclipse.daanse.diagram.notation.edge.Edges;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;
import org.eclipse.daanse.diagram.kind.schema.TableBody;

/**
 * Builds a "where is this type used?" diagram. Given one
 * {@link SQLSimpleType} or {@link SQLDistinctType}, the result shows the
 * type as a single node in the centre and — for every schema that contains
 * at least one referencing column — a SchemaBody container with the
 * matching tables. Only the columns that actually use the type are listed,
 * each connected to the type node by a dashed dependency edge labelled
 * with the column name.
 */
public final class CwmTypeUsageConverter {

    private static final String ID_TYPE = "ut";
    private static final String ID_SCHEMA = "uschema";
    private static final String ID_TABLE = "ut_t";
    private static final String ID_EDGE = "typeuse";

    private static final Stereotype DISTINCT_TYPE_S = new Stereotype("DISTINCT TYPE", Palette.CYAN_700, Icons.DATATYPE);
    private static final Stereotype SIMPLE_TYPE_S   = new Stereotype("SIMPLE TYPE",   Palette.TEAL_700, Icons.DATATYPE);
    private static final Stereotype TYPE_S          = new Stereotype("TYPE",          Palette.GRAY_700, Icons.DATATYPE);

    public Diagram convert(Classifier type, Schema schema) {
        return convert(type, List.of(schema));
    }

    public Diagram convert(Classifier type, List<Schema> schemas) {
        String title = type != null && type.getName() != null
                ? "uses of " + type.getName() : "type usage";
        DiagramBuilder b = DiagramBuilder.of(title);

        buildTypeNode(b, type);

        for (Schema schema : schemas) {
            Map<ColumnSet, List<Column>> hits = collectHits(schema, type);
            if (hits.isEmpty()) continue;

            b.container(schema, Names.n(schema.getName()), "SCHEMA", Icons.SCHEMA, ID_SCHEMA);

            for (Map.Entry<ColumnSet, List<Column>> e : hits.entrySet()) {
                ColumnSet cs = e.getKey();
                TableBody tb = new TableBody(cs.getName()).headerKind(headerKindOf(cs));
                for (Column c : e.getValue()) {
                    TableBody.Column col = new TableBody.Column(c.getName())
                            .type(typeLabel(c.getType()));
                    if (c.getIsNullable() == NullableType.COLUMN_NO_NULLS) col.notNull();
                    tb.addColumn(col);
                }
                b.node(cs, ID_TABLE, tb);
                b.nest(schema, cs);

                for (Column c : e.getValue()) {
                    Edges.dependency(b, tb.port(c.getName(), PortSide.EAST), type, ID_EDGE)
                            .label(c.getName())
                            .done();
                }
            }
        }
        return b.diagram();
    }

    private Map<ColumnSet, List<Column>> collectHits(Schema schema, Classifier type) {
        Map<ColumnSet, List<Column>> hits = new LinkedHashMap<>();
        for (ModelElement me : schema.getOwnedElement()) {
            if (!(me instanceof ColumnSet cs)) continue;
            // Skip user-defined types; SQLStructuredType also extends ColumnSet
            // but is never the *target* of a type-usage scan.
            if (cs instanceof SQLDistinctType || cs instanceof SQLStructuredType) continue;
            List<Column> matches = new ArrayList<>();
            for (Feature f : cs.getFeature()) {
                if (!(f instanceof Column c)) continue;
                if (c.getType() == type) matches.add(c);
            }
            if (!matches.isEmpty()) hits.put(cs, matches);
        }
        return hits;
    }

    private TableBody.HeaderKind headerKindOf(ColumnSet cs) {
        if (cs instanceof View) return TableBody.HeaderKind.VIEW;
        if (cs instanceof QueryColumnSet) return TableBody.HeaderKind.QUERY_COLUMN_SET;
        return TableBody.HeaderKind.TABLE;
    }

    private void buildTypeNode(DiagramBuilder b, Classifier type) {
        if (type instanceof SQLDistinctType dt) {
            LabelledBoxBody body = b.labelled(dt, DISTINCT_TYPE_S, Names.n(dt.getName()), ID_TYPE);
            body.addRow(formatBase(dt));
            return;
        }
        if (type instanceof SQLSimpleType sst) {
            LabelledBoxBody body = b.labelled(sst, SIMPLE_TYPE_S, Names.n(sst.getName()), ID_TYPE);
            String detail = formatSimple(sst);
            if (detail != null) body.addRow(detail);
            return;
        }
        b.labelled(type, TYPE_S, Names.n(type != null ? type.getName() : null), ID_TYPE);
    }

    private static String typeLabel(Classifier c) {
        if (c instanceof SQLSimpleType sst) {
            String detail = formatSimple(sst);
            return detail != null ? detail : Names.n(sst.getName());
        }
        return c != null ? Names.n(c.getName()) : "?";
    }

    private static String formatSimple(SQLSimpleType sst) {
        String n = Names.n(sst.getName());
        if (sst.getCharacterMaximumLength() > 0L) {
            return n + "(" + sst.getCharacterMaximumLength() + ")";
        }
        if (sst.getNumericPrecision() > 0L) {
            return n + "(" + sst.getNumericPrecision()
                    + (sst.getNumericScale() > 0L ? "," + sst.getNumericScale() : "")
                    + ")";
        }
        return n;
    }

    private static String formatBase(SQLDistinctType dt) {
        SQLSimpleType base = dt.getSqlSimpleType();
        String n = base != null && base.getName() != null ? base.getName() : "?";
        if (dt.getLength() > 0L) return n + "(" + dt.getLength() + ")";
        if (dt.getPrecision() > 0L) {
            return n + "(" + dt.getPrecision()
                    + (dt.getScale() > 0L ? "," + dt.getScale() : "") + ")";
        }
        if (base != null) {
            String detail = formatSimple(base);
            return detail != null ? detail : n;
        }
        return n;
    }
}
