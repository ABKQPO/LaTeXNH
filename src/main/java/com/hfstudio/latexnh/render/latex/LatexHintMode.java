package com.hfstudio.latexnh.render.latex;

import java.awt.RenderingHints;

public enum LatexHintMode {

    DEFAULT,
    ON,
    OFF;

    public Object toGeneralAntialiasHint() {
        return switch (this) {
            case DEFAULT -> RenderingHints.VALUE_ANTIALIAS_DEFAULT;
            case ON -> RenderingHints.VALUE_ANTIALIAS_ON;
            case OFF -> RenderingHints.VALUE_ANTIALIAS_OFF;
        };
    }

    public Object toTextAntialiasHint() {
        return switch (this) {
            case DEFAULT -> RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
            case ON -> RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            case OFF -> RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
        };
    }
}
