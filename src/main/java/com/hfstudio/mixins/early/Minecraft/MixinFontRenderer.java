package com.hfstudio.mixins.early.Minecraft;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hfstudio.config.ModConfig;
import com.hfstudio.render.latex.LatexRenderContext;
import com.hfstudio.render.latex.LatexRenderTracker;
import com.hfstudio.render.latex.LatexRenderer;
import com.hfstudio.render.latex.LatexTextureCache;
import com.hfstudio.render.markdown.MarkdownParser;
import com.hfstudio.render.markdown.TextSegment;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Unique
    private static final ThreadLocal<Boolean> RENDERING = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final ThreadLocal<Boolean> COMPUTING_WIDTH = ThreadLocal.withInitial(() -> false);

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
        if (RENDERING.get()) {
            return;
        }
        if (text == null || !MarkdownParser.containsMarkup(text)) {
            return;
        }

        RENDERING.set(true);
        try {
            cir.setReturnValue(renderMixed(text, x, y, color, shadow));
        } finally {
            RENDERING.set(false);
        }
    }

    @Unique
    private int renderMixed(String text, int x, int y, int color, boolean shadow) {
        List<TextSegment> segments = MarkdownParser.parseSegments(text);
        int currentX = x;

        for (TextSegment segment : segments) {
            if (!segment.isLatex()) {
                String formatted = MarkdownParser.toMinecraftFormatted(segment.content);
                if (!formatted.isEmpty()) {
                    currentX = renderString(formatted, currentX, y, color, shadow);
                }
                continue;
            }

            String sourceText = segment.toSourceText();
            if (LatexRenderContext.INSTANCE.shouldRenderSource(segment)) {
                currentX = renderString(sourceText, currentX, y, color, shadow);
                continue;
            }

            float displayHeight = segment.isDisplayLatex() ? ModConfig.render.inlineHeight * 1.5f
                : ModConfig.render.inlineHeight;

            if (shadow) {
                int shadowWidth = LatexRenderer.INSTANCE.measureLatexWidth(segment.content, displayHeight);
                if (shadowWidth > 0) {
                    currentX += shadowWidth + 1;
                } else {
                    currentX = renderString(sourceText, currentX, y, color, true);
                }
                continue;
            }

            float topY = y - (displayHeight - 8f) / 2f;
            int renderedWidth = LatexRenderer.INSTANCE.drawLatex(segment.content, currentX, topY, displayHeight);
            if (renderedWidth > 0) {
                LatexRenderTracker.INSTANCE
                    .register(segment.content, currentX, (int) topY, renderedWidth, (int) displayHeight);
                currentX += renderedWidth + 1;
                continue;
            }

            int rawStart = currentX;
            currentX = renderString(sourceText, currentX, y, color, false);
            LatexRenderTracker.INSTANCE.registerFailed(segment.content, rawStart, y, currentX - rawStart, 9);
        }

        return currentX;
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
        List<TextSegment> segments = MarkdownParser.parseSegments(text);
        int total = 0;

        for (TextSegment segment : segments) {
            if (!segment.isLatex()) {
                total += getStringWidth(MarkdownParser.toMinecraftFormatted(segment.content));
                continue;
            }

            String sourceText = segment.toSourceText();
            if (LatexRenderContext.INSTANCE.shouldRenderSource(segment)
                || LatexTextureCache.INSTANCE.hasFailed(segment.content)) {
                total += getStringWidth(sourceText);
                continue;
            }

            float displayHeight = segment.isDisplayLatex() ? ModConfig.render.inlineHeight * 1.5f
                : ModConfig.render.inlineHeight;
            int renderedWidth = LatexRenderer.INSTANCE.measureLatexWidth(segment.content, displayHeight);
            total += renderedWidth > 0 ? renderedWidth + 1 : getStringWidth(sourceText);
        }

        return total;
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
