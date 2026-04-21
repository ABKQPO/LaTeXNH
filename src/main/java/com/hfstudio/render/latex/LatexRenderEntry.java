package com.hfstudio.render.latex;

public final class LatexRenderEntry {

    public final String formula;
    public final int x;
    public final int y;
    public final int w;
    public final int h;
    public final boolean failed;

    public LatexRenderEntry(String formula, int x, int y, int w, int h, boolean failed) {
        this.formula = formula;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.failed = failed;
    }
}
