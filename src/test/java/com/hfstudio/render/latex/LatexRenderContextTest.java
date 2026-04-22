package com.hfstudio.render.latex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.hfstudio.render.markdown.TextSegment;

class LatexRenderContextTest {

    @AfterEach
    void tearDown() {
        LatexRenderContext.INSTANCE.clearEditingFormula();
        LatexRenderContext.INSTANCE.clearRenderSlice();
    }

    @Test
    void shouldRenderSourceMatchesSelectedFormulaWithinShiftedRenderSlice() {
        LatexRenderContext.INSTANCE.setEditingFormula(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 6, 9));
        LatexRenderContext.INSTANCE.setRenderSliceBaseOffset(4);

        assertTrue(
            LatexRenderContext.INSTANCE
                .shouldRenderSource(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 2, 5)));
    }

    @Test
    void shouldRenderSourceDoesNotMatchOtherFormulaInSameSlice() {
        LatexRenderContext.INSTANCE.setEditingFormula(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 6, 9));
        LatexRenderContext.INSTANCE.setRenderSliceBaseOffset(4);

        assertFalse(
            LatexRenderContext.INSTANCE
                .shouldRenderSource(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 6, 9)));
    }

    @Test
    void shouldNotRenderSourceWhenSelectedFormulaIsMarkedForInlinePreview() {
        LatexRenderContext.INSTANCE
            .setEditingFormula(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 6, 9), false);
        LatexRenderContext.INSTANCE.setRenderSliceBaseOffset(4);

        assertFalse(
            LatexRenderContext.INSTANCE
                .shouldRenderSource(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 2, 5)));
    }

    @Test
    void shouldRenderSourceMatchesSelectedFormulaWithinFormattedRenderSlice() {
        LatexRenderContext.INSTANCE.setEditingFormula(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 6, 9));
        LatexRenderContext.INSTANCE.setRenderSlice("\u00a7r$x$", 6);

        assertTrue(
            LatexRenderContext.INSTANCE
                .shouldRenderSource(new TextSegment("x", TextSegment.SegmentType.LATEX_INLINE, 2, 5)));
    }
}
