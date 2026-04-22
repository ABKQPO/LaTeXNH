package com.hfstudio.latexnh.render.latex;

import java.util.List;

import com.hfstudio.latexnh.render.markdown.MarkdownParser;
import com.hfstudio.latexnh.render.markdown.TextSegment;

public final class TextFieldLatexPreview {

    @FunctionalInterface
    public interface WidthMeasurer {

        int measure(String text);
    }

    public static final class Plan {

        public final boolean inlinePreview;
        public final boolean renderedTooltipOnHover;
        public final int prefixWidth;
        public final TextSegment anchorSegment;
        public final int visibleStartIndex;
        public final String visibleText;

        private Plan(boolean inlinePreview, boolean renderedTooltipOnHover, int prefixWidth, TextSegment anchorSegment,
            int visibleStartIndex, String visibleText) {
            this.inlinePreview = inlinePreview;
            this.renderedTooltipOnHover = renderedTooltipOnHover;
            this.prefixWidth = prefixWidth;
            this.anchorSegment = anchorSegment;
            this.visibleStartIndex = visibleStartIndex;
            this.visibleText = visibleText;
        }

        public static Plan none() {
            return new Plan(false, false, 0, null, 0, "");
        }
    }

    private TextFieldLatexPreview() {}

    public static int resolvePreviewCursor(boolean focused, int cursorPosition, int visibleStartIndex) {
        return focused ? cursorPosition : visibleStartIndex;
    }

    public static Plan plan(String visibleText, TextSegment visibleSegment, WidthMeasurer widthMeasurer,
        int renderedLatexWidth, int availableWidth) {
        if (visibleSegment == null || !visibleSegment.isLatex()) {
            return Plan.none();
        }

        int prefixWidth = widthMeasurer.measure(visibleText.substring(0, visibleSegment.startIndex));
        int suffixWidth = widthMeasurer.measure(visibleText.substring(visibleSegment.endIndex));
        boolean inlinePreview = renderedLatexWidth > 0
            && prefixWidth + renderedLatexWidth + suffixWidth <= availableWidth;
        return new Plan(
            inlinePreview,
            !inlinePreview && renderedLatexWidth > 0,
            prefixWidth,
            visibleSegment,
            0,
            visibleText);
    }

    public static Plan plan(String text, int cursorPosition, int rawVisibleStartIndex, String rawVisibleText,
        WidthMeasurer widthMeasurer, int availableWidth) {
        if (text == null || text.isEmpty()) {
            return Plan.none();
        }

        String safeVisibleText = rawVisibleText == null ? "" : rawVisibleText;
        int safeVisibleStart = clamp(rawVisibleStartIndex, 0, text.length());
        int safeCursor = clamp(cursorPosition, 0, text.length());
        int safeVisibleEnd = Math.min(text.length(), safeVisibleStart + safeVisibleText.length());

        TextSegment anchorSegment = findBoundarySegment(text, safeCursor, safeVisibleStart, safeVisibleEnd);
        if (anchorSegment == null) {
            return new Plan(false, false, 0, null, safeVisibleStart, safeVisibleText);
        }

        if (anchorSegment.containsCursor(safeCursor)) {
            return new Plan(false, true, 0, anchorSegment, safeVisibleStart, safeVisibleText);
        }

        int previewStart = Math.min(anchorSegment.startIndex, safeCursor);
        int previewEnd = Math.max(anchorSegment.endIndex, safeCursor);
        if (previewStart >= previewEnd) {
            return new Plan(false, true, 0, anchorSegment, safeVisibleStart, safeVisibleText);
        }

        if (widthMeasurer.measure(text.substring(previewStart, previewEnd)) > availableWidth) {
            return new Plan(false, true, 0, anchorSegment, safeVisibleStart, safeVisibleText);
        }

        while (previewEnd < text.length()
            && widthMeasurer.measure(text.substring(previewStart, previewEnd + 1)) <= availableWidth) {
            previewEnd++;
        }

        while (previewStart > 0
            && widthMeasurer.measure(text.substring(previewStart - 1, previewEnd)) <= availableWidth) {
            previewStart--;
        }

        return new Plan(true, false, 0, anchorSegment, previewStart, text.substring(previewStart, previewEnd));
    }

    private static TextSegment findBoundarySegment(String text, int cursorPosition, int visibleStart, int visibleEnd) {
        List<TextSegment> segments = MarkdownParser.parseSegments(text);
        TextSegment best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (TextSegment segment : segments) {
            if (!segment.isLatex() || !isClippedByVisibleWindow(segment, visibleStart, visibleEnd)) {
                continue;
            }

            int distance = distanceToCursor(segment, cursorPosition);
            if (best == null || distance < bestDistance
                || (distance == bestDistance && segment.endIndex > best.endIndex)) {
                best = segment;
                bestDistance = distance;
            }
        }

        return best;
    }

    private static boolean isClippedByVisibleWindow(TextSegment segment, int visibleStart, int visibleEnd) {
        boolean overlaps = segment.startIndex < visibleEnd && segment.endIndex > visibleStart;
        if (!overlaps) {
            return false;
        }
        return visibleStart > segment.startIndex || visibleEnd < segment.endIndex;
    }

    private static int distanceToCursor(TextSegment segment, int cursorPosition) {
        if (cursorPosition >= segment.startIndex && cursorPosition <= segment.endIndex) {
            return 0;
        }
        return Math.min(Math.abs(cursorPosition - segment.startIndex), Math.abs(cursorPosition - segment.endIndex));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
