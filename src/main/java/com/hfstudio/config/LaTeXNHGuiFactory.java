package com.hfstudio.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.IModGuiFactory;

public class LaTeXNHGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft mcInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return LaTeXNHGuiConfig.class;
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
