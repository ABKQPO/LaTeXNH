package com.hfstudio.latexnh.mixins.early.minecraft;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.keybind.KeyBindings;
import com.hfstudio.latexnh.render.latex.LatexRenderContext;
import com.hfstudio.latexnh.render.latex.LatexRenderer;
import com.hfstudio.latexnh.render.latex.LatexTextureCache;
import com.hfstudio.latexnh.render.latex.TextFieldLatexPreview;
import com.hfstudio.latexnh.render.markdown.MarkdownParser;
import com.hfstudio.latexnh.render.markdown.TextSegment;
import com.hfstudio.latexnh.tooltip.LatexTooltipMode;
import com.hfstudio.latexnh.tooltip.SelectedTooltipAnchorMode;
import com.hfstudio.latexnh.tooltip.TooltipState;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {

    @Unique
    private TextFieldLatexPreview.Plan latexnh$boundaryPreviewPlan;

    @Unique
    private int latexnh$originalLineScrollOffset;

    @Unique
    private boolean latexnh$lineScrollOffsetOverridden;

    @Shadow
    private String text;

    @Shadow
    private int cursorPosition;

    @Shadow
    private boolean isFocused;

    @Shadow
    private FontRenderer field_146211_a;

    @Shadow
    private int lineScrollOffset;

    @Shadow
    public abstract int getWidth();

    @Shadow
    public int xPosition;

    @Shadow
    public int yPosition;

    @Inject(method = "drawTextBox", at = @At("HEAD"))
    private void latexnh$beforeDrawTextBox(CallbackInfo ci) {
        LatexRenderContext.INSTANCE.clearRenderSlice();
        latexnh$boundaryPreviewPlan = null;
        latexnh$originalLineScrollOffset = lineScrollOffset;
        latexnh$lineScrollOffsetOverridden = false;
        if (!ModConfig.render.enableLatexRendering || text == null || text.isEmpty()) {
            LatexRenderContext.INSTANCE.clearEditingFormula();
            return;
        }

        TextSegment activeSegment = isFocused ? MarkdownParser.getLatexSegmentAtCursor(text, cursorPosition) : null;
        if (activeSegment != null) {
            LatexRenderContext.INSTANCE.setEditingFormula(activeSegment, true);
            return;
        }

        latexnh$boundaryPreviewPlan = latexnh$buildBoundaryPreviewPlan();
        if (latexnh$boundaryPreviewPlan.inlinePreview) {
            lineScrollOffset = latexnh$boundaryPreviewPlan.visibleStartIndex;
            latexnh$lineScrollOffsetOverridden = true;
        }

        LatexRenderContext.INSTANCE.clearEditingFormula();
    }

    @Inject(method = "drawTextBox", at = @At("RETURN"))
    private void latexnh$afterDrawTextBox(CallbackInfo ci) {
        try {
            TextSegment activeSegment = !isFocused || text == null ? null
                : MarkdownParser.getLatexSegmentAtCursor(text, cursorPosition);
            LatexRenderContext.INSTANCE.clearEditingFormula();
            LatexRenderContext.INSTANCE.clearRenderSlice();
            if (!ModConfig.render.enableLatexRendering || !isFocused
                || text == null
                || text.isEmpty()
                || activeSegment == null) {
                return;
            }

            LatexRenderer.INSTANCE.getOrCreateTexture(activeSegment.content);
            LatexTooltipMode tooltipMode = LatexTooltipMode
                .fromHotkeys(KeyBindings.isPreviewSelectedLatexDown(), KeyBindings.isShowLatexDown());
            boolean followMouse = ModConfig.render.selectedTooltipAnchorMode == SelectedTooltipAnchorMode.FOLLOW_CURSOR;
            int tooltipAnchorX = xPosition + 2;
            int tooltipAnchorY = yPosition - 4;
            if (!LatexTextureCache.INSTANCE.hasFailed(activeSegment.content)) {
                if (TextFieldLatexPreview
                    .resolveShortcutTooltipFormula(text, isFocused, cursorPosition, tooltipMode.isActive()) != null) {
                    if (tooltipMode.rendersLatex()) {
                        TooltipState.INSTANCE.requestSelectedLatexTooltip(
                            activeSegment.content,
                            activeSegment.renderScale,
                            tooltipAnchorX,
                            tooltipAnchorY,
                            followMouse);
                    } else if (tooltipMode.rendersSourceText()) {
                        TooltipState.INSTANCE.requestSelectedTextTooltip(
                            MarkdownParser.buildSourceLines(activeSegment.content),
                            tooltipAnchorX,
                            tooltipAnchorY,
                            followMouse);
                    }
                }
                return;
            }

            if (tooltipMode.isActive()) {
                String errorInfo = LatexTextureCache.INSTANCE.getError(activeSegment.content);
                TooltipState.INSTANCE.requestSelectedTextTooltip(
                    MarkdownParser.buildErrorLines(activeSegment.content, errorInfo),
                    tooltipAnchorX,
                    tooltipAnchorY,
                    followMouse);
            }
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
            target = "Lnet/minecraft/client/gui/FontRenderer;trimStringToWidth(Ljava/lang/String;I)Ljava/lang/String;"))
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
            ordinal = 0))
    private int latexnh$drawPrefixWithRenderSlice(FontRenderer fontRenderer, String value, int x, int y, int color) {
        return latexnh$drawStringWithSlice(fontRenderer, value, x, y, color, lineScrollOffset);
    }

    @Redirect(
        method = "drawTextBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;III)I",
            ordinal = 1))
    private int latexnh$drawSuffixWithRenderSlice(FontRenderer fontRenderer, String value, int x, int y, int color) {
        return latexnh$drawStringWithSlice(fontRenderer, value, x, y, color, cursorPosition);
    }

    @Redirect(
        method = "drawTextBox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I"))
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
    private TextFieldLatexPreview.Plan latexnh$buildBoundaryPreviewPlan() {
        int previewCursorPosition = TextFieldLatexPreview
            .resolvePreviewCursor(isFocused, cursorPosition, lineScrollOffset);
        String rawVisibleText = field_146211_a.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
        return TextFieldLatexPreview.plan(
            text,
            previewCursorPosition,
            lineScrollOffset,
            rawVisibleText,
            field_146211_a::getStringWidth,
            getWidth());
    }

}
