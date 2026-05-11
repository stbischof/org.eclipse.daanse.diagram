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
package org.eclipse.daanse.diagram.notation.edge;

import org.eclipse.daanse.diagram.core.style.EndpointDecoration;
import org.eclipse.daanse.diagram.core.style.LineStyle;

/**
 * Static appearance of an edge: line style plus the decoration drawn at
 * each endpoint. {@link #color()} is an optional CSS color override (null
 * means "use the renderer's default for this edge").
 */
public record EdgeStyle(LineStyle line,
                        EndpointDecoration source,
                        EndpointDecoration target,
                        String color) {

    public static EdgeStyle of(LineStyle line, EndpointDecoration source, EndpointDecoration target) {
        return new EdgeStyle(line, source, target, null);
    }
}
