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
package org.eclipse.daanse.diagram.notation.edge;

import org.eclipse.daanse.diagram.notation.style.Cardinality;
import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.style.EndpointDecoration;
import org.eclipse.daanse.diagram.core.style.LineStyle;

/**
 * Fluent configurator returned by every {@link Edges} factory. Wraps the
 * {@link DEdge} that the factory created, allowing the caller to attach a
 * label, override the decoration style, and stamp on cardinalities. The
 * underlying {@link DEdge} is already part of the diagram — calling
 * {@link #done()} only returns it for further reference.
 */
public final class EdgeBuilder {

    private final DEdge edge;

    EdgeBuilder(DEdge edge) {
        this.edge = edge;
    }

    public EdgeBuilder label(String s) {
        edge.label(s);
        return this;
    }

    public EdgeBuilder cssClass(String c) {
        edge.cssClass(c);
        return this;
    }

    public EdgeBuilder line(LineStyle ls) {
        edge.lineStyle(ls);
        return this;
    }

    public EdgeBuilder source(EndpointDecoration d) {
        edge.sourceDecoration(d);
        return this;
    }

    public EdgeBuilder target(EndpointDecoration d) {
        edge.targetDecoration(d);
        return this;
    }

    /** Override the edge stroke colour with an explicit CSS value. */
    public EdgeBuilder color(String c) {
        edge.color(c);
        return this;
    }

    public EdgeBuilder sourceCardinality(Cardinality c) {
        edge.sourceCardinality(c == null ? null : c.format());
        return this;
    }

    public EdgeBuilder targetCardinality(Cardinality c) {
        edge.targetCardinality(c == null ? null : c.format());
        return this;
    }

    /** Apply both endpoint cardinalities at once; either may be null. */
    public EdgeBuilder cardinalities(Cardinality src, Cardinality tgt) {
        sourceCardinality(src);
        targetCardinality(tgt);
        return this;
    }

    public DEdge done() {
        return edge;
    }
}
