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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.daanse.cwm.model.cwm.foundation.datatypes.QueryExpression;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Feature;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Column;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.NamedColumnSet;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.QueryColumnSet;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Schema;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.View;
import org.eclipse.daanse.cwm.query.resolve.ProducedColumn;
import org.eclipse.daanse.cwm.query.resolve.QueryResolver;
import org.eclipse.daanse.cwm.query.resolve.Resolution;
import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DEndpoint;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.SchemaBody;
import org.eclipse.daanse.diagram.core.render.LabelledBoxOptions;
import org.eclipse.daanse.diagram.core.render.SchemaContainerOptions;
import org.eclipse.daanse.diagram.core.render.TableOptions;
import org.eclipse.daanse.diagram.kind.schema.TableBody;

/**
 * Named diagram showing a {@link QueryColumnSet} or {@link View}'s lineage:
 * the target NCS itself is rendered alongside the source tables it queries;
 * per-output-column lineage edges run from each output port to the underlying
 * source column it came from; an aggregate "south-port" edge from the target
 * to every column the query references in any clause (WHERE, JOIN, GROUP BY,
 * …) makes the full read-set visible at a glance.
 *
 * <p>Source tables and columns the query never touches are pruned, so the
 * reader sees exactly what the target consumes.</p>
 */
public final class QueryDiagram {

    public enum KindDisplay { LEAF, CHAIN }

    public record Features(
            boolean onlyUsedTables,
            boolean onlyUsedColumns,
            boolean lineageEdges,
            boolean usageEdges,
            boolean cardinalities,
            KindDisplay kindDisplay) {

        public static Features minimal() {
            return new Features(true, true, true, false, false, KindDisplay.LEAF);
        }

        public static Features full() {
            return new Features(true, true, true, true, true, KindDisplay.CHAIN);
        }

        public Features withOnlyUsedTables(boolean v)   { return new Features(v, onlyUsedColumns, lineageEdges, usageEdges, cardinalities, kindDisplay); }
        public Features withOnlyUsedColumns(boolean v)  { return new Features(onlyUsedTables, v, lineageEdges, usageEdges, cardinalities, kindDisplay); }
        public Features withLineageEdges(boolean v)     { return new Features(onlyUsedTables, onlyUsedColumns, v, usageEdges, cardinalities, kindDisplay); }
        public Features withUsageEdges(boolean v)       { return new Features(onlyUsedTables, onlyUsedColumns, lineageEdges, v, cardinalities, kindDisplay); }
        public Features withCardinalities(boolean v)    { return new Features(onlyUsedTables, onlyUsedColumns, lineageEdges, usageEdges, v, kindDisplay); }
        public Features withKindDisplay(KindDisplay v)  { return new Features(onlyUsedTables, onlyUsedColumns, lineageEdges, usageEdges, cardinalities, v); }
    }

    private final Features features;
    private final List<Schema> schemas;
    private final org.eclipse.daanse.cwm.model.cwm.resource.relational.ColumnSet target;
    private final QueryExpression query;

    public QueryDiagram(Features features, List<Schema> schemas, QueryColumnSet qcs) {
        this(features, schemas, qcs, qcs == null ? null : qcs.getQuery());
    }

    public QueryDiagram(Features features, List<Schema> schemas, View view) {
        this(features, schemas, view, view == null ? null : view.getQueryExpression());
    }

    private QueryDiagram(Features features, List<Schema> schemas,
            org.eclipse.daanse.cwm.model.cwm.resource.relational.ColumnSet target,
            QueryExpression query) {
        this.features = features;
        this.schemas = schemas;
        this.target = target;
        this.query = query;
    }

    public Resolution resolve() {
        return QueryResolver.of(schemas).resolve(query);
    }

    public Diagram toDiagram() {
        Resolution res = QueryResolver.of(schemas).resolve(query);

        // Build the include filter additively from the resolution: only the
        // tables the query touches (plus the target itself) and only the
        // columns it references (plus the target's declared output columns)
        // make it into the converted diagram. No FK edges, types or
        // procedures — none are relevant to the query slice.
        Set<org.eclipse.daanse.cwm.model.cwm.resource.relational.ColumnSet> includedSets =
                new HashSet<>();
        if (res.ok()) includedSets.addAll(res.tablesUsed());
        includedSets.add(target);

        Map<org.eclipse.daanse.cwm.model.cwm.resource.relational.ColumnSet, Set<Column>>
                includedColumns = new HashMap<>();
        if (res.ok()) {
            for (Column c : res.columnsUsed()) {
                if (c.eContainer() instanceof org.eclipse.daanse.cwm.model.cwm.resource.relational.ColumnSet cs) {
                    includedColumns.computeIfAbsent(cs, k -> new HashSet<>()).add(c);
                }
            }
        }
        Set<Column> targetCols = new HashSet<>();
        for (Feature f : target.getFeature()) if (f instanceof Column c) targetCols.add(c);
        includedColumns.put(target, targetCols);

        boolean restrictTables = features.onlyUsedTables && res.ok();
        boolean restrictColumns = features.onlyUsedColumns && res.ok();

        CwmSchemaConverter.Filter filter = new CwmSchemaConverter.Filter(
                cs -> !restrictTables || includedSets.contains(cs),
                (cs, c) -> {
                    if (!restrictColumns) return true;
                    Set<Column> allowed = includedColumns.get(cs);
                    return allowed != null && allowed.contains(c);
                },
                false,  // FK edges are noise in a lineage view
                false,  // simple/distinct/structured types not relevant
                false   // procedures/functions not relevant
        );

        Diagram d = new CwmSchemaConverter().convert(schemas, filter);

        // Locate the target node + every source-table node by name.
        DNode targetNode = null;
        Map<String, DNode> tableByName = new HashMap<>();
        for (DNode n : d.allNodes()) {
            if (!(n.body() instanceof TableBody tb)) continue;
            if (target.getName() != null && target.getName().equals(tb.title()) && targetNode == null) {
                targetNode = n;
            } else {
                tableByName.put(tb.title(), n);
            }
        }

        applyOptions(d);

        if (targetNode != null && res.ok()) {
            if (features.lineageEdges) addLineageEdges(d, targetNode, res, tableByName);
            if (features.usageEdges)   addUsageEdges(d, targetNode, res, tableByName);
        }

        d.title(res.ok()
                ? "lineage of " + (target.getName() == null ? "?" : target.getName())
                        + ": " + res.columnsUsed().size() + " column(s) across " + res.tablesUsed().size() + " table(s)"
                : "query failed: " + res.failure().map(Enum::name).orElse("?") + " — " + res.message());
        return d;
    }

