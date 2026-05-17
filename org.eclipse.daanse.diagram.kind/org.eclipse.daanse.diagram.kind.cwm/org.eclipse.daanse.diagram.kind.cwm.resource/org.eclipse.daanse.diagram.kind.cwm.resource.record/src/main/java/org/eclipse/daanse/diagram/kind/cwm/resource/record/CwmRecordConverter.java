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

import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Classifier;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Feature;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.ModelElement;
import org.eclipse.daanse.cwm.model.cwm.objectmodel.core.Package;
import org.eclipse.daanse.cwm.model.cwm.resource.record.Field;
import org.eclipse.daanse.cwm.model.cwm.resource.record.FixedOffsetField;
import org.eclipse.daanse.cwm.model.cwm.resource.record.Group;
import org.eclipse.daanse.cwm.model.cwm.resource.record.RecordDef;
import org.eclipse.daanse.cwm.model.cwm.resource.record.RecordFile;
import org.eclipse.daanse.diagram.core.Diagram;
import org.eclipse.daanse.diagram.core.DiagramBuilder;
import org.eclipse.daanse.diagram.core.Icons;
import org.eclipse.daanse.diagram.core.LabelledBoxBody;
import org.eclipse.daanse.diagram.core.style.Palette;
import org.eclipse.daanse.diagram.core.style.Stereotype;
import org.eclipse.daanse.diagram.core.util.Names;

/**
 * Renders a CWM Record package: each RecordFile becomes a folder-style
 * SchemaBody container holding one RecordDef child per record format
 * inside it; each RecordDef lists its Field entries with type, length
 * and (for FixedOffsetField) byte offset. A free-standing Group type
 * is rendered next to the files.
 */
public final class CwmRecordConverter {

    private static final String ID_FILE = "rf";
    private static final String ID_DEF  = "rd";
    private static final String ID_GROUP = "rg";
    private static final String ID_EDGE = "rec_file";

    private static final Stereotype RECORD_DEF_S = new Stereotype("RECORD DEF", Palette.BLUE_900, Icons.RECORD_DEF);
    private static final Stereotype GROUP_S      = new Stereotype("GROUP",      Palette.TEAL_700, Icons.RECORD_FIELD);

    public Diagram convert(Package pkg) {
        DiagramBuilder b = DiagramBuilder.of(Names.n(pkg.getName()));

        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof RecordFile rf) {
                b.container(rf, Names.n(rf.getName()), "RECORD FILE", Icons.RECORD_FILE, ID_FILE);
                for (RecordDef rd : rf.getRecord()) {
                    buildRecordDef(b, rd);
                    b.nest(rf, rd);
                }
            } else if (me instanceof RecordDef rd && !b.has(rd)) {
                buildRecordDef(b, rd);
            } else if (me instanceof Group g) {
                buildGroup(b, g);
            }
        }
        // Cross-RecordFile back-references: a RecordDef can list its files,
        // independent of containment. Draw "in" edges for non-parent ones.
        for (ModelElement me : pkg.getOwnedElement()) {
            if (me instanceof RecordDef rd && b.has(rd)) {
                for (RecordFile rf : rd.getFile()) {
                    if (!pkg.getOwnedElement().contains(rf)) continue;
                    if (!b.has(rf)) continue;
                    if (b.node(rd).parent() == b.node(rf)) continue;
                    b.edge(rd, rf, ID_EDGE).label("in");
                }
            }
        }
        return b.diagram();
    }

    private void buildRecordDef(DiagramBuilder b, RecordDef rd) {
        LabelledBoxBody body = b.labelled(rd, RECORD_DEF_S, Names.n(rd.getName()), ID_DEF);
        if (rd.isIsFixedWidth()) {
            body.addRow("fixed-width");
        } else if (rd.getFieldDelimiter() != null && !rd.getFieldDelimiter().isEmpty()) {
            body.addRow("delim: " + rd.getFieldDelimiter());
        }
        if (rd.getTextDelimiter() != null && !rd.getTextDelimiter().isEmpty()) {
            body.addRow("text: " + rd.getTextDelimiter());
        }
        for (Feature f : rd.getFeature()) {
            if (f instanceof Field fld) body.addRow(formatField(fld));
        }
    }

    private void buildGroup(DiagramBuilder b, Group g) {
        LabelledBoxBody body = b.labelled(g, GROUP_S, Names.n(g.getName()), ID_GROUP);
        for (Feature f : g.getFeature()) {
            if (f instanceof Field fld) body.addRow(formatField(fld));
        }
    }

    /**
     * Formats one {@link Field} for the record body. Layout:
     * {@code <name> : <type>  ([offset]+len[.precision[.scale]])}
     * where the offset is only included for {@link FixedOffsetField}.
     */
    private static String formatField(Field f) {
        StringBuilder sb = new StringBuilder();
        sb.append(Names.n(f.getName()))
          .append(" : ")
          .append(typeName(f.getType()));
        StringBuilder meta = new StringBuilder();
        if (f instanceof FixedOffsetField fof) {
            meta.append("@").append(fof.getOffset());
        }
        if (f.getLength() > 0) {
            if (meta.length() > 0) meta.append("+");
            meta.append("len ").append(f.getLength());
        }
        if (f.getPrecision() > 0) {
            if (meta.length() > 0) meta.append(", ");
            meta.append("p ").append(f.getPrecision());
            if (f.getScale() > 0) meta.append(".").append(f.getScale());
        }
        if (meta.length() > 0) sb.append("  (").append(meta).append(")");
        return sb.toString();
    }

    private static String typeName(Classifier c) {
        return c != null && c.getName() != null ? c.getName() : "?";
    }
}
