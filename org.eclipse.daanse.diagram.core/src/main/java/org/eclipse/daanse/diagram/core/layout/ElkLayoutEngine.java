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
package org.eclipse.daanse.diagram.core.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DEndpoint;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.PortConstraints;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkBendPoint;
import org.eclipse.elk.graph.ElkConnectableShape;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.util.ElkGraphUtil;

/**
 * Drives ELK Java to lay out a {@link Diagram} and writes the computed
 * positions and edge routes back into the diagram model. Supports hierarchy,
 * fixed-position ports and hyperedges (for composite foreign keys).
 */
public final class ElkLayoutEngine {

    static {
        LayoutMetaDataService.getInstance().registerLayoutMetaDataProviders(
                new LayeredMetaDataProvider());
    }

    public void layout(Diagram diagram) {
        trunks.clear();
        for (DNode n : diagram.allNodes()) {
            n.body().layoutPorts(n);
        }

        ElkNode root = ElkGraphUtil.createGraph();
        applyOptions(root, diagram.options());

        Map<DNode, ElkNode> nodeMap = new HashMap<>();
        Map<DPort, ElkPort> portMap = new HashMap<>();
        Map<DEdge, List<ElkEdge>> edgeMap = new HashMap<>();

        for (DNode n : diagram.topLevelNodes()) {
            buildNode(n, root, nodeMap, portMap);
        }
        for (DEdge e : diagram.edges()) {
            buildEdge(e, nodeMap, portMap, edgeMap);
        }

        new RecursiveGraphLayoutEngine().layout(root, new BasicProgressMonitor());

        for (Map.Entry<DNode, ElkNode> en : nodeMap.entrySet()) {
            ElkNode e = en.getValue();
            DNode d = en.getKey();
            d.setPosition(e.getX(), e.getY());
            d.setSize(e.getWidth(), e.getHeight());
        }
        for (Map.Entry<DPort, ElkPort> pe : portMap.entrySet()) {
            ElkPort ep = pe.getValue();
            pe.getKey().position(ep.getX(), ep.getY())
                    .size(ep.getWidth(), ep.getHeight());
        }
        for (Map.Entry<DEdge, List<ElkEdge>> en : edgeMap.entrySet()) {
            DEdge de = en.getKey();
            de.sections().clear();
            for (ElkEdge ee : en.getValue()) {
                double[] off = absOrigin((ElkNode) ee.getContainingNode());
                for (ElkEdgeSection s : ee.getSections()) {
                    DEdge.Section sec = new DEdge.Section();
                    sec.startX = s.getStartX() + off[0];
                    sec.startY = s.getStartY() + off[1];
                    sec.endX = s.getEndX() + off[0];
                    sec.endY = s.getEndY() + off[1];
                    for (ElkBendPoint bp : s.getBendPoints()) {
                        sec.bendPoints.add(new double[] {
                                bp.getX() + off[0], bp.getY() + off[1] });
                    }
                    de.sections().add(sec);
                }
                for (ElkLabel lbl : ee.getLabels()) {
                    de.setLabelLayout(lbl.getX() + off[0], lbl.getY() + off[1],
                            lbl.getWidth(), lbl.getHeight());
                    break;
                }
            }
        }
        // Insert a synthetic trunk section between source-legs and target-legs
        // for each hyperedge, so the renderer draws one continuous blue line
        // across the invisible junction node.
        for (Trunk t : trunks) {
            double[] off = absOrigin((ElkNode) t.junction.getParent());
            double wx = t.junction.getX() + t.west.getX() + t.west.getWidth() + off[0];
            double wy = t.junction.getY() + t.west.getY() + t.west.getHeight() / 2 + off[1];
            double ex = t.junction.getX() + t.east.getX() + off[0];
            double ey = t.junction.getY() + t.east.getY() + t.east.getHeight() / 2 + off[1];
            DEdge.Section trunk = new DEdge.Section();
            trunk.startX = wx;
            trunk.startY = wy;
            trunk.endX = ex;
            trunk.endY = ey;
            if (t.insertAt <= t.edge.sections().size()) {
                t.edge.sections().add(t.insertAt, trunk);
            } else {
                t.edge.sections().add(trunk);
            }
        }

        for (DNode n : diagram.allNodes()) {
            n.body().layoutPorts(n);
        }
    }

    private void buildNode(DNode d, ElkNode parent,
                           Map<DNode, ElkNode> nodeMap,
                           Map<DPort, ElkPort> portMap) {
        ElkNode en = ElkGraphUtil.createNode(parent);
        en.setIdentifier(d.id());
        en.setDimensions(d.width(), d.height());
        applyOptions(en, d.layoutOptions());

        if (!d.ports().isEmpty()) {
            en.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        }
        // Nodes with children need a hint to ELK that their size is locked.
        if (d.isSizeFixed()) {
            en.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS,
                    java.util.EnumSet.noneOf(org.eclipse.elk.core.options.SizeConstraint.class));
        }

