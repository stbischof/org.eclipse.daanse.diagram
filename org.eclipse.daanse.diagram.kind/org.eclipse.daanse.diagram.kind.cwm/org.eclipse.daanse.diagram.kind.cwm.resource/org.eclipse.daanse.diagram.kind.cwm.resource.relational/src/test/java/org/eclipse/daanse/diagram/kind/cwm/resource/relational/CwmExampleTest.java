package org.eclipse.daanse.diagram.kind.cwm.resource.relational;

import java.util.List;
import org.eclipse.daanse.cwm.model.cwm.foundation.datatypes.DatatypesFactory;
import org.eclipse.daanse.cwm.model.cwm.foundation.datatypes.QueryExpression;
import org.eclipse.daanse.cwm.model.cwm.foundation.keysindexes.Index;
import org.eclipse.daanse.cwm.model.cwm.foundation.keysindexes.IndexedFeature;
import org.eclipse.daanse.cwm.model.cwm.foundation.keysindexes.KeysindexesFactory;
import org.eclipse.daanse.cwm.model.cwm.foundation.keysindexes.UniqueKey;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.BehavioralFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.Parameter;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.ParameterDirectionKind;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.BooleanExpression;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Class;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Expression;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Feature;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.StructuralFeature;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.CheckConstraint;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Column;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.ForeignKey;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.PrimaryKey;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Procedure;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.QueryColumnSet;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.RelationalFactory;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLSimpleType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Schema;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Table;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Trigger;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.UniqueConstraint;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.View;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.ActionOrientationType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.ConditionTimingType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.EventManipulationType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.NullableType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.ProcedureType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.eclipse.daanse.diagram.kind.cwm.resource.relational.DatabaseDiagram;

public final class CwmExampleTest {
    private static final RelationalFactory RF = RelationalFactory.eINSTANCE;
    private static final KeysindexesFactory KF = KeysindexesFactory.eINSTANCE;
    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final BehavioralFactory BF = BehavioralFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    /** Database diagram showcase: schemas, tables, views, primary/foreign keys
     *  (incl. composite FK), indexes, unique/check constraints, triggers,
     *  procedures, functions, and FK relationships across schemas.
     *
     *  <p>Variants: minimal (titled boxes only), full (every feature on),
     *  keys-and-relations (PK/FK columns + FK edges), columns-only (every
     *  column visible without auxiliary sections).</p>
     */
    @org.junit.jupiter.api.Test
    void diagram_database() throws Exception {
        Supplier<java.util.List<Schema>> schemas = () -> {
            Schema sales = buildSchema();
            Schema warehouse = buildWarehouseSchema(sales);
            // Add procedures + a function to the sales schema so this diagram
            // also exercises stored code.
            SQLSimpleType tBigint = simple("BIGINT");
            SQLSimpleType tVarchar = varchar(100);
            SQLSimpleType tDecimal = simple("DECIMAL");
            tDecimal.setNumericPrecision(12L); tDecimal.setNumericScale(2L);
            Procedure place = RF.createProcedure();
            place.setName("place_order");
            place.setType(ProcedureType.PROCEDURE);
            addParam(place, "in_customer_id", tBigint, ParameterDirectionKind.PDK_IN);
            // Default value showcase — gets a "= 'web'" row underneath.
            Parameter inChannel = addParam(place, "in_channel",
                    tVarchar, ParameterDirectionKind.PDK_IN);
            org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Expression chDef =
                    CF.createExpression(); chDef.setBody("'web'");
            inChannel.setDefaultValue(chDef);
            addParam(place, "in_email",       tVarchar, ParameterDirectionKind.PDK_IN);
            addParam(place, "out_order_id",   tBigint, ParameterDirectionKind.PDK_OUT);
            sales.getOwnedElement().add(place);
            Procedure ltv = RF.createProcedure();
            ltv.setName("customer_lifetime_value");
            ltv.setType(ProcedureType.FUNCTION);
            addParam(ltv, "customer_id", tBigint, ParameterDirectionKind.PDK_IN);
            addParam(ltv, "value",       tDecimal, ParameterDirectionKind.PDK_RETURN);
            sales.getOwnedElement().add(ltv);
            return List.of(sales, warehouse);
        };

        Map<String, Supplier<Diagram>> variants = new LinkedHashMap<>();
        variants.put("minimal",
                () -> new DatabaseDiagram(DatabaseDiagram.Features.minimal(), schemas.get()).toDiagram());
        variants.put("full",
                () -> new DatabaseDiagram(DatabaseDiagram.Features.full(), schemas.get()).toDiagram());
        variants.put("keys-and-relations",
                () -> new DatabaseDiagram(
                        DatabaseDiagram.Features.minimal()
                                .withColumns(true).withOnlyKeyColumns(true)
                                .withPkIcons(true).withFkIcons(true)
                                .withFkEdges(true).withCardinalities(true),
                        schemas.get()).toDiagram());
        variants.put("columns-only",
                () -> new DatabaseDiagram(
                        DatabaseDiagram.Features.minimal()
                                .withColumns(true).withColumnTypes(true)
                                .withPkIcons(true).withFkIcons(true)
                                .withNullableMarker(true).withDefaultMarker(true),
                        schemas.get()).toDiagram());
        M.writeDiagram("database", variants);
    }

