package com.hfstudio.render.latex;

import com.hfstudio.LaTeXNH;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public final class LatexCacheCleanupHandler {

    public static final LatexCacheCleanupHandler INSTANCE = new LatexCacheCleanupHandler();

    private boolean pendingCleanup = false;

    private LatexCacheCleanupHandler() {}

    public static void register() {
        FMLCommonHandler.instance()
            .bus()
            .register(INSTANCE);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!pendingCleanup) return;

        pendingCleanup = false;
        LaTeXNH.LOG.info("[LaTeXNH] Clearing LaTeX GL texture cache...");
        LatexTextureCache.INSTANCE.clearAll();
    }

    public void scheduleClear() {
        pendingCleanup = true;
    }
}
