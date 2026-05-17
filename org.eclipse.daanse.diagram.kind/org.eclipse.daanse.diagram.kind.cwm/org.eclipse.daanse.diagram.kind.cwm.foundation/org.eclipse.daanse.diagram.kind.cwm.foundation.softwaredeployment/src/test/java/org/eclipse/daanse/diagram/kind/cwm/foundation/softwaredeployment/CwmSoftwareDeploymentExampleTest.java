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
package org.eclipse.daanse.diagram.kind.cwm.foundation.softwaredeployment;

import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.Component;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DataManager;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DataProvider;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DeployedComponent;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.DeployedSoftwareSystem;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.Machine;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.ProviderConnection;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.Site;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.SoftwareSystem;
import org.eclipse.daanse.cwm.model.cwm.foundation.softwaredeployment.SoftwaredeploymentFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

class CwmSoftwareDeploymentExampleTest {

    private static final SoftwaredeploymentFactory SDF = SoftwaredeploymentFactory.eINSTANCE;
    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    private static void single(String name, Package pkg) throws Exception {
        java.util.function.Supplier<org.eclipse.daanse.diagram.core.Diagram> mk =
                () -> new CwmSoftwareDeploymentConverter().convert(pkg);
        M.writeSingleton(name, RenderMatrix.minimalAndFull(mk, mk));
    }

    /* ---- Singletons -------------------------------------------------- */

