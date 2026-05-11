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
package org.eclipse.daanse.diagram.notation.layout;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DiagramBuilder;

/**
 * Helpers for the common "sub-area container" layout pattern: a
 * {@code SchemaBody}-style box nested inside another container, with
 * {@code elk.direction=DOWN} so its children stack vertically.
 *
 * <p>Replaces the recurring 4-line ritual</p>
 * <pre>
 *   Object key = new Object();
 *   b.container(key, "&lt;title&gt;", "&lt;KIND&gt;", icon, idPrefix);
 *   b.node(key).layoutOption("elk.direction", "DOWN");
 *   b.nest(parentKey, key);
 * </pre>
 * with a single call.
 */
public final class Areas {

    private Areas() {}

    /**
     * Create a new sub-area container nested inside {@code parentKey}, returning
     * the sentinel key under which the area is registered. Direction defaults
     * to {@code DOWN} so leaf nodes nested in the area stack vertically.
     */
    public static Object subArea(DiagramBuilder b, Object parentKey,
                                  String title, String kind, String icon,
                                  String idPrefix) {
        Object key = new Object();
        b.container(key, title, kind, icon, idPrefix);
        b.node(key).layoutOption("elk.direction", "DOWN");
        b.nest(parentKey, key);
        return key;
    }

    /**
     * Variant where the caller supplies the sentinel key (useful when the key
     * needs to be looked up later, e.g. from a per-schema map).
     */
    public static DNode subArea(DiagramBuilder b, Object key, Object parentKey,
                                  String title, String kind, String icon,
                                  String idPrefix) {
        b.container(key, title, kind, icon, idPrefix);
        DNode area = b.node(key);
        area.layoutOption("elk.direction", "DOWN");
        b.nest(parentKey, key);
        return area;
    }
}
