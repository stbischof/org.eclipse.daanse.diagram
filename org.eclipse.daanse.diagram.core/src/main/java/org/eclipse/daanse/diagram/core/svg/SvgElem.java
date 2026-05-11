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
package org.eclipse.daanse.diagram.core.svg;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chainable SVG element. Intentionally tiny: an element has a tag name, a
 * set of attributes (rendered in insertion order), optional inline text
 * content, and nested children.
 */
public final class SvgElem {

    private final String tag;
    private final Map<String, String> attrs = new LinkedHashMap<>();
    private final List<Object> children = new ArrayList<>();
    private String text;

    public SvgElem(String tag) {
        this.tag = tag;
    }

    public SvgElem attr(String name, String value) {
        if (value != null) {
            attrs.put(name, value);
        }
        return this;
    }

    public SvgElem attr(String name, double value) {
        return attr(name, fmt(value));
    }

    public SvgElem attr(String name, int value) {
        return attr(name, Integer.toString(value));
    }

    public SvgElem cls(String cssClass) {
        return attr("class", cssClass);
    }

    public SvgElem style(String style) {
        return attr("style", style);
    }

    public SvgElem text(String t) {
        this.text = t;
        return this;
    }

    public SvgElem add(SvgElem child) {
        if (child != null) {
            children.add(child);
        }
        return this;
    }

    /** Add a raw SVG fragment. Useful for <defs> or hand-written snippets. */
    public SvgElem raw(String xml) {
        if (xml != null && !xml.isEmpty()) {
            children.add(new Raw(xml));
        }
        return this;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        write(sb, 0);
        return sb.toString();
    }

    void write(StringBuilder sb, int indent) {
        indent(sb, indent).append('<').append(tag);
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            sb.append(' ').append(e.getKey()).append("=\"")
                    .append(escapeAttr(e.getValue())).append('"');
        }
        if (children.isEmpty() && (text == null || text.isEmpty())) {
            sb.append("/>\n");
            return;
        }
        sb.append('>');
        if (text != null) {
            sb.append(escapeText(text));
        }
        if (!children.isEmpty()) {
            sb.append('\n');
            for (Object c : children) {
                if (c instanceof SvgElem e) {
                    e.write(sb, indent + 1);
                } else if (c instanceof Raw r) {
                    indent(sb, indent + 1).append(r.xml).append('\n');
                }
            }
            indent(sb, indent);
        }
        sb.append("</").append(tag).append(">\n");
    }

    private static StringBuilder indent(StringBuilder sb, int n) {
        for (int i = 0; i < n; i++) {
            sb.append("  ");
        }
        return sb;
    }

    public static String escapeText(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String escapeAttr(String s) {
        return escapeText(s).replace("\"", "&quot;");
    }

    /** Trim trailing zeros from doubles so attributes stay compact. */
    public static String fmt(double v) {
        if (v == (long) v) {
            return Long.toString((long) v);
        }
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }

    private record Raw(String xml) {}
}
