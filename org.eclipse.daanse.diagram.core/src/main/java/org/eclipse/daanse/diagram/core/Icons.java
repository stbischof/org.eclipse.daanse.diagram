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
package org.eclipse.daanse.diagram.core;

/**
 * 12x12 SVG path glyphs used as header icons in {@link LabelledBoxBody}.
 * Each path is normalised to fit inside a 12x12 box with a 1px margin
 * (so the visible drawing lives inside x=1..11, y=1..11). Glyphs are
 * filled with the title-bar foreground colour and clipped to the
 * circular badge drawn behind them.
 */
public final class Icons {
    private Icons() {}

    // --- ObjectModel -----------------------------------------------------
    /** UML class — three-compartment rectangle. */
    public static final String CLASS = "M1 2h10v8H1zM1 5h10M1 8h10";
    /** Data type — block with horizontal lines, like a database leaf. */
    public static final String DATATYPE =
            "M1 2h10v8H1zM2 4h8M2 6h8M2 8h8";
    /** UML interface / abstract — circle. */
    public static final String INTERFACE =
            "M6 1a5 5 0 1 0 0.001 0zM6 3a3 3 0 1 0 0.001 0";

    // --- Business Information --------------------------------------------
    /** Person silhouette — responsible party / contact. */
    public static final String PERSON =
            "M6 1a2 2 0 1 1 0 4a2 2 0 1 1 0-4zM2 11c0-2 2-4 4-4s4 2 4 4z";
    /** Address book / contact card. */
    public static final String CONTACT_CARD =
            "M1 2h10v8H1zM3 4h6M3 6h6M3 8h4";
    /** Envelope. */
    public static final String EMAIL =
            "M1 3h10v6H1zM1 3l5 4l5-4";
    /** Telephone handset. */
    public static final String PHONE =
            "M3 1l2 2l-1 2c1 2 2 3 4 4l2-1l2 2l-1 2c-4 0-9-5-9-9z";
    /** Map pin. */
    public static final String LOCATION =
            "M6 1c-2.5 0-4 1.8-4 4c0 3 4 6 4 6s4-3 4-6c0-2.2-1.5-4-4-4zM6 3a1.5 1.5 0 1 1 0 3a1.5 1.5 0 1 1 0-3";
    /** Document — sheet with corner fold. */
    public static final String DOCUMENT =
            "M2 1h6l3 3v7H2zM8 1v3h3M3 6h6M3 8h6";
    /** Description / note — sheet with lines. */
    public static final String DESCRIPTION =
            "M2 1h8v10H2zM3 3h6M3 5h6M3 7h6M3 9h4";

    // --- Software Deployment ---------------------------------------------
    /** Datacenter / globe — site. */
    public static final String SITE =
            "M6 1a5 5 0 1 0 0.001 0zM1 6h10M6 1c-2 1-3 3-3 5s1 4 3 5c2-1 3-3 3-5s-1-4-3-5";
    /** Server / machine. */
    public static final String MACHINE =
            "M1 2h10v3H1zM1 7h10v3H1zM3 3.5h.5M3 8.5h.5";
    /** Cube — component. */
    public static final String COMPONENT =
            "M6 1l5 2.5v5L6 11L1 8.5v-5zM1 3.5l5 2.5l5-2.5M6 6v5";
    /** Stack of cubes — software system. */
    public static final String SOFTWARE_SYSTEM =
            "M1 4h6v6H1zM5 1h6v6H5zM5 4h2v3";
    /** Power plug — connection. */
    public static final String CONNECTION =
            "M2 2v3a4 4 0 0 0 4 4v2M4 1v3M8 1v3M6 9c2 0 4-2 4-4V2";
    /** Database canister — data manager / data provider. */
    public static final String DATABASE =
            "M2 2h8v8H2zM2 4h8M2 6h8M2 8h8";

