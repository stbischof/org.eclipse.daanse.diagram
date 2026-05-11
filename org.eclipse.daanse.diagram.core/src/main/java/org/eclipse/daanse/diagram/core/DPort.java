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

import java.util.Objects;

/** A port is an anchor point on a node, used for port-level connections. */
public final class DPort {

    private final String id;
    private final DNode node;
    private PortSide side = PortSide.UNDEFINED;
    private String label;
    private double x, y, width = 4, height = 4;

    public DPort(String id, DNode node) {
        this.id = Objects.requireNonNull(id);
        this.node = Objects.requireNonNull(node);
    }

    public DPort side(PortSide side) { this.side = side; return this; }
    public DPort label(String l) { this.label = l; return this; }
    public DPort position(double x, double y) { this.x = x; this.y = y; return this; }
    public DPort size(double w, double h) { this.width = w; this.height = h; return this; }

    public String id() { return id; }
    public DNode node() { return node; }
    public PortSide side() { return side; }
    public String label() { return label; }
    public double x() { return x; }
    public double y() { return y; }
    public double width() { return width; }
    public double height() { return height; }
}
