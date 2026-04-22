package com.hfstudio.latexnh.render.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.RenderingHints;

import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL11;

class LatexRenderTuningTest {

    @Test
    void textureFilteringMapsToExpectedOpenGlFilters() {
        assertEquals(GL11.GL_LINEAR, LatexTextureFiltering.LINEAR.getGlConstant());
        assertEquals(GL11.GL_NEAREST, LatexTextureFiltering.NEAREST.getGlConstant());
    }

    @Test
    void hintModeMapsToExpectedAwtHints() {
        assertSame(RenderingHints.VALUE_ANTIALIAS_DEFAULT, LatexHintMode.DEFAULT.toGeneralAntialiasHint());
        assertSame(RenderingHints.VALUE_ANTIALIAS_ON, LatexHintMode.ON.toGeneralAntialiasHint());
        assertSame(RenderingHints.VALUE_ANTIALIAS_OFF, LatexHintMode.OFF.toGeneralAntialiasHint());

        assertSame(RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, LatexHintMode.DEFAULT.toTextAntialiasHint());
        assertSame(RenderingHints.VALUE_TEXT_ANTIALIAS_ON, LatexHintMode.ON.toTextAntialiasHint());
        assertSame(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, LatexHintMode.OFF.toTextAntialiasHint());
    }

    @Test
    void renderQualityMapsToExpectedAwtQualityHints() {
        assertSame(RenderingHints.VALUE_RENDER_QUALITY, LatexRenderQuality.QUALITY.toRenderingHint());
        assertSame(RenderingHints.VALUE_RENDER_DEFAULT, LatexRenderQuality.BALANCED.toRenderingHint());
        assertSame(RenderingHints.VALUE_RENDER_SPEED, LatexRenderQuality.SPEED.toRenderingHint());

        assertSame(
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY,
            LatexRenderQuality.QUALITY.toAlphaInterpolationHint());
        assertSame(
            RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT,
            LatexRenderQuality.BALANCED.toAlphaInterpolationHint());
        assertSame(RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED, LatexRenderQuality.SPEED.toAlphaInterpolationHint());

        assertSame(RenderingHints.VALUE_COLOR_RENDER_QUALITY, LatexRenderQuality.QUALITY.toColorRenderingHint());
        assertSame(RenderingHints.VALUE_COLOR_RENDER_DEFAULT, LatexRenderQuality.BALANCED.toColorRenderingHint());
        assertSame(RenderingHints.VALUE_COLOR_RENDER_SPEED, LatexRenderQuality.SPEED.toColorRenderingHint());
    }

    @Test
    void cacheKeyChangesWhenRenderTuningChanges() {
        LatexRenderStyle nearestStyle = new LatexRenderStyle(
            0xFFFFFFFF,
            0xFF000000,
            2,
            "default",
            true,
            60.0f,
            LatexTextureFiltering.NEAREST,
            LatexHintMode.ON,
            LatexHintMode.ON,
            LatexRenderQuality.QUALITY,
            true);
        LatexRenderStyle linearStyle = new LatexRenderStyle(
            0xFFFFFFFF,
            0xFF000000,
            2,
            "default",
            true,
            72.0f,
            LatexTextureFiltering.LINEAR,
            LatexHintMode.OFF,
            LatexHintMode.DEFAULT,
            LatexRenderQuality.BALANCED,
            false);

        org.junit.jupiter.api.Assertions.assertNotEquals(nearestStyle.cacheKey(), linearStyle.cacheKey());
    }
}
