package com.hfstudio.config;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.hfstudio.LaTeXNH;

public class LaTeXNHGuiConfig extends SimpleGuiConfig {

    public LaTeXNHGuiConfig(GuiScreen parentScreen) {
        super(parentScreen, LaTeXNH.MODID, LaTeXNH.MODNAME, false, ModConfig.class);
    }
}
