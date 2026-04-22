package com.hfstudio.latexnh.render.markdown;

public final class TextSegment {

    private static final float DEFAULT_RENDER_SCALE = 1.0f;

    public enum SegmentType {
        PLAIN,
        LATEX_INLINE,
        LATEX_DISPLAY
    }

    public final String content;
    public final SegmentType type;
    public final int startIndex;
    public final int endIndex;
    public final String sourceText;
    public final float renderScale;

    public TextSegment(String content, SegmentType type) {
        this(content, type, -1, -1, null, DEFAULT_RENDER_SCALE);
    }

    public TextSegment(String content, SegmentType type, int startIndex, int endIndex) {
        this(content, type, startIndex, endIndex, null, DEFAULT_RENDER_SCALE);
    }

    public TextSegment(String content, SegmentType type, int startIndex, int endIndex, String sourceText,
        float renderScale) {
        this.content = content == null ? "" : content;
        this.type = type == null ? SegmentType.PLAIN : type;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.sourceText = sourceText == null ? buildDefaultSourceText(this.content, this.type) : sourceText;
        this.renderScale = renderScale > 0.0f ? renderScale : DEFAULT_RENDER_SCALE;
    }

    public boolean isLatex() {
        return type != SegmentType.PLAIN;
    }

    public boolean isDisplayLatex() {
        return type == SegmentType.LATEX_DISPLAY;
    }

    public int getDelimiterLength() {
        if (type == SegmentType.LATEX_DISPLAY) {
            return 2;
        }
        return type == SegmentType.LATEX_INLINE ? 1 : 0;
    }

    public boolean containsCursor(int cursorPos) {
        if (!isLatex() || startIndex < 0 || endIndex < 0) {
            return false;
        }
        int contentStart = startIndex + getDelimiterLength();
        return cursorPos > contentStart && cursorPos < endIndex;
    }

    public String toSourceText() {
        return sourceText;
    }

    private static String buildDefaultSourceText(String content, SegmentType type) {
        if (type == null || type == SegmentType.PLAIN) {
            return content == null ? "" : content;
        }
        String delimiter = type == SegmentType.LATEX_DISPLAY ? "$$" : "$";
        return delimiter + (content == null ? "" : content) + delimiter;
    }
}
