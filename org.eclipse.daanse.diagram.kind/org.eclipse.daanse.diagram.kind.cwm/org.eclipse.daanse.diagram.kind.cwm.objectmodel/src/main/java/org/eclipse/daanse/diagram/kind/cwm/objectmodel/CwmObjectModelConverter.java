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
package org.eclipse.daanse.diagram.kind.cwm.objectmodel;

import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.Operation;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.Parameter;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Attribute;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Class;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.DataType;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Feature;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.relationships.Association;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.relationships.AssociationEnd;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.relationships.Generalization;
import org.eclipse.daanse.diagram.notation.edge.Edges;
import org.eclipse.daanse.diagram.notation.style.Cardinality;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;

/**
 * Renders a CWM ObjectModel package: every contained Class / DataType
 * becomes a labelled box listing its attributes and operations
 * (operation parameters formatted UML-style with {@code in}/{@code out}
 * direction). Generalizations and Associations turn into edges.
 */
public final class CwmObjectModelConverter {

    private static final String ID_CLASS = "c";
    private static final String ID_DT    = "d";
    private static final String ID_GEN   = "gen";
    private static final String ID_ASC   = "asc";

    private static final Stereotype CLASS_S          = new Stereotype("CLASS",    Palette.BLUE_900,  Icons.CLASS);
    private static final Stereotype CLASS_ABSTRACT_S = new Stereotype("CLASS",    Palette.BLUE_900,  Icons.INTERFACE);
    private static final Stereotype DATATYPE_S       = new Stereotype("DATATYPE", Palette.GREEN_900, Icons.DATATYPE);

    public Diagram convert(Package pkg) {
        DiagramBuilder b = DiagramBuilder.of(Names.n(pkg.getName()));

        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof Class cls) buildClass(b, cls);
            else if (me instanceof DataType dt) b.labelled(dt, DATATYPE_S, Names.n(dt.getName()), ID_DT);
        }

        // Generalizations: child --|> parent. Associations: end-to-end with cardinalities.
        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof Generalization g
                    && b.has(g.getChild()) && b.has(g.getParent())) {
                Edges.inheritance(b, g.getChild(), g.getParent(), ID_GEN).done();
            } else if (me instanceof Association assoc && assoc.getFeature().size() >= 2) {
                AssociationEnd a = (AssociationEnd) assoc.getFeature().get(0);
                AssociationEnd c = (AssociationEnd) assoc.getFeature().get(1);
                if (b.has(a.getType()) && b.has(c.getType())) {
                    Edges.association(b, a.getType(), c.getType(),
                                    multOf(a), multOf(c), ID_ASC)
                            .label(assoc.getName() != null ? assoc.getName() : "")
                            .done();
                }
            }
        }
        return b.diagram();
    }

    private static Cardinality multOf(AssociationEnd ae) {
        if (ae.getMultiplicity() == null || ae.getMultiplicity().getRange().isEmpty()) {
            return null;
        }
        var r = ae.getMultiplicity().getRange().get(0);
        return new Cardinality((int) r.getLower(), (int) r.getUpper());
    }

    private void buildClass(DiagramBuilder b, Class cls) {
        Stereotype s = cls.isIsAbstract() ? CLASS_ABSTRACT_S : CLASS_S;
        LabelledBoxBody body = b.labelled(cls, s, Names.n(cls.getName()), ID_CLASS);
        if (cls.isIsAbstract()) body.stereotype("abstract");
        for (Feature f : cls.getFeature()) {
            if (f instanceof Attribute attr) {
                body.addRow("+ " + attr.getName() + " : " + typeOf(attr.getType()));
            }
        }
        for (Feature f : cls.getFeature()) {
            if (f instanceof Operation op) body.addRow(formatOperation(op));
        }
    }

    private String formatOperation(Operation op) {
        StringBuilder sb = new StringBuilder("+ ").append(op.getName()).append("(");
        boolean first = true;
        String returnType = null;
        for (Parameter p : op.getParameter()) {
            String raw = p.getKind() != null ? p.getKind().getLiteral() : "";
            String kind = raw.startsWith("pdk_") ? raw.substring(4) : raw;
            if ("return".equalsIgnoreCase(kind)) {
                returnType = typeOf(p.getType());
                continue;
            }
            if (!first) sb.append(", ");
            sb.append(kind.isEmpty() ? "" : kind + " ").append(p.getName())
                    .append(" : ").append(typeOf(p.getType()));
            first = false;
        }
        sb.append(")");
        if (returnType != null) sb.append(" : ").append(returnType);
        return sb.toString();
    }

    private static String typeOf(Classifier c) {
        return c != null && c.getName() != null ? c.getName() : "?";
    }
}
