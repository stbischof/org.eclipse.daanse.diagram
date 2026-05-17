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
package org.eclipse.daanse.diagram.kind.cwm.objectmodel;

import java.util.function.Supplier;

import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.BehavioralFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.Operation;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.Parameter;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.behavioral.ParameterDirectionKind;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Attribute;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Class;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.DataType;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

class CwmObjectModelExampleTest {

    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final BehavioralFactory BF = BehavioralFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    @Test
    void singleton_class() throws Exception {
        Supplier<Diagram> mk = () -> {
            Package pkg = CF.createPackage(); pkg.setName("class-alone");
            DataType tString = dt(pkg, "String");
            Class c = CF.createClass(); c.setName("Customer");
            addAttr(c, "name", tString);
            addOp(c, "hello", null, null, null);
            pkg.getOwnedElement().add(c);
            return new CwmObjectModelConverter().convert(pkg);
        };
        M.writeSingleton("class", RenderMatrix.minimalAndFull(mk, mk));
    }

    @Test
    void singleton_class_abstract() throws Exception {
        Supplier<Diagram> mk = () -> {
            Package pkg = CF.createPackage(); pkg.setName("abstract-class-alone");
            Class p = CF.createClass(); p.setName("Party"); p.setIsAbstract(true);
            pkg.getOwnedElement().add(p);
            return new CwmObjectModelConverter().convert(pkg);
        };
        M.writeSingleton("class-abstract", RenderMatrix.minimalAndFull(mk, mk));
    }

    @Test
    void singleton_dataType() throws Exception {
        Supplier<Diagram> mk = () -> {
            Package pkg = CF.createPackage(); pkg.setName("datatype-alone");
            dt(pkg, "Money");
            return new CwmObjectModelConverter().convert(pkg);
        };
        M.writeSingleton("datatype", RenderMatrix.minimalAndFull(mk, mk));
    }

    private static DataType dt(Package pkg, String name) {
        DataType d = CF.createDataType(); d.setName(name);
        pkg.getOwnedElement().add(d);
        return d;
    }

    private static Attribute addAttr(Class c, String name, Classifier type) {
        Attribute a = CF.createAttribute(); a.setName(name); a.setType(type);
        c.getFeature().add(a);
        return a;
    }

    private static Operation addOp(Class c, String name, String paramName,
            Classifier type, ParameterDirectionKind kind) {
        Operation op = BF.createOperation(); op.setName(name);
        if (paramName != null) {
            Parameter p = BF.createParameter();
            p.setName(paramName); p.setType(type); p.setKind(kind);
            op.getParameter().add(p);
        }
        c.getFeature().add(op);
        return op;
    }
}
