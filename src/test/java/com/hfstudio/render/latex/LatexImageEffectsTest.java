package com.hfstudio.render.latex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class LatexImageEffectsTest {

    @Test
    void applyOutlineExpandsImageAndKeepsCenterColor() {
        BufferedImage source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, 0xFFFFFFFF);

        BufferedImage outlined = LatexImageEffects.applyOutline(source, 0xFF000000, 1);

        assertEquals(3, outlined.getWidth());
        assertEquals(3, outlined.getHeight());
        assertEquals(0xFFFFFFFF, outlined.getRGB(1, 1));
        assertEquals(0xFF000000, outlined.getRGB(0, 0));
        assertEquals(0xFF000000, outlined.getRGB(2, 0));
        assertEquals(0xFF000000, outlined.getRGB(0, 2));
        assertEquals(0xFF000000, outlined.getRGB(2, 2));
    }
}
