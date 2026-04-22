package com.hfstudio.latexnh.tooltip;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LatexTooltipModeTest {

    @Test
    void returnsNoneWhenNoTooltipHotkeyIsPressed() {
        LatexTooltipMode mode = LatexTooltipMode.fromHotkeys(false, false);

        assertSame(LatexTooltipMode.NONE, mode);
        assertFalse(mode.isActive());
    }

    @Test
    void returnsRenderedLatexWhenCtrlPreviewHotkeyIsPressed() {
        LatexTooltipMode mode = LatexTooltipMode.fromHotkeys(true, false);

        assertSame(LatexTooltipMode.RENDERED_LATEX, mode);
        assertTrue(mode.isActive());
        assertTrue(mode.rendersLatex());
        assertFalse(mode.rendersSourceText());
    }

    @Test
    void returnsSourceTextWhenAltSourceHotkeyIsPressed() {
        LatexTooltipMode mode = LatexTooltipMode.fromHotkeys(false, true);

        assertSame(LatexTooltipMode.SOURCE_TEXT, mode);
        assertTrue(mode.isActive());
        assertFalse(mode.rendersLatex());
        assertTrue(mode.rendersSourceText());
    }

    @Test
    void ctrlTakesPriorityWhenBothTooltipHotkeysArePressed() {
        LatexTooltipMode mode = LatexTooltipMode.fromHotkeys(true, true);

        assertSame(LatexTooltipMode.RENDERED_LATEX, mode);
    }
}
