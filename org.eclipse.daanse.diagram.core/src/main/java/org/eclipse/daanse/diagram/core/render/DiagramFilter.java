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
package org.eclipse.daanse.diagram.core.render;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DEndpoint;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.SchemaBody;

/**
 * Positive-list filters for diagrams: each named diagram declares the set
 * of node "kinds" it wants to render, and the converter output is pruned
 * down to that set. Any node whose kind is not in the allow-list, plus
 * every edge touching a pruned node, is removed before layout.
 *
 * <p>The "kind" of a node is the leaf segment of its title-bar caption
 * (e.g. {@code "TABLE"}, {@code "MACHINE"}, {@code "SIMPLE TYPE"}). This
 * mirrors how each body type already advertises itself in the rendered
 * SVG, so the allow-list reads as exactly the captions a user would see.
 * Bodies whose kind cannot be determined (custom body types not handled
 * by {@link #leafKind(DNode)}) are pruned by default — guaranteeing the
 * output contains only nodes that the diagram explicitly opts into.</p>
 *
 * <p>Detection rules for {@link #leafKind(DNode)}:</p>
 * <ul>
 *   <li>{@link LabelledBoxBody} — leaf of {@code kindChain()}.</li>
 *   <li>{@link SchemaBody} — its {@code kind()} string.</li>
 *   <li>Any other body that exposes a {@code headerKind()} method
 *       returning a {@code HeaderKind}-style enum (e.g. schema's
 *       {@code TableBody}) is matched via reflection-free {@code toString}
 *       conversion ({@code "QUERY_COLUMN_SET" → "QUERY COLUMN SET"}).</li>
 * </ul>
 */
public final class DiagramFilter {

    private DiagramFilter() {}

    /** Best-effort leaf-kind extraction. Returns {@code null} for body
     *  types we don't recognise. */
    public static String leafKind(DNode node) {
        NodeBody body = node.body();
        if (body instanceof LabelledBoxBody lb) {
            return lb.kindChain().isEmpty() ? null
                    : lb.kindChain().get(lb.kindChain().size() - 1);
        }
        if (body instanceof SchemaBody sb) {
            return sb.kind();
        }
        // For any other body type, expect a public {@code headerKind()}
        // method returning an enum (TableBody pattern). We don't import
        // schema-module types from core; reflection-free dispatch via a
        // toString round-trip on the enum name.
        try {
            var m = body.getClass().getMethod("headerKind");
            Object kind = m.invoke(body);
            if (kind != null) {
                return kind.toString().replace('_', ' ');
            }
        } catch (ReflectiveOperationException ignored) {
            // not a headerKind-style body
        }
        return null;
    }

    /**
     * Retain only the nodes whose leaf kind is in {@code allowedKinds};
     * remove everything else from both the diagram's top-level list (by
     * detaching from any parent that owns them) and from each parent's
     * children list. Edges whose endpoints touch a removed node are also
     * removed.
     */
    public static void retainNodesWithKindIn(Diagram d, Set<String> allowedKinds) {
        Set<DNode> removed = new HashSet<>();
        for (DNode n : d.allNodes()) {
            String k = leafKind(n);
            if (k == null || !allowedKinds.contains(k)) {
                removed.add(n);
            }
        }
        d.edges().removeIf(e -> touchesAny(e, removed));
        for (DNode parent : d.allNodes()) {
            parent.children().removeIf(removed::contains);
        }
    }

    /** True iff any source or target of {@code e} is in {@code nodes}. */
    private static boolean touchesAny(DEdge e, Set<DNode> nodes) {
        for (DEndpoint ep : e.sources()) if (nodes.contains(ep.node())) return true;
        for (DEndpoint ep : e.targets()) if (nodes.contains(ep.node())) return true;
        return false;
    }
}
