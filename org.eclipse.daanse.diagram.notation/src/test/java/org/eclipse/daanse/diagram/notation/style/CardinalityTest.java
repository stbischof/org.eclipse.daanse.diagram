/*
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*/
package org.eclipse.daanse.diagram.notation.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class CardinalityTest {

    @Test
    void parseStarShorthand() {
        assertEquals(Cardinality.ZERO_TO_MANY, Cardinality.parse("*"));
    }

    @Test
    void parseSingleInteger() {
        assertEquals(new Cardinality(1, 1), Cardinality.parse("1"));
        assertEquals(new Cardinality(7, 7), Cardinality.parse("7"));
    }

    @Test
    void parseRanges() {
        assertEquals(Cardinality.ZERO_TO_MANY, Cardinality.parse("0..*"));
        assertEquals(Cardinality.ONE_TO_MANY,  Cardinality.parse("1..*"));
        assertEquals(Cardinality.ZERO_TO_ONE,  Cardinality.parse("0..1"));
        assertEquals(new Cardinality(3, 7),    Cardinality.parse("3..7"));
    }

    @Test
    void formatRoundTrips() {
        for (String s : new String[] { "0..*", "1..*", "0..1", "1", "3..7" }) {
            assertEquals(s, Cardinality.parse(s).format(),
                    "round trip failed for " + s);
        }
    }

    @Test
    void unboundedFlag() {
        assertTrue(Cardinality.ZERO_TO_MANY.isUnbounded());
        assertTrue(Cardinality.ONE_TO_MANY.isUnbounded());
        assertTrue(!Cardinality.ONE.isUnbounded());
    }

    @Test
    void rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Cardinality(-1, 1));
    }

    @Test
    void rejectsInvertedRange() {
        assertThrows(IllegalArgumentException.class, () -> new Cardinality(5, 2));
    }

    @Test
    void rejectsMalformed() {
        assertThrows(IllegalArgumentException.class, () -> Cardinality.parse(""));
        assertThrows(IllegalArgumentException.class, () -> Cardinality.parse("a..b"));
        assertThrows(IllegalArgumentException.class, () -> Cardinality.parse("..*"));
    }
}
