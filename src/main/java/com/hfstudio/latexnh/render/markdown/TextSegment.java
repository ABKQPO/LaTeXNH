package com.hfstudio.latexnh.render.markdown;

public final class TextSegment {

    public enum SegmentType {
        PLAIN,
        LATEX_INLINE,
        LATEX_DISPLAY
    }

    public final String content;
    public final SegmentType type;
    public final int startIndex;
    public final int endIndex;

    public TextSegment(String content, SegmentType type) {
        this(content, type, -1, -1);
    }

    public TextSegment(String content, SegmentType type, int startIndex, int endIndex) {
        this.content = content;
        this.type = type;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
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
        if (!isLatex()) {
            return false;
        }
        int contentStart = startIndex + getDelimiterLength();
        int contentEnd = endIndex - getDelimiterLength();
        return cursorPos > contentStart && cursorPos < contentEnd;
    }

    public String toSourceText() {
        if (!isLatex()) {
            return content;
        }
        String delimiter = isDisplayLatex() ? "$$" : "$";
        return delimiter + content + delimiter;
    }
}
