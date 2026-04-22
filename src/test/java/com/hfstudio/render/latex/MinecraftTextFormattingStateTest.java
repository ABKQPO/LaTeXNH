package com.hfstudio.render.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MinecraftTextFormattingStateTest {

    @Test
    void appliesColorAndStyleCodesToCurrentState() {
        MinecraftTextFormattingState state = new MinecraftTextFormattingState(0x80336699);

        state.apply("\u00a7aGreen\u00a7lBold");

        assertEquals(0x8055FF55, state.getCurrentColorArgb());
        assertEquals("\u00a7a\u00a7l", state.buildFormattingPrefix());
    }

    @Test
    void colorCodesClearPreviousStyleFlags() {
        MinecraftTextFormattingState state = new MinecraftTextFormattingState(0xFF112233);

        state.apply("\u00a7lBold\u00a7cRed");

        assertEquals(0xFFFF5555, state.getCurrentColorArgb());
        assertEquals("\u00a7c", state.buildFormattingPrefix());
    }

    @Test
    void resetRestoresBaseColorAndClearsFormattingPrefix() {
        MinecraftTextFormattingState state = new MinecraftTextFormattingState(0x80445566);

        state.apply("\u00a7a\u00a7lText\u00a7r");

        assertEquals(0x80445566, state.getCurrentColorArgb());
        assertEquals("", state.buildFormattingPrefix());
    }

    @Test
    void usesProvidedPaletteWhilePreservingBaseAlpha() {
        int[] palette = new int[32];
        palette[1] = 0x00ABCDEF;
        MinecraftTextFormattingState state = new MinecraftTextFormattingState(0x80445566, palette);

        state.apply("\u00a71Blue");

        assertEquals(0x80ABCDEF, state.getCurrentColorArgb());
        assertEquals("\u00a71", state.buildFormattingPrefix());
    }
}
