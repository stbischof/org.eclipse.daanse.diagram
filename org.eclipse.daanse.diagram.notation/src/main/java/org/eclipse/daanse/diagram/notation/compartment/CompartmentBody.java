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
package org.eclipse.daanse.diagram.notation.compartment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.svg.SvgElem;

/** {@link NodeBody} composed of an optional {@link TitleBar} on top and a
 *  vertical stack of {@link Compartment}s below. Designed to absorb the
 *  shared "boxed name + sections of rows" pattern that Schema's
 *  {@code TableBody} and Ecore's {@code EClassBody} re-implement
 *  separately today. */
public final class CompartmentBody implements NodeBody {

    private static final double DEFAULT_MIN_WIDTH = 180.0;

    private final TitleBar titleBar;
    private final List<Compartment> compartments = new ArrayList<>();
    private double minWidth = DEFAULT_MIN_WIDTH;

    public CompartmentBody(TitleBar titleBar) {
        this.titleBar = titleBar;
    }

    public CompartmentBody add(Compartment c) {
        if (c != null) compartments.add(c);
        return this;
    }

    public CompartmentBody minWidth(double w) {
        this.minWidth = w;
        return this;
    }

    public List<Compartment> compartments() {
        return compartments;
    }

    @Override
    public double[] sizeHint(DNode node) {
        double h = (titleBar != null ? titleBar.height() : 0);
        for (Compartment c : compartments) h += c.height();
        return new double[] { minWidth, h };
    }

    @Override
    public void render(SvgElem g, DNode node) {
        double w = node.width();
        double h = node.height();
        g.add(new SvgElem("rect").cls("dv-frame")
                .attr("x", 0).attr("y", 0)
                .attr("width", w).attr("height", h));
        double cursor = 0;
        if (titleBar != null) {
            titleBar.render(g, cursor, w);
            cursor += titleBar.height();
        }
        for (Compartment c : compartments) {
            cursor = c.render(g, cursor, w);
        }
    }

    /** Aggregate port pairs contributed by every {@link PortRow} in every
     *  compartment. Called once during {@link DNode}'s construction. */
    @Override
    public List<DPort> createPorts(DNode node) {
        List<DPort> all = new ArrayList<>();
        for (Compartment c : compartments) {
            for (Row r : c.rows()) all.addAll(r.createPorts(node));
        }
        return all;
    }

    /** Re-anchor every row's ports to its current vertical centre after the
     *  final node size is known. */
    @Override
    public void layoutPorts(DNode node) {
        double w = node.width();
        double cursor = (titleBar != null ? titleBar.height() : 0);
        for (Compartment c : compartments) {
            cursor += c.headerHeight();
            for (Row r : c.rows()) {
                r.layoutPorts(node, cursor + r.height() / 2, w);
                cursor += r.height();
            }
        }
    }

    /** Look up the port for a {@link PortRow} by its key. Returns
     *  {@code null} if no row registered under {@code key} or if the
     *  matching row is not a {@link PortRow}. */
    public DPort port(String key, PortSide side) {
        for (Compartment c : compartments) {
            for (Row r : c.rows()) {
                if (r instanceof PortRow pr && key.equals(pr.key())) {
                    return pr.port(side);
                }
            }
        }
        return null;
    }
}
