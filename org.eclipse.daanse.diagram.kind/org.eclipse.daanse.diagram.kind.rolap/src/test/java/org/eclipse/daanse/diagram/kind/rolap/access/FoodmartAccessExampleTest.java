package org.eclipse.daanse.diagram.kind.rolap.access;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.eclipse.daanse.diagram.kind.rolap.access.AccessDiagramBuilder;
import org.eclipse.daanse.diagram.kind.rolap.access.AccessModel;
import org.eclipse.daanse.diagram.kind.rolap.access.FoodmartAccessParser;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;

public final class FoodmartAccessExampleTest {
    private static final String FOODMART_XMI = "/home/stbischof/Downloads/all-tutorials(9)/complex.foodmart/mapping/catalog.xmi";

    @org.junit.jupiter.api.Test
    void diagrams() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(java.nio.file.Files.exists(Path.of(FOODMART_XMI)),
            "Skipping: Foodmart catalog.xmi not present at " + FOODMART_XMI);
        AccessModel model = new FoodmartAccessParser().parse(FOODMART_XMI);
        Diagram d = new AccessDiagramBuilder().build(model);
        new ElkLayoutEngine().layout(d);
        String svg = new SvgRenderer().render(d);
        Path out = Path.of(System.getProperty("examples.dir", "examples"), "foodmart-access.svg");
        Files.createDirectories(out.toAbsolutePath().getParent(), new FileAttribute[0]);
        Files.writeString(out, (CharSequence)svg, new OpenOption[0]);
        System.out.println("Wrote " + String.valueOf(out.toAbsolutePath()));
    }
}

