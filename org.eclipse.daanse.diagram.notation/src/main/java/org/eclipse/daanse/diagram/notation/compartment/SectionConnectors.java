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
package org.eclipse.daanse.diagram.notation.compartment;

import java.util.List;

import org.eclipse.daanse.diagram.core.svg.SvgElem;

/**
 * Generalised "in-node connector" renderer for the dog-leg paths that link
 * a feature row (column / attribute / reference) to a corresponding row in
 * a section compartment further down the same node — used by:
 * <ul>
 *   <li>CWM Relational {@code TableBody}: indexes / unique constraints /
 *       check constraints / triggers / defaults each connect their section
 *       row(s) back to the column rows they reference.</li>
 *   <li>Ecore {@code EClassBody}: defaults section row connects back to the
 *       attribute row that carries the {@code D} badge.</li>
 * </ul>
 *
 * <p>Both implementations independently encoded the same path shape: anchor
 * at the row's right-gutter inside edge → run out into the gutter to a
 * per-target lane → vertical to the target row's y → back into the target
 * row. {@link #render} draws this for any number of {@link Group}s,
 * assigning one lane per group so connectors do not overlap.</p>
 */
public final class SectionConnectors {

    private SectionConnectors() {}

    /**
     * One connector group: a single target row (in the lower section) and
     * one or more source rows (in the upper feature compartment). All
     * sources in a group share a single lane in the right gutter, so a
     * "this index covers columns A, B, C" relationship draws three paths
     * fanning into one lane and meeting at the index row.
     */
    public record Group(double targetMidY, double[] sourceMidYs) {

        /** Single source → single target convenience. */
        public static Group of(double sourceMidY, double targetMidY) {
            return new Group(targetMidY, new double[] { sourceMidY });
        }

        /** Many sources → one target. */
        public static Group of(double targetMidY, List<Double> sourceMidYs) {
            double[] arr = new double[sourceMidYs.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = sourceMidYs.get(i);
            return new Group(targetMidY, arr);
        }
    }

    /**
     * Visual styling for the connector paths and anchor dots. The defaults
     * ({@link #of(String)}) match the EClassBody defaults connector look —
     * a 1.4-px line, no CSS class. The CWM Relational table uses a thicker
     * stroke and CSS classes for theming, configurable via the all-args
     * constructor.
     */
    public record Style(String color, double strokeWidth,
                         String pathClass, String dotClass, double dotRadius) {

        public static Style of(String color) {
            return new Style(color, 1.4, null, null, 1.6);
        }
    }

    /**
     * Draw the connector dog-legs for {@code groups} into {@code g}.
     *
     * @param g           target SVG group; paths and anchor dots are added here
     * @param groups      groups to draw — each consumes one lane
     * @param anchorX     x at which connectors touch the row (the inner edge
     *                    of the right-gutter strip — typically the right side
     *                    of the kind-icon column)
     * @param laneSpacing horizontal pitch between adjacent lanes (px)
     * @param style       stroke / class / dot styling
     * @param laneBase    starting lane index — pass {@code 0} for the first
     *                    section, then thread the return value into the next
     *                    call to stack multiple section types in one gutter
     * @return the next lane index, ready for a follow-up call
     */
    public static int render(SvgElem g,
                              List<Group> groups,
                              double anchorX,
                              double laneSpacing,
                              Style style,
                              int laneBase) {
        if (groups == null || groups.isEmpty()) {
            return laneBase;
        }
        String pathStyle = "fill:none;stroke:" + style.color()
                + ";stroke-width:" + style.strokeWidth()
                + ";stroke-linecap:round";
        String anchorStyle = "fill:" + style.color();
        int lane = laneBase;
        for (Group group : groups) {
            double targetY = group.targetMidY();
            double laneX = anchorX + 4.0 + lane * laneSpacing;
            for (double sourceY : group.sourceMidYs()) {
                String path = "M" + SvgElem.fmt(anchorX) + "," + SvgElem.fmt(sourceY)
                        + " L" + SvgElem.fmt(laneX) + "," + SvgElem.fmt(sourceY)
                        + " L" + SvgElem.fmt(laneX) + "," + SvgElem.fmt(targetY)
                        + " L" + SvgElem.fmt(anchorX) + "," + SvgElem.fmt(targetY);
                SvgElem p = new SvgElem("path").attr("d", path).style(pathStyle);
                if (style.pathClass() != null) p.cls(style.pathClass());
                g.add(p);
                SvgElem dot = new SvgElem("circle")
                        .attr("cx", anchorX).attr("cy", sourceY)
                        .attr("r", style.dotRadius()).style(anchorStyle);
                if (style.dotClass() != null) dot.cls(style.dotClass());
                g.add(dot);
            }
            SvgElem dot = new SvgElem("circle")
                    .attr("cx", anchorX).attr("cy", targetY)
                    .attr("r", style.dotRadius()).style(anchorStyle);
            if (style.dotClass() != null) dot.cls(style.dotClass());
            g.add(dot);
            lane++;
        }
        return lane;
    }

    /** Convenience overload using the default style for {@code color}. */
    public static int render(SvgElem g,
                              List<Group> groups,
                              double anchorX,
                              double laneSpacing,
                              String color,
                              int laneBase) {
        return render(g, groups, anchorX, laneSpacing, Style.of(color), laneBase);
    }
}
