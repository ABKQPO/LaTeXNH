package com.hfstudio.latexnh.render.latex;

import com.hfstudio.latexnh.render.markdown.TextSegment;

public final class LatexRenderContext {

    public static final LatexRenderContext INSTANCE = new LatexRenderContext();

    private final ThreadLocal<ActiveFormula> activeFormula = new ThreadLocal<>();
    private final ThreadLocal<RenderSlice> renderSlice = new ThreadLocal<>();

    private LatexRenderContext() {}

    public void setEditingFormula(TextSegment segment) {
        setEditingFormula(segment, true);
    }

    public void setEditingFormula(TextSegment segment, boolean renderSource) {
        if (segment == null || !segment.isLatex()) {
            activeFormula.remove();
            return;
        }
        activeFormula
            .set(new ActiveFormula(segment.content, segment.type, segment.startIndex, segment.endIndex, renderSource));
    }

    public void clearEditingFormula() {
        activeFormula.remove();
    }

    public void setRenderSliceBaseOffset(int baseOffset) {
        setRenderSlice(null, baseOffset);
    }

    public void setRenderSlice(String renderedText, int baseOffset) {
        renderSlice.set(new RenderSlice(renderedText, baseOffset));
    }

    public void clearRenderSlice() {
        renderSlice.remove();
    }

    public boolean shouldRenderSource(TextSegment segment) {
        ActiveFormula active = activeFormula.get();
        return active != null && active.renderSource && active.matches(segment, renderSlice.get());
    }

    private static final class ActiveFormula {

        private final String formula;
        private final TextSegment.SegmentType type;
        private final int startIndex;
        private final int endIndex;
        private final boolean renderSource;

        private ActiveFormula(String formula, TextSegment.SegmentType type) {
            this(formula, type, -1, -1, true);
        }

        private ActiveFormula(String formula, TextSegment.SegmentType type, int startIndex, int endIndex,
            boolean renderSource) {
            this.formula = formula;
            this.type = type;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.renderSource = renderSource;
        }

        private boolean matches(TextSegment segment, RenderSlice renderSlice) {
            if (segment == null || !segment.isLatex() || type != segment.type || !formula.equals(segment.content)) {
                return false;
            }
            if (startIndex < 0 || endIndex < 0 || segment.startIndex < 0 || segment.endIndex < 0) {
                return true;
            }
            int candidateStart = segment.startIndex;
            int candidateEnd = segment.endIndex;
            if (renderSlice != null) {
                candidateStart = renderSlice.toRawIndex(candidateStart);
                candidateEnd = renderSlice.toRawIndex(candidateEnd);
            }
            return startIndex == candidateStart && endIndex == candidateEnd;
        }
    }

    private static final class RenderSlice {

        private final String renderedText;
        private final int baseOffset;

        private RenderSlice(String renderedText, int baseOffset) {
            this.renderedText = renderedText;
            this.baseOffset = baseOffset;
        }

        private int toRawIndex(int formattedIndex) {
            if (formattedIndex <= 0 || renderedText == null || renderedText.isEmpty()) {
                return baseOffset + Math.max(0, formattedIndex);
            }

            int clampedIndex = Math.min(formattedIndex, renderedText.length());
            int visibleChars = 0;

            for (int i = 0; i < clampedIndex; i++) {
                char ch = renderedText.charAt(i);
                if (ch == MinecraftTextFormattingState.FORMATTING_CHAR && i + 1 < renderedText.length()
                    && MinecraftTextFormattingState.isFormattingCode(renderedText.charAt(i + 1))) {
                    i++;
                    continue;
                }
                visibleChars++;
            }

            return baseOffset + visibleChars;
        }
    }
}
