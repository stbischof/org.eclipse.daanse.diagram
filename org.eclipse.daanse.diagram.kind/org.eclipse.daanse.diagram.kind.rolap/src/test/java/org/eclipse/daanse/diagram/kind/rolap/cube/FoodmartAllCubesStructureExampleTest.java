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

public final class FoodmartAllCubesStructureExampleTest {
    private static final String FOODMART_XMI = "/home/stbischof/Downloads/all-tutorials(9)/complex.foodmart/mapping/catalog.xmi";

    @org.junit.jupiter.api.Test
    void diagrams() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(java.nio.file.Files.exists(Path.of(FOODMART_XMI)),
            "Skipping: Foodmart catalog.xmi not present at " + FOODMART_XMI);
        MappingModel m = new CatalogMappingParser().parse(FOODMART_XMI);
        Path outDir = Path.of(System.getProperty("examples.dir", "examples"), "foodmart-cubes");
        Files.createDirectories(outDir, new FileAttribute[0]);
        CubeStructureDiagramBuilder builder = new CubeStructureDiagramBuilder();
        ElkLayoutEngine layout = new ElkLayoutEngine();
        SvgRenderer renderer = new SvgRenderer();
        int written = 0;
        for (MappingModel.Cube cube : m.cubes.values()) {
            String fileName = FoodmartAllCubesStructureExampleTest.sanitize(cube.name != null ? cube.name : cube.id) + ".svg";
            try {
                Diagram d = builder.build(m, cube.id);
                layout.layout(d);
                String svg = renderer.render(d);
                Path out = outDir.resolve(fileName);
                Files.writeString(out, (CharSequence)svg, new OpenOption[0]);
                ++written;
                System.out.println("Wrote " + String.valueOf(out.toAbsolutePath()));
            }
            catch (RuntimeException ex) {
                System.err.println("Skipping " + cube.id + ": " + ex.getMessage());
            }
        }
        System.out.println("Total " + written + " cube SVGs in " + String.valueOf(outDir.toAbsolutePath()));
    }

    private static String sanitize(String s) {
        return s.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}

