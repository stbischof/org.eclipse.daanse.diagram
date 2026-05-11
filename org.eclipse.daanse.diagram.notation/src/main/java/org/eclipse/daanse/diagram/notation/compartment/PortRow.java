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
package org.eclipse.daanse.diagram.notation.compartment;

import java.util.List;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

/**
 * Row that contributes a WEST and EAST port pair anchored to its vertical
 * centre. Used for column-exact docking on table rows, attribute / reference
 * rows on UML class compartments, etc. Visually identical to
 * {@link IconRow}; the port pair is invisible but addressable via
 * {@link #port(PortSide)} and {@link CompartmentBody#port(String, PortSide)}.
 */
public final class PortRow implements Row {

    private static final double PORT_SIZE = 4.0;

    private final String key;
    private final IconRow body;
    private DPort west;
    private DPort east;

    public PortRow(String key, IconBadge badge, String name, String rightText) {
        this.key = key;
        this.body = new IconRow(badge, name, rightText);
    }

    /** Stable key (typically the column / reference name) used to look the
     *  port pair up via {@link CompartmentBody#port(String, PortSide)}. */
    public String key() {
        return key;
    }

    /** Returns the port for the requested side; {@code null} if
     *  {@link #createPorts(DNode)} has not yet run. */
    public DPort port(PortSide side) {
        return side == PortSide.WEST ? west : east;
    }

    @Override
    public double height() {
        return body.height();
    }

    @Override
    public void render(SvgElem g, double y, double width) {
        body.render(g, y, width);
    }

    @Override
    public List<DPort> createPorts(DNode owner) {
        west = new DPort(owner.id() + ".west." + key, owner)
                .side(PortSide.WEST).size(PORT_SIZE, PORT_SIZE);
        east = new DPort(owner.id() + ".east." + key, owner)
                .side(PortSide.EAST).size(PORT_SIZE, PORT_SIZE);
        return List.of(west, east);
    }

    @Override
    public void layoutPorts(DNode owner, double rowMidY, double width) {
        if (west != null) west.position(0, rowMidY - PORT_SIZE / 2);
        if (east != null) east.position(width - PORT_SIZE, rowMidY - PORT_SIZE / 2);
    }
}