    /** Query-diagram showcase: resolve a multi-join query against the
     *  sales+warehouse schemas, render the slice of the model the query
     *  actually touches. Demonstrates the integration with
     *  {@code org.eclipse.daanse.cwm.query.resolve}. */
    @org.junit.jupiter.api.Test
    void diagram_query() throws Exception {
        // A realistic 3-table join + a WHERE clause — exercises tables-used,
        // columns-used, and per-clause classification.
        String sql = "SELECT c.name, c.email, p.sku, p.name AS product_name, "
                + "ol.qty, ol.product_id "
                + "FROM customer c "
                + "JOIN order_version o ON c.id = o.customer_id "
                + "JOIN order_line ol ON o.id = ol.order_id AND o.version = ol.order_version "
                + "JOIN product p ON ol.product_id = p.id "
                + "WHERE p.active = TRUE AND c.email LIKE '%@example.org'";

        java.util.function.Supplier<TargetAndSchemas<QueryColumnSet>> ctx = () -> {
            Schema sales = buildSchema();
            Schema warehouse = buildWarehouseSchema(sales);
            QueryColumnSet qcs = buildCustomerOrderSummary(sales, sql);
            return new TargetAndSchemas<>(qcs, List.of(sales, warehouse));
        };

        Map<String, Supplier<Diagram>> variants = new LinkedHashMap<>();
        variants.put("minimal", () -> {
            TargetAndSchemas<QueryColumnSet> c = ctx.get();
            return new org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram(
                    org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram.Features.minimal(),
                    c.schemas, c.target).toDiagram();
        });
        variants.put("full", () -> {
            TargetAndSchemas<QueryColumnSet> c = ctx.get();
            return new org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram(
                    org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram.Features.full(),
                    c.schemas, c.target).toDiagram();
        });
        variants.put("with-context", () -> {
            TargetAndSchemas<QueryColumnSet> c = ctx.get();
            return new org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram(
                    org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram.Features
                            .full().withOnlyUsedTables(false),
                    c.schemas, c.target).toDiagram();
        });
        M.writeDiagram("query", variants);
    }

    /** Same lineage flow, applied to a {@link View}. Views and
     *  QueryColumnSets both carry a {@code QueryExpression}, so QueryDiagram
     *  treats them interchangeably as resolution targets. */
    @org.junit.jupiter.api.Test
    void diagram_view() throws Exception {
        // A 2-table join with aggregation — typical of an analytical view.
        String sql = "SELECT c.id, c.email, c.name, "
                + "COUNT(o.id) AS order_count, SUM(o.total) AS lifetime_total "
                + "FROM customer c "
                + "JOIN order_version o ON c.id = o.customer_id "
                + "GROUP BY c.id, c.email, c.name";

        java.util.function.Supplier<TargetAndSchemas<View>> ctx = () -> {
            Schema sales = buildSchema();
            Schema warehouse = buildWarehouseSchema(sales);
            View view = buildCustomerLifetimeView(sales, sql);
            return new TargetAndSchemas<>(view, List.of(sales, warehouse));
        };

        Map<String, Supplier<Diagram>> variants = new LinkedHashMap<>();
        variants.put("minimal", () -> {
            TargetAndSchemas<View> c = ctx.get();
            return new org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram(
                    org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram.Features.minimal(),
                    c.schemas, c.target).toDiagram();
        });
        variants.put("full", () -> {
            TargetAndSchemas<View> c = ctx.get();
            return new org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram(
                    org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram.Features.full(),
                    c.schemas, c.target).toDiagram();
        });
        variants.put("with-context", () -> {
            TargetAndSchemas<View> c = ctx.get();
            return new org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram(
                    org.eclipse.daanse.diagram.kind.cwm.resource.relational.QueryDiagram.Features
                            .full().withOnlyUsedTables(false),
                    c.schemas, c.target).toDiagram();
        });
        M.writeDiagram("view", variants);
    }

