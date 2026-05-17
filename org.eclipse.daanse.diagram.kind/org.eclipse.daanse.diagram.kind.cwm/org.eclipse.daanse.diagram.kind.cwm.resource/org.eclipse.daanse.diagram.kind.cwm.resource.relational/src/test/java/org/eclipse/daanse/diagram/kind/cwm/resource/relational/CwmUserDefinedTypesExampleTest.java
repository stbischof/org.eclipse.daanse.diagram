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
package org.eclipse.daanse.diagram.kind.cwm.resource.relational;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Column;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.RelationalFactory;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLDistinctType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.SQLSimpleType;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Schema;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.Table;
import org.eclipse.daanse.cwm.model.cwm.resource.relational.enumerations.NullableType;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

/**
 * "type-usage" diagram showcase. The "payments" model has:
 * <ul>
 *   <li>two distinct types — USD (DECIMAL(12,2)) and Email (VARCHAR(255))</li>
 *   <li>three tables (invoice / refund / customer) whose non-PK columns
 *       reference one of those types</li>
 *   <li>three simple types (BIGINT / DECIMAL / VARCHAR) materialised as
 *       nodes when the corresponding feature is on, with inheritance
 *       arrows from each distinct type to its base simple type</li>
 * </ul>
 */
class CwmUserDefinedTypesExampleTest {

    private static final RelationalFactory RF = RelationalFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    @Test
    void diagram_type_usage() throws Exception {
        Supplier<List<Schema>> schemas = () -> List.of(buildPaymentsSchema());
        Map<String, Supplier<Diagram>> variants = new LinkedHashMap<>();
        variants.put("minimal",
                () -> new TypeUsageDiagram(TypeUsageDiagram.Features.minimal(), schemas.get()).toDiagram());
        variants.put("full",
                () -> new TypeUsageDiagram(TypeUsageDiagram.Features.full(), schemas.get()).toDiagram());
        variants.put("distinct-only",
                () -> new TypeUsageDiagram(
                        TypeUsageDiagram.Features.full().withSimpleTypeNodes(false)
                                .withDistinctTypeInheritanceEdges(false),
                        schemas.get()).toDiagram());
        M.writeDiagram("type-usage", variants);
    }

    @Test
    void singleton_distinct_type() throws Exception {
        Supplier<Schema> mk = () -> {
            Schema schema = RF.createSchema(); schema.setName("types-alone");
            SQLSimpleType decimal = simple("DECIMAL");
            decimal.setNumericPrecision(12L); decimal.setNumericScale(2L);
            SQLDistinctType usd = RF.createSQLDistinctType();
            usd.setName("USD"); usd.setSqlSimpleType(decimal);
            usd.setPrecision(12L); usd.setScale(2L);
            schema.getOwnedElement().add(usd);
            return schema;
        };
        M.writeSingleton("distinct-type", RenderMatrix.minimalAndFull(
                () -> new DatabaseDiagram(DatabaseDiagram.Features.minimal(), mk.get()).toDiagram(),
                () -> new DatabaseDiagram(DatabaseDiagram.Features.full(),    mk.get()).toDiagram()));
    }

    private static Schema buildPaymentsSchema() {
        Schema schema = RF.createSchema();
        schema.setName("payments");

        SQLSimpleType decimal = simple("DECIMAL");
        decimal.setNumericPrecision(12L); decimal.setNumericScale(2L);
        SQLSimpleType varchar = simple("VARCHAR");
        SQLSimpleType bigint = simple("BIGINT");

        SQLDistinctType usd = RF.createSQLDistinctType();
        usd.setName("USD");
        usd.setSqlSimpleType(decimal);
        usd.setPrecision(12L); usd.setScale(2L);
        schema.getOwnedElement().add(usd);

        SQLDistinctType email = RF.createSQLDistinctType();
        email.setName("Email");
        email.setSqlSimpleType(varchar);
        email.setLength(255L);
        schema.getOwnedElement().add(email);

        Table invoice = RF.createTable();
        invoice.setName("invoice");
        column(invoice, "id", bigint, false);
        column(invoice, "billed_to", email, true);
        column(invoice, "total", usd, false);
        column(invoice, "tax", usd, true);
        schema.getOwnedElement().add(invoice);

        Table refund = RF.createTable();
        refund.setName("refund");
        column(refund, "id", bigint, false);
        column(refund, "amount", usd, false);
        schema.getOwnedElement().add(refund);

        Table customer = RF.createTable();
        customer.setName("customer");
        column(customer, "id", bigint, false);
        column(customer, "primary_email", email, false);
        schema.getOwnedElement().add(customer);

        return schema;
    }

    private static SQLSimpleType simple(String name) {
        SQLSimpleType t = RF.createSQLSimpleType();
        t.setName(name);
        return t;
    }

    private static Column column(Table t, String name, Classifier type, boolean nullable) {
        Column c = RF.createColumn();
        c.setName(name);
        c.setType(type);
        c.setIsNullable(nullable ? NullableType.COLUMN_NULLABLE : NullableType.COLUMN_NO_NULLS);
        t.getFeature().add(c);
        return c;
    }
}
