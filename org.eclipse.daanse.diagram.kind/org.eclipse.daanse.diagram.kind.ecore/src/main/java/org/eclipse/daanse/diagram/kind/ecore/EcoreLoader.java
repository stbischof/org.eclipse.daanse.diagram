package org.eclipse.daanse.diagram.kind.ecore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public final class EcoreLoader {
    private EcoreLoader() {
    }

    public static List<EPackage> load(String path) {
        ResourceSetImpl rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
        rs.getPackageRegistry().put("http://www.eclipse.org/emf/2002/Ecore", EcorePackage.eINSTANCE);
        URI uri = URI.createFileURI(new File(path).getAbsolutePath());
        Resource res = rs.getResource(uri, true);
        ArrayList<EPackage> result = new ArrayList<EPackage>();
        for (EObject eo : res.getContents()) {
            if (!(eo instanceof EPackage)) continue;
            EPackage p = (EPackage)eo;
            result.add(p);
        }
        return result;
    }
}

