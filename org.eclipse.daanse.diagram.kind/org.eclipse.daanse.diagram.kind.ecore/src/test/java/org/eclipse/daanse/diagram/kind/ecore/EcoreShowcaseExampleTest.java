/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*/
package org.eclipse.daanse.diagram.kind.ecore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.eclipse.daanse.diagram.core.layout.ElkLayoutEngine;
import org.eclipse.daanse.diagram.core.svg.SvgRenderer;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.Test;

/**
 * Hand-built Ecore showcase exercising the non-trivial bits of the
 * EClassBody renderer: derived attributes, derived containment references,
 * bidirectional references via {@code eOpposite}, and operations with
 * several parameters that the new renderer breaks out onto sub-rows under
 * each operation header. No edge-side cardinality pills — multiplicity is
 * inline on the structural-feature row.
 */
final class EcoreShowcaseExampleTest {

    private static final EcoreFactory F = EcoreFactoryImpl.eINSTANCE;
    private static final EcorePackage P = EcorePackage.eINSTANCE;

    @Test
    void diagram_showcase() throws Exception {
        EPackage pkg = F.createEPackage();
        pkg.setName("library");
        pkg.setNsPrefix("lib");
        pkg.setNsURI("https://example.org/library");

        // --- Datatypes --------------------------------------------------
        EDataType money = F.createEDataType();
        money.setName("Money");
        money.setInstanceClassName("java.math.BigDecimal");
        pkg.getEClassifiers().add(money);

        EDataType isbn = F.createEDataType();
        isbn.setName("ISBN");
        isbn.setInstanceClassName("java.lang.String");
        pkg.getEClassifiers().add(isbn);

        EDataType emailAddress = F.createEDataType();
        emailAddress.setName("EmailAddress");
        emailAddress.setInstanceClassName("java.lang.String");
        pkg.getEClassifiers().add(emailAddress);

        // --- Enumerations ----------------------------------------------

        pkg.getEClassifiers().add(makeEnum("Status",       "ACTIVE", "INACTIVE", "ARCHIVED"));
        pkg.getEClassifiers().add(makeEnum("Genre",        "FICTION", "NON_FICTION", "SCIENCE", "POETRY"));
        pkg.getEClassifiers().add(makeEnum("MemberLevel",  "BASIC", "PREMIUM", "VIP"));
        EEnum status = (EEnum) pkg.getEClassifier("Status");

        // --- Interface ---------------------------------------------------
        EClass loanable = F.createEClass();
        loanable.setName("Loanable");
        loanable.setInterface(true);
        // Interfaces in Ecore are also EClass instances, just with the
        // interface flag — they typically have operations, no attributes.
        EOperation lend = F.createEOperation();
        lend.setName("lend");
        addParam(lend, "to", P.getEString());
        addParam(lend, "until", P.getEDate());
        lend.setEType(P.getEBoolean());
        loanable.getEOperations().add(lend);
        EOperation isAvailable = F.createEOperation();
        isAvailable.setName("isAvailable");
        isAvailable.setEType(P.getEBoolean());
        loanable.getEOperations().add(isAvailable);
        pkg.getEClassifiers().add(loanable);

        // --- Address ----------------------------------------------------
        EClass address = F.createEClass();
        address.setName("Address");
        addAttr(address, "street", P.getEString(), 1, 1, false);
        addAttr(address, "city",   P.getEString(), 1, 1, false);
        addAttr(address, "zip",    P.getEString(), 0, 1, false);
        pkg.getEClassifiers().add(address);

        // --- Book -------------------------------------------------------
        EClass book = F.createEClass();
        book.setName("Book");
        addAttr(book, "title", P.getEString(), 1, 1, false);
        addAttr(book, "year",  P.getEInt(),    0, 1, false);
        addAttr(book, "price", money,          0, 1, false);
        book.getESuperTypes().add(loanable);
        pkg.getEClassifiers().add(book);

        // --- Person (abstract) ------------------------------------------
        EClass person = F.createEClass();
        person.setName("Person");
        person.setAbstract(true);
        EAttribute idAttr = addAttr(person, "id", P.getEInt(), 1, 1, false);
        idAttr.setID(true);
        addAttr(person, "firstName", P.getEString(), 1, 1, false);
        addAttr(person, "lastName",  P.getEString(), 1, 1, false);
        // Derived: computed from firstName + lastName
        EAttribute fullName = addAttr(person, "fullName", P.getEString(), 1, 1, true);
        EAttribute statusA = addAttr(person, "status", status, 0, 1, false);
        statusA.setDefaultValueLiteral("ACTIVE");

        // Containment ref: Person owns Addresses
        EReference addresses = F.createEReference();
        addresses.setName("addresses");
        addresses.setEType(address);
        addresses.setLowerBound(0);
        addresses.setUpperBound(-1);
        addresses.setContainment(true);
        person.getEStructuralFeatures().add(addresses);

        // Bidirectional ref pair: Person.books ↔ Book.owners
        EReference personBooks = F.createEReference();
        personBooks.setName("books");
        personBooks.setEType(book);
        personBooks.setLowerBound(0);
        personBooks.setUpperBound(-1);
        person.getEStructuralFeatures().add(personBooks);

        EReference bookOwners = F.createEReference();
        bookOwners.setName("owners");
        bookOwners.setEType(person);
        bookOwners.setLowerBound(1);
        bookOwners.setUpperBound(-1);
        book.getEStructuralFeatures().add(bookOwners);
        personBooks.setEOpposite(bookOwners);
        bookOwners.setEOpposite(personBooks);

        // Operations on Person — multiple params, mixed return types.
        EOperation greet = F.createEOperation();
        greet.setName("greet");
        greet.setEType(P.getEString());
        addParam(greet, "language", P.getEString());
        addParam(greet, "formal",   P.getEBoolean());
        person.getEOperations().add(greet);

        EOperation rename = F.createEOperation();
        rename.setName("rename");
        addParam(rename, "firstName", P.getEString());
        addParam(rename, "lastName",  P.getEString());
        person.getEOperations().add(rename);

        pkg.getEClassifiers().add(person);

        // --- Author extends Person --------------------------------------
        EClass author = F.createEClass();
        author.setName("Author");
        author.getESuperTypes().add(person);
        addAttr(author, "pseudonym", P.getEString(), 0, 1, false);

        EOperation publish = F.createEOperation();
        publish.setName("publish");
        publish.setEType(book);
        addParam(publish, "title",  P.getEString());
        addParam(publish, "year",   P.getEInt());
        addParam(publish, "price",  money);
        author.getEOperations().add(publish);

        pkg.getEClassifiers().add(author);

        // --- Library (root container, holds books and members) --------
        EClass library = F.createEClass();
        library.setName("Library");
        addAttr(library, "name",     P.getEString(), 1, 1, false);
        EAttribute libEmail = addAttr(library, "contact", emailAddress, 0, 1, false);
        libEmail.setDefaultValueLiteral("info@example.org");

        EReference libBooks = F.createEReference();
        libBooks.setName("books");
        libBooks.setEType(book);
        libBooks.setLowerBound(0);
        libBooks.setUpperBound(-1);
        libBooks.setContainment(true);
        library.getEStructuralFeatures().add(libBooks);

        // --- Member ----------------------------------------------------
        EClass member = F.createEClass();
        member.setName("Member");
        EAttribute mid = addAttr(member, "id", P.getEInt(), 1, 1, false);
        mid.setID(true);
        addAttr(member, "name",  P.getEString(),    1, 1, false);
        addAttr(member, "email", emailAddress,      1, 1, false);
        EEnum memberLevel = (EEnum) pkg.getEClassifier("MemberLevel");
        EAttribute level = addAttr(member, "level", memberLevel, 1, 1, false);
        level.setDefaultValueLiteral("BASIC");

        EOperation upgrade = F.createEOperation();
        upgrade.setName("upgrade");
        addParam(upgrade, "to", memberLevel);
        member.getEOperations().add(upgrade);

        // --- Loan ------------------------------------------------------
        EClass loan = F.createEClass();
        loan.setName("Loan");
        addAttr(loan, "since",  P.getEDate(),       1, 1, false);
        EAttribute due = addAttr(loan, "dueDate", P.getEDate(),  1, 1, false);
        addAttr(loan, "renewals", P.getEInt(),       0, 1, false)
                .setDefaultValueLiteral("0");
        EAttribute returned = addAttr(loan, "returned", P.getEBoolean(), 0, 1, false);
        returned.setDefaultValueLiteral("false");
        // Demonstrate the new flag columns: derived (computed), transient
        // (not persisted), volatile (no backing field).
        addAttr(loan, "overdue", P.getEBoolean(), 0, 1, true);  // derived
        EAttribute lastAccess = addAttr(loan, "lastAccess", P.getEDate(), 0, 1, false);
        lastAccess.setTransient(true);
        EAttribute liveBalance = addAttr(loan, "liveBalance", money, 0, 1, false);
        liveBalance.setVolatile(true);
        liveBalance.setTransient(true);
        liveBalance.setDerived(true);

        EReference loanBook = F.createEReference();
        loanBook.setName("book");
        loanBook.setEType(book);
        loanBook.setLowerBound(1);
        loanBook.setUpperBound(1);
        loan.getEStructuralFeatures().add(loanBook);

        EReference loanMember = F.createEReference();
        loanMember.setName("borrower");
        loanMember.setEType(member);
        loanMember.setLowerBound(1);
        loanMember.setUpperBound(1);
        loan.getEStructuralFeatures().add(loanMember);

        EReference libLoans = F.createEReference();
        libLoans.setName("loans");
        libLoans.setEType(loan);
        libLoans.setLowerBound(0);
        libLoans.setUpperBound(-1);
        libLoans.setContainment(true);
        library.getEStructuralFeatures().add(libLoans);

        EReference libMembers = F.createEReference();
        libMembers.setName("members");
        libMembers.setEType(member);
        libMembers.setLowerBound(0);
        libMembers.setUpperBound(-1);
        libMembers.setContainment(true);
        library.getEStructuralFeatures().add(libMembers);

        pkg.getEClassifiers().add(library);
        pkg.getEClassifiers().add(member);
        pkg.getEClassifiers().add(loan);

        // --- Sub-package "library.audit" with AuditEntry ---------------
        EPackage auditPkg = F.createEPackage();
        auditPkg.setName("audit");
        auditPkg.setNsPrefix("audit");
        auditPkg.setNsURI("https://example.org/library/audit");
        pkg.getESubpackages().add(auditPkg);

        EClass auditEntry = F.createEClass();
        auditEntry.setName("AuditEntry");
        addAttr(auditEntry, "occurredAt", P.getEDate(),   1, 1, false);
        addAttr(auditEntry, "actor",       P.getEString(), 1, 1, false);
        addAttr(auditEntry, "action",      P.getEString(), 1, 1, false);
        auditPkg.getEClassifiers().add(auditEntry);

        // Loan → AuditEntry: cross-package reference (into a sub-package).
        EReference loanAudit = F.createEReference();
        loanAudit.setName("audit");
        loanAudit.setEType(auditEntry);
        loanAudit.setLowerBound(0);
        loanAudit.setUpperBound(-1);
        loan.getEStructuralFeatures().add(loanAudit);

        // --- Sibling package "payment" --------------------------------
        EPackage paymentPkg = F.createEPackage();
        paymentPkg.setName("payment");
        paymentPkg.setNsPrefix("pmt");
        paymentPkg.setNsURI("https://example.org/payment");

        EClass payment = F.createEClass();
        payment.setName("Payment");
        addAttr(payment, "amount",   money,           1, 1, false);
        addAttr(payment, "currency", P.getEString(),  1, 1, false)
                .setDefaultValueLiteral("EUR");
        addAttr(payment, "settled",  P.getEBoolean(), 0, 1, false)
                .setDefaultValueLiteral("false");
        paymentPkg.getEClassifiers().add(payment);

        // Loan → Payment: cross-package reference (into a sibling).
        EReference loanPayment = F.createEReference();
        loanPayment.setName("payments");
        loanPayment.setEType(payment);
        loanPayment.setLowerBound(0);
        loanPayment.setUpperBound(-1);
        loanPayment.setContainment(true);
        loan.getEStructuralFeatures().add(loanPayment);

        // --- Render -----------------------------------------------------
        Diagram d = new EcoreConverter().convert(List.of(pkg, paymentPkg));
        new ElkLayoutEngine().layout(d);
        String svg = new SvgRenderer().render(d);

        Path out = RenderMatrix.examplesDir()
                .resolve("diagrams/ecore-class/showcase/full.svg");
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.writeString(out, svg);

        // Second variant: showInheritedFeatures=true so Author lists
        // Person's attributes/refs/ops in italic with the inherited icon.
        Diagram d2 = new EcoreConverter()
                .options(EcoreConverter.Options.defaults().withShowInheritedFeatures(true))
                .convert(List.of(pkg, paymentPkg));
        new ElkLayoutEngine().layout(d2);
        String svg2 = new SvgRenderer().render(d2);
        Path out2 = RenderMatrix.examplesDir()
                .resolve("diagrams/ecore-class/showcase/full-with-inherited.svg");
        Files.writeString(out2, svg2);
    }

    private static EEnum makeEnum(String name, String... literalNames) {
        EEnum e = F.createEEnum();
        e.setName(name);
        for (int i = 0; i < literalNames.length; i++) {
            EEnumLiteral l = F.createEEnumLiteral();
            l.setName(literalNames[i]);
            l.setLiteral(literalNames[i]);
            l.setValue(i);
            e.getELiterals().add(l);
        }
        return e;
    }

    private static EAttribute addAttr(EClass owner, String name, EDataType type,
                                       int lo, int hi, boolean derived) {
        EAttribute a = F.createEAttribute();
        a.setName(name);
        a.setEType(type);
        a.setLowerBound(lo);
        a.setUpperBound(hi);
        a.setDerived(derived);
        owner.getEStructuralFeatures().add(a);
        return a;
    }

    private static void addParam(EOperation op, String name, EDataType type) {
        EParameter p = F.createEParameter();
        p.setName(name);
        p.setEType(type);
        op.getEParameters().add(p);
    }
}
