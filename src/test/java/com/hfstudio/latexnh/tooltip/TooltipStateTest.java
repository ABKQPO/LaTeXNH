package com.hfstudio.latexnh.tooltip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.hfstudio.latexnh.render.latex.LatexRenderEntry;

class TooltipStateTest {

    @AfterEach
    void tearDown() {
        TooltipState.INSTANCE.clear();
    }

    @Test
    void beginFrameClearsInlineTooltipFlag() {
        TooltipState.INSTANCE.markInlineTooltipRendered();

        assertTrue(TooltipState.INSTANCE.hasInlineTooltipRendered());

        TooltipState.INSTANCE.beginFrame();

        assertFalse(TooltipState.INSTANCE.hasInlineTooltipRendered());
    }

    @Test
    void resolveStableHoveredEntryKeepsRecentHitAcrossSingleMiss() {
        LatexRenderEntry entry = new LatexRenderEntry("x", 10, 20, 30, 12, false);

        assertSame(entry, TooltipState.INSTANCE.resolveStableHoveredEntry(entry, 15, 25));
        assertSame(entry, TooltipState.INSTANCE.resolveStableHoveredEntry(null, 15, 25));
        assertNull(TooltipState.INSTANCE.resolveStableHoveredEntry(null, 200, 200));
    }

    @Test
    void storesSelectedLatexTooltipRequestUntilNextFrame() {
        TooltipState.INSTANCE.requestSelectedLatexTooltip("x", 1.5f, 12, 18, true);

        TooltipState.SelectedTooltipRequest request = TooltipState.INSTANCE.getSelectedTooltipRequest();
        assertNotNull(request);
        assertTrue(request.renderLatex());
        assertEquals("x", request.formula());
        assertEquals(1.5f, request.renderScale(), 0.0001f);
        assertEquals(12, request.anchorX());
        assertEquals(18, request.anchorY());
        assertTrue(request.followMouse());

        TooltipState.INSTANCE.beginFrame();
        assertNull(TooltipState.INSTANCE.getSelectedTooltipRequest());
    }

    @Test
    void storesSelectedTextTooltipRequest() {
        TooltipState.INSTANCE.requestSelectedTextTooltip(Arrays.asList("line1", "line2"), 3, 4, false);

        TooltipState.SelectedTooltipRequest request = TooltipState.INSTANCE.getSelectedTooltipRequest();
        assertNotNull(request);
        assertFalse(request.renderLatex());
        assertEquals(Arrays.asList("line1", "line2"), request.textLines());
        assertEquals(1.0f, request.renderScale(), 0.0001f);
        assertEquals(3, request.anchorX());
        assertEquals(4, request.anchorY());
        assertFalse(request.followMouse());
    }
}
