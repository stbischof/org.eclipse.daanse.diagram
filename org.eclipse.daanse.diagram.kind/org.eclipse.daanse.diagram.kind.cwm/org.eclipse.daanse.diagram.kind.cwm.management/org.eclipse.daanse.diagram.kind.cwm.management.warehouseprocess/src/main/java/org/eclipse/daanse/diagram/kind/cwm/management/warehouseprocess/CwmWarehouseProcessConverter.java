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
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.WarehouseProcess;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.WarehouseStep;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.CalendarDate;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.IntervalEvent;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.PointInTimeEvent;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.RecurringPointInTimeEvent;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.RetryEvent;
import org.eclipse.daanse.cwm.model.cwm.management.warehouseprocess.events.WarehouseEvent;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.diagram.notation.edge.Edges;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;

/**
 * Renders a CWM Management Warehouse Process package: WarehouseActivity →
 * WarehouseStep ownership, plus the schedule events (PointInTime, Recurring,
 * Interval, Retry) wired via {@code warehouseProcess} back-reference, plus
 * a CalendarDate carrier.
 */
public final class CwmWarehouseProcessConverter {

    private static final String ID_NODE = "wp";
    private static final String ID_EDGE = "wpe";

    private static final Stereotype ACTIVITY_S  = new Stereotype("WAREHOUSE ACTIVITY",                     Palette.BLUE_900,   Icons.GEAR);
    private static final Stereotype STEP_S      = new Stereotype("WAREHOUSE STEP",                         Palette.PURPLE_900, Icons.STEP);
    private static final Stereotype RECURRING_S = new Stereotype("POINT-IN-TIME EVENT ▸ RECURRING EVENT",  Palette.TEAL_700,   Icons.CALENDAR);
    private static final Stereotype POINT_S     = new Stereotype("SCHEDULE EVENT ▸ POINT-IN-TIME EVENT",   Palette.TEAL_700,   Icons.CLOCK);
    private static final Stereotype INTERVAL_S  = new Stereotype("SCHEDULE EVENT ▸ INTERVAL EVENT",        Palette.CYAN_700,   Icons.INTERVAL);
    private static final Stereotype RETRY_S     = new Stereotype("INTERNAL EVENT ▸ RETRY EVENT",           Palette.ORANGE_900, Icons.RETRY);
    private static final Stereotype CALENDAR_S  = new Stereotype("CALENDAR DATE",                          Palette.GRAY_700,   Icons.CALENDAR);
    private static final Stereotype EVENT_S     = new Stereotype("EVENT",                                  Palette.GRAY_700,   Icons.EVENT);

    public Diagram convert(Package pkg) {
        DiagramBuilder b = DiagramBuilder.of(Names.n(pkg.getName()));
        for (ModelElement me : pkg.getOwnedElement()) render(b, me);
        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof WarehouseActivity wa) {
                for (WarehouseStep step : wa.getWarehouseStep()) {
                    if (b.has(step)) b.edge(wa, step, ID_EDGE).label("step");
                }
            }
            if (me instanceof WarehouseEvent ev) {
                WarehouseProcess p = ev.getWarehouseProcess();
                if (p != null && b.has(p)) {
                    Edges.trigger(b, ev, p, ID_EDGE).label("triggers").done();
                }
            }
        }
        return b.diagram();
    }

    private void render(DiagramBuilder b, ModelElement me) {
        if (me instanceof WarehouseActivity wa) {
            LabelledBoxBody body = b.labelled(wa, ACTIVITY_S, Names.n(wa.getName()), ID_NODE);
            if (wa.isIsSequential()) body.addRow("sequential");
            return;
        }
        if (me instanceof WarehouseStep ws) {
            b.labelled(ws, STEP_S, Names.n(ws.getName()), ID_NODE);
            return;
        }
        if (me instanceof RecurringPointInTimeEvent ev) {
            LabelledBoxBody body = b.labelled(ev, RECURRING_S, Names.n(ev.getName()), ID_NODE);
            if (ev.getRecurringType() != null) body.addRow("type: " + ev.getRecurringType().getLiteral());
            String at = formatRecurringAt(ev);
            if (!at.isEmpty()) body.addRow("at: " + at);
            if (ev.getFrequencyFactor() > 0) body.addRow("every: " + ev.getFrequencyFactor());
            return;
        }
        if (me instanceof PointInTimeEvent ev) {
            b.labelled(ev, POINT_S, Names.n(ev.getName()), ID_NODE);
            return;
        }
        if (me instanceof IntervalEvent ev) {
            LabelledBoxBody body = b.labelled(ev, INTERVAL_S, Names.n(ev.getName()), ID_NODE);
            if (ev.getDuration() != null) body.addRow("duration: " + ev.getDuration());
            return;
        }
        if (me instanceof RetryEvent ev) {
            LabelledBoxBody body = b.labelled(ev, RETRY_S, Names.n(ev.getName()), ID_NODE);
            if (ev.getMaxCount() > 0) body.addRow("max retries: " + ev.getMaxCount());
            if (ev.getWaitDuration() != null) body.addRow("wait: " + ev.getWaitDuration());
            return;
        }
        if (me instanceof CalendarDate cd) {
            LabelledBoxBody body = b.labelled(cd, CALENDAR_S, Names.n(cd.getName()), ID_NODE);
            if (cd.getSpecificDate() != null) body.addRow(cd.getSpecificDate());
            return;
        }
        if (me instanceof WarehouseEvent ev) {
            b.labelled(ev, EVENT_S, Names.n(ev.getName()), ID_NODE);
        }
    }

    /** Stitches the populated time fields of a RecurringPointInTimeEvent
     *  into a compact "DD/MM HH:MM:SS" representation; missing fields are
     *  omitted (e.g. a daily 02:00 event reads "02:00:00"). */
    private static String formatRecurringAt(RecurringPointInTimeEvent ev) {
        StringBuilder sb = new StringBuilder();
        if (ev.getDayOfMonth() > 0) sb.append(pad2(ev.getDayOfMonth()));
        if (ev.getMonth() > 0) {
            if (sb.length() > 0) sb.append("/");
            sb.append(pad2(ev.getMonth()));
        }
        if (sb.length() > 0) sb.append(" ");
        if (ev.getHour() >= 0 || ev.getMinute() > 0 || ev.getSecond() > 0) {
            sb.append(pad2(ev.getHour())).append(":")
              .append(pad2(ev.getMinute())).append(":")
              .append(pad2(ev.getSecond()));
        }
        return sb.toString().trim();
    }

    private static String pad2(long v) {
        return v < 10 ? "0" + v : Long.toString(v);
    }
}
