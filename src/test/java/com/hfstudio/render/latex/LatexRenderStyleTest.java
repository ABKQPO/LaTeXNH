package com.hfstudio.render.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LatexRenderStyleTest {

    @Test
    void parseColorOrDefaultSupportsRgbAndArgbHex() {
        assertEquals(0xFF336699, LatexRenderStyle.parseColorOrDefault("#336699", 0));
        assertEquals(0x80336699, LatexRenderStyle.parseColorOrDefault("80336699", 0));
    }

    @Test
    void parseColorOrDefaultFallsBackForInvalidValues() {
        assertEquals(0xFFABCDEF, LatexRenderStyle.parseColorOrDefault("not-a-color", 0xFFABCDEF));
        assertEquals(0xFFABCDEF, LatexRenderStyle.parseColorOrDefault("#12345", 0xFFABCDEF));
    }
}
