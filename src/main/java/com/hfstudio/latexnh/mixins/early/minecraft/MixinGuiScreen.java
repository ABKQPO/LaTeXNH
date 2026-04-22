package com.hfstudio.latexnh.mixins.early.minecraft;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.tooltip.TooltipState;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void latexnh$clearTooltipStateWhenDisabled(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!ModConfig.render.enableLatexRendering) {
            TooltipState.INSTANCE.clear();
        }
    }
}
