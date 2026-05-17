package org.eclipse.daanse.diagram.kind.ecore;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;

/**
 * Ecore bodies (EPackageBody, EClassBody) don't yet read render features, so
 * we emit a single "full" SVG into the standard matrix folder shape rather
 * than six identical copies.
 */
public final class CwmEcoreExampleTest {
    private static final String CWM_ECORE = "/home/stbischof/git/daanse/org.eclipse.daanse.cwm/model/model/cwm.ecore";

    @org.junit.jupiter.api.Test
    void diagrams() throws Exception {
        Diagram d = new EcoreConverter().convert(EcoreLoader.load(CWM_ECORE));
        new ElkLayoutEngine().layout(d);
        String svg = new SvgRenderer().render(d);
        Path out = RenderMatrix.examplesDir().resolve("diagrams/ecore-class/cwm/full.svg");
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.writeString(out, svg);
    }
}

