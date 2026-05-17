package org.eclipse.daanse.diagram.kind.rolap.access;

import java.util.Locale;

public final class AccessColors {
    private AccessColors() {
    }

    public static String bg(String access) {
        if (access == null) {
            return "#e5e7eb";
        }
        return switch (access.toLowerCase(Locale.ROOT)) {
            case "all" -> "#10b981";
            case "all_dimensions" -> "#3b82f6";
            case "custom" -> "#f59e0b";
            case "none" -> "#dc2626";
            default -> "#6b7280";
        };
    }

    public static String fg(String access) {
        return "#ffffff";
    }

    public static String edge(String access) {
        if (access == null) {
            return "#9ca3af";
        }
        return switch (access.toLowerCase(Locale.ROOT)) {
            case "all" -> "#047857";
            case "all_dimensions" -> "#1d4ed8";
            case "custom" -> "#b45309";
            case "none" -> "#991b1b";
            default -> "#6b7280";
        };
    }
}

