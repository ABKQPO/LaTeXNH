package com.hfstudio.latexnh.render.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
