package org.eclipse.daanse.diagram.kind.rolap.access;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.eclipse.daanse.diagram.kind.rolap.access.AccessModel;
import org.eclipse.daanse.diagram.kind.rolap.access.ObjectBody;
import org.eclipse.daanse.diagram.kind.rolap.access.RoleBody;
import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DEndpoint;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.NodeBody;

public final class AccessDiagramBuilder {
    public Diagram build(AccessModel m) {
        DNode n;
        String label;
        AccessModel.NamedObj o;
        Diagram d = new Diagram().title("access");
        d.option("elk.direction", "RIGHT");
        d.option("elk.spacing.nodeNode", "30");
        d.option("elk.layered.spacing.nodeNodeBetweenLayers", "70");
        HashMap<String, DNode> objNodes = new HashMap<String, DNode>();
        for (AccessModel.Catalog c : m.catalogs.values()) {
            DNode n2 = d.addNode(new DNode("cat_" + c.id, (NodeBody)new ObjectBody("CATALOG", c.displayName(), "#1e3a8a", "#1e3a8a")));
            objNodes.put(c.id, n2);
        }
        LinkedHashSet<String> usedCubes = new LinkedHashSet<String>();
        LinkedHashSet<String> usedHierarchies = new LinkedHashSet<String>();
        LinkedHashSet<String> usedSchemas = new LinkedHashSet<String>();
        for (AccessModel.AccessRole r : m.roles.values()) {
            for (AccessModel.CatalogGrant cg : r.catalogGrants) {
                for (AccessModel.CubeGrant ug : cg.cubeGrants) {
                    if (ug.cubeId != null) {
                        usedCubes.add(ug.cubeId);
                    }
                    for (AccessModel.HierarchyGrant hg : ug.hierarchyGrants) {
                        if (hg.hierarchyId == null) continue;
                        usedHierarchies.add(hg.hierarchyId);
                    }
                }
                for (AccessModel.DbSchemaGrant sg : cg.schemaGrants) {
                    if (sg.schemaId == null) continue;
                    usedSchemas.add(sg.schemaId);
                }
            }
        }
        for (String id : usedCubes) {
            o = m.cubes.get(id);
            if (o == null) continue;
            label = "VIRTUAL_CUBE".equals(o.kind) ? "VIRTUAL CUBE" : "CUBE";
            n = d.addNode(new DNode("obj_" + id, (NodeBody)new ObjectBody(label, o.displayName(), "#047857", "#047857")));
            objNodes.put(id, n);
        }
        for (String id : usedHierarchies) {
            o = m.hierarchies.get(id);
            if (o == null) continue;
            DNode n3 = d.addNode(new DNode("obj_" + id, (NodeBody)new ObjectBody("HIERARCHY", o.displayName().isEmpty() ? id : o.displayName(), "#b45309", "#b45309")));
            objNodes.put(id, n3);
        }
        for (String id : usedSchemas) {
            o = m.schemas.get(id);
            label = o != null ? o.displayName() : id;
            n = d.addNode(new DNode("obj_" + id, (NodeBody)new ObjectBody("DATABASE SCHEMA", label, "#374151", "#374151")));
            objNodes.put(id, n);
        }
        for (AccessModel.AccessRole r : m.roles.values()) {
            this.buildRole(d, r, m, objNodes);
        }
        return d;
    }

    private void buildRole(Diagram d, AccessModel.AccessRole r, AccessModel m, Map<String, DNode> objNodes) {
        RoleBody body = new RoleBody(r.name != null ? r.name : r.id);
        for (AccessModel.CatalogGrant cg : r.catalogGrants) {
            RoleBody.Row row;
            RoleBody.Row catRow = body.addRow(RoleBody.GrantKind.CATALOG, "Catalog", cg.catalogAccess != null ? cg.catalogAccess : "all");
            for (AccessModel.CubeGrant ug : cg.cubeGrants) {
                String cubeName = AccessDiagramBuilder.nameFor(m, ug.cubeId, "Cube");
                row = body.addRow(RoleBody.GrantKind.CUBE, cubeName, ug.cubeAccess != null ? ug.cubeAccess : "all");
                row.target(ug.cubeId);
                for (AccessModel.HierarchyGrant hg : ug.hierarchyGrants) {
                    String hLabel = AccessDiagramBuilder.nameFor(m, hg.hierarchyId, "Hierarchy");
                    if (hg.topLevelId != null || hg.bottomLevelId != null) {
                        StringBuilder sb = new StringBuilder(hLabel);
                        if (hg.topLevelId != null) {
                            sb.append("  top=").append(AccessDiagramBuilder.simpleId(hg.topLevelId));
                        }
                        if (hg.bottomLevelId != null) {
                            sb.append("  bot=").append(AccessDiagramBuilder.simpleId(hg.bottomLevelId));
                        }
                        hLabel = sb.toString();
                    }
                    RoleBody.Row hRow = body.addRow(RoleBody.GrantKind.HIERARCHY, hLabel, hg.hierarchyAccess != null ? hg.hierarchyAccess : "all");
                    hRow.target(hg.hierarchyId);
                    for (AccessModel.MemberGrant mg : hg.memberGrants) {
                        body.addMemberRow(mg.member != null ? mg.member : "?", mg.memberAccess != null ? mg.memberAccess : "all");
                    }
                }
            }
            for (AccessModel.DbSchemaGrant sg : cg.schemaGrants) {
                String sLabel = AccessDiagramBuilder.nameFor(m, sg.schemaId, "Schema");
                row = body.addRow(RoleBody.GrantKind.SCHEMA, sLabel, sg.databaseSchemaAccess != null ? sg.databaseSchemaAccess : "all");
                row.target(sg.schemaId);
            }
            catRow.target(null);
        }
        DNode roleNode = d.addNode(new DNode("role_" + r.id, (NodeBody)body));
        for (RoleBody.Row row : body.rows()) {
            DNode target;
            if (row.targetId == null || (target = objNodes.get(row.targetId)) == null) continue;
            String access = row.access;
            DEdge edge = new DEdge("g_" + roleNode.id() + "_" + row.targetId).label(access != null ? access.toUpperCase() : "").cssClass("dv-access");
            edge.addSource((DEndpoint)new DEndpoint.PortEndpoint(body.port(row)));
            edge.addTarget((DEndpoint)new DEndpoint.NodeEndpoint(target));
            d.addEdge(edge);
        }
    }

    private static String nameFor(AccessModel m, String id, String fallback) {
        if (id == null) {
            return fallback;
        }
        AccessModel.NamedObj o = m.cubes.get(id);
        if (o == null) {
            o = m.hierarchies.get(id);
        }
        if (o == null) {
            o = m.schemas.get(id);
        }
        if (o == null) {
            o = m.levels.get(id);
        }
        if (o == null && m.catalogs.containsKey(id)) {
            o = m.catalogs.get(id);
        }
        if (o != null && o.name != null && !o.name.isEmpty()) {
            return o.name;
        }
        return AccessDiagramBuilder.simpleId(id);
    }

    private static String simpleId(String id) {
        String s;
        if (id == null) {
            return "?";
        }
        int slash = id.indexOf(47);
        String string = s = slash >= 0 ? id.substring(slash + 1) : id;
        if (s.startsWith("_")) {
            s = s.substring(1);
        }
        return s;
    }
}