    /* ------------------------------------------------------------------ */

    private static String ownerName(Column c) {
        return c.eContainer() instanceof NamedColumnSet n && n.getName() != null
                ? n.getName() : null;
    }

    private static boolean touchesAny(DEdge e, Set<DNode> nodes) {
        for (var ep : e.sources()) if (nodes.contains(ep.node())) return true;
        for (var ep : e.targets()) if (nodes.contains(ep.node())) return true;
        return false;
    }

    private void applyOptions(Diagram d) {
        TableOptions tableOpts = TableOptions.builder()
                .showColumns(true)
                .showColumnTypes(true)
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
            if (body instanceof TableBody tb) tb.options(tableOpts);
            else if (body instanceof SchemaBody sb) sb.options(schemaOpts);
            else if (body instanceof LabelledBoxBody lb) lb.options(labelledOpts);
            double[] sz = body.sizeHint(n);
            n.setSize(sz[0], sz[1]);
            body.layoutPorts(n);
        }
    }

    /** One dependency edge per declared output column → its source CWM
     *  Column's port. Output columns whose projection is a function/union/
     *  literal (no single source) are skipped — they have no lineage. */
    private void addLineageEdges(Diagram d, DNode targetNode, Resolution res,
                                  Map<String, DNode> tableByName) {
        TableBody targetBody = (TableBody) targetNode.body();
        // Match declared output feature columns to produced columns by index +
        // name. Resolution.producedColumns() is in projection order; the
        // target's getFeature() is in declaration order.
        List<ProducedColumn> produced = res.producedColumns();
        Map<String, ProducedColumn> byName = new LinkedHashMap<>();
        for (ProducedColumn pc : produced) {
            if (pc.name() != null) byName.put(pc.name().toLowerCase(Locale.ROOT), pc);
        }
        int i = 0;
        for (Feature f : target.getFeature()) {
            if (!(f instanceof Column declared) || declared.getName() == null) { i++; continue; }
            String key = declared.getName().toLowerCase(Locale.ROOT);
            ProducedColumn pc = byName.get(key);
            // Fallback: positional match if name doesn't line up.
            if (pc == null && i < produced.size()) pc = produced.get(i);
            i++;
            if (pc == null || pc.source() == null) continue;
            String srcTable = ownerName(pc.source());
            if (srcTable == null) continue;
            DNode tableNode = tableByName.get(srcTable);
            if (tableNode == null || !(tableNode.body() instanceof TableBody srcBody)) continue;
            DPort srcPort = portOrNull(srcBody, pc.source().getName(), PortSide.EAST);
            DPort tgtPort = portOrNull(targetBody, declared.getName(), PortSide.WEST);
            if (srcPort == null || tgtPort == null) continue;
            DEdge edge = new DEdge("lineage_" + Integer.toHexString(System.identityHashCode(declared)));
            edge.kind(DEdge.Kind.DEPENDENCY);
            edge.label(declared.getName());
            edge.addSource(new DEndpoint.PortEndpoint(srcPort));
            edge.addTarget(new DEndpoint.PortEndpoint(tgtPort));
            d.addEdge(edge);
        }
    }

    /** Add a synthetic SOUTH port on the target and connect it to every used
     *  source column (any clause), so the target's bottom side advertises the
     *  full read-set including WHERE / JOIN / GROUP BY / etc. — not just the
     *  projected output columns. */
    private void addUsageEdges(Diagram d, DNode targetNode, Resolution res,
                                Map<String, DNode> tableByName) {
        DPort southPort = new DPort(targetNode.id() + ".south", targetNode)
                .side(PortSide.SOUTH)
                .label("uses");
        // Position centered along the bottom of the target node.
        southPort.position(targetNode.width() / 2.0 - 2, targetNode.height() - 4);
        targetNode.addPort(southPort);

        for (Column used : res.columnsUsed()) {
            String t = ownerName(used);
            if (t == null) continue;
            DNode tableNode = tableByName.get(t);
            if (tableNode == null || !(tableNode.body() instanceof TableBody srcBody)) continue;
            DPort srcPort = portOrNull(srcBody, used.getName(), PortSide.WEST);
            if (srcPort == null) continue;
            DEdge edge = new DEdge("uses_" + Integer.toHexString(System.identityHashCode(used)));
            edge.kind(DEdge.Kind.DEPENDENCY);
            edge.addSource(new DEndpoint.PortEndpoint(southPort));
            edge.addTarget(new DEndpoint.PortEndpoint(srcPort));
            d.addEdge(edge);
        }
    }

    private static DPort portOrNull(TableBody tb, String columnName, PortSide side) {
        try {
            return tb.port(columnName, side);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
