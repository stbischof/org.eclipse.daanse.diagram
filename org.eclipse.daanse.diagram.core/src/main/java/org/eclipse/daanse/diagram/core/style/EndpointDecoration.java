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
package org.eclipse.daanse.diagram.core.style;

/**
 * Visual decoration drawn at an edge endpoint. Used by {@code DEdge}'s
 * {@code sourceDecoration()} / {@code targetDecoration()} fields, which
 * the SVG renderer dispatches on.
 *
 * <p>Covers the common UML notations (open/closed arrow, hollow/filled
 * triangle and diamond) plus crow's-foot notation for ER diagrams. When
 * an edge has no decoration set, the renderer falls back to a marker
 * picked from {@link org.eclipse.daanse.diagram.core.DEdge.Kind}.</p>
 */
public enum EndpointDecoration {
    NONE,
    OPEN_ARROW,
    CLOSED_ARROW,
    TRIANGLE_HOLLOW,
    TRIANGLE_FILLED,
    DIAMOND_HOLLOW,
    DIAMOND_FILLED,
    CROWS_FOOT_MANY,
    CROWS_FOOT_ONE,
    CROWS_FOOT_MANY_OPT,
    CROWS_FOOT_ONE_OPT
}
