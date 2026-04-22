package com.hfstudio.latexnh.mixins.early.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.latexnh.render.latex.LatexFontRenderGuards;

@Pseudo
@Mixin(targets = "bre.smoothfont.FontRendererHook", remap = false)
public abstract class MixinSmoothFontFontRendererHook {

    @Inject(method = "renderStringExitHook(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void latexnh$skipFalseDisable(String text, CallbackInfo ci) {
        if (LatexFontRenderGuards.consumeSmoothFontExitBypass()) {
            ci.cancel();
        }
    }
}
