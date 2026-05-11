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
package org.eclipse.daanse.diagram.notation.style;

/**
 * Multiplicity / cardinality range with an optional unbounded upper end
 * (encoded as {@link #UNBOUNDED}, i.e. {@code -1}).
 *
 * <p>Two canonical text forms round-trip through {@link #parse(String)} and
 * {@link #format()}: a single integer (e.g. {@code "1"}) for an exact bound,
 * and {@code "lo..hi"} for a range where {@code hi} may be {@code "*"} for
 * unbounded. The shorthand {@code "*"} parses as {@code 0..*}.</p>
 */
public record Cardinality(int lower, int upper) {

    public static final int UNBOUNDED = -1;

    public static final Cardinality ZERO_TO_MANY = new Cardinality(0, UNBOUNDED);
    public static final Cardinality ONE          = new Cardinality(1, 1);
    public static final Cardinality ZERO_TO_ONE  = new Cardinality(0, 1);
    public static final Cardinality ONE_TO_MANY  = new Cardinality(1, UNBOUNDED);

    public Cardinality {
        if (lower < 0) {
            throw new IllegalArgumentException("lower must be >= 0, was " + lower);
        }
        if (upper != UNBOUNDED && upper < lower) {
            throw new IllegalArgumentException(
                    "upper (" + upper + ") must be >= lower (" + lower + ") or UNBOUNDED");
        }
    }

    /** Convenience factory mirroring the constructor with EMF-style "{@code -1}
     *  means unbounded" semantics — useful when bridging from a model where
     *  upper bounds are stored that way. */
    public static Cardinality of(int lower, int upper) {
        return new Cardinality(lower, upper);
    }

    /** Parse {@code "*"}, {@code "0..*"}, {@code "1"}, {@code "1..*"},
     *  {@code "3..7"}. Whitespace is trimmed. */
    public static Cardinality parse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("cardinality string must not be null");
        }
        String t = s.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("cardinality string must not be empty");
        }
        if ("*".equals(t)) {
            return ZERO_TO_MANY;
        }
        int dotdot = t.indexOf("..");
        if (dotdot < 0) {
            int n = parseNonNegative(t);
            return new Cardinality(n, n);
        }
        int lo = parseNonNegative(t.substring(0, dotdot));
        String hiPart = t.substring(dotdot + 2);
        int hi = "*".equals(hiPart) ? UNBOUNDED : parseNonNegative(hiPart);
        return new Cardinality(lo, hi);
    }

    private static int parseNonNegative(String s) {
        int n;
        try {
            n = Integer.parseInt(s.trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("not a valid cardinality bound: '" + s + "'", nfe);
        }
        if (n < 0) {
            throw new IllegalArgumentException("cardinality bound must be >= 0: " + n);
        }
        return n;
    }

    /** True iff the upper end is unbounded ({@code *}). */
    public boolean isUnbounded() {
        return upper == UNBOUNDED;
    }

    /** Canonical text form: a single integer when {@code lower == upper},
     *  otherwise {@code "lo..hi"} with {@code hi} replaced by {@code "*"}
     *  for an unbounded upper end. */
    public String format() {
        if (upper == lower) {
            return Integer.toString(lower);
        }
        return lower + ".." + (upper == UNBOUNDED ? "*" : Integer.toString(upper));
    }

    @Override
    public String toString() {
        return format();
    }
}
