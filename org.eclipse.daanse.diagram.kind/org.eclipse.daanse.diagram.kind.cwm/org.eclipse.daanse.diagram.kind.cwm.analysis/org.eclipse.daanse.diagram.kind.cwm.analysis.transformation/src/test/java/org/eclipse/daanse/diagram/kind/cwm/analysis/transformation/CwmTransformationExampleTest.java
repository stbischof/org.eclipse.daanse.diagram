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
package org.eclipse.daanse.diagram.kind.cwm.analysis.transformation;

import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.DataObjectSet;
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.StepPrecedence;
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.Transformation;
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.TransformationActivity;
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.TransformationFactory;
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.TransformationStep;
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.TransformationTask;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Class;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

class CwmTransformationExampleTest {

    private static final TransformationFactory TF = TransformationFactory.eINSTANCE;
    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    @Test
    void singleton_activity() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("activity-alone");
        TransformationActivity a = TF.createTransformationActivity();
        a.setName("nightly-load");
        a.setCreationDate("2026-04-25");
        pkg.getOwnedElement().add(a);
        single("transformation-activity", pkg);
    }

    @Test
    void singleton_task() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("task-alone");
        TransformationTask t = TF.createTransformationTask();
        t.setName("loadFactSales");
        pkg.getOwnedElement().add(t);
        single("transformation-task", pkg);
    }

    @Test
    void singleton_step() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("step-alone");
        TransformationStep s = TF.createTransformationStep();
        s.setName("step.loadFact");
        pkg.getOwnedElement().add(s);
        single("transformation-step", pkg);
    }

    @Test
    void singleton_transformation() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("transformation-alone");
        Transformation t = TF.createTransformation();
        t.setName("loadFactSales");
        t.setFunctionDescription("INSERT INTO fact_sales SELECT * FROM stage");
        pkg.getOwnedElement().add(t);
        single("transformation", pkg);
    }

    @Test
    void singleton_dataObjectSet_() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("dos-alone");
        DataObjectSet dos = TF.createDataObjectSet();
        dos.setName("staging");
        Class staging = CF.createClass(); staging.setName("staging.sales_csv");
        pkg.getOwnedElement().add(staging);
        dos.getElement().add(staging);
        pkg.getOwnedElement().add(dos);
        single("data-object-set", pkg);
    }

    private static void single(String name, Package pkg) throws Exception {
        java.util.function.Supplier<org.eclipse.daanse.diagram.core.Diagram> mk =
                () -> new CwmTransformationConverter().convert(pkg);
        M.writeSingleton(name, RenderMatrix.minimalAndFull(mk, mk));
    }
}
