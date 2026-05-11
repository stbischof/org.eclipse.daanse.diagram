/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*/
package org.eclipse.daanse.diagram.notation.compartment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.PortSide;
import org.junit.jupiter.api.Test;

final class PortRowTest {

    @Test
    void portRowsRegisterAndAnchor() {
        TitleBar tb = new TitleBar("Person", null, "#dbeafe", "#1e3a8a");
        Compartment refs = new Compartment("REFERENCES", "#bfdbfe", "#eff6ff");
        refs.add(new PortRow("addresses", null, "addresses", "List<Address>"));
        refs.add(new PortRow("manager",   null, "manager",   "Person"));

        CompartmentBody body = new CompartmentBody(tb).add(refs);
        DNode n = new DNode("n", body);

        // 2 rows × 2 ports each = 4 ports total.
        assertEquals(4, n.ports().size());

        // Lookup by key + side returns the matching port handles.
        DPort wAddr = body.port("addresses", PortSide.WEST);
        DPort eAddr = body.port("addresses", PortSide.EAST);
        DPort wMgr  = body.port("manager",   PortSide.WEST);
        DPort eMgr  = body.port("manager",   PortSide.EAST);
        assertNotNull(wAddr); assertNotNull(eAddr);
        assertNotNull(wMgr);  assertNotNull(eMgr);
        assertEquals(PortSide.WEST, wAddr.side());
        assertEquals(PortSide.EAST, eAddr.side());

        // Ports anchor along the row's vertical centre.
        n.setSize(220, body.sizeHint(n)[1]);
        body.layoutPorts(n);
        // Title bar (no stereotype) = 18; header = 14; first row mid = 18+14+9 = 41.
        assertEquals(41.0, wAddr.y() + wAddr.height() / 2, 0.01);
        // West port sits flush with the left edge.
        assertEquals(0.0, wAddr.x(), 0.01);
        // East port sits flush with the right edge minus its width.
        assertEquals(220 - wAddr.width(), eAddr.x(), 0.01);
    }

    @Test
    void unknownKeyReturnsNull() {
        Compartment c = new Compartment(null, null, null);
        c.add(new PortRow("a", null, "A", null));
        CompartmentBody body = new CompartmentBody(null).add(c);
        new DNode("n", body);
        assertNull(body.port("missing", PortSide.WEST));
    }
}
