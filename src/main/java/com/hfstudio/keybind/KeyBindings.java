package com.hfstudio.keybind;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;

public final class KeyBindings {

    public static final KeyBinding SHOW_LATEX = new KeyBinding(
        "key.latexnh.showLatex",
        Keyboard.KEY_LMENU,
        "key.categories.latexnh");

    public static void register() {
        ClientRegistry.registerKeyBinding(SHOW_LATEX);
    }

    public static boolean isShowLatexDown() {
        return Keyboard.isKeyDown(SHOW_LATEX.getKeyCode());
    }

    private KeyBindings() {}
}
