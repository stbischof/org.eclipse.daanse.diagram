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

/**
 * A small visual marker drawn next to a row's leading edge: PK 🔑, FK ↗,
 * abstract A, etc. {@code svgPath} is an SVG {@code <path d="…">} string
 * authored in a 12x12 viewBox; {@code label} is an optional textual
 * fallback (rendered as a single character glyph when no path is given).
 */
public record IconBadge(String svgPath, String fillColor, String strokeColor,
                        String label, String tooltip) {

    /** Path-based badge in a fixed 12x12 viewBox. */
    public static IconBadge path(String svgPath, String fillColor, String strokeColor, String tooltip) {
        return new IconBadge(svgPath, fillColor, strokeColor, null, tooltip);
    }

    /** Text-based badge (single character or short tag). */
    public static IconBadge text(String label, String fillColor, String tooltip) {
        return new IconBadge(null, fillColor, null, label, tooltip);
    }
}
