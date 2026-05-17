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

import java.util.List;

import org.eclipse.daanse.cwm.model.cwm.resource.relational.Schema;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.SchemaBody;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.daanse.diagram.core.render.DiagramFilter;
import org.eclipse.daanse.diagram.core.render.LabelledBoxOptions;
import org.eclipse.daanse.diagram.core.render.SchemaContainerOptions;
import org.eclipse.daanse.diagram.core.render.TableOptions;
import org.eclipse.daanse.diagram.kind.schema.TableBody;

/**
 * The "database" named diagram: relational schema with tables, views,
 * query column sets, primary/foreign keys, indexes, uniques, checks,
 * triggers, defaults, plus stored procedures and functions.
 *
 * <p>Configuration is supplied via a typed {@link Features} record passed
 * to the constructor. Two anchors, {@link Features#minimal()} and
 * {@link Features#full()}, are provided; consumers can build any custom
 * combination on top of either via {@code with*} methods.</p>
 */
public final class DatabaseDiagram {

    public enum KindDisplay { LEAF, CHAIN }

    public record Features(
            boolean columns,
            boolean columnTypes,
            boolean onlyKeyColumns,
            boolean pkIcons,
            boolean fkIcons,
            boolean nullableMarker,
            boolean defaultMarker,
            boolean indexes,
            boolean indexConnectors,
            boolean uniques,
            boolean uniqueConnectors,
            boolean checks,
            boolean checkConnectors,
            boolean triggers,
            boolean triggerConnectors,
            boolean defaults,
            boolean defaultConnectors,
            boolean fkEdges,
            boolean cardinalities,
            boolean procedures,
            boolean stereotype,
            KindDisplay kindDisplay) {

        public static Features minimal() {
            return new Features(
                    false, false, false,
                    false, false, false, false,
                    false, false,
                    false, false,
                    false, false,
                    false, false,
                    false, false,
                    false, false,
                    false,
                    false,
                    KindDisplay.LEAF);
        }

        public static Features full() {
            return new Features(
                    true, true, false,
                    true, true, true, true,
                    true, true,
                    true, true,
                    true, true,
                    true, true,
                    true, true,
                    true, true,
                    true,
                    false,
                    KindDisplay.CHAIN);
        }

        /* with-style copy methods */
        public Features withColumns(boolean v) { return new Features(v, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withColumnTypes(boolean v) { return new Features(columns, v, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withOnlyKeyColumns(boolean v) { return new Features(columns, columnTypes, v, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withPkIcons(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, v, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withFkIcons(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, v, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withNullableMarker(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, v, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withDefaultMarker(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, v, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withIndexes(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, v, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withIndexConnectors(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, v, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withUniques(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, v, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withUniqueConnectors(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, v, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withChecks(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, v, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withCheckConnectors(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, v, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withTriggers(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, v, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withTriggerConnectors(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, v, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withDefaults(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, v, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withDefaultConnectors(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, v, fkEdges, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withFkEdges(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, v, cardinalities, procedures, stereotype, kindDisplay); }
        public Features withCardinalities(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, v, procedures, stereotype, kindDisplay); }
        public Features withProcedures(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, v, stereotype, kindDisplay); }
        public Features withStereotype(boolean v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, v, kindDisplay); }
        public Features withKindDisplay(KindDisplay v) { return new Features(columns, columnTypes, onlyKeyColumns, pkIcons, fkIcons, nullableMarker, defaultMarker, indexes, indexConnectors, uniques, uniqueConnectors, checks, checkConnectors, triggers, triggerConnectors, defaults, defaultConnectors, fkEdges, cardinalities, procedures, stereotype, v); }
    }

    private final Features features;
    private final List<Schema> schemas;

    public DatabaseDiagram(Features features, List<Schema> schemas) {
        this.features = features;
        this.schemas = schemas;
    }

    public DatabaseDiagram(Features features, Schema schema) {
        this(features, List.of(schema));
    }

    public Diagram toDiagram() {
        Diagram d = new CwmSchemaConverter().convert(schemas);

        // Positive-list filter — the database diagram shows ONLY these
        // node kinds and prunes everything else (SQL type nodes, future
        // additions, anything unfamiliar).
        Set<String> allowed = new HashSet<>(Set.of(
                "SCHEMA",
                "TABLE", "VIEW", "QUERY COLUMN SET"));
        if (features.procedures) {
            allowed.add("PROCEDURES");
            allowed.add("FUNCTIONS");
            allowed.add("PROCEDURE");
            allowed.add("FUNCTION");
        }
        DiagramFilter.retainNodesWithKindIn(d, allowed);

        TableOptions tableOpts = TableOptions.builder()
                .showColumns(features.columns)
                .showColumnTypes(features.columnTypes)
                .showOnlyKeyColumns(features.onlyKeyColumns)
                .showPkIcons(features.pkIcons)
                .showFkIcons(features.fkIcons)
                .showNullableColumn(features.nullableMarker)
                .showDefaultColumn(features.defaultMarker)
                .showIndexes(features.indexes)
                .showIndexConnectors(features.indexConnectors)
                .showUniqueConstraints(features.uniques)
                .showUniqueConnectors(features.uniqueConnectors)
                .showCheckConstraints(features.checks)
                .showCheckConnectors(features.checkConnectors)
                .showTriggers(features.triggers)
                .showTriggerConnectors(features.triggerConnectors)
                .showDefaults(features.defaults)
                .showDefaultConnectors(features.defaultConnectors)
                .showFkEdges(features.fkEdges)
                .showCardinalities(features.cardinalities)
                .showStereotype(features.stereotype)
                .showKindAncestors(features.kindDisplay == KindDisplay.CHAIN)
                .build();
        LabelledBoxOptions labelledOpts = LabelledBoxOptions.builder()
                .showStereotype(features.stereotype)
                .showRows(true)
                .showKindAncestors(features.kindDisplay == KindDisplay.CHAIN)
                .build();
        SchemaContainerOptions schemaOpts = SchemaContainerOptions.builder()
                .showKindAncestors(features.kindDisplay == KindDisplay.CHAIN)
                .build();

        // Apply options to every existing body and re-compute size hints.
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

        return d;
    }
}