    private record TargetAndSchemas<T>(T target, List<Schema> schemas) {}

    private static QueryColumnSet buildCustomerOrderSummary(Schema sales, String sql) {
        SQLSimpleType tBigint = simple("BIGINT");
        SQLSimpleType tInt = simple("INT");
        SQLSimpleType tVarchar100 = varchar(100);
        SQLSimpleType tVarchar255 = varchar(255);
        SQLSimpleType tVarchar32 = varchar(32);
        QueryColumnSet qcs = RF.createQueryColumnSet();
        qcs.setName("customer_order_summary");
        qcs.getFeature().add(declColumn("name",         tVarchar100));
        qcs.getFeature().add(declColumn("email",        tVarchar255));
        qcs.getFeature().add(declColumn("sku",          tVarchar32));
        qcs.getFeature().add(declColumn("product_name", tVarchar100));
        qcs.getFeature().add(declColumn("qty",          tInt));
        qcs.getFeature().add(declColumn("product_id",   tBigint));
        QueryExpression qe = DatatypesFactory.eINSTANCE.createQueryExpression();
        qe.setLanguage("SQL");
        qe.setBody(sql);
        qcs.setQuery(qe);
        sales.getOwnedElement().add(qcs);
        return qcs;
    }

    private static View buildCustomerLifetimeView(Schema sales, String sql) {
        SQLSimpleType tBigint = simple("BIGINT");
        SQLSimpleType tInt = simple("INT");
        SQLSimpleType tDecimal = simple("DECIMAL");
        tDecimal.setNumericPrecision(12L); tDecimal.setNumericScale(2L);
        SQLSimpleType tVarchar100 = varchar(100);
        SQLSimpleType tVarchar255 = varchar(255);
        View view = RF.createView();
        view.setName("v_customer_lifetime");
        view.setIsReadOnly(true);
        view.getFeature().add(declColumn("id",             tBigint));
        view.getFeature().add(declColumn("email",          tVarchar255));
        view.getFeature().add(declColumn("name",           tVarchar100));
        view.getFeature().add(declColumn("order_count",    tInt));
        view.getFeature().add(declColumn("lifetime_total", tDecimal));
        QueryExpression qe = DatatypesFactory.eINSTANCE.createQueryExpression();
        qe.setLanguage("SQL");
        qe.setBody(sql);
        view.setQueryExpression(qe);
        sales.getOwnedElement().add(view);
        return view;
    }

    private static Column declColumn(String name, SQLSimpleType type) {
        Column c = RF.createColumn();
        c.setName(name);
        c.setType((Classifier) type);
        return c;
    }

    @org.junit.jupiter.api.Test
    void singleton_procedure() throws Exception {
        Schema schema = RF.createSchema();
        schema.setName("ops");
        SQLSimpleType tBigint = simple("BIGINT");
        SQLSimpleType tVarchar = varchar(100);
        Procedure p = RF.createProcedure();
        p.setName("place_order");
        p.setType(ProcedureType.PROCEDURE);
        addParam(p, "in_customer_id", tBigint, ParameterDirectionKind.PDK_IN);
        addParam(p, "in_email",       tVarchar, ParameterDirectionKind.PDK_IN);
        addParam(p, "out_order_id",   tBigint, ParameterDirectionKind.PDK_OUT);
        addParam(p, "io_status",      tVarchar, ParameterDirectionKind.PDK_INOUT);
        schema.getOwnedElement().add(p);
        M.writeSingleton("procedure", RenderMatrix.minimalAndFull(
                () -> new DatabaseDiagram(DatabaseDiagram.Features.minimal(), schema).toDiagram(),
                () -> new DatabaseDiagram(DatabaseDiagram.Features.full(),    schema).toDiagram()));
    }

