package com.hfstudio.latexnh.render.latex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MixedTextRenderSupportTest {

    @Test
    void vanillaShadowPassSkipsLatexButAngelicaCombinedPassRendersIt() {
        assertFalse(MixedTextRenderSupport.shouldRenderLatexInCurrentPass(true, false));
        assertTrue(MixedTextRenderSupport.shouldRenderLatexInCurrentPass(true, true));
        assertTrue(MixedTextRenderSupport.shouldRenderLatexInCurrentPass(false, false));
    }
}
