package com.hfstudio.latexnh.config;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.hfstudio.latexnh.LaTeXNH;
import com.hfstudio.latexnh.ModConfig;

public class LaTeXNHGuiConfig extends SimpleGuiConfig {

    public LaTeXNHGuiConfig(GuiScreen parentScreen) {
        super(parentScreen, LaTeXNH.MODID, LaTeXNH.MODNAME, false, ModConfig.class);
    }
}
