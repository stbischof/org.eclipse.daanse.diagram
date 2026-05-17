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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.Component;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DataManager;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DataProvider;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DeployedComponent;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DeployedSoftwareSystem;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.Machine;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.ProviderConnection;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.Site;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.SoftwareSystem;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Namespace;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;

/**
 * Renders a CWM Software Deployment package: Sites, Machines and the
 * DeployedComponents on each, plus their underlying Component / Software
 * System and ProviderConnection edges between data manager / data provider.
 */
public final class CwmSoftwareDeploymentConverter {

    private static final String ID_NODE = "sd";
    private static final String ID_EDGE = "sde";

    private static final Stereotype MACHINE_S         = new Stereotype("MACHINE",                            Palette.TEAL_700,   Icons.MACHINE);
    private static final Stereotype DATA_PROVIDER_S   = new Stereotype("DATA MANAGER ▸ DATA PROVIDER",       Palette.VIOLET_500, Icons.DATABASE);
    private static final Stereotype DATA_MANAGER_S    = new Stereotype("DEPLOYED COMPONENT ▸ DATA MANAGER",  Palette.VIOLET_700, Icons.DATABASE);
    private static final Stereotype DEPLOYED_SS_S     = new Stereotype("DEPLOYED SOFTWARE SYSTEM",           Palette.INDIGO_900, Icons.SOFTWARE_SYSTEM);
    private static final Stereotype DEPLOYED_COMP_S   = new Stereotype("PACKAGE ▸ DEPLOYED COMPONENT",       Palette.PURPLE_900, Icons.COMPONENT);
    private static final Stereotype COMPONENT_S       = new Stereotype("COMPONENT",                           Palette.GRAY_700,   Icons.COMPONENT);
    private static final Stereotype SOFTWARE_SYSTEM_S = new Stereotype("SOFTWARE SYSTEM",                     Palette.GRAY_700,   Icons.SOFTWARE_SYSTEM);

    public Diagram convert(Package pkg) {
        DiagramBuilder b = DiagramBuilder.of(Names.n(pkg.getName()));

        // Pass 1: collect every owned ModelElement so VMs nested inside a host
        // Machine are picked up as well as the ones at package level. Two-pass
        // because a Machine's body type depends on whether it has nested Machines.
        List<ModelElement> all = new ArrayList<>();
        collectOwned(pkg, all);

        // Pass 2: render every node.
        for (ModelElement me : all) {
            renderElement(b, me, hasNestedMachine(me));
        }

        // Pass 3: containment-style nesting. Site→Machine, Site→subSite,
        // Machine→Machine (host→VM via ownedElement) become visual nesting.
        for (ModelElement me : all) {
            if (me instanceof Machine m && m.getSite() != null) {
                b.nest(m.getSite(), me);
            }
            if (me instanceof Site s && !s.getContainingSite().isEmpty()) {
                b.nest(s.getContainingSite().get(0), me);
            }
            if (me instanceof Machine vm && vm.eContainer() instanceof Machine host) {
                b.nest(host, me);
            }
        }

        // Pass 4: explicit edges only for relationships not expressed as nesting.
        for (ModelElement me : all) {
            if (me instanceof Machine m) {
                for (DeployedComponent dc : m.getDeployedComponent()) {
                    if (b.has(dc)) b.edge(m, dc, ID_EDGE).label("hosts");
                }
            }
            if (me instanceof DeployedComponent dc) {
                for (DeployedSoftwareSystem dss : dc.getDeployedSoftwareSystem()) {
                    if (b.has(dss)) b.edge(dc, dss, ID_EDGE).label("of system");
                }
            }
            if (me instanceof DeployedSoftwareSystem dss && dss.getSoftwareSystem() != null) {
                if (b.has(dss.getSoftwareSystem())) {
                    b.edge(dss, dss.getSoftwareSystem(), ID_EDGE).label("based on");
                }
            }
            if (me instanceof ProviderConnection pc
                    && pc.getDataProvider() != null && pc.getDataManager() != null
                    && b.has(pc.getDataManager()) && b.has(pc.getDataProvider())) {
                b.edge(pc.getDataManager(), pc.getDataProvider(), ID_EDGE)
                        .label(pc.getName() != null ? pc.getName() : "→ provider");
            }
        }
        return b.diagram();
    }

