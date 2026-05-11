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

import java.util.List;

import org.eclipse.daanse.diagram.core.svg.SvgElem;

/**
 * A node's rendering strategy. Separating this from {@link DNode} lets users
 * plug in custom visuals (Table with columns, class box with compartments,
 * schema container, etc.) without subclassing the core model.
 */
public interface NodeBody {

    /** Intrinsic minimum {w, h} before layout. */
    double[] sizeHint(DNode node);

    /**
     * Emit SVG into {@code g}, which is already translated so that (0, 0) is
     * the top-left corner of this node in the node's own local coordinates.
     */
    void render(SvgElem g, DNode node);

    /** Ports contributed by this body (called once from {@link DNode}'s ctor). */
    default List<DPort> createPorts(DNode node) { return List.of(); }

    /**
     * Called after the final node size is known (e.g. if expanded by the
     * layout) so the body can re-anchor ports. Coordinates are node-local.
     */
    default void layoutPorts(DNode node) {}

    /**
     * Optional ELK padding string (e.g. {@code [top=36,left=20,bottom=20,right=20]})
     * applied to this node so a container body can guarantee room for its
     * own chrome (title tab, borders, etc.) before any child is placed.
     * Return {@code null} to inherit the diagram default.
     */
    default String elkPadding() { return null; }
}
