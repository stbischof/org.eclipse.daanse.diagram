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
import org.eclipse.daanse.diagram.core.NodeBody;

/**
 * Node-level helpers for body-driven nodes built via
 * {@link DiagramBuilder#labelled} (or {@link DiagramBuilder#node}).
 *
 * <p>{@code DiagramBuilder.labelled} captures {@code body.sizeHint(node)} at
 * the moment the {@link DNode} is created — i.e. <i>before</i> the caller
 * adds any rows. If rows are added afterwards, the node frame stays at the
 * 0-row size and ELK clamps the node accordingly, hiding the content.
 * {@link #applyBodySize} recomputes the hint and locks the node so the
 * frame matches the post-addRow size.</p>
 */
public final class Nodes {

    private Nodes() {}

    /**
     * Recomputes the node's size from its body and locks it via
     * {@link DNode#fixSize}. Call after the body's
     * {@code addRow}/{@code addColumn}/etc. have all been applied.
     */
    public static DNode applyBodySize(DiagramBuilder b, Object key, NodeBody body) {
        DNode n = b.node(key);
        if (n == null) {
            throw new IllegalArgumentException("No node registered under key " + key);
        }
        return applyBodySize(n, body);
    }

    /** Direct-handle variant of {@link #applyBodySize(DiagramBuilder, Object, NodeBody)}. */
    public static DNode applyBodySize(DNode n, NodeBody body) {
        double[] sh = body.sizeHint(n);
        n.setSize(sh[0], sh[1]);
        n.fixSize();
        return n;
    }
}
