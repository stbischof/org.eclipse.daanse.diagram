package org.eclipse.daanse.diagram.kind.rolap.cube;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.daanse.diagram.core.DEdge;
import org.eclipse.daanse.diagram.core.DEndpoint;
import org.eclipse.daanse.diagram.core.DNode;
import org.eclipse.daanse.diagram.core.DPort;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.NodeBody;
import org.eclipse.daanse.diagram.core.PortSide;
import org.eclipse.daanse.diagram.kind.rolap.cube.CubeBody;
import org.eclipse.daanse.diagram.kind.rolap.cube.DimensionBody;
import org.eclipse.daanse.diagram.kind.rolap.cube.HierarchyBody;
import org.eclipse.daanse.diagram.kind.rolap.cube.MappingModel;
import org.eclipse.daanse.diagram.kind.rolap.cube.SourceBody;
import org.eclipse.daanse.diagram.kind.schema.TableBody;

public final class CubeStructureDiagramBuilder {
    /*
     * WARNING - void declaration
     */
    public Diagram build(MappingModel m, String cubeId) {
        TableBody tb;
        Diagram d = new Diagram().title("cube-structure");
        d.option("elk.direction", "RIGHT");
        d.option("elk.spacing.nodeNode", "30");
        d.option("elk.layered.spacing.nodeNodeBetweenLayers", "80");
        MappingModel.Cube cube = m.cubes.get(cubeId);
        if (cube == null) {
            throw new IllegalArgumentException("no cube " + cubeId);
        }
        LinkedHashSet<String> usedHierarchies = new LinkedHashSet<String>();
        LinkedHashSet<String> usedSources = new LinkedHashSet<String>();
        LinkedHashSet<String> usedTables = new LinkedHashSet<String>();
        if (cube.queryId != null) {
            this.collectSourceAndTables(cube.queryId, m, usedSources, usedTables);
        }
        for (MappingModel.DimensionConnector dimensionConnector : cube.connectors) {
            MappingModel.Dimension dim = m.dimensions.get(dimensionConnector.dimensionId);
            if (dim == null) continue;
            for (String hid : dim.hierarchyIds) {
                usedHierarchies.add(hid);
                MappingModel.Hierarchy h = m.hierarchies.get(hid);
                if (h == null || h.queryId == null) continue;
                this.collectSourceAndTables(h.queryId, m, usedSources, usedTables);
            }
        }
        CubeBody cubeBody = new CubeBody(cube.name, cube.virtual);
        for (MappingModel.Measure ms : cube.measures) {
            String col = this.labelColumn(m, ms.columnId);
            cubeBody.addMeasure(ms.name + "  : " + (ms.aggregator != null ? ms.aggregator : "?") + "(" + col + ")");
        }
        DNode dNode = d.addNode(new DNode("cube_" + cube.id, (NodeBody)cubeBody));
        HashMap<String, DNode> dimNodes = new HashMap<String, DNode>();
        HashMap<String, DNode> hierNodes = new HashMap<String, DNode>();
        HashMap<String, HierarchyBody> hierBodies = new HashMap<String, HierarchyBody>();
        for (MappingModel.DimensionConnector con : cube.connectors) {
            MappingModel.Dimension dim = m.dimensions.get(con.dimensionId);
            if (dim == null) continue;
            String fkLabel = this.labelColumn(m, con.foreignKeyId);
            DimensionBody db = new DimensionBody(con.overrideName != null ? con.overrideName : dim.name, fkLabel, dim.time);
            DNode dNode2 = d.addNode(new DNode("dim_" + con.id, (NodeBody)db));
            dimNodes.put(con.id, dNode2);
            DEdge ce = new DEdge("cd_" + con.id).label((String)(fkLabel != null ? "fk: " + fkLabel : ""));
            ce.addSource((DEndpoint)new DEndpoint.NodeEndpoint(dNode));
            ce.addTarget((DEndpoint)new DEndpoint.NodeEndpoint(dNode2));
            d.addEdge(ce);
            for (String hid : dim.hierarchyIds) {
                MappingModel.Hierarchy h = m.hierarchies.get(hid);
                if (h == null) continue;
                DNode hn = (DNode)hierNodes.get(hid);
                if (hn == null) {
                    HierarchyBody hb = this.buildHierarchyBody(h, m);
                    hierBodies.put(hid, hb);
                    hn = d.addNode(new DNode("hier_" + hid, (NodeBody)hb));
                    hierNodes.put(hid, hn);
                }
                DEdge dh = new DEdge("dh_" + con.id + "_" + hid);
                dh.addSource((DEndpoint)new DEndpoint.NodeEndpoint(dNode2));
                dh.addTarget((DEndpoint)new DEndpoint.NodeEndpoint(hn));
                d.addEdge(dh);
            }
        }
        HashMap<String, DNode> sourceNodes = new HashMap<>();
        HashMap<String, SourceBody> sourceBodies = new HashMap<>();
        for (String sid : usedSources) {
            MappingModel.Source s = m.sources.get(sid);
            if (s == null) continue;
            SourceBody body;
            if (s instanceof MappingModel.TableSource ts) {
                body = new SourceBody("TABLE SOURCE");
                MappingModel.Table t = m.tables.get(ts.tableId);
                body.addRow("table = " + (t != null ? t.name : ts.tableId));
            } else {
                MappingModel.JoinSource js = (MappingModel.JoinSource) s;
                body = new SourceBody("JOIN SOURCE");
                String l = js.left != null ? this.labelColumn(m, js.left.keyColumnId) : "?";
                String r = js.right != null ? this.labelColumn(m, js.right.keyColumnId) : "?";
                body.addRow("left.key = " + l);
                body.addRow("right.key = " + r);
            }
            DNode sn = d.addNode(new DNode("src_" + sid, body));
            sourceNodes.put(sid, sn);
            sourceBodies.put(sid, body);
        }
        if (cube.queryId != null && sourceNodes.containsKey(cube.queryId)) {
            DEdge e = new DEdge("cq_" + cube.id);
            e.addSource((DEndpoint)new DEndpoint.PortEndpoint(cubeBody.queryPort()));
            e.addTarget((DEndpoint)new DEndpoint.NodeEndpoint((DNode)sourceNodes.get(cube.queryId)));
            e.label("query");
            d.addEdge(e);
        }
        for (Map.Entry en : hierNodes.entrySet()) {
            DNode dNode3;
            MappingModel.Hierarchy h = m.hierarchies.get(en.getKey());
            if (h == null || h.queryId == null || (dNode3 = (DNode)sourceNodes.get(h.queryId)) == null) continue;
            HierarchyBody hb = (HierarchyBody)hierBodies.get(en.getKey());
            DEdge e = new DEdge("hq_" + (String)en.getKey());
            e.addSource((DEndpoint)new DEndpoint.NodeEndpoint((DNode)en.getValue()));
            e.addTarget((DEndpoint)new DEndpoint.NodeEndpoint(dNode3));
            e.label("query");
            d.addEdge(e);
        }
        HashMap<String, DNode> tableNodes = new HashMap<String, DNode>();
        HashMap<String, TableBody> tableBodies = new HashMap<String, TableBody>();
        for (String string : usedTables) {
            MappingModel.Table t = m.tables.get(string);
            if (t == null) continue;
            TableBody localTb = new TableBody(t.name).headerKind(TableBody.HeaderKind.TABLE);
            for (MappingModel.Column c : t.columns) {
                localTb.addColumn(new TableBody.Column(c.name));
            }
            DNode tn = d.addNode(new DNode("tbl_" + string, localTb));
            tableNodes.put(string, tn);
            tableBodies.put(string, localTb);
        }
        for (String string : usedSources) {
            MappingModel.Source s = m.sources.get(string);
            if (s instanceof MappingModel.TableSource) {
                MappingModel.TableSource ts = (MappingModel.TableSource)s;
                if (ts.tableId != null) {
                    DNode src = (DNode)sourceNodes.get(string);
                    DNode tn = (DNode)tableNodes.get(ts.tableId);
                    if (src == null || tn == null) continue;
                    DEdge e = new DEdge("st_" + string);
                    e.addSource((DEndpoint)new DEndpoint.NodeEndpoint(src));
                    e.addTarget((DEndpoint)new DEndpoint.NodeEndpoint(tn));
                    d.addEdge(e);
                    continue;
                }
            }
            if (!(s instanceof MappingModel.JoinSource)) continue;
            MappingModel.JoinSource js = (MappingModel.JoinSource)s;
            int rowIdx = 0;
            for (MappingModel.JoinSide side : List.of(js.left, js.right)) {
                TableBody tb2;
                if (side == null) {
                    ++rowIdx;
                    continue;
                }
                DPort fromPort = ((SourceBody)sourceBodies.get(string)).rowEastPort(rowIdx);
                MappingModel.Column col = m.columns.get(side.keyColumnId);
                if (col != null && col.tableId != null && (tb2 = (TableBody)tableBodies.get(col.tableId)) != null) {
                    DEdge e = new DEdge("jk_" + string + "_" + rowIdx);
                    e.addSource((DEndpoint)new DEndpoint.PortEndpoint(fromPort));
                    e.addTarget((DEndpoint)new DEndpoint.PortEndpoint(tb2.port(col.name, PortSide.WEST)));
                    d.addEdge(e);
                }
                ++rowIdx;
            }
        }
        for (Map.Entry entry : hierBodies.entrySet()) {
            MappingModel.Hierarchy h = m.hierarchies.get(entry.getKey());
            HierarchyBody hb = (HierarchyBody)entry.getValue();
            if (h == null) continue;
            for (String lid : h.levelIds) {
                MappingModel.Level lv = m.levels.get(lid);
                if (lv == null) continue;
                this.wireLevelProp(d, hb, lid, "column", lv.columnId, m, tableBodies);
                this.wireLevelProp(d, hb, lid, "name", lv.nameColumnId, m, tableBodies);
                this.wireLevelProp(d, hb, lid, "caption", lv.captionColumnId, m, tableBodies);
                this.wireLevelProp(d, hb, lid, "ordinal", lv.ordinalColumnId, m, tableBodies);
                this.wireLevelProp(d, hb, lid, "parent", lv.parentColumnId, m, tableBodies);
            }
        }
        for (int i = 0; i < cube.measures.size(); ++i) {
            MappingModel.Column col;
            MappingModel.Measure measure = cube.measures.get(i);
            if (measure.columnId == null || (col = m.columns.get(measure.columnId)) == null || col.tableId == null || (tb = (TableBody)tableBodies.get(col.tableId)) == null) continue;
            DPort east = cubeBody.measurePort(i);
            DEdge e = new DEdge("mc_" + measure.id);
            e.addSource((DEndpoint)new DEndpoint.PortEndpoint(east));
            e.addTarget((DEndpoint)new DEndpoint.PortEndpoint(tb.port(col.name, PortSide.WEST)));
            d.addEdge(e);
        }
        return d;
    }

