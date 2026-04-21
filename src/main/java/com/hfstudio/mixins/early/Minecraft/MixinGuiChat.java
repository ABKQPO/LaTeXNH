package com.hfstudio.mixins.early.Minecraft;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.config.ModConfig;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Shadow
    protected GuiTextField inputField;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void latexnh$onInitGui(CallbackInfo ci) {
        if (inputField == null) return;
        inputField.setMaxStringLength(ModConfig.render.chatInputMaxLength);
    }
}
