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
package org.eclipse.daanse.diagram.core.render;

/**
 * Per-node switches controlling which parts of a {@code LabelledBoxBody}
 * are rendered. The icon, kind/type caption and name are <b>always</b>
 * rendered as part of the title bar identity.
 *
 * <table>
 *   <caption>Available toggles</caption>
 *   <tr><th>Flag</th><th>What it controls</th></tr>
 *   <tr><td>{@link #showStereotype}</td><td>The optional stereotype text under the kind caption.</td></tr>
 *   <tr><td>{@link #showRows}</td><td>The body rows beneath the title bar.</td></tr>
 *   <tr><td>{@link #showKindAncestors}</td><td>When true, the title shows {@code "PARENT ▸ LEAF"}; when false, only the leaf kind.</td></tr>
 * </table>
 */
public final class LabelledBoxOptions {

    public final boolean showStereotype;
    public final boolean showRows;
    public final boolean showKindAncestors;

    private LabelledBoxOptions(Builder b) {
        this.showStereotype = b.showStereotype;
        this.showRows = b.showRows;
        this.showKindAncestors = b.showKindAncestors;
    }

    public static Builder builder() { return new Builder(); }

    public static LabelledBoxOptions defaults() {
        return builder().build();
    }

    public static LabelledBoxOptions titleOnly() {
        return builder().showRows(false).build();
    }

    public static LabelledBoxOptions minimal() {
        return builder()
                .showStereotype(false)
                .showRows(false)
                .showKindAncestors(false)
                .build();
    }

    public Builder toBuilder() {
        return new Builder()
                .showStereotype(showStereotype)
                .showRows(showRows)
                .showKindAncestors(showKindAncestors);
    }

    public static final class Builder {
        private boolean showStereotype = true;
        private boolean showRows = true;
        private boolean showKindAncestors = true;

        public Builder showStereotype(boolean v) { showStereotype = v; return this; }
        public Builder showRows(boolean v) { showRows = v; return this; }
        public Builder showKindAncestors(boolean v) { showKindAncestors = v; return this; }

        public LabelledBoxOptions build() { return new LabelledBoxOptions(this); }
    }
}