    private void wireLevelProp(Diagram d, HierarchyBody hb, String levelId, String propName, String columnId, MappingModel m, Map<String, TableBody> tableBodies) {
        if (columnId == null) {
            return;
        }
        MappingModel.Column col = m.columns.get(columnId);
        if (col == null || col.tableId == null) {
            return;
        }
        TableBody tb = tableBodies.get(col.tableId);
        if (tb == null) {
            return;
        }
        DPort src = hb.propertyPort(levelId, propName);
        if (src == null) {
            return;
        }
        DEdge e = new DEdge("lc_" + levelId + "_" + propName);
        e.addSource((DEndpoint)new DEndpoint.PortEndpoint(src));
        e.addTarget((DEndpoint)new DEndpoint.PortEndpoint(tb.port(col.name, PortSide.WEST)));
        d.addEdge(e);
    }

    private HierarchyBody buildHierarchyBody(MappingModel.Hierarchy h, MappingModel m) {
        HierarchyBody hb = new HierarchyBody(h.name != null && !h.name.isEmpty() ? h.name : CubeStructureDiagramBuilder.simpleId(h.id), this.labelColumn(m, h.primaryKeyId), h.parentChild);
        for (String lid : h.levelIds) {
            MappingModel.Level lv = m.levels.get(lid);
            if (lv == null) continue;
            HierarchyBody.LevelRow row = hb.addLevel(lid, lv.name).uniqueMembers(lv.uniqueMembers);
            if (lv.columnId != null) {
                row.addProp("column", this.labelColumn(m, lv.columnId));
            }
            if (lv.nameColumnId != null) {
                row.addProp("name", this.labelColumn(m, lv.nameColumnId));
            }
            if (lv.captionColumnId != null) {
                row.addProp("caption", this.labelColumn(m, lv.captionColumnId));
            }
            if (lv.ordinalColumnId != null) {
                row.addProp("ordinal", this.labelColumn(m, lv.ordinalColumnId));
            }
            if (lv.parentColumnId == null) continue;
            row.addProp("parent", this.labelColumn(m, lv.parentColumnId));
        }
        return hb;
    }

