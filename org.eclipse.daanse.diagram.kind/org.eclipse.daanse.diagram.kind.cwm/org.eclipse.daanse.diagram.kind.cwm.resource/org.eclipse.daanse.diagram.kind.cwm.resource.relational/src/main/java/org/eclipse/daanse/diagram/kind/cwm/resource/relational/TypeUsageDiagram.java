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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.daanse.cwm.model.cwm.resource.relational.Schema;
import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.SchemaBody;
import org.eclipse.daanse.diagram.core.render.DiagramFilter;
import org.eclipse.daanse.diagram.core.render.LabelledBoxOptions;
import org.eclipse.daanse.diagram.core.render.SchemaContainerOptions;
import org.eclipse.daanse.diagram.core.render.TableOptions;
import org.eclipse.daanse.diagram.kind.schema.TableBody;

/**
 * The "type-usage" named diagram: a database schema where the focus is on
 * SQL types — distinct types, simple types they inherit from, and the
 * columns that reference them. Tables show only key columns plus columns
 * whose type is one of the rendered type nodes.
 */
public final class TypeUsageDiagram {

    public enum KindDisplay { LEAF, CHAIN }

    public record Features(
            boolean simpleTypeNodes,
            boolean distinctTypeInheritanceEdges,
            boolean columnTypeRefEdges,
            boolean cardinalities,
            KindDisplay kindDisplay) {

        public static Features minimal() {
            return new Features(false, false, false, false, KindDisplay.LEAF);
        }

        public static Features full() {
            return new Features(true, true, true, true, KindDisplay.CHAIN);
        }

        public Features withSimpleTypeNodes(boolean v) {
            return new Features(v, distinctTypeInheritanceEdges, columnTypeRefEdges, cardinalities, kindDisplay);
        }
        public Features withDistinctTypeInheritanceEdges(boolean v) {
            return new Features(simpleTypeNodes, v, columnTypeRefEdges, cardinalities, kindDisplay);
        }
        public Features withColumnTypeRefEdges(boolean v) {
            return new Features(simpleTypeNodes, distinctTypeInheritanceEdges, v, cardinalities, kindDisplay);
        }
        public Features withCardinalities(boolean v) {
            return new Features(simpleTypeNodes, distinctTypeInheritanceEdges, columnTypeRefEdges, v, kindDisplay);
        }
        public Features withKindDisplay(KindDisplay v) {
            return new Features(simpleTypeNodes, distinctTypeInheritanceEdges, columnTypeRefEdges, cardinalities, v);
        }
    }

    private final Features features;
    private final List<Schema> schemas;

    public TypeUsageDiagram(Features features, List<Schema> schemas) {
        this.features = features;
        this.schemas = schemas;
    }

    public TypeUsageDiagram(Features features, Schema schema) {
        this(features, List.of(schema));
    }

    public Diagram toDiagram() {
        Diagram d = new CwmSchemaConverter().convert(schemas);

        // Positive-list filter — the type-usage diagram shows tables/schemas
        // and the type nodes the user opted into. Everything else is pruned.
        Set<String> allowed = new HashSet<>(Set.of(
                "SCHEMA",
                "TABLE", "VIEW", "QUERY COLUMN SET",
                "DISTINCT TYPE", "STRUCTURED TYPE"));
        if (features.simpleTypeNodes) allowed.add("SIMPLE TYPE");
        DiagramFilter.retainNodesWithKindIn(d, allowed);

        // Tables show only key columns by default. We also reuse onlyKeyColumns
        // to act as "show only the columns that participate in something" —
        // the dependency edges visually connect type-refs to specific columns
        // anyway, so non-key non-typed columns are noise here.
        TableOptions tableOpts = TableOptions.builder()
                .showColumns(true)
                .showColumnTypes(true)
                .showOnlyKeyColumns(false)
                .showPkIcons(true)
                .showFkIcons(true)
                .showNullableColumn(false)
                .showDefaultColumn(false)
                .showIndexes(false).showIndexConnectors(false)
                .showUniqueConstraints(false).showUniqueConnectors(false)
                .showCheckConstraints(false).showCheckConnectors(false)
                .showTriggers(false).showTriggerConnectors(false)
                .showDefaults(false).showDefaultConnectors(false)
                .showFkEdges(false)
                .showCardinalities(features.cardinalities)
                .showStereotype(false)
                .showKindAncestors(features.kindDisplay == KindDisplay.CHAIN)
                .build();
        LabelledBoxOptions labelledOpts = LabelledBoxOptions.builder()
                .showRows(true)
                .showStereotype(false)
                .showKindAncestors(features.kindDisplay == KindDisplay.CHAIN)
                .build();
        SchemaContainerOptions schemaOpts = SchemaContainerOptions.builder()
                .showKindAncestors(features.kindDisplay == KindDisplay.CHAIN)
                .build();

        for (DNode n : d.allNodes()) {
            NodeBody body = n.body();
            if (body instanceof TableBody tb) {
                tb.options(tableOpts);
            } else if (body instanceof SchemaBody sb) {
                sb.options(schemaOpts);
            } else if (body instanceof LabelledBoxBody lb) {
                lb.options(labelledOpts);
            }
            double[] sz = body.sizeHint(n);
            n.setSize(sz[0], sz[1]);
            body.layoutPorts(n);
        }

        // Edge-level feature toggles — type-ref dashed edges and Distinct→Simple
        // inheritance edges. Edges to nodes that were already pruned were
        // dropped by DiagramFilter; here we just respect the explicit flags.
        Iterator<DEdge> it = d.edges().iterator();
        while (it.hasNext()) {
            DEdge e = it.next();
            String id = e.id() == null ? "" : e.id();
            if (id.startsWith("typeinherit_") && !features.distinctTypeInheritanceEdges) it.remove();
            else if (id.startsWith("typeref_") && !features.columnTypeRefEdges) it.remove();
        }
        return d;
    }
}
