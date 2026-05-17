package org.eclipse.daanse.diagram.kind.rolap.cube;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.daanse.diagram.kind.rolap.cube.MappingModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class CatalogMappingParser {
    public MappingModel parse(String filePath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new File(filePath));
        Element root = doc.getDocumentElement();
        MappingModel m = new MappingModel();
        for (Element e : CatalogMappingParser.everyDescendant(root)) {
            String type = CatalogMappingParser.qual(e, "xsi:type");
            String tag = e.getLocalName();
            String id = idAttr(e);
            if ("Schema".equals(tag) || type != null && type.endsWith("Schema")) {
                this.parseSchema(e, m);
                continue;
            }
            if ("PhysicalCube".equals(tag) || "VirtualCube".equals(tag)) {
                this.parseCube(e, m, "VirtualCube".equals(tag));
                continue;
            }
            if ("StandardDimension".equals(tag) || "TimeDimension".equals(tag)) {
                MappingModel.Dimension d = m.dimensions.computeIfAbsent(id, MappingModel.Dimension::new);
                d.name = e.getAttribute("name");
                d.time = "TimeDimension".equals(tag);
                CatalogMappingParser.addAll(d.hierarchyIds, e.getAttribute("hierarchies"));
                continue;
            }
            if ("ExplicitHierarchy".equals(tag) || "ParentChildHierarchy".equals(tag)) {
                MappingModel.Hierarchy h = m.hierarchies.computeIfAbsent(id, MappingModel.Hierarchy::new);
                h.name = e.getAttribute("name");
                h.parentChild = "ParentChildHierarchy".equals(tag);
                h.queryId = CatalogMappingParser.nullOrEmpty(e.getAttribute("query"));
                h.primaryKeyId = CatalogMappingParser.nullOrEmpty(e.getAttribute("primaryKey"));
                CatalogMappingParser.addAll(h.levelIds, e.getAttribute("levels"));
                continue;
            }
            if ("Level".equals(tag)) {
                MappingModel.Level l = m.levels.computeIfAbsent(id, MappingModel.Level::new);
                l.name = e.getAttribute("name");
                l.columnId = CatalogMappingParser.nullOrEmpty(e.getAttribute("column"));
                l.nameColumnId = CatalogMappingParser.nullOrEmpty(e.getAttribute("nameColumn"));
                l.captionColumnId = CatalogMappingParser.nullOrEmpty(e.getAttribute("captionColumn"));
                l.parentColumnId = CatalogMappingParser.nullOrEmpty(e.getAttribute("parentColumn"));
                l.uniqueMembers = "true".equalsIgnoreCase(e.getAttribute("uniqueMembers"));
                for (Element oc : CatalogMappingParser.children(e, "ordinalColumns")) {
                    l.ordinalColumnId = CatalogMappingParser.nullOrEmpty(oc.getAttribute("column"));
                }
                continue;
            }
            if ("TableSource".equals(tag)) {
                MappingModel.TableSource ts = (MappingModel.TableSource)m.sources.computeIfAbsent(id, MappingModel.TableSource::new);
                ts.tableId = CatalogMappingParser.nullOrEmpty(e.getAttribute("table"));
                continue;
            }
            if (!"JoinSource".equals(tag)) continue;
            MappingModel.JoinSource js = (MappingModel.JoinSource)m.sources.computeIfAbsent(id, MappingModel.JoinSource::new);
            for (Element side : CatalogMappingParser.children(e, "left")) {
                js.left = this.parseJoinSide(side);
            }
            for (Element side : CatalogMappingParser.children(e, "right")) {
                js.right = this.parseJoinSide(side);
            }
        }
        return m;
    }

    private void parseSchema(Element schema, MappingModel m) {
        for (Element t : CatalogMappingParser.children(schema, "ownedElement")) {
            String xsi = CatalogMappingParser.qual(t, "xsi:type");
            if (xsi == null || !xsi.endsWith("Table")) continue;
            MappingModel.Table tbl = m.tables.computeIfAbsent(idAttr(t), MappingModel.Table::new);
            tbl.name = t.getAttribute("name");
            for (Element f : CatalogMappingParser.children(t, "feature")) {
                String xsiF = CatalogMappingParser.qual(f, "xsi:type");
                if (xsiF == null || !xsiF.endsWith("Column")) continue;
                MappingModel.Column c = m.columns.computeIfAbsent(idAttr(f), MappingModel.Column::new);
                c.name = f.getAttribute("name");
                c.typeId = CatalogMappingParser.nullOrEmpty(f.getAttribute("type"));
                c.tableId = tbl.id;
                tbl.columns.add(c);
            }
        }
    }

    private void parseCube(Element e, MappingModel m, boolean virtual) {
        MappingModel.Cube c = m.cubes.computeIfAbsent(idAttr(e), MappingModel.Cube::new);
        c.name = e.getAttribute("name");
        c.virtual = virtual;
        c.queryId = CatalogMappingParser.nullOrEmpty(e.getAttribute("query"));
        for (Element dc : CatalogMappingParser.children(e, "dimensionConnectors")) {
            MappingModel.DimensionConnector con = new MappingModel.DimensionConnector();
            con.id = idAttr(dc);
            con.overrideName = CatalogMappingParser.nullOrEmpty(dc.getAttribute("overrideDimensionName"));
            con.foreignKeyId = CatalogMappingParser.nullOrEmpty(dc.getAttribute("foreignKey"));
            con.dimensionId = CatalogMappingParser.nullOrEmpty(dc.getAttribute("dimension"));
            c.connectors.add(con);
        }
        for (Element mg : CatalogMappingParser.children(e, "measureGroups")) {
            for (Element ms : CatalogMappingParser.children(mg, "measures")) {
                MappingModel.Measure measure = new MappingModel.Measure();
                measure.id = idAttr(ms);
                measure.name = ms.getAttribute("name");
                String t = CatalogMappingParser.qual(ms, "xsi:type");
                measure.aggregator = t != null && t.contains(":") ? t.substring(t.indexOf(58) + 1).replace("Measure", "").toLowerCase() : "?";
                measure.columnId = CatalogMappingParser.nullOrEmpty(ms.getAttribute("column"));
                c.measures.add(measure);
            }
        }
    }

    private MappingModel.JoinSide parseJoinSide(Element s) {
        MappingModel.JoinSide js = new MappingModel.JoinSide();
        js.queryId = CatalogMappingParser.nullOrEmpty(s.getAttribute("query"));
        js.keyColumnId = CatalogMappingParser.nullOrEmpty(s.getAttribute("key"));
        return js;
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

    private static List<Element> everyDescendant(Element root) {
        ArrayList<Element> out = new ArrayList<Element>();
        ArrayDeque<Element> stack = new ArrayDeque<Element>();
        NodeList top = root.getChildNodes();
        for (int i = top.getLength() - 1; i >= 0; --i) {
            Node n = top.item(i);
            if (n.getNodeType() != 1) continue;
            stack.push((Element)n);
        }
        while (!stack.isEmpty()) {
            Element e = (Element)stack.pop();
            out.add(e);
            NodeList nl = e.getChildNodes();
            for (int i = nl.getLength() - 1; i >= 0; --i) {
                Node n = nl.item(i);
                if (n.getNodeType() != 1) continue;
                stack.push((Element)n);
            }
        }
        return out;
    }

    private static String qual(Element e, String name) {
        String v = e.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type");
        if (v == null || v.isEmpty()) {
            v = e.getAttribute(name);
        }
        return v == null || v.isEmpty() ? null : v;
    }

    private static String nullOrEmpty(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    private static void addAll(List<String> sink, String spaceSep) {
        if (spaceSep == null || spaceSep.isEmpty()) {
            return;
        }
        for (String s : spaceSep.split("\\s+")) {
            if (s.isEmpty()) continue;
            sink.add(s);
        }
    }
    /** Returns xmi:id if set, falling back to id. */
    private static String idAttr(org.w3c.dom.Element e) {
        String v = e.getAttribute("xmi:id");
        return v == null || v.isEmpty() ? e.getAttribute("id") : v;
    }
}

