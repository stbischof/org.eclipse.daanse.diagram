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

import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Contact;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Description;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Document;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Email;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Location;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.ResponsibleParty;
import org.eclipse.daanse.cwm.model.cwm.foundation.businessinformation.Telephone;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;

/**
 * Renders CWM Foundation Business Information packages: ResponsibleParty
 * boxes on the left, the contacts they own (Email/Telephone/Location),
 * Description and Document objects, and ownership edges between them.
 */
public final class CwmBusinessInformationConverter {

    private static final String ID_NODE = "bi";
    private static final String ID_EDGE = "be";

    private static final Stereotype RESPONSIBLE_S = new Stereotype("RESPONSIBLE PARTY", Palette.BLUE_900,   Icons.PERSON);
    private static final Stereotype CONTACT_S     = new Stereotype("CONTACT",           Palette.INDIGO_900, Icons.CONTACT_CARD);
    private static final Stereotype EMAIL_S       = new Stereotype("EMAIL",             Palette.CYAN_700,   Icons.EMAIL);
    private static final Stereotype PHONE_S       = new Stereotype("TELEPHONE",         Palette.CYAN_700,   Icons.PHONE);
    private static final Stereotype LOCATION_S    = new Stereotype("LOCATION",          Palette.CYAN_700,   Icons.LOCATION);
    private static final Stereotype DESCRIPTION_S = new Stereotype("DESCRIPTION",       Palette.ORANGE_900, Icons.DESCRIPTION);
    private static final Stereotype DOCUMENT_S    = new Stereotype("DOCUMENT",          Palette.GRAY_700,   Icons.DOCUMENT);

    public Diagram convert(Package pkg) {
        DiagramBuilder b = DiagramBuilder.of(Names.n(pkg.getName()));
        for (ModelElement me : pkg.getOwnedElement()) renderElement(b, me);
        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof ResponsibleParty rp) {
                for (ModelElement target : rp.getModelElement()) {
                    if (b.has(target)) b.edge(rp, target, ID_EDGE).label("responsible for");
                }
            }
            if (me instanceof Contact c) {
                for (Email e : c.getEmail())     if (b.has(e)) b.edge(c, e, ID_EDGE).label("email");
                for (Telephone t : c.getTelephone()) if (b.has(t)) b.edge(c, t, ID_EDGE).label("tel");
                for (Location l : c.getLocation())   if (b.has(l)) b.edge(c, l, ID_EDGE).label("loc");
            }
        }
        return b.diagram();
    }

    private void renderElement(DiagramBuilder b, ModelElement me) {
        if (me instanceof ResponsibleParty rp) {
            LabelledBoxBody body = b.labelled(rp, RESPONSIBLE_S, Names.n(rp.getName()), ID_NODE);
            if (rp.getResponsibility() != null) body.addRow("role: " + rp.getResponsibility());
            return;
        }
        if (me instanceof Contact c) {
            b.labelled(c, CONTACT_S, Names.n(c.getName()), ID_NODE);
            return;
        }
        if (me instanceof Email em) {
            LabelledBoxBody body = b.labelled(em, EMAIL_S, Names.n(em.getName()), ID_NODE);
            if (em.getEmailAddress() != null) body.addRow(em.getEmailAddress());
            if (em.getEmailType() != null) body.addRow("type: " + em.getEmailType());
            return;
        }
        if (me instanceof Telephone t) {
            LabelledBoxBody body = b.labelled(t, PHONE_S, Names.n(t.getName()), ID_NODE);
            if (t.getPhoneNumber() != null) body.addRow(t.getPhoneNumber());
            if (t.getPhoneType() != null) body.addRow("type: " + t.getPhoneType());
            return;
        }
        if (me instanceof Location l) {
            LabelledBoxBody body = b.labelled(l, LOCATION_S, Names.n(l.getName()), ID_NODE);
            if (l.getAddress() != null) body.addRow(l.getAddress());
            String cityLine = "";
            if (l.getPostCode() != null) cityLine += l.getPostCode() + " ";
            if (l.getCity() != null) cityLine += l.getCity();
            if (!cityLine.isBlank()) body.addRow(cityLine);
            if (l.getCountry() != null) body.addRow(l.getCountry());
            return;
        }
        if (me instanceof Description ds) {
            LabelledBoxBody body = b.labelled(ds, DESCRIPTION_S, Names.n(ds.getName()), ID_NODE);
            if (ds.getType() != null) body.addRow("type: " + ds.getType());
            if (ds.getLanguage() != null) body.addRow("lang: " + ds.getLanguage());
            if (ds.getBody() != null) body.addRow(Names.snippet(ds.getBody(), 50));
            return;
        }
        if (me instanceof Document doc) {
            LabelledBoxBody body = b.labelled(doc, DOCUMENT_S, Names.n(doc.getName()), ID_NODE);
            if (doc.getReference() != null) body.addRow(doc.getReference());
            if (doc.getType() != null) body.addRow("type: " + doc.getType());
        }
    }
}
