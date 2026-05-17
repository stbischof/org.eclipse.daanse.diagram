package org.eclipse.daanse.diagram.kind.rolap.access;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.daanse.diagram.kind.rolap.access.AccessModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class FoodmartAccessParser {
    public AccessModel parse(String filePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new File(filePath));
        Element root = doc.getDocumentElement();
        AccessModel m = new AccessModel();
        NodeList children = root.getChildNodes();
        block15: for (int i = 0; i < children.getLength(); ++i) {
            Node n = children.item(i);
            if (n.getNodeType() != 1) continue;
            Element e = (Element)n;
            String tag = e.getLocalName();
            String id = e.getAttribute("xmi:id");
            String name = e.getAttribute("name");
            switch (tag) {
                case "Catalog": {
                    this.parseCatalog(e, m);
                    continue block15;
                }
                case "PhysicalCube":
                case "VirtualCube": {
                    AccessModel.NamedObj c = m.namedObj(m.cubes, id, tag.equals("VirtualCube") ? "VIRTUAL_CUBE" : "CUBE");
                    c.name = name;
                    continue block15;
                }
                case "ExplicitHierarchy": {
                    AccessModel.NamedObj h = m.namedObj(m.hierarchies, id, "HIERARCHY");
                    h.name = name;
                    continue block15;
                }
                case "Level": {
                    AccessModel.NamedObj l = m.namedObj(m.levels, id, "LEVEL");
                    l.name = name;
                    continue block15;
                }
                case "AccessRole": {
                    this.parseAccessRole(e, m);
                    continue block15;
                }
                default: {
                    if (!tag.endsWith("Schema") && !tag.equalsIgnoreCase("DatabaseSchema") && !tag.equalsIgnoreCase("PhysicalSchema")) continue block15;
                    AccessModel.NamedObj s = m.namedObj(m.schemas, id, "SCHEMA");
                    s.name = name;
                }
            }
        }
        return m;
    }

    private void parseCatalog(Element e, AccessModel m) {
        AccessModel.Catalog c = m.catalog(e.getAttribute("xmi:id"));
        c.name = e.getAttribute("name");
        FoodmartAccessParser.addAll(c.cubeIds, e.getAttribute("cubes"));
        FoodmartAccessParser.addAll(c.roleIds, e.getAttribute("accessRoles"));
        FoodmartAccessParser.addAll(c.schemaIds, e.getAttribute("dbschemas"));
        for (String sid : c.schemaIds) {
            m.namedObj(m.schemas, sid, "SCHEMA");
        }
    }

    private void parseAccessRole(Element e, AccessModel m) {
        AccessModel.AccessRole r = m.role(e.getAttribute("xmi:id"));
        r.name = e.getAttribute("name");
        FoodmartAccessParser.addAll(r.referencedRoles, e.getAttribute("referencedAccessRoles"));
        for (Element cge : FoodmartAccessParser.children(e, "accessCatalogGrants")) {
            AccessModel.CatalogGrant cg = new AccessModel.CatalogGrant();
            cg.catalogAccess = FoodmartAccessParser.attr(cge, "catalogAccess", null);
            for (Element cube : FoodmartAccessParser.children(cge, "cubeGrants")) {
                AccessModel.CubeGrant ug = new AccessModel.CubeGrant();
                ug.cubeAccess = FoodmartAccessParser.attr(cube, "cubeAccess", null);
                ug.cubeId = FoodmartAccessParser.attr(cube, "cube", null);
                for (Element h : FoodmartAccessParser.children(cube, "hierarchyGrants")) {
                    AccessModel.HierarchyGrant hg = new AccessModel.HierarchyGrant();
                    hg.hierarchyAccess = FoodmartAccessParser.attr(h, "hierarchyAccess", null);
                    hg.hierarchyId = FoodmartAccessParser.attr(h, "hierarchy", null);
                    hg.topLevelId = FoodmartAccessParser.attr(h, "topLevel", null);
                    hg.bottomLevelId = FoodmartAccessParser.attr(h, "bottomLevel", null);
                    for (Element mg : FoodmartAccessParser.children(h, "memberGrants")) {
                        AccessModel.MemberGrant memberGrant = new AccessModel.MemberGrant();
                        memberGrant.memberAccess = FoodmartAccessParser.attr(mg, "memberAccess", null);
                        memberGrant.member = FoodmartAccessParser.attr(mg, "member", null);
                        hg.memberGrants.add(memberGrant);
                    }
                    ug.hierarchyGrants.add(hg);
                }
                cg.cubeGrants.add(ug);
            }
            for (Element sge : FoodmartAccessParser.children(cge, "databaseSchemaGrants")) {
                AccessModel.DbSchemaGrant sg = new AccessModel.DbSchemaGrant();
                sg.databaseSchemaAccess = FoodmartAccessParser.attr(sge, "databaseSchemaAccess", null);
                sg.schemaId = FoodmartAccessParser.attr(sge, "databaseSchema", null);
                cg.schemaGrants.add(sg);
            }
            r.catalogGrants.add(cg);
        }
    }

    private static List<Element> children(Element parent, String localName) {
        ArrayList<Element> out = new ArrayList<Element>();
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeType() != 1 || !localName.equals(n.getNodeName())) continue;
            out.add((Element)n);
        }
        return out;
    }

    private static String attr(Element e, String name, String defaultValue) {
        String v = e.getAttribute(name);
        return v == null || v.isEmpty() ? defaultValue : v;
    }

    private static void addAll(List<String> sink, String spaceSeparated) {
        if (spaceSeparated == null || spaceSeparated.isEmpty()) {
            return;
        }
        for (String s : spaceSeparated.split("\\s+")) {
            if (s.isEmpty()) continue;
            sink.add(s);
        }
    }
}

