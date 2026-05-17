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
import org.eclipse.daanse.diagram.core.Diagram;

/**
 * The "deployment-infrastructure" named diagram: Site → host Machine →
 * VM Machines → containers, expressed via {@code ownedElement} nesting.
 *
 * <p>The Features are intentionally narrow — infrastructure is mostly
 * about <i>nesting</i>, which is structural rather than toggleable. The
 * remaining knobs are the same machine detail rows and kind-display
 * found on {@link DeploymentApplicationDiagram}.</p>
 */
public final class DeploymentInfrastructureDiagram {

    public record Features(
            boolean machineDetailRows,
            DeploymentApplicationDiagram.KindDisplay kindDisplay) {

        public static Features minimal() {
            return new Features(false, DeploymentApplicationDiagram.KindDisplay.LEAF);
        }

        public static Features full() {
            return new Features(true, DeploymentApplicationDiagram.KindDisplay.CHAIN);
        }

        public Features withMachineDetailRows(boolean v) {
            return new Features(v, kindDisplay);
        }

        public Features withKindDisplay(DeploymentApplicationDiagram.KindDisplay v) {
            return new Features(machineDetailRows, v);
        }
    }

    private final Features features;
    private final Package pkg;

    public DeploymentInfrastructureDiagram(Features features, Package pkg) {
        this.features = features;
        this.pkg = pkg;
    }

    public Diagram toDiagram() {
        Diagram d = new CwmSoftwareDeploymentConverter().convert(pkg);
        DeploymentApplicationDiagram.applyOptions(d,
                new DeploymentApplicationDiagram.Features(features.machineDetailRows(), features.kindDisplay()));
        return d;
    }
}
