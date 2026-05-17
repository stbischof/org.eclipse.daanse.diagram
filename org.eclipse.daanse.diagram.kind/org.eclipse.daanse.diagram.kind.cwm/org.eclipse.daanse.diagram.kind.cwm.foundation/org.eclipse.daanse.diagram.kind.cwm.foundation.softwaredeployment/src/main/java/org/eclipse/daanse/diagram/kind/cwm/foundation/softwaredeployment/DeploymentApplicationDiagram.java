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
package org.eclipse.daanse.diagram.kind.cwm.foundation.softwaredeployment;

import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.SchemaBody;
import org.eclipse.daanse.diagram.core.render.LabelledBoxOptions;
import org.eclipse.daanse.diagram.core.render.SchemaContainerOptions;

/**
 * The "deployment-application" named diagram: components, software systems,
 * deployed components / software systems, data managers / providers and
 * the provider connections wiring them together.
 */
public final class DeploymentApplicationDiagram {

    public enum KindDisplay { LEAF, CHAIN }

    public record Features(
            boolean detailRows,
            KindDisplay kindDisplay) {

        public static Features minimal() {
            return new Features(false, KindDisplay.LEAF);
        }

        public static Features full() {
            return new Features(true, KindDisplay.CHAIN);
        }

        public Features withDetailRows(boolean v) {
            return new Features(v, kindDisplay);
        }

        public Features withKindDisplay(KindDisplay v) {
            return new Features(detailRows, v);
        }
    }

    private final Features features;
    private final Package pkg;

    public DeploymentApplicationDiagram(Features features, Package pkg) {
        this.features = features;
        this.pkg = pkg;
    }

    public Diagram toDiagram() {
        Diagram d = new CwmSoftwareDeploymentConverter().convert(pkg);
        applyOptions(d, features);
        return d;
    }

    static void applyOptions(Diagram d, Features features) {
        LabelledBoxOptions labelledOpts = LabelledBoxOptions.builder()
                .showRows(features.detailRows())
                .showStereotype(false)
                .showKindAncestors(features.kindDisplay() == KindDisplay.CHAIN)
                .build();
        SchemaContainerOptions schemaOpts = SchemaContainerOptions.builder()
                .showKindAncestors(features.kindDisplay() == KindDisplay.CHAIN)
                .build();
        for (DNode n : d.allNodes()) {
            NodeBody body = n.body();
            if (body instanceof LabelledBoxBody lb) lb.options(labelledOpts);
            else if (body instanceof SchemaBody sb) sb.options(schemaOpts);
            double[] sz = body.sizeHint(n);
            n.setSize(sz[0], sz[1]);
            body.layoutPorts(n);
        }
    }
}