    @org.junit.jupiter.api.Test
    void singleton_function() throws Exception {
        Schema schema = RF.createSchema();
        schema.setName("ops");
        SQLSimpleType tBigint = simple("BIGINT");
        SQLSimpleType tDecimal = simple("DECIMAL");
        tDecimal.setNumericPrecision(12L); tDecimal.setNumericScale(2L);
        Procedure f = RF.createProcedure();
        f.setName("customer_lifetime_value");
        f.setType(ProcedureType.FUNCTION);
        addParam(f, "customer_id", tBigint,  ParameterDirectionKind.PDK_IN);
        addParam(f, "value",       tDecimal, ParameterDirectionKind.PDK_RETURN);
        schema.getOwnedElement().add(f);
        M.writeSingleton("function", RenderMatrix.minimalAndFull(
                () -> new DatabaseDiagram(DatabaseDiagram.Features.minimal(), schema).toDiagram(),
                () -> new DatabaseDiagram(DatabaseDiagram.Features.full(),    schema).toDiagram()));
    }

    private static Parameter addParam(Procedure proc, String name, SQLSimpleType type,
            ParameterDirectionKind kind) {
        Parameter p = BF.createParameter();
        p.setName(name); p.setType((Classifier) type); p.setKind(kind);
        proc.getParameter().add(p);
        return p;
    }

    public static Schema buildSchema() {
        Schema schema = RF.createSchema();
        schema.setName("sales");
        SQLSimpleType tBigint = CwmExampleTest.simple("BIGINT");
        SQLSimpleType tInt = CwmExampleTest.simple("INT");
        SQLSimpleType tBoolean = CwmExampleTest.simple("BOOLEAN");
        SQLSimpleType tVarchar32 = CwmExampleTest.varchar(32);
        SQLSimpleType tVarchar100 = CwmExampleTest.varchar(100);
        SQLSimpleType tVarchar255 = CwmExampleTest.varchar(255);
        SQLSimpleType tDecimal = CwmExampleTest.simple("DECIMAL");
        tDecimal.setNumericPrecision(12L);
        tDecimal.setNumericScale(2L);
        Table customer = CwmExampleTest.table(schema, "customer");
        Column cId = CwmExampleTest.column(customer, "id", tBigint, false);
        CwmExampleTest.column(customer, "email", tVarchar255, false);
        CwmExampleTest.column(customer, "name", tVarchar100, true);
        CwmExampleTest.primaryKey(customer, "pk_customer", cId);
        Index ixEmail = CwmExampleTest.uniqueIndex(schema, customer, "ix_customer_email");
        CwmExampleTest.addIndexedFeature(ixEmail, CwmExampleTest.getColumn(customer, "email"));
        Table orderV = CwmExampleTest.table(schema, "order_version");
        Column oId = CwmExampleTest.column(orderV, "id", tBigint, false);
        Column oVer = CwmExampleTest.column(orderV, "version", tInt, false);
        Column oCustId = CwmExampleTest.column(orderV, "customer_id", tBigint, false);
        Column oTotal = CwmExampleTest.column(orderV, "total", tDecimal, true);
        CwmExampleTest.setDefault(oTotal, "0");
        CwmExampleTest.primaryKey(orderV, "pk_order_version", oId, oVer);
        CwmExampleTest.foreignKey(orderV, "fk_order_customer", customer, oCustId);
        Index ixOrderCust = CwmExampleTest.index(schema, orderV, "ix_order_customer");
        CwmExampleTest.addIndexedFeature(ixOrderCust, oCustId);
        CwmExampleTest.trigger(schema, orderV, "trg_order_audit", ConditionTimingType.AFTER, EventManipulationType.UPDATE, ActionOrientationType.ROW);
        CwmExampleTest.trigger(schema, orderV, "trg_order_stmt_refresh", ConditionTimingType.AFTER, EventManipulationType.INSERT, ActionOrientationType.STATEMENT);
        Table product = CwmExampleTest.table(schema, "product");
        Column pId = CwmExampleTest.column(product, "id", tBigint, false);
        Column pSku = CwmExampleTest.column(product, "sku", tVarchar32, false);
        CwmExampleTest.column(product, "name", tVarchar100, true);
        Column pActive = CwmExampleTest.column(product, "active", tBoolean, false);
        CwmExampleTest.setDefault(pActive, "TRUE");
        CwmExampleTest.primaryKey(product, "pk_product", pId);
        Index ixSku = CwmExampleTest.uniqueIndex(schema, product, "ix_product_sku");
        CwmExampleTest.addIndexedFeature(ixSku, pSku);
        CwmExampleTest.uniqueConstraint(product, "uq_product_sku", pSku);
        CwmExampleTest.checkConstraint(product, "chk_product_sku_length", "char_length(sku) > 0", pSku);
        CwmExampleTest.trigger(schema, product, "trg_product_audit", ConditionTimingType.AFTER, EventManipulationType.UPDATE, ActionOrientationType.ROW);
        CwmExampleTest.trigger(schema, product, "trg_product_reindex", ConditionTimingType.AFTER, EventManipulationType.INSERT, ActionOrientationType.STATEMENT);
        Table line = CwmExampleTest.table(schema, "order_line");
        Column lOrderId = CwmExampleTest.column(line, "order_id", tBigint, false);
        Column lOrderVer = CwmExampleTest.column(line, "order_version", tInt, false);
        Column lLineNo = CwmExampleTest.column(line, "line_no", tInt, false);
        Column lProduct = CwmExampleTest.column(line, "product_id", tBigint, false);
        Column lQty = CwmExampleTest.column(line, "qty", tInt, false);
        CwmExampleTest.setDefault(lQty, "1");
        CwmExampleTest.primaryKey(line, "pk_order_line", lOrderId, lOrderVer, lLineNo);
        CwmExampleTest.foreignKey(line, "fk_line_product", product, lProduct);
        CwmExampleTest.foreignKey(line, "fk_line_order_composite", orderV, lOrderId, lOrderVer);
        Index ixLineProduct = CwmExampleTest.index(schema, line, "ix_order_line_product");
        CwmExampleTest.addIndexedFeature(ixLineProduct, lProduct);
        Index ixLineOrder = CwmExampleTest.index(schema, line, "ix_order_line_order");
        CwmExampleTest.addIndexedFeature(ixLineOrder, lOrderId);
        CwmExampleTest.addIndexedFeature(ixLineOrder, lOrderVer);
        View customerSummary = RF.createView();
        customerSummary.setName("v_customer_summary");
        customerSummary.setIsReadOnly(true);
        CwmExampleTest.column(customerSummary, "id", tBigint, false);
        CwmExampleTest.column(customerSummary, "email", tVarchar255, true);
        CwmExampleTest.column(customerSummary, "order_count", tInt, true);
        schema.getOwnedElement().add(customerSummary);
        return schema;
    }

