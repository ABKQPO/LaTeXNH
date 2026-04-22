package com.hfstudio.latexnh.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.IModGuiFactory;

public class LaTeXNHGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft mcInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return com.hfstudio.latexnh.config.LaTeXNHGuiConfig.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
