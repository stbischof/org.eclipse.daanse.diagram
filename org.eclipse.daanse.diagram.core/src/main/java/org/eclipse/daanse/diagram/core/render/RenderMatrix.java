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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;

/**
 * Writes diagram SVGs into a structured export tree.
 *
 * <p>Two buckets:</p>
 * <ul>
 *   <li>{@code <baseDir>/singletons/<name>/<variant>.svg} — one node type
 *       rendered alone, in any number of feature-set variants.</li>
 *   <li>{@code <baseDir>/diagrams/<name>/<variant>.svg} — an explicitly named
 *       composite diagram (database, deployment-application, …) in any
 *       number of feature-set variants.</li>
 * </ul>
 *
 * <p>Each variant is a name + a {@link Supplier} that produces a fresh
 * {@link Diagram} configured for that variant. The supplier is called
 * once per variant; the matrix runs ELK layout and SVG render in turn.</p>
 *
 * <p>Variants are typically just {@code "minimal"} and {@code "full"} but
 * a test can declare any number of feature-combination showcases (e.g.
 * {@code "keys-and-relations"}, {@code "columns-only"}). There is no
 * fixed enumeration: each named diagram chooses its own variants.</p>
 */
public final class RenderMatrix {

    private final Path baseDir;

    private RenderMatrix(Path baseDir) {
        this.baseDir = baseDir;
    }

    public static RenderMatrix to(Path baseDir) {
        return new RenderMatrix(baseDir);
    }

    /** Convenience: locate the shared examples directory at the reactor root,
     *  honouring the {@code examples.dir} system property. */
    public static Path examplesDir() {
        return Path.of(System.getProperty("examples.dir", "examples"));
    }

    /** Render every variant to {@code <baseDir>/<bucket>/<name>/<variantName>.svg}. */
    public void writeBucket(String bucket, String name,
                            Map<String, Supplier<Diagram>> variants) throws IOException {
        for (Map.Entry<String, Supplier<Diagram>> e : variants.entrySet()) {
            Diagram d = e.getValue().get();
            new ElkLayoutEngine().layout(d);
            String svg = new SvgRenderer().render(d);
            Path out = baseDir.resolve(bucket).resolve(name)
                    .resolve(e.getKey() + ".svg");
            Files.createDirectories(out.toAbsolutePath().getParent());
            Files.writeString(out, svg);
        }
    }

    /** Render a single-node example to {@code <baseDir>/singletons/<name>/}. */
    public void writeSingleton(String name, Map<String, Supplier<Diagram>> variants)
            throws IOException {
        writeBucket("singletons", name, variants);
    }

    /** Render a named composite diagram to {@code <baseDir>/diagrams/<name>/}. */
    public void writeDiagram(String name, Map<String, Supplier<Diagram>> variants)
            throws IOException {
        writeBucket("diagrams", name, variants);
    }

    /** Sugar: build a 2-entry variants map ({@code minimal}, {@code full}). */
    public static Map<String, Supplier<Diagram>> minimalAndFull(
            Supplier<Diagram> minimal, Supplier<Diagram> full) {
        Map<String, Supplier<Diagram>> m = new LinkedHashMap<>();
        m.put("minimal", minimal);
        m.put("full", full);
        return m;
    }
}
