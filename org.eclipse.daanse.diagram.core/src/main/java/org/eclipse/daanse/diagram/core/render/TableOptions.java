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
 * Per-node switches that control which parts of a {@code TableBody} get
 * rendered.
 *
 * <p>Every flag is independent — turning one off never silently turns
 * another off. Consumers either build a {@link TableOptions} by hand or
 * pick one of the named presets:</p>
 *
 * <table>
 *   <caption>Preset modes</caption>
 *   <tr><th>Preset</th><th>What's visible</th></tr>
 *   <tr><td>{@link #defaults()}</td><td>everything (full ER mode)</td></tr>
 *   <tr><td>{@link #listOnly()}</td><td>only title bars</td></tr>
 *   <tr><td>{@link #packageOnly()}</td><td>title bars + their schema container chrome</td></tr>
 *   <tr><td>{@link #columnsOnly()}</td><td>columns but no indexes/uniques/checks/triggers/defaults</td></tr>
 *   <tr><td>{@link #overview()}</td><td>PK columns and FK edges only — classic ER overview</td></tr>
 *   <tr><td>{@link #relationsOnly()}</td><td>list of tables + FK edges between them — entity-relation graph</td></tr>
 * </table>
 *
 * <table>
 *   <caption>Per-section flags</caption>
 *   <tr><th>Group</th><th>Flag</th></tr>
 *   <tr><td>columns</td><td>{@link #showColumns}, {@link #showColumnTypes},
 *       {@link #showPkIcons}, {@link #showFkIcons},
 *       {@link #showNullableColumn}, {@link #showDefaultColumn}</td></tr>
 *   <tr><td>indexes</td><td>{@link #showIndexes}, {@link #showIndexConnectors}</td></tr>
 *   <tr><td>uniques</td><td>{@link #showUniqueConstraints}, {@link #showUniqueConnectors}</td></tr>
 *   <tr><td>checks</td><td>{@link #showCheckConstraints}, {@link #showCheckConnectors}</td></tr>
 *   <tr><td>triggers</td><td>{@link #showTriggers}, {@link #showTriggerConnectors}</td></tr>
 *   <tr><td>defaults</td><td>{@link #showDefaults}, {@link #showDefaultConnectors}</td></tr>
 *   <tr><td>edges</td><td>{@link #showFkEdges}, {@link #showCardinalities}</td></tr>
 *   <tr><td>chrome</td><td>{@link #showHeaderIcon}, {@link #showStereotype}</td></tr>
 * </table>
 */
public final class TableOptions {

    /** Pre-set rendering modes that pick a sensible combination of flags. */
    public enum ViewMode { FULL, LIST, PACKAGE_ONLY, COLUMNS_ONLY, OVERVIEW, RELATIONS_ONLY,
            KEYS_AND_RELATIONS, KEYS_ONLY }

    public final boolean showColumns;
    /** When true, hide non-key columns: only those flagged PK or FK are
     *  rendered as rows. Section visibility (indexes/uniques/checks/...)
     *  is unaffected — turn those off separately if not wanted. */
    public final boolean showOnlyKeyColumns;
    public final boolean showColumnTypes;
    public final boolean showPkIcons;
    public final boolean showFkIcons;
    public final boolean showNullableColumn;
    public final boolean showDefaultColumn;
    public final boolean showIndexes;
    public final boolean showIndexConnectors;
    public final boolean showUniqueConstraints;
    public final boolean showUniqueConnectors;
    public final boolean showCheckConstraints;
    public final boolean showCheckConnectors;
    public final boolean showTriggers;
    public final boolean showTriggerConnectors;
    public final boolean showDefaults;
    public final boolean showDefaultConnectors;
    public final boolean showFkEdges;
    public final boolean showCardinalities;
    public final boolean showHeaderIcon;
    public final boolean showStereotype;
    /** When true, the title shows {@code "COLUMN SET ▸ TABLE"} (or VIEW /
     *  QUERY COLUMN SET); when false, only the leaf kind. */
    public final boolean showKindAncestors;
    public final ViewMode mode;

    private TableOptions(Builder b) {
        this.showColumns = b.showColumns;
        this.showOnlyKeyColumns = b.showOnlyKeyColumns;
        this.showColumnTypes = b.showColumnTypes;
        this.showPkIcons = b.showPkIcons;
        this.showFkIcons = b.showFkIcons;
        this.showNullableColumn = b.showNullableColumn;
        this.showDefaultColumn = b.showDefaultColumn;
        this.showIndexes = b.showIndexes;
        this.showIndexConnectors = b.showIndexConnectors;
        this.showUniqueConstraints = b.showUniqueConstraints;
        this.showUniqueConnectors = b.showUniqueConnectors;
        this.showCheckConstraints = b.showCheckConstraints;
        this.showCheckConnectors = b.showCheckConnectors;
        this.showTriggers = b.showTriggers;
        this.showTriggerConnectors = b.showTriggerConnectors;
        this.showDefaults = b.showDefaults;
        this.showDefaultConnectors = b.showDefaultConnectors;
        this.showFkEdges = b.showFkEdges;
        this.showCardinalities = b.showCardinalities;
        this.showHeaderIcon = b.showHeaderIcon;
        this.showStereotype = b.showStereotype;
        this.showKindAncestors = b.showKindAncestors;
        this.mode = b.mode;
    }

    public static Builder builder() { return new Builder(); }

    /** Everything visible — full ER mode. */
    public static TableOptions defaults() {
        return builder().build();
    }

    /** Only title bars — useful as a top-level "what tables are here" overview. */
    public static TableOptions listOnly() {
        return builder()
                .showColumns(false)
                .showIndexes(false).showIndexConnectors(false)
                .showUniqueConstraints(false).showUniqueConnectors(false)
                .showCheckConstraints(false).showCheckConnectors(false)
                .showTriggers(false).showTriggerConnectors(false)
                .showDefaults(false).showDefaultConnectors(false)
                .showFkEdges(false).showCardinalities(false)
                .mode(ViewMode.LIST)
                .build();
    }

    /** Same as {@link #listOnly()} but the schema container chrome stays visible. */
    public static TableOptions packageOnly() {
        return listOnly().toBuilder().mode(ViewMode.PACKAGE_ONLY).build();
    }

    /** Columns visible but no auxiliary sections. */
    public static TableOptions columnsOnly() {
        return builder()
                .showIndexes(false).showIndexConnectors(false)
                .showUniqueConstraints(false).showUniqueConnectors(false)
                .showCheckConstraints(false).showCheckConnectors(false)
                .showTriggers(false).showTriggerConnectors(false)
                .showDefaults(false).showDefaultConnectors(false)
                .mode(ViewMode.COLUMNS_ONLY)
                .build();
    }

    /** PK columns and FK edges only — classic ER overview. */
    public static TableOptions overview() {
        return columnsOnly().toBuilder()
                .showColumnTypes(false)
                .showFkIcons(false)
                .showNullableColumn(false)
                .showDefaultColumn(false)
                .mode(ViewMode.OVERVIEW)
                .build();
    }

    /** Tables as title bars only with FK relationships between them. */
    public static TableOptions relationsOnly() {
        return listOnly().toBuilder()
                .showFkEdges(true)
                .showCardinalities(true)
                .mode(ViewMode.RELATIONS_ONLY)
                .build();
    }

    /** Only the columns participating in keys (PK / FK) are shown, plus
     *  the FK edges between tables. Auxiliary sections (indexes, uniques,
     *  checks, triggers, defaults) are hidden so the result reads as a
     *  classic ER overview. */
    public static TableOptions keysAndRelations() {
        return columnsOnly().toBuilder()
                .showOnlyKeyColumns(true)
                .showColumnTypes(false)
                .showNullableColumn(false)
                .showDefaultColumn(false)
                .showFkEdges(true)
                .showCardinalities(true)
                .mode(ViewMode.KEYS_AND_RELATIONS)
                .build();
    }

    /** Same as {@link #keysAndRelations()} but FK edges are suppressed —
     *  useful when only the column-shape of each table matters. */
    public static TableOptions keysOnly() {
        return keysAndRelations().toBuilder()
                .showFkEdges(false)
                .showCardinalities(false)
                .mode(ViewMode.KEYS_ONLY)
                .build();
    }

    public Builder toBuilder() {
        return new Builder()
                .showColumns(showColumns)
                .showOnlyKeyColumns(showOnlyKeyColumns)
                .showColumnTypes(showColumnTypes)
                .showPkIcons(showPkIcons)
                .showFkIcons(showFkIcons)
                .showNullableColumn(showNullableColumn)
                .showDefaultColumn(showDefaultColumn)
                .showIndexes(showIndexes)
                .showIndexConnectors(showIndexConnectors)
                .showUniqueConstraints(showUniqueConstraints)
                .showUniqueConnectors(showUniqueConnectors)
                .showCheckConstraints(showCheckConstraints)
                .showCheckConnectors(showCheckConnectors)
                .showTriggers(showTriggers)
                .showTriggerConnectors(showTriggerConnectors)
                .showDefaults(showDefaults)
                .showDefaultConnectors(showDefaultConnectors)
                .showFkEdges(showFkEdges)
                .showCardinalities(showCardinalities)
                .showHeaderIcon(showHeaderIcon)
                .showStereotype(showStereotype)
                .showKindAncestors(showKindAncestors)
                .mode(mode);
    }

    public static final class Builder {
        private boolean showColumns = true;
        private boolean showOnlyKeyColumns = false;
        private boolean showColumnTypes = true;
        private boolean showPkIcons = true;
        private boolean showFkIcons = true;
        private boolean showNullableColumn = true;
        private boolean showDefaultColumn = true;
        private boolean showIndexes = true;
        private boolean showIndexConnectors = true;
        private boolean showUniqueConstraints = true;
        private boolean showUniqueConnectors = true;
        private boolean showCheckConstraints = true;
        private boolean showCheckConnectors = true;
        private boolean showTriggers = true;
        private boolean showTriggerConnectors = false;
        private boolean showDefaults = true;
        private boolean showDefaultConnectors = true;
        private boolean showFkEdges = true;
        private boolean showCardinalities = true;
        private boolean showHeaderIcon = true;
        private boolean showStereotype = false;
        private boolean showKindAncestors = true;
        private ViewMode mode = ViewMode.FULL;

        public Builder showColumns(boolean v) { showColumns = v; return this; }
        public Builder showOnlyKeyColumns(boolean v) { showOnlyKeyColumns = v; return this; }
        public Builder showColumnTypes(boolean v) { showColumnTypes = v; return this; }
        public Builder showPkIcons(boolean v) { showPkIcons = v; return this; }
        public Builder showFkIcons(boolean v) { showFkIcons = v; return this; }
        public Builder showNullableColumn(boolean v) { showNullableColumn = v; return this; }
        public Builder showDefaultColumn(boolean v) { showDefaultColumn = v; return this; }
        public Builder showIndexes(boolean v) { showIndexes = v; return this; }
        public Builder showIndexConnectors(boolean v) { showIndexConnectors = v; return this; }
        public Builder showUniqueConstraints(boolean v) { showUniqueConstraints = v; return this; }
        public Builder showUniqueConnectors(boolean v) { showUniqueConnectors = v; return this; }
        public Builder showCheckConstraints(boolean v) { showCheckConstraints = v; return this; }
        public Builder showCheckConnectors(boolean v) { showCheckConnectors = v; return this; }
        public Builder showTriggers(boolean v) { showTriggers = v; return this; }
        public Builder showTriggerConnectors(boolean v) { showTriggerConnectors = v; return this; }
        public Builder showDefaults(boolean v) { showDefaults = v; return this; }
        public Builder showDefaultConnectors(boolean v) { showDefaultConnectors = v; return this; }
        public Builder showFkEdges(boolean v) { showFkEdges = v; return this; }
        public Builder showCardinalities(boolean v) { showCardinalities = v; return this; }
        public Builder showHeaderIcon(boolean v) { showHeaderIcon = v; return this; }
        public Builder showStereotype(boolean v) { showStereotype = v; return this; }
        public Builder showKindAncestors(boolean v) { showKindAncestors = v; return this; }
        public Builder mode(ViewMode v) { mode = v; return this; }

        public TableOptions build() { return new TableOptions(this); }
    }
}
