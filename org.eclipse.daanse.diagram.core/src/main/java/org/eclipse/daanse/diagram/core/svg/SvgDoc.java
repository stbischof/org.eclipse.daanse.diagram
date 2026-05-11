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
package org.eclipse.daanse.diagram.core.svg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Thin wrapper that assembles a root <svg> with viewbox, <defs> and <style>. */
public final class SvgDoc {

    private final SvgElem root;
    private final SvgElem defs;
    private final SvgElem styleEl;
    private final SvgElem body;

    public SvgDoc(double width, double height) {
        this.root = new SvgElem("svg")
                .attr("xmlns", "http://www.w3.org/2000/svg")
                .attr("width", width)
                .attr("height", height)
                .attr("viewBox", "0 0 " + SvgElem.fmt(width) + " " + SvgElem.fmt(height));
        this.defs = new SvgElem("defs");
        this.styleEl = new SvgElem("style").attr("type", "text/css");
        this.body = new SvgElem("g").cls("diagramvis-body");
        root.add(defs);
        defs.add(styleEl);
        root.add(body);
    }

    public SvgElem defs() { return defs; }
    public SvgElem body() { return body; }

    public SvgDoc css(String css) {
        styleEl.raw("<![CDATA[\n" + css + "\n]]>");
        return this;
    }

    public String render() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + root.render();
    }

    public void writeTo(Path out) throws java.io.IOException {
        Files.createDirectories(out.toAbsolutePath().getParent());
        Files.writeString(out, render(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
