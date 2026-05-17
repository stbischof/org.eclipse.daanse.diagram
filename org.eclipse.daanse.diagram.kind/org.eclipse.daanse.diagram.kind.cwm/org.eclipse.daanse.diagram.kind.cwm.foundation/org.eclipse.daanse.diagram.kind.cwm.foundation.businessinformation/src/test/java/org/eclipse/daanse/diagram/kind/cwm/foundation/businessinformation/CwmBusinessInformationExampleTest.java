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
package org.eclipse.daanse.diagram.kind.cwm.foundation.businessinformation;

import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.BusinessinformationFactory;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Contact;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Description;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Document;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Email;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Location;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.ResponsibleParty;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Telephone;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

class CwmBusinessInformationExampleTest {

    private static final BusinessinformationFactory BIF = BusinessinformationFactory.eINSTANCE;
    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    @Test
    void singleton_responsibleParty() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("rp-alone");
        ResponsibleParty rp = BIF.createResponsibleParty();
        rp.setName("Sales Operations");
        rp.setResponsibility("Catalog owner");
        pkg.getOwnedElement().add(rp);
        single("responsible-party", pkg);
    }

    @Test
    void singleton_contact() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("contact-alone");
        Contact c = BIF.createContact(); c.setName("Jane Doe");
        pkg.getOwnedElement().add(c);
        single("contact", pkg);
    }

    @Test
    void singleton_email() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("email-alone");
        Email e = BIF.createEmail(); e.setName("primary mail");
        e.setEmailAddress("jane.doe@example.org");
        e.setEmailType("work");
        pkg.getOwnedElement().add(e);
        single("email", pkg);
    }

    @Test
    void singleton_telephone() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("phone-alone");
        Telephone t = BIF.createTelephone(); t.setName("office line");
        t.setPhoneNumber("+49 30 1234567");
        t.setPhoneType("voice");
        pkg.getOwnedElement().add(t);
        single("telephone", pkg);
    }

    @Test
    void singleton_location() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("loc-alone");
        Location l = BIF.createLocation(); l.setName("HQ");
        l.setAddress("Friedrichstraße 1");
        l.setPostCode("10117"); l.setCity("Berlin"); l.setCountry("DE");
        pkg.getOwnedElement().add(l);
        single("location", pkg);
    }

    @Test
    void singleton_description() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("desc-alone");
        Description desc = BIF.createDescription();
        desc.setName("Catalog purpose");
        desc.setLanguage("en"); desc.setType("text/plain");
        desc.setBody("Operational sales analytics for the EMEA region.");
        pkg.getOwnedElement().add(desc);
        single("description", pkg);
    }

    @Test
    void singleton_document() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("doc-alone");
        Document doc = BIF.createDocument();
        doc.setName("Operations runbook");
        doc.setType("application/pdf");
        doc.setReference("https://wiki.example.org/runbooks/sales");
        pkg.getOwnedElement().add(doc);
        single("document", pkg);
    }

    @Test
    void diagram_business_contacts() throws Exception {
        Package pkg = CF.createPackage();
        pkg.setName("orgInfo");

        ResponsibleParty owner = BIF.createResponsibleParty();
        owner.setName("Sales Operations");
        owner.setResponsibility("Owner of the Sales catalog");
        pkg.getOwnedElement().add(owner);

        Contact c = BIF.createContact();
        c.setName("Jane Doe");
        pkg.getOwnedElement().add(c);

        Email e = BIF.createEmail();
        e.setName("primary mail");
        e.setEmailAddress("jane.doe@example.org");
        e.setEmailType("work");
        pkg.getOwnedElement().add(e);
        c.getEmail().add(e);

        Telephone t = BIF.createTelephone();
        t.setName("office line");
        t.setPhoneNumber("+49 30 1234567");
        t.setPhoneType("voice");
        pkg.getOwnedElement().add(t);
        c.getTelephone().add(t);

        Location l = BIF.createLocation();
        l.setName("HQ");
        l.setAddress("Friedrichstraße 1");
        l.setPostCode("10117");
        l.setCity("Berlin");
        l.setCountry("DE");
        pkg.getOwnedElement().add(l);
        c.getLocation().add(l);

        Description desc = BIF.createDescription();
        desc.setName("Catalog purpose");
        desc.setLanguage("en");
        desc.setType("text/plain");
        desc.setBody("Operational sales analytics for the EMEA region.");
        pkg.getOwnedElement().add(desc);

        Document doc = BIF.createDocument();
        doc.setName("Operations runbook");
        doc.setType("application/pdf");
        doc.setReference("https://wiki.example.org/runbooks/sales");
        pkg.getOwnedElement().add(doc);

        owner.getModelElement().add(c);
        owner.getModelElement().add(desc);
        owner.getModelElement().add(doc);

        M.writeDiagram("business-contacts", RenderMatrix.minimalAndFull(
                () -> new BusinessContactsDiagram(BusinessContactsDiagram.Features.minimal(), pkg).toDiagram(),
                () -> new BusinessContactsDiagram(BusinessContactsDiagram.Features.full(),    pkg).toDiagram()));
    }

    private static void single(String name, Package pkg) throws Exception {
        java.util.function.Supplier<org.eclipse.daanse.diagram.core.Diagram> mk =
                () -> new CwmBusinessInformationConverter().convert(pkg);
        M.writeSingleton(name, RenderMatrix.minimalAndFull(mk, mk));
    }
}
