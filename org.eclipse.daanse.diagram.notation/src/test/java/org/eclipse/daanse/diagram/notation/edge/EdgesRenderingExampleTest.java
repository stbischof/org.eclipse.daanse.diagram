/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*/
package org.eclipse.daanse.diagram.notation.edge;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.daanse.diagram.notation.style.Cardinality;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;
import org.junit.jupiter.api.Test;

final class EdgesRenderingExampleTest {

    private static final Stereotype CLASS_S =
            new Stereotype("CLASS", Palette.BLUE_900, "");

    @Test
    void rendersInheritanceCompositionDependency() throws Exception {
        DiagramBuilder b = DiagramBuilder.of("common-edges");

        Object person = key("Person");
        Object animal = key("Animal");
        Object address = key("Address");
        Object logger = key("Logger");

        b.labelled(person,  CLASS_S, "Person",  "n");
        b.labelled(animal,  CLASS_S, "Animal",  "n");
        b.labelled(address, CLASS_S, "Address", "n");
        b.labelled(logger,  CLASS_S, "Logger",  "n");

        Edges.inheritance(b, person, animal).done();
        Edges.composition(b, person, address, Cardinality.ZERO_TO_MANY).done();
        Edges.dependency(b, person, logger).done();

        Diagram d = b.diagram();
        new ElkLayoutEngine().layout(d);
        String svg = new SvgRenderer().render(d);

        assertTrue(svg.contains("<svg"), "rendered output is SVG");
        assertTrue(svg.contains("dv-deco-triangle-hollow"),
                "inheritance edge registers a hollow-triangle marker");
        assertTrue(svg.contains("dv-deco-diamond-filled"),
                "composition edge registers a filled-diamond marker");
        assertTrue(svg.contains("stroke-dasharray"),
                "dependency edge has a dashed stroke");

        Path out = Path.of(System.getProperty("examples.dir", "examples"),
                "diagram-common", "edges-vocabulary.svg");
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.writeString(out, svg);
    }

    /** Returns a fresh, identity-distinct key for each call so the
     *  IdentityHashMap inside DiagramBuilder treats them as separate. */
    private static Object key(String name) {
        return new Object() {
            @Override public String toString() { return name; }
        };
    }
}
