package com.hfstudio.latexnh.mixins.early.compat;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.render.latex.LatexRenderContext;
import com.hfstudio.latexnh.render.latex.LatexRenderer;
import com.hfstudio.latexnh.render.latex.LatexTextureCache;
import com.hfstudio.latexnh.render.latex.TextFieldLatexPreview;
import com.hfstudio.latexnh.render.markdown.MarkdownParser;
import com.hfstudio.latexnh.render.markdown.TextSegment;
import com.hfstudio.latexnh.tooltip.TooltipRenderer;

@Pseudo
@Mixin(targets = "codechicken.nei.FormattedTextField", remap = false)
public abstract class MixinNEIFormattedTextField extends GuiTextField {

    @Unique
    private TextFieldLatexPreview.Plan latexnh$boundaryPreviewPlan;

    @Unique
    private int latexnh$originalLineScrollOffset;

    @Unique
    private boolean latexnh$lineScrollOffsetOverridden;

    @Shadow
    protected FontRenderer fontRenderer;

    @Shadow
    protected int lineScrollOffset;

    public MixinNEIFormattedTextField(FontRenderer p_i1032_1_, int p_i1032_2_, int p_i1032_3_, int p_i1032_4_,
        int p_i1032_5_) {
        super(p_i1032_1_, p_i1032_2_, p_i1032_3_, p_i1032_4_, p_i1032_5_);
    }

    @Inject(method = "drawTextBox", at = @At("HEAD"))
    private void latexnh$beforeDrawTextBox(CallbackInfo ci) {
        LatexRenderContext.INSTANCE.clearRenderSlice();
        latexnh$boundaryPreviewPlan = null;
        latexnh$originalLineScrollOffset = lineScrollOffset;
        latexnh$lineScrollOffsetOverridden = false;

        String currentText = getText();
        if (!ModConfig.render.enableLatexRendering || currentText == null || currentText.isEmpty()) {
            LatexRenderContext.INSTANCE.clearEditingFormula();
            return;
        }

        TextSegment activeSegment = isFocused()
            ? MarkdownParser.getLatexSegmentAtCursor(currentText, getCursorPosition())
            : null;
        if (activeSegment != null) {
            LatexRenderContext.INSTANCE.setEditingFormula(activeSegment, true);
            return;
        }

        latexnh$boundaryPreviewPlan = latexnh$buildBoundaryPreviewPlan(currentText);
        if (latexnh$boundaryPreviewPlan.inlinePreview) {
            lineScrollOffset = latexnh$boundaryPreviewPlan.visibleStartIndex;
            latexnh$lineScrollOffsetOverridden = true;
        }

        LatexRenderContext.INSTANCE.clearEditingFormula();
    }

    @Inject(method = "drawTextBox", at = @At("RETURN"))
    private void latexnh$afterDrawTextBox(CallbackInfo ci) {
        try {
            String currentText = getText();
            TextSegment activeSegment = !isFocused() || currentText == null ? null
                : MarkdownParser.getLatexSegmentAtCursor(currentText, getCursorPosition());

            LatexRenderContext.INSTANCE.clearEditingFormula();
            LatexRenderContext.INSTANCE.clearRenderSlice();
            if (!ModConfig.render.enableLatexRendering || !isFocused()
                || currentText == null
                || currentText.isEmpty()
                || activeSegment == null) {
                return;
            }

            LatexRenderer.INSTANCE.getOrCreateTexture(activeSegment.content);
            if (!LatexTextureCache.INSTANCE.hasFailed(activeSegment.content)) {
                return;
            }

            String errorInfo = LatexTextureCache.INSTANCE.getError(activeSegment.content);
            TooltipRenderer.INSTANCE.renderTextTooltip(
                MarkdownParser.buildErrorLines(activeSegment.content, errorInfo),
                xPosition + 2,
                yPosition - 4);
        } finally {
            if (latexnh$lineScrollOffsetOverridden) {
                lineScrollOffset = latexnh$originalLineScrollOffset;
            }
            latexnh$boundaryPreviewPlan = null;
            latexnh$lineScrollOffsetOverridden = false;
        }
    }

    @Redirect(
        method = "drawTextBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;trimStringToWidth(Ljava/lang/String;I)Ljava/lang/String;",
            ordinal = 1),
        require = 0)
    private String latexnh$useBoundaryPreviewVisibleText(FontRenderer fontRenderer, String value, int width) {
        if (latexnh$boundaryPreviewPlan != null && latexnh$boundaryPreviewPlan.inlinePreview) {
            return latexnh$boundaryPreviewPlan.visibleText;
        }
        return fontRenderer.trimStringToWidth(value, width);
    }

    @Redirect(
        method = "drawTextBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;III)I",
            ordinal = 1),
        require = 0)
    private int latexnh$drawPrefixWithRenderSlice(FontRenderer fontRenderer, String value, int x, int y, int color) {
        return latexnh$drawStringWithSlice(fontRenderer, value, x, y, color, lineScrollOffset);
    }

    @Redirect(
        method = "drawTextBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;III)I",
            ordinal = 2),
        require = 0)
    private int latexnh$drawSuffixWithRenderSlice(FontRenderer fontRenderer, String value, int x, int y, int color) {
        return latexnh$drawStringWithSlice(fontRenderer, value, x, y, color, getCursorPosition());
    }

    @Redirect(
        method = "drawTextBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I"),
        require = 0)
    private int latexnh$getStringWidthWithRenderSlice(FontRenderer fontRenderer, String value) {
        LatexRenderContext.INSTANCE.setRenderSlice(value, lineScrollOffset);
        try {
            return fontRenderer.getStringWidth(value);
        } finally {
            LatexRenderContext.INSTANCE.clearRenderSlice();
        }
    }

    @Unique
    private int latexnh$drawStringWithSlice(FontRenderer fontRenderer, String value, int x, int y, int color,
        int baseOffset) {
        LatexRenderContext.INSTANCE.setRenderSlice(value, baseOffset);
        try {
            return fontRenderer.drawStringWithShadow(value, x, y, color);
        } finally {
            LatexRenderContext.INSTANCE.clearRenderSlice();
        }
    }

    @Unique
    private TextFieldLatexPreview.Plan latexnh$buildBoundaryPreviewPlan(String currentText) {
        int previewCursorPosition = TextFieldLatexPreview
            .resolvePreviewCursor(isFocused(), getCursorPosition(), lineScrollOffset);
        String rawVisibleText = fontRenderer.trimStringToWidth(currentText.substring(lineScrollOffset), getWidth());
        return TextFieldLatexPreview.plan(
            currentText,
            previewCursorPosition,
            lineScrollOffset,
            rawVisibleText,
            fontRenderer::getStringWidth,
            getWidth());
    }

}
