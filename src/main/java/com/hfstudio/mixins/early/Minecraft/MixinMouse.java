package com.hfstudio.mixins.early.Minecraft;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hfstudio.tooltip.TooltipState;

@Mixin(value = Mouse.class, remap = false)
public class MixinMouse {

    @Inject(method = "getX", at = @At("HEAD"), cancellable = true)
    private static void latexnh$freezeMouseX(CallbackInfoReturnable<Integer> cir) {
        if (TooltipState.INSTANCE.isMouseOverrideActive() && TooltipState.INSTANCE.isMouseFrozen()) {
            cir.setReturnValue(Integer.valueOf(TooltipState.INSTANCE.getLogicalRawMouseX()));
        }
    }

    @Inject(method = "getY", at = @At("HEAD"), cancellable = true)
    private static void latexnh$freezeMouseY(CallbackInfoReturnable<Integer> cir) {
        if (TooltipState.INSTANCE.isMouseOverrideActive() && TooltipState.INSTANCE.isMouseFrozen()) {
            cir.setReturnValue(Integer.valueOf(TooltipState.INSTANCE.getLogicalRawMouseY()));
        }
    }

    @Inject(method = "getEventX", at = @At("HEAD"), cancellable = true)
    private static void latexnh$freezeEventX(CallbackInfoReturnable<Integer> cir) {
        if (TooltipState.INSTANCE.isMouseOverrideActive() && TooltipState.INSTANCE.isMouseFrozen()) {
            cir.setReturnValue(Integer.valueOf(TooltipState.INSTANCE.getLogicalRawMouseX()));
        }
    }

    @Inject(method = "getEventY", at = @At("HEAD"), cancellable = true)
    private static void latexnh$freezeEventY(CallbackInfoReturnable<Integer> cir) {
        if (TooltipState.INSTANCE.isMouseOverrideActive() && TooltipState.INSTANCE.isMouseFrozen()) {
            cir.setReturnValue(Integer.valueOf(TooltipState.INSTANCE.getLogicalRawMouseY()));
        }
    }
}
