package com.hfstudio.latexnh.render.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.scilab.forge.jlatexmath.TeXFormula;

class LatexFontResolverTest {

    @Test
    void resolveUsesControlledSansSerifType() {
        LatexFontResolver.Selection selection = LatexFontResolver
            .resolve(LatexFontFamily.SANS_SERIF, "", availableFonts("Dialog"), () -> null);

        assertEquals(TeXFormula.SANSSERIF, selection.getTeXType());
        assertNull(selection.getExternalFontFamily());
        assertEquals("sans_serif", selection.getCacheToken());
        assertFalse(selection.usesExternalFont());
    }

    @Test
    void resolveUsesSystemFontWhenItExists() {
        LatexFontResolver.Selection selection = LatexFontResolver
            .resolve(LatexFontFamily.SYSTEM, "Noto Sans SC", availableFonts("Dialog", "Noto Sans SC"), () -> null);

        assertEquals("Noto Sans SC", selection.getExternalFontFamily());
        assertTrue(selection.usesExternalFont());
        assertEquals("system:Noto Sans SC", selection.getCacheToken());
    }

    @Test
    void resolveFallsBackToDefaultWhenSystemFontIsMissing() {
        LatexFontResolver.Selection selection = LatexFontResolver
            .resolve(LatexFontFamily.SYSTEM, "Missing Font", availableFonts("Dialog"), () -> null);

        assertEquals("default", selection.getCacheToken());
        assertNull(selection.getExternalFontFamily());
        assertEquals(-1, selection.getTeXType());
        assertFalse(selection.usesExternalFont());
    }

    @Test
    void resolveUsesBundledUnifontWhenAvailable() {
        LatexFontResolver.Selection selection = LatexFontResolver
            .resolve(LatexFontFamily.UNIFONT, "", availableFonts("Dialog"), () -> "Unifont");

        assertEquals("Unifont", selection.getExternalFontFamily());
        assertTrue(selection.usesExternalFont());
        assertEquals("unifont:Unifont", selection.getCacheToken());
    }

    private static Set<String> availableFonts(String... fonts) {
        return new HashSet<>(Arrays.asList(fonts));
    }
}
