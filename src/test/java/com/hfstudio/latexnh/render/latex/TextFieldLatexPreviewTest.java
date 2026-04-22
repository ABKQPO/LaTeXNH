package com.hfstudio.latexnh.render.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TextFieldLatexPreviewTest {

    private static final String FORMULA_SOURCE = "$LATEX$";

    @Test
    void usesVisibleStartAsPreviewCursorWhenTextFieldIsNotFocused() {
        assertEquals(2, TextFieldLatexPreview.resolvePreviewCursor(false, 5, 2));
        assertEquals(5, TextFieldLatexPreview.resolvePreviewCursor(true, 5, 2));
    }

    @Test
    void rebuildsWholeFormulaWhenRawViewportStartsInsideFormulaButRenderedWidthFits() {
        String fullText = FORMULA_SOURCE;

        TextFieldLatexPreview.Plan plan = TextFieldLatexPreview
            .plan(fullText, fullText.length(), 4, "TEX$", text -> measureWithRenderedFormula(text, 4), 4);

        assertTrue(plan.inlinePreview);
        assertFalse(plan.renderedTooltipOnHover);
        assertNotNull(plan.anchorSegment);
        assertEquals(0, plan.visibleStartIndex);
        assertEquals(FORMULA_SOURCE, plan.visibleText);
    }

    @Test
    void keepsTrailingPlainTextWhenCursorAfterFormulaAndCombinedVisualWidthStillFits() {
        String fullText = FORMULA_SOURCE + "123";

        TextFieldLatexPreview.Plan plan = TextFieldLatexPreview
            .plan(fullText, fullText.length(), 4, "TEX$123", text -> measureWithRenderedFormula(text, 4), 7);

        assertTrue(plan.inlinePreview);
        assertFalse(plan.renderedTooltipOnHover);
        assertNotNull(plan.anchorSegment);
        assertEquals(0, plan.visibleStartIndex);
        assertEquals(FORMULA_SOURCE + "123", plan.visibleText);
    }

    @Test
    void keepsRawVisibleSliceWhenRenderedFormulaStillDoesNotFit() {
        String fullText = FORMULA_SOURCE + FORMULA_SOURCE + FORMULA_SOURCE;

        TextFieldLatexPreview.Plan plan = TextFieldLatexPreview
            .plan(fullText, fullText.length(), 4, "TEX$LAT", text -> measureWithRenderedFormula(text, 10), 7);

        assertFalse(plan.inlinePreview);
        assertTrue(plan.renderedTooltipOnHover);
        assertNotNull(plan.anchorSegment);
        assertEquals(4, plan.visibleStartIndex);
        assertEquals("TEX$LAT", plan.visibleText);
    }

    @Test
    void keepsRawVisibleSliceWhenCursorIsInsideFormulaEvenIfRenderedWidthFits() {
        String fullText = FORMULA_SOURCE;

        TextFieldLatexPreview.Plan plan = TextFieldLatexPreview
            .plan(fullText, 4, 0, "$LAT", text -> measureWithRenderedFormula(text, 4), 4);

        assertFalse(plan.inlinePreview);
        assertTrue(plan.renderedTooltipOnHover);
        assertNotNull(plan.anchorSegment);
        assertEquals(0, plan.visibleStartIndex);
        assertEquals("$LAT", plan.visibleText);
    }

    @Test
    void resolvesShortcutTooltipFormulaWhenFocusedCursorIsInsideLatex() {
        assertEquals("LATEX", TextFieldLatexPreview.resolveShortcutTooltipFormula("ab$LATEX$cd", true, 5, true));
        assertEquals("1", TextFieldLatexPreview.resolveShortcutTooltipFormula("$1$", true, 2, true));
        assertEquals(null, TextFieldLatexPreview.resolveShortcutTooltipFormula("ab$LATEX$cd", true, 5, false));
        assertEquals(null, TextFieldLatexPreview.resolveShortcutTooltipFormula("ab$LATEX$cd", false, 5, true));
        assertEquals(null, TextFieldLatexPreview.resolveShortcutTooltipFormula("abcd", true, 2, true));
    }

    private static int measureWithRenderedFormula(String text, int renderedWidth) {
        return text.replace(FORMULA_SOURCE, repeat('x', renderedWidth))
            .length();
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }
}
