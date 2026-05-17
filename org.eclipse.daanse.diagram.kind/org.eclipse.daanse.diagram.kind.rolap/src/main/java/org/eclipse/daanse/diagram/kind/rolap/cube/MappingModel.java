package org.eclipse.daanse.diagram.kind.rolap.cube;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MappingModel {
    public final Map<String, Cube> cubes = new LinkedHashMap<String, Cube>();
    public final Map<String, Dimension> dimensions = new LinkedHashMap<String, Dimension>();
    public final Map<String, Hierarchy> hierarchies = new LinkedHashMap<String, Hierarchy>();
    public final Map<String, Level> levels = new LinkedHashMap<String, Level>();
    public final Map<String, Source> sources = new LinkedHashMap<String, Source>();
    public final Map<String, Table> tables = new LinkedHashMap<String, Table>();
    public final Map<String, Column> columns = new LinkedHashMap<String, Column>();

    public static final class Column {
        public final String id;
        public String name;
        public String typeId;
        public String tableId;

        public Column(String id) {
            this.id = id;
        }
    }

    public static final class Table {
        public final String id;
        public String name;
        public final List<Column> columns = new ArrayList<Column>();

        public Table(String id) {
            this.id = id;
        }
    }

    public static final class JoinSide {
        public String queryId;
        public String keyColumnId;
    }

    public static final class JoinSource
    extends Source {
        public JoinSide left;
        public JoinSide right;

        public JoinSource(String id) {
            super(id);
        }
    }

    public static final class TableSource
    extends Source {
        public String tableId;

        public TableSource(String id) {
            super(id);
        }
    }

    public static abstract sealed class Source
    permits TableSource, JoinSource {
        public final String id;

        public Source(String id) {
            this.id = id;
        }
    }

    public static final class Level {
        public final String id;
        public String name;
        public String columnId;
        public String nameColumnId;
        public String captionColumnId;
        public String ordinalColumnId;
        public String parentColumnId;
        public boolean uniqueMembers;

        public Level(String id) {
            this.id = id;
        }

        public List<String> referencedColumnIds() {
            ArrayList<String> out = new ArrayList<String>();
            if (this.columnId != null) {
                out.add(this.columnId);
            }
            if (this.nameColumnId != null) {
                out.add(this.nameColumnId);
            }
            if (this.captionColumnId != null) {
                out.add(this.captionColumnId);
            }
            if (this.ordinalColumnId != null) {
                out.add(this.ordinalColumnId);
            }
            if (this.parentColumnId != null) {
                out.add(this.parentColumnId);
            }
            return out;
        }
    }

    public static final class Hierarchy {
        public final String id;
        public String name;
        public boolean parentChild;
        public String queryId;
        public String primaryKeyId;
        public final List<String> levelIds = new ArrayList<String>();

        public Hierarchy(String id) {
            this.id = id;
        }
    }

    public static final class Dimension {
        public final String id;
        public String name;
        public boolean time;
        public final List<String> hierarchyIds = new ArrayList<String>();

        public Dimension(String id) {
            this.id = id;
        }
    }

    public static final class Measure {
        public String id;
        public String name;
        public String aggregator;
        public String columnId;
    }

    public static final class DimensionConnector {
        public String id;
        public String overrideName;
        public String foreignKeyId;
        public String dimensionId;
    }

    public static final class Cube {
        public final String id;
        public String name;
        public boolean virtual;
        public String queryId;
        public final List<DimensionConnector> connectors = new ArrayList<DimensionConnector>();
        public final List<Measure> measures = new ArrayList<Measure>();

        public Cube(String id) {
            this.id = id;
        }
    }
}

