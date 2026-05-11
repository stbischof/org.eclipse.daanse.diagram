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
package org.eclipse.daanse.diagram.core.util;

public final class Ids {

    private Ids() {}

    public static String identity(Object o) {
        return Integer.toHexString(System.identityHashCode(o));
    }

    public static String identity(String prefix, Object o) {
        return prefix + "_" + identity(o);
    }

    public static String semantic(String prefix, String name, Object o) {
        String safe = (name == null || name.isEmpty() ? "unnamed" : name)
                .replaceAll("[^A-Za-z0-9_]", "_");
        return prefix + "_" + safe + "_" + identity(o);
    }

    public static String edge(String prefix, Object source, Object target) {
        return prefix + "_" + identity(source) + "_" + identity(target);
    }
}