    private void renderElement(DiagramBuilder b, ModelElement me, boolean isMachineContainer) {
        if (me instanceof Site s) {
            // Site is a container; SchemaBody chrome, no detail rows.
            b.container(s, Names.n(s.getName()), "SITE", Icons.SITE, ID_NODE);
            return;
        }
        if (me instanceof Machine m) {
            String mKind = vmKind(m);
            if (isMachineContainer) {
                b.container(m, Names.n(m.getName()), mKind, Icons.MACHINE, ID_NODE);
                return;
            }
            // Stand-alone machine — render as labelled box with detail rows.
            // Use the depth-aware kind caption (MACHINE / VIRTUAL MACHINE / CONTAINER).
            Stereotype s = new Stereotype(mKind, Palette.TEAL_700, Icons.MACHINE);
            LabelledBoxBody body = b.labelled(m, s, Names.n(m.getName()), ID_NODE);
            if (m.getMachineID() != null) body.addRow("id: " + m.getMachineID());
            if (m.getHostName() != null) body.addRow("host: " + m.getHostName());
            if (m.getIpAddress() != null) body.addRow("ip: " + m.getIpAddress());
            return;
        }
        if (me instanceof DataProvider dp) {
            LabelledBoxBody body = b.labelled(dp, DATA_PROVIDER_S, Names.n(dp.getName()), ID_NODE);
            if (dp.getPathname() != null) body.addRow("path: " + dp.getPathname());
            return;
        }
        if (me instanceof DataManager dm) {
            LabelledBoxBody body = b.labelled(dm, DATA_MANAGER_S, Names.n(dm.getName()), ID_NODE);
            if (dm.getPathname() != null) body.addRow("path: " + dm.getPathname());
            return;
        }
        if (me instanceof DeployedSoftwareSystem dss) {
            LabelledBoxBody body = b.labelled(dss, DEPLOYED_SS_S, Names.n(dss.getName()), ID_NODE);
            if (dss.getFixLevel() != null) body.addRow("fix: " + dss.getFixLevel());
            return;
        }
        if (me instanceof DeployedComponent dc) {
            LabelledBoxBody body = b.labelled(dc, DEPLOYED_COMP_S, Names.n(dc.getName()), ID_NODE);
            if (dc.getPathname() != null) body.addRow("path: " + dc.getPathname());
            return;
        }
        if (me instanceof Component c) {
            b.labelled(c, COMPONENT_S, Names.n(c.getName()), ID_NODE);
            return;
        }
        if (me instanceof SoftwareSystem ss) {
            LabelledBoxBody body = b.labelled(ss, SOFTWARE_SYSTEM_S, Names.n(ss.getName()), ID_NODE);
            if (ss.getType() != null) body.addRow("type: " + ss.getType());
            if (ss.getSubtype() != null) body.addRow("subtype: " + ss.getSubtype());
            if (ss.getSupplier() != null) body.addRow("supplier: " + ss.getSupplier());
            if (ss.getVersion() != null) body.addRow("version: " + ss.getVersion());
            return;
        }
        // ProviderConnection is rendered as an edge only.
    }

    /** Depth-first collect of every {@link ModelElement} owned (transitively)
     *  by the given namespace, including the namespace's own children but
     *  not the namespace itself. */
    private static void collectOwned(Namespace ns, List<ModelElement> out) {
        for (ModelElement child : ns.getOwnedElement()) {
            out.add(child);
            if (child instanceof Namespace sub) collectOwned(sub, out);
        }
    }

    private static boolean hasNestedMachine(ModelElement me) {
        if (!(me instanceof Machine m)) return false;
        for (ModelElement child : m.getOwnedElement()) {
            if (child instanceof Machine) return true;
        }
        return false;
    }

    /** Depth-aware kind caption: a top-level Machine is "MACHINE", one
     *  nested under another Machine reads as "VIRTUAL MACHINE", anything
     *  deeper reads as "CONTAINER" — so the diagram is self-explanatory
     *  even without colour cues. */
    private static String vmKind(Machine m) {
        int depth = 0;
        org.eclipse.emf.ecore.EObject p = m.eContainer();
        while (p instanceof Machine) { depth++; p = p.eContainer(); }
        return switch (depth) {
            case 0 -> "MACHINE";
            case 1 -> "VIRTUAL MACHINE";
            default -> "CONTAINER";
        };
    }
}
