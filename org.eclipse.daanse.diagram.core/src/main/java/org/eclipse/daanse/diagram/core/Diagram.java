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
package org.eclipse.daanse.diagram.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Top-level diagram container. Holds top-level nodes and edges. */
public final class Diagram {

    private final Map<String, DNode> nodesById = new LinkedHashMap<>();
    private final List<DEdge> edges = new ArrayList<>();
    private final Map<String, String> options = new LinkedHashMap<>();
    private String css;
    private String title;

    public Diagram() {
        options.put("elk.algorithm", "layered");
        options.put("elk.direction", "RIGHT");
        options.put("elk.spacing.nodeNode", "40");
        options.put("elk.layered.spacing.nodeNodeBetweenLayers", "60");
        options.put("elk.padding", "[top=20,left=20,bottom=20,right=20]");
        options.put("elk.hierarchyHandling", "INCLUDE_CHILDREN");
        options.put("elk.edgeRouting", "ORTHOGONAL");
        // Place edge labels so they do not overlap nodes/other edges.
        // The larger edgeLabel spacing keeps the caption off the cardinality
        // pills that sit at the endpoints.
        options.put("elk.edgeLabels.placement", "CENTER");
        options.put("elk.edgeLabels.inline", "false");
        options.put("elk.spacing.edgeLabel", "12");
        options.put("elk.spacing.edgeNode", "24");
        options.put("elk.spacing.edgeEdge", "16");
    }

    public DNode addNode(DNode n) {
        nodesById.put(n.id(), n);
        return n;
    }

    /** Removes a node from the diagram's id index, so it no longer appears in
     *  {@link #topLevelNodes()} or {@link #allNodes()}. The caller is responsible
     *  for unlinking it from any parent's children list and from edges. */
    public void removeNode(DNode n) {
        nodesById.remove(n.id());
    }

    public DEdge addEdge(DEdge e) {
        edges.add(e);
        return e;
    }

    public DNode node(String id) {
        return nodesById.get(id);
    }

    public List<DNode> topLevelNodes() {
        List<DNode> result = new ArrayList<>();
        for (DNode n : nodesById.values()) {
            if (n.parent() == null) {
                result.add(n);
            }
        }
        return result;
    }

    public List<DEdge> edges() { return edges; }

    public Map<String, String> options() { return options; }

    public Diagram option(String key, String value) {
        options.put(key, value);
        return this;
    }

    public String css() { return css; }
    public Diagram css(String css) { this.css = css; return this; }

    public String title() { return title; }
    public Diagram title(String t) { this.title = t; return this; }

    /** Walks the diagram and yields every node (nested included) in insertion order. */
    public List<DNode> allNodes() {
        List<DNode> out = new ArrayList<>();
        for (DNode n : topLevelNodes()) {
            collect(n, out);
        }
        return out;
    }

    private void collect(DNode n, List<DNode> out) {
        out.add(n);
        for (DNode c : n.children()) {
            collect(c, out);
        }
    }
}
