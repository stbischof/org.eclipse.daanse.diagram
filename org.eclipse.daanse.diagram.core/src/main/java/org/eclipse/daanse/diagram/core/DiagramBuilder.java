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

import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Ids;

/**
 * Fluent helper for assembling a {@link Diagram} from a model walk.
 *
 * <p>Replaces the recurring boilerplate of every converter:</p>
 *
 * <ul>
 *   <li>maintaining a {@code Map<EObject, DNode>} keyed by source-model element,</li>
 *   <li>generating a node id from {@link System#identityHashCode(Object)} with a per-converter prefix,</li>
 *   <li>generating an edge id from the two endpoint hashes with another prefix,</li>
 *   <li>wrapping {@link DEndpoint.NodeEndpoint} / {@link DEndpoint.PortEndpoint} around the endpoints, and</li>
 *   <li>only adding nodes to the diagram once nesting decisions are settled.</li>
 * </ul>
 *
 * <p>Nodes are looked up by their source-model key (typically an EMF
 * EObject). Top-level membership is decided by {@link DNode#parent()}
 * — children added with {@link #nest(Object, Object)} are automatically
 * excluded from {@link Diagram#topLevelNodes()}, so callers no longer
 * need a parallel "is nested" set.</p>
 */
public final class DiagramBuilder {

    private final Diagram diagram;
    private final Map<Object, DNode> byKey = new IdentityHashMap<>();

    private DiagramBuilder(String title) {
        this.diagram = new Diagram();
        if (title != null) diagram.title(title);
    }

    public static DiagramBuilder of(String title) {
        return new DiagramBuilder(title);
    }

    public Diagram diagram() {
        return diagram;
    }

    /* ---- Nodes ---------------------------------------------------------- */

    /**
     * Register a node with the given body, keyed by {@code key}. The
     * generated id is {@code prefix + "_" + Integer.toHexString(System.identityHashCode(key))}.
     * If a node is already registered under {@code key} the existing one is
     * returned unchanged.
     */
    public DNode node(Object key, String idPrefix, NodeBody body) {
        DNode existing = byKey.get(key);
        if (existing != null) return existing;
        DNode n = new DNode(Ids.identity(idPrefix, key), body);
        byKey.put(key, n);
        diagram.addNode(n);
        return n;
    }

    /**
     * Register a node with a {@link Stereotype}-driven {@link LabelledBoxBody}.
     * The body uses default {@link LabelledBoxBody} options; callers can
     * apply diagram-class-specific render options after construction.
     * Returns the body so callers can add detail rows.
     */
    public LabelledBoxBody labelled(Object key, Stereotype stereotype, String name, String idPrefix) {
        if (byKey.containsKey(key)) {
            throw new IllegalStateException(
                    "Node already registered for key " + key + " (id "
                            + byKey.get(key).id() + ")");
        }
        LabelledBoxBody body = stereotype.body(name);
        DNode n = new DNode(Ids.identity(idPrefix, key), body);
        byKey.put(key, n);
        diagram.addNode(n);
        return body;
    }

    /**
     * Register a {@link SchemaBody} container, inheriting the diagram's
     * default {@code SchemaContainerOptions}.
     */
    public SchemaBody container(Object key, String title, String kind, String icon, String idPrefix) {
        if (byKey.containsKey(key)) {
            throw new IllegalStateException(
                    "Node already registered for key " + key + " (id "
                            + byKey.get(key).id() + ")");
        }
        SchemaBody body = new SchemaBody(title, kind, icon);
        DNode n = new DNode(Ids.identity(idPrefix, key), body);
        byKey.put(key, n);
        diagram.addNode(n);
        return body;
    }

    /** Look up a previously-registered node by its source-model key. */
    public DNode node(Object key) {
        return byKey.get(key);
    }

    /** True iff a node is registered under the given key. */
    public boolean has(Object key) {
        return byKey.containsKey(key);
    }

    /* ---- Nesting -------------------------------------------------------- */

    /** Nest the node registered under {@code childKey} inside the node
     *  registered under {@code parentKey}. Either may be missing — if so
     *  this is a no-op. The child is automatically removed from the
     *  diagram's top-level list (it now has a parent). */
    public DiagramBuilder nest(Object parentKey, Object childKey) {
        DNode p = byKey.get(parentKey);
        DNode c = byKey.get(childKey);
        if (p != null && c != null && c.parent() == null) {
            p.addChild(c);
        }
        return this;
    }

    /** Direct-handle variant of {@link #nest(Object, Object)} for callers
     *  that already hold the {@link DNode}s. */
    public DiagramBuilder nest(DNode parent, DNode child) {
        if (parent != null && child != null && child.parent() == null) {
            parent.addChild(child);
        }
        return this;
    }

    /* ---- Edges ---------------------------------------------------------- */

    /** Build a node-to-node edge between two registered keys. Returns the
     *  {@link DEdge} for further chaining ({@code .label(...)}, {@code .kind(...)} ...).
     *  If either key is unknown the edge is not created and {@code null} is returned. */
    public DEdge edge(Object srcKey, Object tgtKey, String idPrefix) {
        DNode s = byKey.get(srcKey);
        DNode t = byKey.get(tgtKey);
        if (s == null || t == null) return null;
        return edge(s, t, idPrefix);
    }

    public DEdge edge(DNode source, DNode target, String idPrefix) {
        DEdge e = new DEdge(Ids.edge(idPrefix, source, target));
        e.addSource(new DEndpoint.NodeEndpoint(source));
        e.addTarget(new DEndpoint.NodeEndpoint(target));
        diagram.addEdge(e);
        return e;
    }

    /** Build a port-to-port edge. */
    public DEdge portEdge(DPort source, DPort target, String idPrefix) {
        DEdge e = new DEdge(Ids.edge(idPrefix, source, target));
        e.addSource(new DEndpoint.PortEndpoint(source));
        e.addTarget(new DEndpoint.PortEndpoint(target));
        diagram.addEdge(e);
        return e;
    }

    /** Build an edge from a port to a node (no specific port on the target). */
    public DEdge portToNodeEdge(DPort source, DNode target, String idPrefix) {
        DEdge e = new DEdge(Ids.edge(idPrefix, source, target));
        e.addSource(new DEndpoint.PortEndpoint(source));
        e.addTarget(new DEndpoint.NodeEndpoint(target));
        diagram.addEdge(e);
        return e;
    }

    /** Build an empty edge with a known id; caller wires sources/targets.
     *  Useful for hyperedges with multiple endpoints. */
    public DEdge edge(String id) {
        DEdge e = new DEdge(id);
        diagram.addEdge(e);
        return e;
    }
}