    @Test void singleton_site() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("site-alone");
        Site s = SDF.createSite(); s.setName("DC Frankfurt");
        s.setLocationType("Datacenter"); s.setCity("Frankfurt"); s.setCountry("DE");
        pkg.getOwnedElement().add(s);
        single("site", pkg);
    }

    @Test void singleton_machine() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("machine-alone");
        Machine m = SDF.createMachine(); m.setName("etl-prod-01");
        m.setMachineID("srv-001"); m.setHostName("etl-prod-01.example.org");
        m.setIpAddress("10.0.1.10");
        pkg.getOwnedElement().add(m);
        single("machine", pkg);
    }

    @Test void singleton_component() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("component-alone");
        Component c = SDF.createComponent(); c.setName("foodmart-etl");
        pkg.getOwnedElement().add(c);
        single("component", pkg);
    }

    @Test void singleton_softwareSystem() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("sw-alone");
        SoftwareSystem ss = SDF.createSoftwareSystem();
        ss.setName("PostgreSQL"); ss.setType("RDBMS");
        ss.setSubtype("transactional"); ss.setSupplier("PG Global"); ss.setVersion("15.4");
        pkg.getOwnedElement().add(ss);
        single("software-system", pkg);
    }

    @Test void singleton_deployedSoftwareSystem() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("dss-alone");
        DeployedSoftwareSystem dss = SDF.createDeployedSoftwareSystem();
        dss.setName("postgres-15"); dss.setFixLevel("15.4");
        pkg.getOwnedElement().add(dss);
        single("deployed-software-system", pkg);
    }

    @Test void singleton_deployedComponent() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("dc-alone");
        DeployedComponent dc = SDF.createDeployedComponent();
        dc.setName("etl@srvA"); dc.setPathname("/opt/etl");
        pkg.getOwnedElement().add(dc);
        single("deployed-component", pkg);
    }

    @Test void singleton_dataProvider() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("dp-alone");
        DataProvider dp = SDF.createDataProvider();
        dp.setName("warehouse@srvB"); dp.setPathname("/opt/dwh");
        pkg.getOwnedElement().add(dp);
        single("data-provider", pkg);
    }

    @Test void singleton_dataManager() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("dm-alone");
        DataManager dm = SDF.createDataManager();
        dm.setName("etl@srvA"); dm.setPathname("/opt/etl");
        pkg.getOwnedElement().add(dm);
        single("data-manager", pkg);
    }

    /* ---- Named diagrams --------------------------------------------- */

    /** Application-stack view: Site → Machine, with the Components,
     *  DeployedComponents, DataManagers/Providers, DeployedSoftwareSystems
     *  and ProviderConnections that run on top. */
    @Test void diagram_deployment_application() throws Exception {
        Package pkg = CF.createPackage();
        pkg.setName("salesDeployment");

        Site dc1 = SDF.createSite(); dc1.setName("DC Frankfurt");
        dc1.setLocationType("Datacenter");
        dc1.setCity("Frankfurt"); dc1.setCountry("DE");
        pkg.getOwnedElement().add(dc1);

        Machine srvA = SDF.createMachine(); srvA.setName("etl-prod-01");
        srvA.setMachineID("srv-001");
        srvA.setHostName("etl-prod-01.example.org");
        srvA.setIpAddress("10.0.1.10");
        pkg.getOwnedElement().add(srvA);
        dc1.getMachine().add(srvA);

        Machine srvB = SDF.createMachine(); srvB.setName("warehouse-prod-01");
        srvB.setMachineID("srv-002");
        srvB.setHostName("warehouse-prod-01.example.org");
        srvB.setIpAddress("10.0.1.11");
        pkg.getOwnedElement().add(srvB);
        dc1.getMachine().add(srvB);

        Component etl = SDF.createComponent(); etl.setName("foodmart-etl");
        Component warehouse = SDF.createComponent(); warehouse.setName("foodmart-warehouse");
        pkg.getOwnedElement().add(etl);
        pkg.getOwnedElement().add(warehouse);

        SoftwareSystem postgres = SDF.createSoftwareSystem();
        postgres.setName("PostgreSQL");
        postgres.setType("RDBMS"); postgres.setSupplier("PG Global"); postgres.setVersion("15.4");
        pkg.getOwnedElement().add(postgres);

        DeployedSoftwareSystem dssEtl = SDF.createDeployedSoftwareSystem();
        dssEtl.setName("etl-runtime"); dssEtl.setFixLevel("1.4.2");
        dssEtl.setSoftwareSystem(postgres);
        pkg.getOwnedElement().add(dssEtl);

        DeployedSoftwareSystem dssDwh = SDF.createDeployedSoftwareSystem();
        dssDwh.setName("postgres-15"); dssDwh.setFixLevel("15.4");
        dssDwh.setSoftwareSystem(postgres);
        pkg.getOwnedElement().add(dssDwh);

        DataManager etlDep = SDF.createDataManager();
        etlDep.setName("etl@srvA"); etlDep.setPathname("/opt/etl");
        etlDep.getDeployedSoftwareSystem().add(dssEtl);
        pkg.getOwnedElement().add(etlDep);
        srvA.getDeployedComponent().add(etlDep);

        DataProvider whDep = SDF.createDataProvider();
        whDep.setName("warehouse@srvB"); whDep.setPathname("/opt/dwh");
        whDep.getDeployedSoftwareSystem().add(dssDwh);
        pkg.getOwnedElement().add(whDep);
        srvB.getDeployedComponent().add(whDep);

        ProviderConnection pc = SDF.createProviderConnection();
        pc.setName("etl→warehouse");
        pc.setDataManager(etlDep);
        pc.setDataProvider(whDep);
        pkg.getOwnedElement().add(pc);

        M.writeDiagram("deployment-application", RenderMatrix.minimalAndFull(
                () -> new DeploymentApplicationDiagram(DeploymentApplicationDiagram.Features.minimal(), pkg).toDiagram(),
                () -> new DeploymentApplicationDiagram(DeploymentApplicationDiagram.Features.full(),    pkg).toDiagram()));
    }

    /** Datacenter hierarchy: Site → host Machine → VM Machines → containers. */
    @Test void diagram_deployment_infrastructure() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("hierarchy");

        Site site = SDF.createSite(); site.setName("DC Frankfurt");
        pkg.getOwnedElement().add(site);

        Machine host = SDF.createMachine();
        host.setName("hv-prod-01");
        host.setHostName("hv-prod-01.example.org");
        host.setIpAddress("10.0.2.5");
        host.setSite(site);
        pkg.getOwnedElement().add(host);

        Machine vmApp = SDF.createMachine();
        vmApp.setName("vm-app-01");
        vmApp.setHostName("vm-app-01.example.org");
        vmApp.setIpAddress("10.0.2.21");
        host.getOwnedElement().add(vmApp);

        Machine vmDb = SDF.createMachine();
        vmDb.setName("vm-db-01");
        vmDb.setHostName("vm-db-01.example.org");
        vmDb.setIpAddress("10.0.2.22");
        host.getOwnedElement().add(vmDb);

        Machine cWeb = SDF.createMachine();
        cWeb.setName("c-web");
        cWeb.setHostName("c-web.svc.cluster.local");
        vmApp.getOwnedElement().add(cWeb);

        Machine cApi = SDF.createMachine();
        cApi.setName("c-api");
        cApi.setHostName("c-api.svc.cluster.local");
        vmApp.getOwnedElement().add(cApi);

        Machine cPg = SDF.createMachine();
        cPg.setName("c-postgres");
        cPg.setHostName("c-postgres.svc.cluster.local");
        vmDb.getOwnedElement().add(cPg);

        M.writeDiagram("deployment-infrastructure", RenderMatrix.minimalAndFull(
                () -> new DeploymentInfrastructureDiagram(DeploymentInfrastructureDiagram.Features.minimal(), pkg).toDiagram(),
                () -> new DeploymentInfrastructureDiagram(DeploymentInfrastructureDiagram.Features.full(),    pkg).toDiagram()));
    }
}
