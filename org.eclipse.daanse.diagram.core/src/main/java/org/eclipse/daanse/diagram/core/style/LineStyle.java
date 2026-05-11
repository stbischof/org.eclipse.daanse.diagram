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
 * Line style for an edge stroke. {@link #DASHED} historically signalled a
 * dependency edge in the renderer; with explicit line-style support it is
 * available to any edge.
 */
public enum LineStyle {
    SOLID,
    DASHED,
    DOTTED
}