    private static void column(View view, String name, SQLSimpleType type, boolean nullable) {
        Column c = RF.createColumn();
        c.setName(name);
        c.setType((Classifier)type);
        c.setIsNullable(nullable ? NullableType.COLUMN_NULLABLE : NullableType.COLUMN_NO_NULLS);
        view.getFeature().add(c);
    }

    private static SQLSimpleType simple(String name) {
        SQLSimpleType t = RF.createSQLSimpleType();
        t.setName(name);
        return t;
    }

    private static SQLSimpleType varchar(int length) {
        SQLSimpleType t = CwmExampleTest.simple("VARCHAR");
        t.setCharacterMaximumLength((long)length);
        return t;
    }

    private static Table table(Schema schema, String name) {
        Table t = RF.createTable();
        t.setName(name);
        schema.getOwnedElement().add(t);
        return t;
    }

    private static Column column(Table table, String name, SQLSimpleType type, boolean nullable) {
        Column c = RF.createColumn();
        c.setName(name);
        c.setType((Classifier)type);
        c.setIsNullable(nullable ? NullableType.COLUMN_NULLABLE : NullableType.COLUMN_NO_NULLS);
        table.getFeature().add(c);
        return c;
    }

    private static Column getColumn(Table t, String name) {
        for (Feature f : t.getFeature()) {
            Column c;
            if (!(f instanceof Column) || !name.equals((c = (Column)f).getName())) continue;
            return c;
        }
        throw new IllegalArgumentException("no column " + name);
    }

    private static PrimaryKey primaryKey(Table table, String name, Column ... cols) {
        PrimaryKey pk = RF.createPrimaryKey();
        pk.setName(name);
        for (Column c : cols) {
            pk.getFeature().add(c);
        }
        table.getOwnedElement().add(pk);
        return pk;
    }

    private static ForeignKey foreignKey(Table from, String name, Table to, Column ... cols) {
        ForeignKey fk = RF.createForeignKey();
        fk.setName(name);
        for (Column c : cols) {
            fk.getFeature().add(c);
        }
        for (ModelElement owned : to.getOwnedElement()) {
            if (!(owned instanceof PrimaryKey)) continue;
            PrimaryKey pk = (PrimaryKey)owned;
            fk.setUniqueKey((UniqueKey)pk);
            break;
        }
        from.getOwnedElement().add(fk);
        return fk;
    }