    private void collectSourceAndTables(String sid, MappingModel m, Set<String> sources, Set<String> tables) {
        if (sid == null || sources.contains(sid)) {
            return;
        }
        MappingModel.Source s = m.sources.get(sid);
        if (s == null) {
            return;
        }
        sources.add(sid);
        if (s instanceof MappingModel.TableSource) {
            MappingModel.TableSource ts = (MappingModel.TableSource)s;
            if (ts.tableId != null) {
                tables.add(ts.tableId);
            }
        } else if (s instanceof MappingModel.JoinSource) {
            MappingModel.Column c;
            MappingModel.JoinSource js = (MappingModel.JoinSource)s;
            if (js.left != null) {
                this.collectSourceAndTables(js.left.queryId, m, sources, tables);
                if (js.left.keyColumnId != null && (c = m.columns.get(js.left.keyColumnId)) != null && c.tableId != null) {
                    tables.add(c.tableId);
                }
            }
            if (js.right != null) {
                this.collectSourceAndTables(js.right.queryId, m, sources, tables);
                if (js.right.keyColumnId != null && (c = m.columns.get(js.right.keyColumnId)) != null && c.tableId != null) {
                    tables.add(c.tableId);
                }
            }
        }
    }

    private String labelColumn(MappingModel m, String colId) {
        if (colId == null) {
            return null;
        }
        MappingModel.Column c = m.columns.get(colId);
        if (c != null && c.name != null) {
            MappingModel.Table t;
            MappingModel.Table table = t = c.tableId != null ? m.tables.get(c.tableId) : null;
            if (t != null) {
                return t.name + "." + c.name;
            }
            return c.name;
        }
        return CubeStructureDiagramBuilder.simpleId(colId);
    }

    private static String simpleId(String id) {
        if (id == null) {
            return "?";
        }
        return id.startsWith("_") ? id.substring(1) : id;
    }
}

