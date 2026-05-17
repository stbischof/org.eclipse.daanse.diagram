package org.eclipse.daanse.diagram.kind.rolap.access;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AccessModel {
    public final Map<String, Catalog> catalogs = new LinkedHashMap<String, Catalog>();
    public final Map<String, NamedObj> cubes = new LinkedHashMap<String, NamedObj>();
    public final Map<String, NamedObj> hierarchies = new LinkedHashMap<String, NamedObj>();
    public final Map<String, NamedObj> levels = new LinkedHashMap<String, NamedObj>();
    public final Map<String, NamedObj> schemas = new LinkedHashMap<String, NamedObj>();
    public final Map<String, AccessRole> roles = new LinkedHashMap<String, AccessRole>();

    public Catalog catalog(String id) {
        return this.catalogs.computeIfAbsent(id, Catalog::new);
    }

    public NamedObj namedObj(Map<String, NamedObj> bag, String id, String kind) {
        return bag.computeIfAbsent(id, k -> new NamedObj((String)k, kind));
    }

    public AccessRole role(String id) {
        return this.roles.computeIfAbsent(id, AccessRole::new);
    }

    public static final class Catalog
    extends NamedObj {
        public final List<String> cubeIds = new ArrayList<String>();
        public final List<String> roleIds = new ArrayList<String>();
        public final List<String> schemaIds = new ArrayList<String>();

        public Catalog(String id) {
            super(id, "CATALOG");
        }
    }

    public static class NamedObj {
        public final String id;
        public String name;
        public final String kind;

        public NamedObj(String id, String kind) {
            this.id = id;
            this.kind = kind;
        }

        public String displayName() {
            return this.name != null && !this.name.isEmpty() ? this.name : this.id;
        }
    }

    public static final class AccessRole
    extends NamedObj {
        public final List<CatalogGrant> catalogGrants = new ArrayList<CatalogGrant>();
        public final List<String> referencedRoles = new ArrayList<String>();

        public AccessRole(String id) {
            super(id, "ROLE");
        }
    }

    public static final class DbSchemaGrant {
        public String databaseSchemaAccess;
        public String schemaId;
    }

    public static final class MemberGrant {
        public String memberAccess;
        public String member;
    }

    public static final class HierarchyGrant {
        public String hierarchyAccess;
        public String hierarchyId;
        public String topLevelId;
        public String bottomLevelId;
        public final List<MemberGrant> memberGrants = new ArrayList<MemberGrant>();
    }

    public static final class CubeGrant {
        public String cubeAccess;
        public String cubeId;
        public final List<HierarchyGrant> hierarchyGrants = new ArrayList<HierarchyGrant>();
    }

    public static final class CatalogGrant {
        public String catalogAccess;
        public final List<CubeGrant> cubeGrants = new ArrayList<CubeGrant>();
        public final List<DbSchemaGrant> schemaGrants = new ArrayList<DbSchemaGrant>();
    }
}

