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
import org.eclipse.daanse.diagram.core.svg.SvgElem;

/**
 * A single horizontal slice within a {@link Compartment}. Implementations
 * own their text, badge and styling; they only need to report a height
 * and emit SVG into a parent group.
 *
 * <p>Rows may also contribute ports anchored to their vertical position via
 * {@link #createPorts(DNode)} and {@link #layoutPorts(DNode, double, double)}.
 * The owning {@link CompartmentBody} walks the row list and calls these in
 * the right order so each row's port pair lines up with its rendered y.</p>
 */
public interface Row {

    /** Height the row occupies in node-local coordinates. */
    double height();

    /**
     * Emit SVG for this row into {@code g} relative to its top-left at
     * {@code (0, y)}, given the available {@code width}.
     */
    void render(SvgElem g, double y, double width);

    /**
     * Ports this row contributes to the owning node. Called once during the
     * node's port-creation phase. Default: no ports.
     */
    default List<DPort> createPorts(DNode owner) {
        return List.of();
    }

    /**
     * Re-anchor any ports created in {@link #createPorts(DNode)} now that
     * the node's final size is known. {@code rowMidY} is the vertical
     * centre of this row in node-local coordinates; {@code width} is the
     * node's width. Default: no-op.
     */
    default void layoutPorts(DNode owner, double rowMidY, double width) {
        // no ports by default
    }
}
