/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*/
package org.eclipse.daanse.diagram.notation.compartment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.svg.SvgDoc;
import org.eclipse.daanse.diagram.core.svg.SvgElem;
import org.junit.jupiter.api.Test;

final class CompartmentBodySmokeTest {

    @Test
    void sizeHintMatchesLayoutMath() {
        TitleBar tb = new TitleBar("Person", "class", "#dbeafe", "#1e3a8a");
        Compartment attrs = new Compartment("ATTRIBUTES", "#bfdbfe", "#eff6ff");
        attrs.add(new TextRow("name : String"));
        Compartment ops = new Compartment("OPERATIONS", "#fde68a", "#fef3c7");
        ops.add(new IconRow(IconBadge.text("+", "#1f2937", null), "save", null));

        CompartmentBody body = new CompartmentBody(tb).add(attrs).add(ops);

        DNode n = new DNode("n", body);
        double[] sh = body.sizeHint(n);

        // Title (12 stereotype + 18 title) + ATTRS (14 header + 16 row)
        // + OPS (14 header + 18 icon row) = 30 + 30 + 32 = 92.
        assertEquals(180.0, sh[0]);
        assertEquals(92.0,  sh[1], 0.01);
    }

    @Test
    void rendersHeaderAndRowRects() {
        TitleBar tb = new TitleBar("Person", null, "#dbeafe", "#1e3a8a");
        Compartment attrs = new Compartment("ATTRIBUTES", "#bfdbfe", "#eff6ff");
        attrs.add(new TextRow("name : String"));

        CompartmentBody body = new CompartmentBody(tb).add(attrs);
        DNode n = new DNode("n", body);
        n.setSize(220, body.sizeHint(n)[1]);

        SvgDoc doc = new SvgDoc(300, 200);
        SvgElem g = new SvgElem("g");
        body.render(g, n);
        doc.body().add(g);
        String out = doc.render();

        assertTrue(out.contains("Person"), "title text present");
        assertTrue(out.contains("ATTRIBUTES"), "compartment header present");
        assertTrue(out.contains("name : String"), "row text present");
        // The frame, the title bar bg, the compartment header, and the row
        // bg make four rects at minimum.
        long rectCount = out.lines().filter(l -> l.contains("<rect")).count();
        assertTrue(rectCount >= 4, "expected at least 4 rects, got " + rectCount);
    }
}