    // --- Transformation / Process ----------------------------------------
    /** Gear — activity. */
    public static final String GEAR =
            "M6 3a3 3 0 1 1 0 6a3 3 0 1 1 0-6zM6 1v1M6 10v1M1 6h1M10 6h1M2.5 2.5l.7.7M8.8 8.8l.7.7M2.5 9.5l.7-.7M8.8 3.2l.7-.7";
    /** Play arrow — step. */
    public static final String STEP =
            "M2 1l8 5l-8 5z";
    /** Two arrows (transformation) — refresh-like. */
    public static final String TRANSFORMATION =
            "M1 5a5 5 0 0 1 9-2l1-1v3H8l1-1a4 4 0 0 0-7 1zM11 7a5 5 0 0 1-9 2l-1 1V7h3l-1 1a4 4 0 0 0 7-1z";
    /** Arrow right — precedence. */
    public static final String ARROW_RIGHT =
            "M1 5h7V3l3 3l-3 3V7H1z";

    // --- Events ----------------------------------------------------------
    /** Lightning bolt — generic event. */
    public static final String EVENT =
            "M7 1L2 7h3l-1 4l5-6H6z";
    /** Clock — point in time. */
    public static final String CLOCK =
            "M6 1a5 5 0 1 0 0.001 0zM6 3v3l2 2";
    /** Calendar. */
    public static final String CALENDAR =
            "M1 2h10v8H1zM1 4h10M3 1v2M9 1v2M3 6h2M6 6h2M3 8h2";
    /** Hourglass — interval. */
    public static final String INTERVAL =
            "M2 1h8L7 6l3 5H2l3-5z";
    /** Counter-clockwise arrow — retry. */
    public static final String RETRY =
            "M2 6a4 4 0 1 1 1.2 2.8L1 11h4L4 9";

    // --- Operation -------------------------------------------------------
    /** Ruler / measure. */
    public static final String MEASURE =
            "M1 4h10v4H1zM3 4v2M5 4v3M7 4v2M9 4v3";
    /** Pencil — change request. */
    public static final String EDIT =
            "M2 9l5-5l2 2l-5 5H2zM7 4l1-1l2 2l-1 1M2 11h3";
    /** Activity execution — gear inside a clock. */
    public static final String EXECUTION =
            "M6 1a5 5 0 1 0 0.001 0zM6 3a3 3 0 1 1 0 6a3 3 0 1 1 0-6zM6 5v2";

    // --- Resource (Relational / Record) ----------------------------------
    /** Procedure — function symbol. */
    public static final String PROCEDURE =
            "M3 11c2 0 2-2 2.5-5S6 1 8 1M2 6h6";
    /** Rows / record — like a list. */
    public static final String RECORD =
            "M1 2h10v2H1zM1 5h10v2H1zM1 8h10v2H1z";
    /** Table — for tables / column sets. */
    public static final String TABLE =
            "M1 2h10v8H1zM1 4h10M5 4v6M9 4v6";
    /** Eye — for views. */
    public static final String VIEW =
            "M0 6q6-5 12 0q-6 5-12 0zM6 4a2 2 0 1 1 0 4a2 2 0 1 1 0-4";
    /** Magnifier over grid — for query column sets. */
    public static final String QUERY_COLUMN_SET =
            "M1 4h6v6H1zM1 8h6M4 4v6M9 4a2 2 0 1 1 0 4a2 2 0 1 1 0-4M10 6l2 2";
    /** Folder — for Schema container. */
    public static final String SCHEMA =
            "M1 3h4l1 1h5v6H1zM1 5h10";
    /** Catalog — stacked folders. */
    public static final String CATALOG =
            "M1 4h4l1 1h4v5H1zM3 2h4l1 1h3v1";
    /** Record file — flat-file glyph. */
    public static final String RECORD_FILE =
            "M2 1h6l3 3v7H2zM8 1v3h3M3 6h6M3 8h6M3 10h4";
    /** Record def — like a struct definition. */
    public static final String RECORD_DEF =
            "M1 2h10v8H1zM2 4h8M3 6h2M6 6h4M3 8h2M6 8h4";
    /** Record field — like a single line entry. */
    public static final String RECORD_FIELD =
            "M1 4h10v4H1zM3 5v2M3 6h6";
}
