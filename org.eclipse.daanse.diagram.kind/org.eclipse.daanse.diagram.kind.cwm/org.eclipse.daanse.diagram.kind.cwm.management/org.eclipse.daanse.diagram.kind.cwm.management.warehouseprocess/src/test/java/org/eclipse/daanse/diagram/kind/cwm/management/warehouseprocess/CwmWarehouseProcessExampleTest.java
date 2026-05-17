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
package org.eclipse.daanse.diagram.kind.cwm.management.warehouseprocess;

import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.WarehouseActivity;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.WarehouseStep;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.WarehouseprocessFactory;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.datatype.RecurringType;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.CalendarDate;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.EventsFactory;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.IntervalEvent;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.PointInTimeEvent;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.RecurringPointInTimeEvent;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.RetryEvent;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.CoreFactory;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.core.render.RenderMatrix;
import org.junit.jupiter.api.Test;

class CwmWarehouseProcessExampleTest {

    private static final WarehouseprocessFactory WPF = WarehouseprocessFactory.eINSTANCE;
    private static final EventsFactory EVT = EventsFactory.eINSTANCE;
    private static final CoreFactory CF = CoreFactory.eINSTANCE;
    private static final RenderMatrix M = RenderMatrix.to(RenderMatrix.examplesDir());

    @Test
    void singleton_activity() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("activity-alone");
        WarehouseActivity wa = WPF.createWarehouseActivity();
        wa.setName("nightly-load");
        wa.setIsSequential(true);
        pkg.getOwnedElement().add(wa);
        single("warehouse-activity", pkg);
    }

    @Test
    void singleton_step() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("step-alone");
        WarehouseStep ws = WPF.createWarehouseStep(); ws.setName("extract");
        pkg.getOwnedElement().add(ws);
        single("warehouse-step", pkg);
    }

    @Test
    void singleton_pointInTimeEvent() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("pit-alone");
        PointInTimeEvent ev = EVT.createPointInTimeEvent();
        ev.setName("at midnight");
        pkg.getOwnedElement().add(ev);
        single("point-in-time-event", pkg);
    }

    @Test
    void singleton_recurringPointInTimeEvent() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("rec-alone");
        RecurringPointInTimeEvent ev = EVT.createRecurringPointInTimeEvent();
        ev.setName("daily 02:00");
        ev.setRecurringType(RecurringType.EVERY_DAY);
        ev.setHour(2);
        ev.setMinute(0);
        pkg.getOwnedElement().add(ev);
        single("recurring-event", pkg);
    }

    @Test
    void singleton_intervalEvent() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("ivl-alone");
        IntervalEvent ev = EVT.createIntervalEvent();
        ev.setName("interval 15min");
        ev.setDuration("PT15M");
        pkg.getOwnedElement().add(ev);
        single("interval-event", pkg);
    }

    @Test
    void singleton_retryEvent() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("retry-alone");
        RetryEvent ev = EVT.createRetryEvent();
        ev.setName("retry on failure");
        ev.setMaxCount(3);
        ev.setWaitDuration("PT5M");
        pkg.getOwnedElement().add(ev);
        single("retry-event", pkg);
    }

    @Test
    void singleton_calendarDate() throws Exception {
        Package pkg = CF.createPackage(); pkg.setName("cal-alone");
        CalendarDate cd = EVT.createCalendarDate();
        cd.setName("Easter Monday");
        cd.setSpecificDate("2026-04-06");
        pkg.getOwnedElement().add(cd);
        single("calendar-date", pkg);
    }


    private static void single(String name, Package pkg) throws Exception {
        java.util.function.Supplier<org.eclipse.daanse.diagram.core.Diagram> mk =
                () -> new CwmWarehouseProcessConverter().convert(pkg);
        M.writeSingleton(name, RenderMatrix.minimalAndFull(mk, mk));
    }
}
