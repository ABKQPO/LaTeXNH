package com.hfstudio.render.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class LatexFormattingParserTest {

    @Test
    void parseRunsResetsToConfiguredDefaultColor() {
        List<LatexFormattingParser.FormattedRun> runs = LatexFormattingParser
            .parseRuns("x\u00a7cy\u00a7rz", 0xFF336699);

        assertEquals(3, runs.size());
        assertEquals("x", runs.get(0).text);
        assertEquals(0xFF336699, runs.get(0).argb);
        assertEquals("y", runs.get(1).text);
        assertEquals(0xFFFF5555, runs.get(1).argb);
        assertEquals("z", runs.get(2).text);
        assertEquals(0xFF336699, runs.get(2).argb);
    }

    @Test
    void toRenderableFormulaKeepsConfiguredDefaultColorAsBase() {
        String rendered = LatexFormattingParser.toRenderableFormula("x\u00a7cy", 0xFF336699);

        assertTrue(rendered.contains("\\definecolor{latexnhcolor0}{rgb}{1,0.3333,0.3333}"));
        assertTrue(rendered.endsWith("x\\textcolor{latexnhcolor0}{y}"));
    }

    @Test
    void parseRunsUsesProvidedPaletteAndResetsToOuterColor() {
        int[] palette = new int[32];
        palette[1] = 0x00112233;

        List<LatexFormattingParser.FormattedRun> runs = LatexFormattingParser
            .parseRuns("x\u00a71y\u00a7rz", 0x80445566, palette);

        assertEquals(3, runs.size());
        assertEquals("x", runs.get(0).text);
        assertEquals(0x80445566, runs.get(0).argb);
        assertEquals("y", runs.get(1).text);
        assertEquals(0x80112233, runs.get(1).argb);
        assertEquals("z", runs.get(2).text);
        assertEquals(0x80445566, runs.get(2).argb);
    }

    @Test
    void toRenderableFormulaUsesProvidedPaletteColor() {
        int[] palette = new int[32];
        palette[1] = 0x00112233;

        String rendered = LatexFormattingParser.toRenderableFormula("x\u00a71y", 0xFF336699, palette);

        assertTrue(rendered.contains("\\definecolor{latexnhcolor0}{rgb}{0.0667,0.1333,0.2}"));
        assertTrue(rendered.endsWith("x\\textcolor{latexnhcolor0}{y}"));
    }
}
