package com.hfstudio.latexnh.render.latex;

import java.awt.RenderingHints;

public enum LatexRenderQuality {

    QUALITY,
    BALANCED,
    SPEED;

    public Object toRenderingHint() {
        return switch (this) {
            case QUALITY -> RenderingHints.VALUE_RENDER_QUALITY;
            case BALANCED -> RenderingHints.VALUE_RENDER_DEFAULT;
            case SPEED -> RenderingHints.VALUE_RENDER_SPEED;
        };
    }

    public Object toAlphaInterpolationHint() {
        return switch (this) {
            case QUALITY -> RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
            case BALANCED -> RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
            case SPEED -> RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
        };
    }

    public Object toColorRenderingHint() {
        return switch (this) {
            case QUALITY -> RenderingHints.VALUE_COLOR_RENDER_QUALITY;
            case BALANCED -> RenderingHints.VALUE_COLOR_RENDER_DEFAULT;
            case SPEED -> RenderingHints.VALUE_COLOR_RENDER_SPEED;
        };
    }
}
