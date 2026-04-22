package com.hfstudio.latexnh.mixins.early.compat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.render.latex.LatexFontRenderGuards;
import com.hfstudio.latexnh.render.latex.MixedTextRenderSupport;
import com.hfstudio.latexnh.render.markdown.MarkdownParser;

@Pseudo
@Mixin(targets = "com.gtnewhorizons.angelica.client.font.BatchingFontRenderer", remap = false)
public abstract class MixinAngelicaBatchingFontRenderer {

    @Final
    @Shadow
    private int[] colorCode;

    @Shadow
    public abstract float drawString(float anchorX, float anchorY, int color, boolean enableShadow, boolean unicodeFlag,
        CharSequence string, int stringOffset, int stringLength);

    @Inject(method = "drawString(FFIZZLjava/lang/CharSequence;II)F", at = @At("HEAD"), cancellable = true)
    private void latexnh$drawMixed(float anchorX, float anchorY, int color, boolean enableShadow, boolean unicodeFlag,
        CharSequence string, int stringOffset, int stringLength, CallbackInfoReturnable<Float> cir) {
        if (!ModConfig.render.enableLatexRendering) {
            return;
        }
        if (LatexFontRenderGuards.isMixedRenderActive()) {
            return;
        }

        String text = latexnh$extractText(string, stringOffset, stringLength);
        if (text.isEmpty() || !MarkdownParser.containsMarkup(text)) {
            return;
        }

        LatexFontRenderGuards.beginMixedRender();
        try {
            float endX = MixedTextRenderSupport.renderMixed(
                text,
                anchorX,
                anchorY,
                color,
                enableShadow,
                true,
                this.colorCode,
                (segmentText, x, y, drawColor,
                    shadow) -> latexnh$renderPlain(segmentText, x, y, drawColor, shadow, unicodeFlag));
            cir.setReturnValue(endX + (enableShadow ? 1.0f : 0.0f));
        } finally {
            LatexFontRenderGuards.endMixedRender();
        }
    }

    @Unique
    private String latexnh$extractText(CharSequence string, int stringOffset, int stringLength) {
        if (string == null) {
            return "";
        }

        int totalLength = string.length();
        int start = Math.max(0, Math.min(stringOffset, totalLength));
        int end = Math.max(start, Math.min(start + stringLength, totalLength));
        return string.subSequence(start, end)
            .toString();
    }

    @Unique
    private float latexnh$renderPlain(String text, float anchorX, float anchorY, int color, boolean enableShadow,
        boolean unicodeFlag) {
        float rendered = drawString(anchorX, anchorY, color, enableShadow, unicodeFlag, text, 0, text.length());
        return enableShadow ? rendered - 1.0f : rendered;
    }
}
