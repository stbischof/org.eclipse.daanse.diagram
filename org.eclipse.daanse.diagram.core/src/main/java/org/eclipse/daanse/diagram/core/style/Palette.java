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
 * Tailwind-style colour tokens used across diagrams. Every hex literal
 * that appears in a converter or body should reference one of these
 * names so theme changes happen in one place.
 */
public final class Palette {

    private Palette() {}

    public static final String WHITE = "#ffffff";

    public static final String GRAY_50  = "#f9fafb";
    public static final String GRAY_100 = "#f3f4f6";
    public static final String GRAY_200 = "#e5e7eb";
    public static final String GRAY_300 = "#d1d5db";
    public static final String GRAY_400 = "#9ca3af";
    public static final String GRAY_500 = "#6b7280";
    public static final String GRAY_700 = "#374151";
    public static final String GRAY_800 = "#1f2937";
    public static final String GRAY_900 = "#111827";

    public static final String BLUE_50  = "#eff6ff";
    public static final String BLUE_100 = "#dbeafe";
    public static final String BLUE_200 = "#bfdbfe";
    public static final String BLUE_500 = "#3b82f6";
    public static final String BLUE_700 = "#1d4ed8";
    public static final String BLUE_900 = "#1e3a8a";

    public static final String INDIGO_900 = "#312e81";

    public static final String VIOLET_50  = "#f5f3ff";
    public static final String VIOLET_100 = "#ede9fe";
    public static final String VIOLET_200 = "#ddd6fe";
    public static final String VIOLET_300 = "#e9d5ff";
    public static final String VIOLET_500 = "#7c3aed";
    public static final String VIOLET_700 = "#6d28d9";
    public static final String VIOLET_900 = "#4c1d95";

    public static final String PURPLE_50  = "#faf5ff";
    public static final String PURPLE_900 = "#5b21b6";

    public static final String CYAN_700  = "#0e7490";

    public static final String TEAL_700  = "#0f766e";

    public static final String EMERALD_500 = "#10b981";
    public static final String EMERALD_700 = "#047857";

    public static final String GREEN_50  = "#f0fdf4";
    public static final String GREEN_200 = "#bbf7d0";
    public static final String GREEN_900 = "#14532d";

    public static final String YELLOW_50  = "#fffbeb";
    public static final String YELLOW_100 = "#fef3c7";
    public static final String YELLOW_200 = "#fde68a";
    public static final String YELLOW_300 = "#fef9c3";
    public static final String AMBER_500 = "#f59e0b";
    public static final String AMBER_700 = "#b45309";
    public static final String AMBER_900 = "#92400e";

    public static final String RED_600 = "#dc2626";
    public static final String RED_900 = "#991b1b";

    public static final String ORANGE_900 = "#7c2d12";

    public static final String OFF_WHITE = "#fafafa";
}
