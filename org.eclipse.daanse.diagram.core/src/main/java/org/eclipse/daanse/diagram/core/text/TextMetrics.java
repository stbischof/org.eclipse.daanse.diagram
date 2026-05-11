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
package org.eclipse.daanse.diagram.core.text;

/**
 * Single source of truth for text-width estimation. Bodies use this to
 * pick a reasonable initial width before ELK lays things out — ELK then
 * has the freedom to grow nodes if a child needs more room.
 *
 * <p>The estimate is char-count × per-char-width. It is calibrated for
 * the sans-serif fonts used by every body in this module:</p>
 *
 * <ul>
 *   <li>{@link #ROW} — body rows, 11 px regular weight (≈ 6.5 px/char)</li>
 *   <li>{@link #TITLE} — title-bar names, 13 px semibold (≈ 7.5 px/char)</li>
 *   <li>{@link #CAPTION} — small kind/section captions, 9 px semibold (≈ 6.0 px/char)</li>
 * </ul>
 */
public final class TextMetrics {

    private static final double ROW_FACTOR = 6.5;
    private static final double TITLE_FACTOR = 7.5;
    private static final double CAPTION_FACTOR = 6.0;

    private final double factor;

    private TextMetrics(double factor) {
        this.factor = factor;
    }

    public double width(String s) {
        return s == null ? 0 : s.length() * factor;
    }

    public static final TextMetrics ROW = new TextMetrics(ROW_FACTOR);
    public static final TextMetrics TITLE = new TextMetrics(TITLE_FACTOR);
    public static final TextMetrics CAPTION = new TextMetrics(CAPTION_FACTOR);

    public static double row(String s) {
        return ROW.width(s);
    }

    public static double title(String s) {
        return TITLE.width(s);
    }

    public static double caption(String s) {
        return CAPTION.width(s);
    }
}
