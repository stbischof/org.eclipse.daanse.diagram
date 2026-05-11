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

import java.util.List;

import org.eclipse.daanse.diagram.core.LabelledBoxBody;

/**
 * Visual stereotype for a node — the (kind chain, title-bar colour, icon)
 * triple. The kind chain runs from most-general parent to leaf, e.g.
 * {@code ["SCHEDULE EVENT", "INTERVAL EVENT"]}. With
 * {@code LabelledBoxOptions.showKindAncestors=true} the title shows the
 * full chain joined by {@code ▸}; with the option off it shows only the
 * leaf.
 *
 * @param kindChain non-empty list, root parent first, leaf last
 * @param titleBg   hex colour for the title bar (use {@link Palette})
 * @param icon      SVG path string from {@code Icons}
 */
public record Stereotype(List<String> kindChain, String titleBg, String icon) {

    public Stereotype {
        if (kindChain == null || kindChain.isEmpty()) {
            throw new IllegalArgumentException("kindChain must be non-empty");
        }
        if (icon == null) {
            throw new IllegalArgumentException("icon is mandatory");
        }
        kindChain = List.copyOf(kindChain);
    }

    /** Single-element convenience for leaves with no meaningful parent. */
    public Stereotype(String kind, String titleBg, String icon) {
        this(List.of(kind), titleBg, icon);
    }

    /** Two-element convenience: parent ▸ leaf. */
    public Stereotype(String parent, String leaf, String titleBg, String icon) {
        this(List.of(parent, leaf), titleBg, icon);
    }

    /** The leaf (most specific) kind. */
    public String leaf() {
        return kindChain.get(kindChain.size() - 1);
    }

    /** The full chain joined with the {@code ▸} separator. */
    public String chain() {
        return String.join(" ▸ ", kindChain);
    }

    /** Build a fresh body for this stereotype with the given display name. */
    public LabelledBoxBody body(String name) {
        return new LabelledBoxBody(kindChain, name).titleBg(titleBg).icon(icon);
    }
}
