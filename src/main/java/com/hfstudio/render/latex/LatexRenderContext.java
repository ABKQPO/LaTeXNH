package com.hfstudio.render.latex;

import com.hfstudio.render.markdown.TextSegment;

public final class LatexRenderContext {

    public static final LatexRenderContext INSTANCE = new LatexRenderContext();

    private final ThreadLocal<ActiveFormula> activeFormula = new ThreadLocal<>();

    private LatexRenderContext() {}

    public void setEditingFormula(TextSegment segment) {
        if (segment == null || !segment.isLatex()) {
            activeFormula.remove();
            return;
        }
        activeFormula.set(new ActiveFormula(segment.content, segment.type, segment.startIndex, segment.endIndex));
    }

    public void clearEditingFormula() {
        activeFormula.remove();
    }

    public boolean shouldRenderSource(TextSegment segment) {
        ActiveFormula active = activeFormula.get();
        return active != null && active.matches(segment);
    }

    private static final class ActiveFormula {

        private final String formula;
        private final TextSegment.SegmentType type;
        private final int startIndex;
        private final int endIndex;

        private ActiveFormula(String formula, TextSegment.SegmentType type) {
            this(formula, type, -1, -1);
        }

        private ActiveFormula(String formula, TextSegment.SegmentType type, int startIndex, int endIndex) {
            this.formula = formula;
            this.type = type;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        private boolean matches(TextSegment segment) {
            if (segment == null || !segment.isLatex() || type != segment.type || !formula.equals(segment.content)) {
                return false;
            }
            if (startIndex < 0 || endIndex < 0 || segment.startIndex < 0 || segment.endIndex < 0) {
                return true;
            }
            return startIndex == segment.startIndex && endIndex == segment.endIndex;
        }
    }
}
