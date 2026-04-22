package com.hfstudio.latexnh.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class LatexConfigChangeHandlerTest {

    @Test
    void onlyRefreshesLatexnhConfigSaves() {
        AtomicInteger cacheInvalidations = new AtomicInteger();
        AtomicInteger fontInvalidations = new AtomicInteger();

        boolean handled = LatexConfigChangeHandler
            .handleConfigSaved("othermod", cacheInvalidations::incrementAndGet, fontInvalidations::incrementAndGet);

        assertFalse(handled);
        assertEquals(0, cacheInvalidations.get());
        assertEquals(0, fontInvalidations.get());
    }

    @Test
    void refreshesCacheAndFontsForLatexnhConfigSaves() {
        AtomicInteger cacheInvalidations = new AtomicInteger();
        AtomicInteger fontInvalidations = new AtomicInteger();

        boolean handled = LatexConfigChangeHandler
            .handleConfigSaved("latexnh", cacheInvalidations::incrementAndGet, fontInvalidations::incrementAndGet);

        assertTrue(handled);
        assertEquals(1, cacheInvalidations.get());
        assertEquals(1, fontInvalidations.get());
    }
}
