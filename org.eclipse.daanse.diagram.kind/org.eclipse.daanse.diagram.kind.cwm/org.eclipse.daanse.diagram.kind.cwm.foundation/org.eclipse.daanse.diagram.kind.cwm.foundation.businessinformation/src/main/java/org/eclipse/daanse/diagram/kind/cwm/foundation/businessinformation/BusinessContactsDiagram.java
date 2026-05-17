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
package org.eclipse.daanse.diagram.kind.cwm.foundation.businessinformation;

import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.render.LabelledBoxOptions;

/**
 * The "business-contacts" named diagram: ResponsibleParty + Contact tree
 * (Email/Telephone/Location), plus standalone Description and Document
 * elements with ownership edges between them.
 */
public final class BusinessContactsDiagram {

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

    public BusinessContactsDiagram(Features features, Package pkg) {
        this.features = features;
        this.pkg = pkg;
    }

    public Diagram toDiagram() {
        Diagram d = new CwmBusinessInformationConverter().convert(pkg);
        LabelledBoxOptions opts = LabelledBoxOptions.builder()
                .showRows(features.detailRows())
                .showStereotype(false)
                .showKindAncestors(features.kindDisplay() == KindDisplay.CHAIN)
                .build();
        for (DNode n : d.allNodes()) {
            NodeBody body = n.body();
            if (body instanceof LabelledBoxBody lb) lb.options(opts);
            double[] sz = body.sizeHint(n);
            n.setSize(sz[0], sz[1]);
            body.layoutPorts(n);
        }
        return d;
    }
}
