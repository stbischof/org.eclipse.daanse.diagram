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
package org.eclipse.daanse.diagram.kind.cwm.management.warehouseoperation;

import org.eclipse.daanse.cwm.model.cwm.management.warehouseoperation.ActivityExecution;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseoperation.ChangeRequest;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseoperation.Measurement;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseoperation.StepExecution;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseoperation.TransformationExecution;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseoperation.WarehouseoperationFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Class;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

class CwmWarehouseOperationExampleTest {

    private static final WarehouseoperationFactory WOF = WarehouseoperationFactory.eINSTANCE;
    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    @Test
    void singleton_activityExecution() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("ae-alone");
        ActivityExecution ae = WOF.createActivityExecution();
        ae.setName("nightly-load 2026-04-25");
        ae.setStartDate("2026-04-25T02:00:00");
        ae.setEndDate("2026-04-25T02:14:32");
        ae.setSuccessful(true);
        pkg.getOwnedElement().add(ae);
        single("activity-execution", pkg);
    }

    @Test
    void singleton_stepExecution() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("se-alone");
        StepExecution se = WOF.createStepExecution();
        se.setName("extract");
        se.setStartDate("2026-04-25T02:00:00");
        se.setEndDate("2026-04-25T02:02:11");
        se.setSuccessful(true);
        pkg.getOwnedElement().add(se);
        single("step-execution", pkg);
    }

    @Test
    void singleton_transformationExecution() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("txe-alone");
        TransformationExecution txe = WOF.createTransformationExecution();
        txe.setName("loadFactSales exec");
        txe.setStartDate("2026-04-25T02:10:00");
        txe.setEndDate("2026-04-25T02:14:32");
        txe.setSuccessful(true);
        pkg.getOwnedElement().add(txe);
        single("transformation-execution", pkg);
    }

    @Test
    void singleton_measurement() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("meas-alone");
        Measurement m = WOF.createMeasurement();
        m.setName("rowsLoaded");
        m.setValue("1248792");
        m.setUnit("rows");
        m.setType("counter");
        pkg.getOwnedElement().add(m);
        single("measurement", pkg);
    }

    @Test
    void singleton_changeRequest() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("cr-alone");
        ChangeRequest cr = WOF.createChangeRequest();
        cr.setName("CR-101");
        cr.setStatus("OPEN");
        cr.setRequestDate("2026-04-25");
        cr.setChangeDescription("Re-run extract with shifted offset.");
        pkg.getOwnedElement().add(cr);
        single("change-request", pkg);
    }

    @Test @org.junit.jupiter.api.Disabled("dropped — execution-trace not in named-diagrams set")
    void diagram_execution_trace_DROPPED() throws Exception {
        Package pkg = CF.createPackage();
        pkg.setName("nightlyEtlRun");

        ActivityExecution run = WOF.createActivityExecution();
        run.setName("nightly-load 2026-04-25");
        run.setStartDate("2026-04-25T02:00:00");
        run.setEndDate("2026-04-25T02:14:32");
        run.setSuccessful(true);
        pkg.getOwnedElement().add(run);

        StepExecution se1 = WOF.createStepExecution();
        se1.setName("extract");
        se1.setStartDate("2026-04-25T02:00:00");
        se1.setEndDate("2026-04-25T02:02:11");
        se1.setSuccessful(true);
        pkg.getOwnedElement().add(se1); run.getStepExecution().add(se1);

        StepExecution se2 = WOF.createStepExecution();
        se2.setName("load");
        se2.setStartDate("2026-04-25T02:10:00");
        se2.setEndDate("2026-04-25T02:14:32");
        se2.setSuccessful(true);
        pkg.getOwnedElement().add(se2); run.getStepExecution().add(se2);

        TransformationExecution txe = WOF.createTransformationExecution();
        txe.setName("loadFactSales exec");
        txe.setStartDate("2026-04-25T02:10:00");
        txe.setEndDate("2026-04-25T02:14:32");
        txe.setSuccessful(true);
        pkg.getOwnedElement().add(txe);

        Class measured = CF.createClass(); measured.setName("dwh.fact_sales");
        pkg.getOwnedElement().add(measured);

        Measurement rowsLoaded = WOF.createMeasurement();
        rowsLoaded.setName("rowsLoaded");
        rowsLoaded.setValue("1248792");
        rowsLoaded.setUnit("rows");
        rowsLoaded.setModelElement(measured);
        pkg.getOwnedElement().add(rowsLoaded);

        ChangeRequest cr = WOF.createChangeRequest();
        cr.setName("CR-101");
        cr.setStatus("OPEN");
        cr.setRequestDate("2026-04-25");
        cr.setChangeDescription("Re-run extract with shifted offset.");
        cr.getModelElement().add(measured);
        pkg.getOwnedElement().add(cr);

    }

    private static void single(String name, Package pkg) throws Exception {
        java.util.function.Supplier<org.eclipse.daanse.diagram.core.Diagram> mk =
                () -> new CwmWarehouseOperationConverter().convert(pkg);
        M.writeSingleton(name, RenderMatrix.minimalAndFull(mk, mk));
    }
}
