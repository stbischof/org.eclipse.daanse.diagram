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
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Class;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;

/**
 * Renders a CWM Management Warehouse Operation package: ActivityExecution
 * with its StepExecutions, free TransformationExecutions, plus Measurement
 * and ChangeRequest records linked to the model elements they apply to.
 */
public final class CwmWarehouseOperationConverter {

    private static final String ID_NODE = "wo";
    private static final String ID_EDGE = "woe";

    private static final Stereotype ACTIVITY_S       = new Stereotype("TRANSFORMATION EXECUTION ▸ ACTIVITY EXECUTION", Palette.BLUE_900,   Icons.EXECUTION);
    private static final Stereotype STEP_S           = new Stereotype("TRANSFORMATION EXECUTION ▸ STEP EXECUTION",     Palette.PURPLE_900, Icons.STEP);
    private static final Stereotype TRANSFORMATION_S = new Stereotype("TRANSFORMATION EXECUTION",                       Palette.TEAL_700,   Icons.TRANSFORMATION);
    private static final Stereotype MEASUREMENT_S    = new Stereotype("MEASUREMENT",                                    Palette.CYAN_700,   Icons.MEASURE);
    private static final Stereotype CHANGE_S         = new Stereotype("CHANGE REQUEST",                                 Palette.ORANGE_900, Icons.EDIT);
    private static final Stereotype CLASS_S          = new Stereotype("CLASS",                                          Palette.GRAY_700,   Icons.CLASS);

    public Diagram convert(Package pkg) {
        DiagramBuilder b = DiagramBuilder.of(Names.n(pkg.getName()));
        for (ModelElement me : pkg.getOwnedElement()) render(b, me);
        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof ActivityExecution ae) {
                for (StepExecution se : ae.getStepExecution()) {
                    if (b.has(se)) b.edge(ae, se, ID_EDGE).label("step");
                }
            }
            if (me instanceof Measurement meas && meas.getModelElement() != null
                    && b.has(meas.getModelElement())) {
                b.edge(meas, meas.getModelElement(), ID_EDGE).label("of");
            }
            if (me instanceof ChangeRequest cr) {
                for (ModelElement changed : cr.getModelElement()) {
                    if (b.has(changed)) b.edge(cr, changed, ID_EDGE).label("changes");
                }
            }
        }
        return b.diagram();
    }

    private void render(DiagramBuilder b, ModelElement me) {
        // ActivityExecution and StepExecution both extend TransformationExecution.
        // Order the instanceof checks so the most specific type wins.
        if (me instanceof ActivityExecution ae) {
            LabelledBoxBody body = b.labelled(ae, ACTIVITY_S, Names.n(ae.getName()), ID_NODE);
            populateExecution(body, ae);
            return;
        }
        if (me instanceof StepExecution se) {
            LabelledBoxBody body = b.labelled(se, STEP_S, Names.n(se.getName()), ID_NODE);
            populateExecution(body, se);
            return;
        }
        if (me instanceof TransformationExecution tx) {
            LabelledBoxBody body = b.labelled(tx, TRANSFORMATION_S, Names.n(tx.getName()), ID_NODE);
            populateExecution(body, tx);
            return;
        }
        if (me instanceof Measurement meas) {
            LabelledBoxBody body = b.labelled(meas, MEASUREMENT_S, Names.n(meas.getName()), ID_NODE);
            if (meas.getValue() != null) body.addRow("value: " + meas.getValue());
            if (meas.getUnit() != null) body.addRow("unit: " + meas.getUnit());
            if (meas.getType() != null) body.addRow("type: " + meas.getType());
            return;
        }
        if (me instanceof ChangeRequest cr) {
            LabelledBoxBody body = b.labelled(cr, CHANGE_S, Names.n(cr.getName()), ID_NODE);
            if (cr.getStatus() != null) body.addRow("status: " + cr.getStatus());
            if (cr.getRequestDate() != null) body.addRow("requested: " + cr.getRequestDate());
            if (cr.getChangeDescription() != null) body.addRow(cr.getChangeDescription());
            return;
        }
        if (me instanceof Class cls) {
            b.labelled(cls, CLASS_S, Names.n(cls.getName()), ID_NODE);
        }
    }

    /** Adds the standard execution rows (start/end/result) to a body. */
    private static void populateExecution(LabelledBoxBody b, TransformationExecution tx) {
        if (tx.getStartDate() != null) b.addRow("start: " + tx.getStartDate());
        if (tx.getEndDate() != null) b.addRow("end:   " + tx.getEndDate());
        if (tx.isInProgress()) {
            b.addRow("in progress");
        } else {
            b.addRow(tx.isSuccessful() ? "OK" : "FAILED");
        }
    }
}