    private static Index index(Schema schema, Table table, String name) {
        Index idx = KF.createIndex();
        idx.setName(name);
        idx.setSpannedClass((Class)table);
        schema.getOwnedElement().add(idx);
        return idx;
    }

    private static Index uniqueIndex(Schema schema, Table table, String name) {
        Index idx = CwmExampleTest.index(schema, table, name);
        idx.setIsUnique(true);
        return idx;
    }

    private static void addIndexedFeature(Index idx, Column column) {
        IndexedFeature f = KF.createIndexedFeature();
        f.setFeature((StructuralFeature)column);
        idx.getIndexedFeature().add(f);
    }

    private static Trigger trigger(Schema schema, Table table, String name, ConditionTimingType timing, EventManipulationType event, ActionOrientationType orientation) {
        Trigger t = RF.createTrigger();
        t.setName(name);
        t.setConditionTiming(timing);
        t.setEventManipulation(event);
        t.setActionOrientation(orientation);
        t.setTable(table);
        schema.getOwnedElement().add(t);
        return t;
    }

    private static void setDefault(Column c, String body) {
        Expression e = CF.createExpression();
        e.setBody(body);
        c.setInitialValue(e);
    }

    public static Schema buildWarehouseSchema(Schema sales) {
        Schema warehouse = RF.createSchema();
        warehouse.setName("warehouse");
        SQLSimpleType tBigint = CwmExampleTest.simple("BIGINT");
        SQLSimpleType tInt = CwmExampleTest.simple("INT");
        SQLSimpleType tTimestamp = CwmExampleTest.simple("TIMESTAMP");
        Table inventory = CwmExampleTest.table(warehouse, "inventory");
        Column iProduct = CwmExampleTest.column(inventory, "product_id", tBigint, false);
        Column iWarehouse = CwmExampleTest.column(inventory, "warehouse_id", tBigint, false);
        Column iQty = CwmExampleTest.column(inventory, "qty_on_hand", tInt, false);
        CwmExampleTest.setDefault(iQty, "0");
        CwmExampleTest.column(inventory, "updated_at", tTimestamp, false);
        CwmExampleTest.primaryKey(inventory, "pk_inventory", iProduct, iWarehouse);
        Table product = CwmExampleTest.findTable(sales, "product");
        ForeignKey fk = RF.createForeignKey();
        fk.setName("fk_inventory_product");
        fk.getFeature().add(iProduct);
        for (ModelElement owned : product.getOwnedElement()) {
            if (!(owned instanceof PrimaryKey)) continue;
            PrimaryKey pk = (PrimaryKey)owned;
            fk.setUniqueKey((UniqueKey)pk);
            break;
        }
        inventory.getOwnedElement().add(fk);
        Index ix = CwmExampleTest.index(warehouse, inventory, "ix_inventory_warehouse");
        CwmExampleTest.addIndexedFeature(ix, iWarehouse);

        // A system catalog table — exercises the SYS pill rendering.
        Table pgStat = CwmExampleTest.table(warehouse, "pg_stat_inventory");
        pgStat.setIsSystem(true);
        CwmExampleTest.column(pgStat, "relid", tBigint, false);
        CwmExampleTest.column(pgStat, "n_tup_upd", tInt, true);
        CwmExampleTest.column(pgStat, "last_analyze", tTimestamp, true);

        return warehouse;
    }

    private static Table findTable(Schema schema, String name) {
        for (ModelElement me : schema.getOwnedElement()) {
            Table t;
            if (!(me instanceof Table) || !name.equals((t = (Table)me).getName())) continue;
            return t;
        }
        throw new IllegalArgumentException("no table " + name + " in schema " + schema.getName());
    }

    private static UniqueConstraint uniqueConstraint(Table table, String name, Column ... cols) {
        UniqueConstraint u = RF.createUniqueConstraint();
        u.setName(name);
        for (Column c : cols) {
            u.getFeature().add(c);
        }
        table.getOwnedElement().add(u);
        return u;
    }

    private static CheckConstraint checkConstraint(Table table, String name, String body, Column ... cols) {
        CheckConstraint cc = RF.createCheckConstraint();
        cc.setName(name);
        BooleanExpression be = CF.createBooleanExpression();
        be.setBody(body);
        cc.setBody(be);
        for (Column c : cols) {
            cc.getConstrainedElement().add(c);
        }
        table.getOwnedElement().add(cc);
        return cc;
    }
}

