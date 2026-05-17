package org.eclipse.daanse.diagram.kind.rolap.cube;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.kind.rolap.cube.CatalogMappingParser;
import org.eclipse.daanse.diagram.kind.rolap.cube.CubeStructureDiagramBuilder;
import org.eclipse.daanse.diagram.kind.rolap.cube.MappingModel;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;

public final class FoodmartCubeStructureExampleTest {
    private static final String FOODMART_XMI = "/home/stbischof/Downloads/all-tutorials(9)/complex.foodmart/mapping/catalog.xmi";

    @org.junit.jupiter.api.Test
    void diagrams() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(java.nio.file.Files.exists(Path.of(FOODMART_XMI)),
            "Skipping: Foodmart catalog.xmi not present at " + FOODMART_XMI);
        String cubeId = "_cube_store";
        MappingModel m = new CatalogMappingParser().parse(FOODMART_XMI);
        Diagram d = new CubeStructureDiagramBuilder().build(m, cubeId);
        new ElkLayoutEngine().layout(d);
        String svg = new SvgRenderer().render(d);
        Path out = Path.of(System.getProperty("examples.dir", "examples"), "foodmart-" + FoodmartCubeStructureExampleTest.simpleId(cubeId) + "-structure.svg");
        Files.createDirectories(out.toAbsolutePath().getParent(), new FileAttribute[0]);
        Files.writeString(out, (CharSequence)svg, new OpenOption[0]);
        System.out.println("Wrote " + String.valueOf(out.toAbsolutePath()));
    }

    private static String simpleId(String id) {
        return id.startsWith("_") ? id.substring(1) : id;
    }
}

