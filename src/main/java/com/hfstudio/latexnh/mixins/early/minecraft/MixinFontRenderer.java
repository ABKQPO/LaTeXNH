package com.hfstudio.latexnh.mixins.early.minecraft;

import net.minecraft.client.gui.FontRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.render.latex.LatexFontRenderGuards;
import com.hfstudio.latexnh.render.latex.MixedTextRenderSupport;
import com.hfstudio.latexnh.render.markdown.MarkdownParser;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Unique
    private static final ThreadLocal<Boolean> COMPUTING_WIDTH = ThreadLocal.withInitial(() -> false);

    @Shadow
    private int[] colorCode;

    @Shadow
    private int renderString(String text, int x, int y, int color, boolean shadow) {
        return 0;
    }

    @Shadow
    public abstract int getStringWidth(String text);

    @Inject(method = "renderString(Ljava/lang/String;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void latexnh$onRenderString(String text, int x, int y, int color, boolean shadow,
        CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.render.enableLatexRendering) {
            return;
        }
        if (LatexFontRenderGuards.isMixedRenderActive()) {
            return;
        }
        if (text == null || !MarkdownParser.containsMarkup(text)) {
            return;
        }

        LatexFontRenderGuards.beginMixedRender();
        LatexFontRenderGuards.scheduleSmoothFontExitBypass();
        try {
            cir.setReturnValue(
                (int) MixedTextRenderSupport
                    .renderMixed(text, x, y, color, shadow, this.colorCode, this::latexnh$renderPlainSegment));
        } finally {
            LatexFontRenderGuards.endMixedRender();
        }
    }

    @Unique
    private float latexnh$renderPlainSegment(String text, float x, float y, int color, boolean shadow) {
        return renderString(text, (int) x, (int) y, color, shadow);
    }

    @Inject(method = "getStringWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    private void latexnh$onGetStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.render.enableLatexRendering) {
            return;
        }
        if (COMPUTING_WIDTH.get()) {
            return;
        }
        if (text == null || !MarkdownParser.containsMarkup(text)) {
            return;
        }

        COMPUTING_WIDTH.set(true);
        try {
            cir.setReturnValue(computeVisualWidth(text));
        } finally {
            COMPUTING_WIDTH.set(false);
        }
    }

    @Unique
    private int computeVisualWidth(String text) {
        return MixedTextRenderSupport.measureMixed(text, this.colorCode, this::getStringWidth);
    }

    @Inject(method = "sizeStringToWidth(Ljava/lang/String;I)I", at = @At("RETURN"), cancellable = true)
    private void latexnh$sizeStringToWidth(String str, int maxWidth, CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.render.enableLatexRendering) {
            return;
        }
        if (str == null || !MarkdownParser.containsMarkup(str)) {
            return;
        }
        int adjusted = MarkdownParser.adjustSplitPoint(str, cir.getReturnValue());
        if (adjusted != cir.getReturnValue()) {
            cir.setReturnValue(adjusted);
        }
    }

    @Inject(
        method = "trimStringToWidth(Ljava/lang/String;IZ)Ljava/lang/String;",
        at = @At("RETURN"),
        cancellable = true)
    private void latexnh$trimStringToWidth(String text, int width, boolean reverse,
        CallbackInfoReturnable<String> cir) {
        if (!ModConfig.render.enableLatexRendering) {
            return;
        }
        if (text == null || !MarkdownParser.containsMarkup(text)) {
            return;
        }
        String adjusted = MarkdownParser.adjustTrimmedResult(text, cir.getReturnValue(), reverse);
        if (!adjusted.equals(cir.getReturnValue())) {
            cir.setReturnValue(adjusted);
        }
    }
}
