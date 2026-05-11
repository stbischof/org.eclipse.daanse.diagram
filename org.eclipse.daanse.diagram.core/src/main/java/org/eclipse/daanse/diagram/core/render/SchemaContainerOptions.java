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
 * Per-node switches controlling which parts of a {@code SchemaBody}
 * (the dashed-bordered container) are rendered. The icon, kind and title
 * are <b>always</b> rendered when the header tab is on.
 */
public final class SchemaContainerOptions {

    public final boolean showFrame;
    public final boolean showHeaderTab;
    public final boolean showKindAncestors;

    private SchemaContainerOptions(Builder b) {
        this.showFrame = b.showFrame;
        this.showHeaderTab = b.showHeaderTab;
        this.showKindAncestors = b.showKindAncestors;
    }

    public static Builder builder() { return new Builder(); }

    public static SchemaContainerOptions defaults() {
        return builder().build();
    }

    public static SchemaContainerOptions frameless() {
        return builder()
                .showFrame(false)
                .showHeaderTab(false)
                .build();
    }

    public Builder toBuilder() {
        return new Builder()
                .showFrame(showFrame)
                .showHeaderTab(showHeaderTab)
                .showKindAncestors(showKindAncestors);
    }

    public static final class Builder {
        private boolean showFrame = true;
        private boolean showHeaderTab = true;
        private boolean showKindAncestors = true;

        public Builder showFrame(boolean v) { showFrame = v; return this; }
        public Builder showHeaderTab(boolean v) { showHeaderTab = v; return this; }
        public Builder showKindAncestors(boolean v) { showKindAncestors = v; return this; }

        public SchemaContainerOptions build() { return new SchemaContainerOptions(this); }
    }
}
