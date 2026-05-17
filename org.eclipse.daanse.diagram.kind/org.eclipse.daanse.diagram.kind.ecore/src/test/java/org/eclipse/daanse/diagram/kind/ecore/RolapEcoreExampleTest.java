package org.eclipse.daanse.diagram.kind.ecore;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;

public final class RolapEcoreExampleTest {
    private static final String ROLAP_ECORE = "/home/stbischof/git/daanse/org.eclipse.daanse.rolap.mapping/model/model/rolap.mapping.ecore";

    @org.junit.jupiter.api.Test
    void diagrams() throws Exception {
        Diagram d = new EcoreConverter().convert(EcoreLoader.load(ROLAP_ECORE));
        new ElkLayoutEngine().layout(d);
        String svg = new SvgRenderer().render(d);
        Path out = RenderMatrix.examplesDir().resolve("diagrams/ecore-class/rolap/full.svg");
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.writeString(out, svg);
    }
}

