package com.hfstudio.latexnh.config;

import com.hfstudio.latexnh.LaTeXNH;
import com.hfstudio.latexnh.render.latex.LatexFontResolver;
import com.hfstudio.latexnh.render.latex.LatexTextureCache;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class LatexConfigChangeHandler {

    public static final LatexConfigChangeHandler INSTANCE = new LatexConfigChangeHandler();

    private LatexConfigChangeHandler() {}

    public static void register() {
        FMLCommonHandler.instance()
            .bus()
            .register(INSTANCE);
    }

    @SubscribeEvent
    public void onConfigSaved(ConfigChangedEvent.PostConfigChangedEvent event) {
        handleConfigSaved(event.modID, LatexTextureCache.INSTANCE::clearAll, LatexFontResolver::invalidateRuntimeState);
    }

    public static boolean handleConfigSaved(String modId, Runnable cacheInvalidator, Runnable fontInvalidator) {
        if (!LaTeXNH.MODID.equalsIgnoreCase(modId)) {
            return false;
        }

        fontInvalidator.run();
        cacheInvalidator.run();
        return true;
    }
}
