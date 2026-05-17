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
package org.eclipse.daanse.diagram.kind.ecore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.daanse.diagram.notation.edge.Edges;
import org.eclipse.daanse.diagram.notation.layout.Areas;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.core.style.EndpointDecoration;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypedElement;

public final class EcoreConverter {

    private static final String ID_PKG = "pkg";
    private static final String ID_CLASS = "c";
    private static final String ID_ENUM = "e";
    private static final String ID_DATATYPE = "d";
    private static final String ID_INHERIT = "inh";
    private static final String ID_REF = "ref";

    /**
     * Per-converter render switches.
     *
     * @param showInheritedFeatures when {@code true}, every {@code EClass}
     *     body also lists the structural features and operations of all its
     *     supertypes (transitively). The inherited rows render in italic +
     *     dimmer colour with a small {@code ↑} icon and an "inherited from
     *     <Supertype>" tooltip, so they don't blur with own features.
     *     Default {@code false}.
     */
    public record Options(boolean showInheritedFeatures) {
        public static Options defaults() {
            return new Options(false);
        }

        public Options withShowInheritedFeatures(boolean v) {
            return new Options(v);
        }
    }

    private Options options = Options.defaults();

    public EcoreConverter options(Options opts) {
        this.options = opts != null ? opts : Options.defaults();
        return this;
    }

    public Diagram convert(List<EPackage> roots) {
        DiagramBuilder b = DiagramBuilder.of(roots.isEmpty() ? "" : roots.get(0).getName());
        applyDiagramSpacing(b);
        for (EPackage ePackage : roots) buildPackage(b, ePackage, null);
        for (Object key : keys(b, roots)) {
            if (key instanceof EClass ec) {
                emitInheritance(b, ec);
                emitReferenceEdges(b, ec);
            }
        }
        return b.diagram();
    }