        for (DPort p : d.ports()) {
            ElkPort ep = ElkGraphUtil.createPort(en);
            ep.setIdentifier(p.id());
            ep.setLocation(p.x(), p.y());
            ep.setDimensions(p.width(), p.height());
            if (p.side() != PortSide.UNDEFINED) {
                ep.setProperty(CoreOptions.PORT_SIDE, toElk(p.side()));
            }
            portMap.put(p, ep);
        }

        nodeMap.put(d, en);

        for (DNode child : d.children()) {
            buildNode(child, en, nodeMap, portMap);
        }
    }

    private void buildEdge(DEdge d,
                           Map<DNode, ElkNode> nodeMap,
                           Map<DPort, ElkPort> portMap,
                           Map<DEdge, List<ElkEdge>> edgeMap) {
        List<ElkConnectableShape> src = new ArrayList<>();
        List<ElkConnectableShape> tgt = new ArrayList<>();
        for (DEndpoint ep : d.sources()) {
            src.add(resolve(ep, nodeMap, portMap));
        }
        for (DEndpoint ep : d.targets()) {
            tgt.add(resolve(ep, nodeMap, portMap));
        }
        if (src.isEmpty() || tgt.isEmpty()) {
            return;
        }
        ElkNode container = commonAncestor(src, tgt);
        List<ElkEdge> parts = new ArrayList<>();

        if (src.size() == 1 && tgt.size() == 1) {
            ElkEdge ee = ElkGraphUtil.createEdge(null);
            ee.setIdentifier(d.id());
            ee.getSources().add(src.get(0));
            ee.getTargets().add(tgt.get(0));
            container.getContainedEdges().add(ee);
            attachLabel(ee, d);
            parts.add(ee);
        } else {
            // Hyperedge: not natively supported by ELK Layered, so we insert
            // TWO invisible junction nodes — one near the source, one near
            // the target. Source-legs all converge at junctionA.WEST; a
            // single middle edge spans junctionA.EAST → junctionB.WEST;
            // target-legs diverge from junctionB.EAST. Result: the trunk
            // stays bundled until just before the target ports.
            ElkNode junctionA = createJunction(container, d.id() + "_jA");
            ElkPort jaW = junctionA.getPorts().get(0);
            ElkPort jaE = junctionA.getPorts().get(1);
            ElkNode junctionB = createJunction(container, d.id() + "_jB");
            ElkPort jbW = junctionB.getPorts().get(0);
            ElkPort jbE = junctionB.getPorts().get(1);

            int i = 0;
            for (ElkConnectableShape s : src) {
                ElkEdge ee = ElkGraphUtil.createEdge(null);
                ee.setIdentifier(d.id() + "_s" + i++);
                ee.getSources().add(s);
                ee.getTargets().add(jaW);
                container.getContainedEdges().add(ee);
                parts.add(ee);
            }
            // The single, long-running middle trunk.
            ElkEdge middle = ElkGraphUtil.createEdge(null);
            middle.setIdentifier(d.id() + "_middle");
            middle.getSources().add(jaE);
            middle.getTargets().add(jbW);
            container.getContainedEdges().add(middle);
            parts.add(middle);
            attachLabel(middle, d);

            i = 0;
            for (ElkConnectableShape s : tgt) {
                ElkEdge ee = ElkGraphUtil.createEdge(null);
                ee.setIdentifier(d.id() + "_t" + i++);
                ee.getSources().add(jbE);
                ee.getTargets().add(s);
                container.getContainedEdges().add(ee);
                parts.add(ee);
            }
            // Book-keeping for the two invisible slabs inside each junction:
            // the renderer needs a continuous line across them.
            trunks.add(new Trunk(d, junctionA, jaW, jaE, src.size()));
            trunks.add(new Trunk(d, junctionB, jbW, jbE, src.size() + 2));
        }
        edgeMap.put(d, parts);
    }

    /** Creates an invisible 4x4 junction node with one WEST and one EAST port. */
    private ElkNode createJunction(ElkNode container, String id) {
        double jW = 4;
        double jH = 4;
        ElkNode junction = ElkGraphUtil.createNode(container);
        junction.setIdentifier(id);
        junction.setDimensions(jW, jH);
        junction.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS,
                java.util.EnumSet.noneOf(org.eclipse.elk.core.options.SizeConstraint.class));
        junction.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        ElkPort w = ElkGraphUtil.createPort(junction);
        w.setIdentifier(id + "_w");
        w.setLocation(0, jH / 2 - 0.5);
        w.setDimensions(1, 1);
        w.setProperty(CoreOptions.PORT_SIDE, org.eclipse.elk.core.options.PortSide.WEST);
        ElkPort e = ElkGraphUtil.createPort(junction);
        e.setIdentifier(id + "_e");
        e.setLocation(jW - 1, jH / 2 - 0.5);
        e.setDimensions(1, 1);
        e.setProperty(CoreOptions.PORT_SIDE, org.eclipse.elk.core.options.PortSide.EAST);
        return junction;
    }

    /** Junction book-keeping so we can add a trunk section after layout. */
    private record Trunk(DEdge edge, ElkNode junction, ElkPort west, ElkPort east,
                          int insertAt) {}
    private final List<Trunk> trunks = new ArrayList<>();

    /** Roughly estimate a label's bounding box (matching our renderer's font). */
    private void attachLabel(ElkEdge ee, DEdge d) {
        if (d.label() == null || d.label().isEmpty()) {
            return;
        }
        ElkLabel label = ElkGraphUtil.createLabel(ee);
        label.setText(d.label());
        double w = d.label().length() * 5.8 + 4;
        double h = 12;
        label.setDimensions(w, h);
    }

    private ElkConnectableShape resolve(DEndpoint ep,
                                        Map<DNode, ElkNode> nodeMap,
                                        Map<DPort, ElkPort> portMap) {
        if (ep instanceof DEndpoint.PortEndpoint pe) {
            return portMap.get(pe.port());
        }
        return nodeMap.get(ep.node());
    }

    private ElkNode commonAncestor(List<ElkConnectableShape> src,
                                   List<ElkConnectableShape> tgt) {
        List<ElkNode> anc = new ArrayList<>();
        for (ElkConnectableShape s : src) {
            anc.add(containerOf(s));
        }
        for (ElkConnectableShape s : tgt) {
            anc.add(containerOf(s));
        }
        ElkNode lca = anc.get(0);
        for (int i = 1; i < anc.size(); i++) {
            lca = lcaOf(lca, anc.get(i));
        }
        return lca;
    }

    private ElkNode containerOf(ElkConnectableShape s) {
        if (s instanceof ElkPort p) {
            return (ElkNode) p.getParent().getParent();
        }
        ElkNode n = (ElkNode) s;
        return n.getParent() != null ? (ElkNode) n.getParent() : n;
    }

    private ElkNode lcaOf(ElkNode a, ElkNode b) {
        List<ElkNode> pa = ancestors(a);
        List<ElkNode> pb = ancestors(b);
        ElkNode found = pa.get(pa.size() - 1);
        int i = pa.size() - 1, j = pb.size() - 1;
        while (i >= 0 && j >= 0 && pa.get(i) == pb.get(j)) {
            found = pa.get(i);
            i--; j--;
        }
        return found;
    }

    /** Absolute top-left of an ElkNode (summed parent offsets). */
    private double[] absOrigin(ElkNode n) {
        double x = 0, y = 0;
        for (ElkNode cur = n; cur != null && cur.getParent() != null;
                cur = (ElkNode) cur.getParent()) {
            x += cur.getX();
            y += cur.getY();
        }
        return new double[] { x, y };
    }

    private List<ElkNode> ancestors(ElkNode n) {
        List<ElkNode> out = new ArrayList<>();
        for (ElkNode cur = n; cur != null; cur = (ElkNode) cur.getParent()) {
            out.add(cur);
        }
        return out;
    }

    private void applyOptions(ElkNode n, Map<String, String> opts) {
        for (Map.Entry<String, String> e : opts.entrySet()) {
            try {
                var data = LayoutMetaDataService.getInstance().getOptionDataBySuffix(e.getKey());
                if (data == null) {
                    continue;
                }
                Object parsed = data.parseValue(e.getValue());
                if (parsed != null) {
                    n.setProperty(data, parsed);
                }
            } catch (RuntimeException ignored) {
                // Skip options we can't apply — keeps the layout robust against
                // accidentally misspelled option keys from users.
            }
        }
    }

    private org.eclipse.elk.core.options.PortSide toElk(PortSide s) {
        return switch (s) {
            case NORTH -> org.eclipse.elk.core.options.PortSide.NORTH;
            case SOUTH -> org.eclipse.elk.core.options.PortSide.SOUTH;
            case EAST -> org.eclipse.elk.core.options.PortSide.EAST;
            case WEST -> org.eclipse.elk.core.options.PortSide.WEST;
            default -> org.eclipse.elk.core.options.PortSide.UNDEFINED;
        };
    }
}
