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
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.TransformationStep;
import org.eclipse.daanse.cwm.model.cwm.analysis.transformation.TransformationTask;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Class;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.notation.edge.Edges;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;

/**
 * Renders a CWM Analysis Transformation package: TransformationActivity →
 * TransformationTask → TransformationStep ownership; Transformation
 * data-flow edges; StepPrecedence "before" edges between steps.
 */
public final class CwmTransformationConverter {

    private static final String ID_NODE = "tx";
    private static final String ID_EDGE = "txe";

    private static final Stereotype ACTIVITY_S       = new Stereotype("ACTIVITY",        Palette.BLUE_900,   Icons.GEAR);
    private static final Stereotype TASK_S           = new Stereotype("TASK",            Palette.INDIGO_900, Icons.GEAR);
    private static final Stereotype STEP_S           = new Stereotype("STEP",            Palette.PURPLE_900, Icons.STEP);
    private static final Stereotype TRANSFORMATION_S = new Stereotype("TRANSFORMATION",  Palette.TEAL_700,   Icons.TRANSFORMATION);
    private static final Stereotype DATA_OBJECT_S    = new Stereotype("DATA OBJECT SET", Palette.GRAY_700,   Icons.DATABASE);
    private static final Stereotype CLASS_S          = new Stereotype("CLASS",           Palette.GRAY_700,   Icons.CLASS);

    public Diagram convert(Package pkg) {
        DiagramBuilder b = DiagramBuilder.of(Names.n(pkg.getName()));

        // Top-level package contents AND elements owned by an activity
        // (since TransformationActivity is itself a Package).
        for (ModelElement me : pkg.getOwnedElement()) {
            render(b, me);
            if (me instanceof TransformationActivity ta) {
                for (ModelElement child : ta.getOwnedElement()) {
                    if (!b.has(child)) render(b, child);
                }
            }
        }

        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof TransformationActivity ta) {
                for (ModelElement child : ta.getOwnedElement()) {
                    if (b.has(child)) b.edge(ta, child, ID_EDGE).label("owns");
                }
            }
            if (me instanceof TransformationTask task) {
                for (TransformationStep step : task.getStep()) {
                    if (b.has(step)) b.edge(task, step, ID_EDGE).label("step");
                }
                for (Transformation t : task.getTransformation()) {
                    if (b.has(t)) b.edge(task, t, ID_EDGE).label("uses");
                }
            }
            if (me instanceof Transformation t) {
                for (DataObjectSet srcSet : t.getSource()) {
                    for (ModelElement src : srcSet.getElement()) {
                        if (b.has(src)) {
                            Edges.dependency(b, src, t, ID_EDGE).label("source").done();
                        }
                    }
                }
                for (DataObjectSet tgtSet : t.getTarget()) {
                    for (ModelElement tgt : tgtSet.getElement()) {
                        if (b.has(tgt)) {
                            // Data flows out of the transformation into a target — modelled
                            // as a dependency, not an FK (FK was a semantic-mismatch reuse).
                            Edges.dependency(b, t, tgt, ID_EDGE).label("target").done();
                        }
                    }
                }
            }
            if (me instanceof StepPrecedence sp) {
                // CWM models precedence as a Dependency: supplier=preceding, client=succeeding.
                for (ModelElement preceding : sp.getSupplier()) {
                    for (ModelElement succeeding : sp.getClient()) {
                        if (b.has(preceding) && b.has(succeeding)) {
                            b.edge(preceding, succeeding, ID_EDGE).label("before");
                        }
                    }
                }
            }
        }
        return b.diagram();
    }

    private void render(DiagramBuilder b, ModelElement me) {
        if (me instanceof TransformationActivity ta) {
            LabelledBoxBody body = b.labelled(ta, ACTIVITY_S, Names.n(ta.getName()), ID_NODE);
            if (ta.getCreationDate() != null) body.addRow("created: " + ta.getCreationDate());
            return;
        }
        if (me instanceof TransformationTask task) {
            b.labelled(task, TASK_S, Names.n(task.getName()), ID_NODE);
            return;
        }
        if (me instanceof TransformationStep s) {
            b.labelled(s, STEP_S, Names.n(s.getName()), ID_NODE);
            return;
        }
        if (me instanceof Transformation t) {
            LabelledBoxBody body = b.labelled(t, TRANSFORMATION_S, Names.n(t.getName()), ID_NODE);
            if (t.getFunctionDescription() != null) body.addRow("fn: " + t.getFunctionDescription());
            return;
        }
        if (me instanceof DataObjectSet dos) {
            b.labelled(dos, DATA_OBJECT_S, Names.n(dos.getName()), ID_NODE);
            return;
        }
        if (me instanceof Class cls) {
            b.labelled(cls, CLASS_S, Names.n(cls.getName()), ID_NODE);
        }
    }
}
