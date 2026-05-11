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
import java.util.Objects;

/**
 * A diagram node. Has identity, an optional parent (for nesting such as a
 * Schema containing Tables), a body that owns its intrinsic content and
 * rendering, ports on its border, and nested children.
 * <p>
 * Position/size are filled in by the layout step.
 */
public final class DNode {

    private final String id;
    private final NodeBody body;
    private final List<DPort> ports = new ArrayList<>();
    private final List<DNode> children = new ArrayList<>();
    private final Map<String, String> layoutOptions = new LinkedHashMap<>();
    private DNode parent;

    private double x, y, width, height;
    private boolean sizeFixed;

    public DNode(String id, NodeBody body) {
        this.id = Objects.requireNonNull(id);
        this.body = Objects.requireNonNull(body);
        double[] hint = body.sizeHint(this);
        this.width = hint[0];
        this.height = hint[1];
        body.createPorts(this).forEach(this::addPort);
        if (body.elkPadding() != null) {
            layoutOption("elk.padding", body.elkPadding());
        }
    }

    public String id() { return id; }
    public NodeBody body() { return body; }

    public List<DPort> ports() { return ports; }
    public List<DNode> children() { return children; }
    public DNode parent() { return parent; }

    public Map<String, String> layoutOptions() { return layoutOptions; }

    public DNode layoutOption(String key, String value) {
        layoutOptions.put(key, value);
        return this;
    }

    public DNode addChild(DNode child) {
        child.parent = this;
        children.add(child);
        return child;
    }

    public DPort addPort(DPort port) {
        ports.add(port);
        return port;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double width() { return width; }
    public double height() { return height; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setSize(double w, double h) { this.width = w; this.height = h; }

    /** Marks the node as having a fixed size the layout must not override. */
    public DNode fixSize() { this.sizeFixed = true; return this; }
    public boolean isSizeFixed() { return sizeFixed; }

    /** Absolute x including all ancestors. Valid after layout. */
    public double absX() {
        return (parent == null ? 0 : parent.absX()) + x;
    }

    public double absY() {
        return (parent == null ? 0 : parent.absY()) + y;
    }

    @Override
    public String toString() {
        return "DNode[" + id + "]";
    }
}
