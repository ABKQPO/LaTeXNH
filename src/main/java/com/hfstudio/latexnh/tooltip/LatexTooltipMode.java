package com.hfstudio.latexnh.tooltip;

public enum LatexTooltipMode {

    NONE,
    RENDERED_LATEX,
    SOURCE_TEXT;

    public boolean isActive() {
        return this != NONE;
    }

    public boolean rendersLatex() {
        return this == RENDERED_LATEX;
    }

    public boolean rendersSourceText() {
        return this == SOURCE_TEXT;
    }

    public static LatexTooltipMode fromHotkeys(boolean previewHotkeyDown, boolean sourceHotkeyDown) {
        if (previewHotkeyDown) {
            return RENDERED_LATEX;
        }
        if (sourceHotkeyDown) {
            return SOURCE_TEXT;
        }
        return NONE;
    }
}
