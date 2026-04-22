package com.hfstudio.latexnh.render.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class MarkdownParserTest {

    @Test
    void adjustSplitPointSkipsFormulaWhenOnlyFormattingPrefixesIt() {
        assertEquals(7, MarkdownParser.adjustSplitPoint("\u00a7a$123$", 4));
    }

    @Test
    void adjustSplitPointStillBacksUpWhenVisibleTextPrefixesFormula() {
        assertEquals(4, MarkdownParser.adjustSplitPoint("ab\u00a7a$123$", 6));
    }

    @Test
    void parseSegmentsRecognizesScaleSuffixAsLatexMetadata() {
        List<TextSegment> segments = MarkdownParser.parseSegments("ab$x$[scale=1.5]cd");

        assertEquals(3, segments.size());
        assertEquals("x", segments.get(1).content);
        assertEquals(
            "$x$[scale=1.5]",
            segments.get(1)
                .toSourceText());
        assertEquals(1.5f, segments.get(1).renderScale, 0.0001f);
    }

    @Test
    void parseSegmentsKeepsMalformedScaleSuffixAsPlainText() {
        List<TextSegment> segments = MarkdownParser.parseSegments("$x$[scale=nope]");

        assertEquals(2, segments.size());
        assertEquals(
            "$x$",
            segments.get(0)
                .toSourceText());
        assertEquals(1.0f, segments.get(0).renderScale, 0.0001f);
        assertEquals("[scale=nope]", segments.get(1).content);
    }

    @Test
    void getLatexSegmentAtCursorSupportsSingleCharacterFormula() {
        TextSegment segment = MarkdownParser.getLatexSegmentAtCursor("$1$", 2);

        assertNotNull(segment);
        assertEquals("1", segment.content);
    }
}
