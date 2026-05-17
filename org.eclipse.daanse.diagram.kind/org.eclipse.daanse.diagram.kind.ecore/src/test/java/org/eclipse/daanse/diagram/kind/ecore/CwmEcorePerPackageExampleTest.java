package org.eclipse.daanse.diagram.kind.ecore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;
import org.eclipse.emf.ecore.EPackage;

/**
 * One {@code full.svg} per CWM Ecore sub-package, under
 * {@code ecore/cwm/<qualified-name>/full.svg}.
 */
public final class CwmEcorePerPackageExampleTest {
    private static final String CWM_ECORE = "/home/stbischof/git/daanse/org.eclipse.daanse.cwm/model/model/cwm.ecore";

    @org.junit.jupiter.api.Test
    void diagrams() throws Exception {
        List<EPackage> roots = EcoreLoader.load(CWM_ECORE);
        List<EPackage> all = EcoreConverter.allPackages(roots);
        Path baseDir = RenderMatrix.examplesDir().resolve("diagrams").resolve("ecore-class").resolve("cwm-packages");
        EcoreConverter converter = new EcoreConverter();
        for (EPackage pkg : all) {
            if (pkg.getEClassifiers().isEmpty()) continue;
            Diagram d = converter.convertSinglePackage(pkg);
            new ElkLayoutEngine().layout(d);
            String svg = new SvgRenderer().render(d);
            String slug = EcoreConverter.qualifiedName(pkg).replaceAll("[^A-Za-z0-9_.-]", "_");
            Path out = baseDir.resolve(slug).resolve("full.svg");
            Files.createDirectories(out.toAbsolutePath().getParent());
            Files.writeString(out, svg);
        }
    }
}