    public Diagram convertSinglePackage(EPackage pkg) {
        DiagramBuilder b = DiagramBuilder.of(qualifiedName(pkg));
        applyDiagramSpacing(b);
        // Delegate to buildPackage so the DATATYPES / ENUMERATIONS sub-area
        // grouping kicks in for the single-package case too.
        buildPackage(b, pkg, null);
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EClass ec) {
                emitInheritance(b, ec);
                emitReferenceEdges(b, ec);
            }
        }
        return b.diagram();
    }

    /** Extra vertical clearance between edges crossing between layers and the
     *  nodes themselves, so an inheritance edge entering a class via its
     *  top-centre {@code NORTH} port has a visible perpendicular run before
     *  it hits the arrowhead. The default is too tight; bumping to 30 keeps
     *  the line readable without spreading classes too far apart. */
    private static void applyDiagramSpacing(DiagramBuilder b) {
        b.diagram().option("elk.layered.spacing.edgeNodeBetweenLayers", "80");
        // Extra spacing between consecutive layers so inheritance edges have
        // a long perpendicular run before the arrowhead — the arrow marker
        // covers ~7px of the line, so we need at least that much clear
        // route past the last bend.
        b.diagram().option("elk.layered.spacing.nodeNodeBetweenLayers", "100");
    }

    public static List<EPackage> allPackages(List<EPackage> roots) {
        ArrayList<EPackage> out = new ArrayList<>();
        ArrayDeque<EPackage> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            EPackage p = queue.pop();
            out.add(p);
            queue.addAll((Collection<EPackage>) p.getESubpackages());
        }
        return out;
    }

    private void buildPackage(DiagramBuilder b, EPackage pkg, DNode parent) {
        String label = parent == null ? qualifiedName(pkg) : pkg.getName();
        DNode pkgNode = b.node(pkg, ID_PKG,
                new EPackageBody(label, pkg.getNsPrefix(), pkg.getNsURI()));
        if (parent != null) b.nest(parent, pkgNode);
        // Pre-scan: build sub-area containers only when they would carry
        // anything, mirroring the CWM Relational pattern (DATATYPES /
        // ENUMERATIONS / FUNCTIONS / PROCEDURES sub-areas inside a SCHEMA).
        boolean hasEnums = false;
        boolean hasDatatypes = false;
        for (EClassifier c : pkg.getEClassifiers()) {
            if (c instanceof EEnum) hasEnums = true;
            else if (c instanceof EDataType) hasDatatypes = true; // EEnum extends EDataType — order matters
        }
        Object datatypesArea = hasDatatypes ? Areas.subArea(b, pkg, "datatypes",
                "DATATYPES", Icons.DATATYPE, ID_PKG + "_dts") : null;
        Object enumsArea = hasEnums ? Areas.subArea(b, pkg, "enumerations",
                "ENUMERATIONS", Icons.DATATYPE, ID_PKG + "_enum") : null;
        for (EClassifier c : pkg.getEClassifiers()) {
            buildClassifier(b, c);
            if (!b.has(c)) continue;
            if (c instanceof EEnum) {
                b.nest(enumsArea != null ? enumsArea : pkg, c);
            } else if (c instanceof EDataType) {
                b.nest(datatypesArea != null ? datatypesArea : pkg, c);
            } else {
                b.nest(pkg, c);
            }
        }
        for (EPackage sub : pkg.getESubpackages()) buildPackage(b, sub, pkgNode);
    }

    private void buildClassifier(DiagramBuilder b, EClassifier c) {
        if (c instanceof EClass ec) {
            EClassBody.Kind kind = ec.isInterface() ? EClassBody.Kind.INTERFACE
                    : (ec.isAbstract() ? EClassBody.Kind.ABSTRACT_CLASS : EClassBody.Kind.CLASS);
            EClassBody body = new EClassBody(ec.getName()).kind(kind);
            for (EAttribute a : ec.getEAttributes()) body.addAttribute(toAttribute(a));
            for (EReference r : ec.getEReferences()) body.addReference(toReference(r));
            for (EOperation op : ec.getEOperations()) body.addOperation(toOperation(op));
            if (options.showInheritedFeatures()) {
                // EMF's getEAll* return own + inherited; an inherited feature
                // is just one whose containing class is a supertype of ec.
                // EcoreUtil has no dedicated "inherited only" accessor, so
                // the identity check is the canonical filter.
                for (EAttribute a : ec.getEAllAttributes()) {
                    EClass owner = a.getEContainingClass();
                    if (owner == ec) continue;
                    body.addAttribute(toAttribute(a)
                            .inheritedFrom(owner != null ? owner.getName() : ""));
                }
                for (EReference r : ec.getEAllReferences()) {
                    EClass owner = r.getEContainingClass();
                    if (owner == ec) continue;
                    body.addReference(toReference(r)
                            .inheritedFrom(owner != null ? owner.getName() : ""));
                }
                for (EOperation op : ec.getEAllOperations()) {
                    EClass owner = op.getEContainingClass();
                    if (owner == ec) continue;
                    body.addOperation(toOperation(op)
                            .inheritedFrom(owner != null ? owner.getName() : ""));
                }
            }
            b.node(ec, ID_CLASS, body);
            return;
        }
        if (c instanceof EEnum ee) {
            EClassBody body = new EClassBody(ee.getName()).kind(EClassBody.Kind.ENUM);
            for (EEnumLiteral lit : ee.getELiterals()) {
                body.addLiteral(new EClassBody.Literal(lit.getName())
                        .value(lit.getValue()).literal(lit.getLiteral()));
            }
            b.node(ee, ID_ENUM, body);
            return;
        }
        if (c instanceof EDataType dt) {
            EClassBody body = new EClassBody(dt.getName())
                    .kind(EClassBody.Kind.DATATYPE)
                    .instanceClassName(dt.getInstanceClassName());
            b.node(dt, ID_DATATYPE, body);
        }
    }

    private EClassBody.Attribute toAttribute(EAttribute a) {
        EClassBody.Attribute attr = new EClassBody.Attribute(a.getName())
                .type(typeName(a))
                .multiplicity(multiplicity(a));
        if (a.isID()) attr.id();
        if (a.isDerived()) attr.derived();
        if (a.isTransient()) attr.transientFlag();
        if (a.isVolatile()) attr.volatileFlag();
        if (a.getDefaultValueLiteral() != null) attr.defaultValue(a.getDefaultValueLiteral());
        return attr;
    }

    private EClassBody.Reference toReference(EReference r) {
        EClassBody.Reference ref = new EClassBody.Reference(r.getName())
                .type(typeName(r))
                .multiplicity(multiplicity(r));
        if (r.isContainment()) ref.containment();
        if (r.isDerived()) ref.derived();
        if (r.isTransient()) ref.transientFlag();
        if (r.isVolatile()) ref.volatileFlag();
        if (r.getEOpposite() != null) ref.opposite(r.getEOpposite().getName());
        return ref;
    }

    private EClassBody.Operation toOperation(EOperation op) {
        EClassBody.Operation o = new EClassBody.Operation(op.getName());
        // Each parameter and the operation's return value carry their own
        // multiplicity in EMF. Append the {@code lower..upper} form to the
        // type so it lines up in the right-aligned type column with the
        // multiplicities on attributes and references.
        for (EParameter p : op.getEParameters()) {
            o.param(p.getName(), typeName(p) + " " + multiplicity(p));
        }
        if (op.getEType() != null) {
            o.returnType(typeName(op) + " " + multiplicity(op));
        }
        return o;
    }

    private void emitInheritance(DiagramBuilder b, EClass ec) {
        DNode source = b.node(ec);
        if (source == null) return;
        EClassBody sourceBody = (EClassBody) source.body();
        EList<EClass> supers = ec.getESuperTypes();
        for (EClass sup : supers) {
            DNode tgt = b.node(sup);
            if (tgt == null) continue;
            EClassBody tgtBody = (EClassBody) tgt.body();
            // Top-of-class on both ends so the inheritance arrow comes out of
            // the child's title-bar top and lands on the parent's title-bar
            // top, never colliding with the reference rows on the sides.
            Edges.inheritance(b,
                    sourceBody.classPort(PortSide.NORTH),
                    tgtBody.classPort(PortSide.NORTH),
                    ID_INHERIT)
                    .color(ECORE_EDGE_COLOR)
                    .done();
        }
    }

    /** Default dark gray used for every Ecore edge — inheritance, containment,
     *  non-containment, bidirectional. The reference kind is communicated by
     *  the icon on the structural-feature row (filled diamond / chevron),
     *  not by colour; pinning every edge to one neutral colour keeps the
     *  diagram visually quiet so the row icons can do the talking. */
    private static final String ECORE_EDGE_COLOR = "#374151";

    private void emitReferenceEdges(DiagramBuilder b, EClass ec) {
        DNode source = b.node(ec);
        if (source == null) return;
        EClassBody sourceBody = (EClassBody) source.body();
        for (EReference r : ec.getEReferences()) {
            EClassifier tgtType = r.getEType();
            if (tgtType == null) continue;
            DNode tgtNode = b.node(tgtType);
            if (tgtNode == null || !(tgtNode.body() instanceof EClassBody tgtBody)) continue;
            EReference opp = r.getEOpposite();
            if (opp != null) {
                if (r.getName().compareTo(opp.getName()) > 0) continue;
                if (!tgtBody.hasRefPort(opp.getName())) continue;
                Edges.association(b,
                        sourceBody.refPort(r.getName(), PortSide.EAST),
                        tgtBody.refPort(opp.getName(), PortSide.WEST),
                        null, null, ID_REF)
                        .color(ECORE_EDGE_COLOR)
                        .target(EndpointDecoration.NONE)
                        .done();
                continue;
            }
            if (r.isContainment()) {
                Edges.dependency(b,
                        sourceBody.refPort(r.getName(), PortSide.EAST),
                        tgtBody.classPort(PortSide.WEST),
                        ID_REF)
                        .color(ECORE_EDGE_COLOR)
                        .target(EndpointDecoration.NONE)
                        .done();
            } else {
                Edges.association(b,
                        sourceBody.refPort(r.getName(), PortSide.EAST),
                        tgtBody.classPort(PortSide.WEST),
                        null, null, ID_REF)
                        .color(ECORE_EDGE_COLOR)
                        .target(EndpointDecoration.NONE)
                        .done();
            }
        }
    }

    private static String typeName(ETypedElement e) {
        if (e.getEType() == null) return "?";
        return e.getEType().getName();
    }

    /** Always {@code lower..upper}. {@code [1,1]} renders as {@code "1..1"}
     *  rather than the shorthand {@code "1"}, so all rows of a class line
     *  up consistently and the reader doesn't have to know the convention. */
    private static String multiplicity(ETypedElement e) {
        int lo = e.getLowerBound();
        int hi = e.getUpperBound();
        return lo + ".." + (hi == -1 ? "*" : Integer.toString(hi));
    }

    public static String qualifiedName(EPackage pkg) {
        StringBuilder sb = new StringBuilder();
        for (EPackage p = pkg; p != null; p = p.getESuperPackage()) {
            if (sb.length() > 0) sb.insert(0, '.');
            sb.insert(0, p.getName() == null ? "unnamed" : p.getName());
        }
        return sb.toString();
    }

    /** Iterate every EClassifier in a list of root packages (and their sub-packages). */
    private static List<Object> keys(DiagramBuilder b, List<EPackage> roots) {
        List<Object> out = new ArrayList<>();
        ArrayDeque<EPackage> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            EPackage p = queue.pop();
            for (EClassifier c : p.getEClassifiers()) {
                if (b.has(c)) out.add(c);
            }
            queue.addAll((Collection<EPackage>) p.getESubpackages());
        }
        return out;
    }
}
