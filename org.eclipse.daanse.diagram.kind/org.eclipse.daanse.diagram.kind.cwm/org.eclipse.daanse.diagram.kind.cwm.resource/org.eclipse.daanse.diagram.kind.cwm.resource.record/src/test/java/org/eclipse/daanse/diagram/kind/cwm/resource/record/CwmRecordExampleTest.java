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
package org.eclipse.daanse.diagram.kind.cwm.resource.record;

import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.DataType;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.cwm.model.cwm.resource.record.Field;
import org.eclipse.daanse.cwm.model.cwm.resource.record.FixedOffsetField;
import org.eclipse.daanse.cwm.model.cwm.resource.record.Group;
import org.eclipse.daanse.cwm.model.cwm.resource.record.RecordDef;
import org.eclipse.daanse.cwm.model.cwm.resource.record.RecordFactory;
import org.eclipse.daanse.cwm.model.cwm.resource.record.RecordFile;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

class CwmRecordExampleTest {

    private static final RecordFactory REC = RecordFactory.eINSTANCE;
    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    @Test
    void singleton_recordDef() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("rd-alone");
        DataType tString = dt(pkg, "STRING");
        DataType tInt    = dt(pkg, "INT");
        RecordDef rd = REC.createRecordDef();
        rd.setName("CustomerCsvRow");
        rd.setFieldDelimiter(",");
        rd.setTextDelimiter("\"");
        addField(rd, "id",    tInt,    8, 0, 0);
        addField(rd, "email", tString, 64, 0, 0);
        pkg.getOwnedElement().add(rd);
        single("record-def", pkg);
    }

    @Test
    void singleton_recordDef_fixedwidth() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("rd-fw-alone");
        DataType tString = dt(pkg, "STRING");
        DataType tInt    = dt(pkg, "INT");
        RecordDef rd = REC.createRecordDef();
        rd.setName("EbcdicFixedRow");
        rd.setIsFixedWidth(true);
        addFixedField(rd, "id",      tInt,    0, 8);
        addFixedField(rd, "name",    tString, 8, 32);
        addFixedField(rd, "balance", tInt,   40, 12);
        pkg.getOwnedElement().add(rd);
        single("record-def-fixed-width", pkg);
    }

    @Test
    void singleton_recordFile() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("rf-alone");
        RecordFile rf = REC.createRecordFile();
        rf.setName("customers.csv");
        rf.setIsSelfDescribing(false);
        rf.setRecordDelimiter(10);
        rf.setSkipRecords(1);
        pkg.getOwnedElement().add(rf);
        single("record-file", pkg);
    }

    @Test
    void singleton_group() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("group-alone");
        DataType tInt = dt(pkg, "INT");
        Group g = REC.createGroup();
        g.setName("Address");
        addField(g, "zip",    tInt, 5, 0, 0);
        addField(g, "houseNo", tInt, 4, 0, 0);
        pkg.getOwnedElement().add(g);
        single("record-group", pkg);
    }

    private static DataType dt(Package pkg, String name) {
        DataType d = CF.createDataType(); d.setName(name);
        pkg.getOwnedElement().add(d);
        return d;
    }

    private static Field addField(org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier owner,
            String name, DataType type, long length, long precision, long scale) {
        Field f = REC.createField();
        f.setName(name); f.setType(type);
        f.setLength(length); f.setPrecision(precision); f.setScale(scale);
        owner.getFeature().add(f);
        return f;
    }

    private static FixedOffsetField addFixedField(RecordDef rd, String name,
            DataType type, long offset, long length) {
        FixedOffsetField f = REC.createFixedOffsetField();
        f.setName(name); f.setType(type);
        f.setOffset(offset); f.setLength(length);
        rd.getFeature().add(f);
        return f;
    }

    private static void single(String name, Package pkg) throws Exception {
        java.util.function.Supplier<org.eclipse.daanse.diagram.core.Diagram> mk =
                () -> new CwmRecordConverter().convert(pkg);
        M.writeSingleton(name, RenderMatrix.minimalAndFull(mk, mk));
    }
}
