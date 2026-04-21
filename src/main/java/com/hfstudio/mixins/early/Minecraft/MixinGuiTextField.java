package com.hfstudio.mixins.early.Minecraft;

import net.minecraft.client.gui.GuiTextField;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.config.ModConfig;
import com.hfstudio.render.latex.LatexRenderContext;
import com.hfstudio.render.latex.LatexRenderer;
import com.hfstudio.render.latex.LatexTextureCache;
import com.hfstudio.render.markdown.MarkdownParser;
import com.hfstudio.render.markdown.TextSegment;
import com.hfstudio.tooltip.TooltipRenderer;

@Mixin(GuiTextField.class)
public class MixinGuiTextField {

    @Shadow
    private String text;

    @Shadow
    private int cursorPosition;

    @Shadow
    private boolean isFocused;

    @Shadow
    public int xPosition;

    @Shadow
    public int yPosition;

    @Inject(method = "drawTextBox", at = @At("HEAD"))
    private void latexnh$beforeDrawTextBox(CallbackInfo ci) {
        if (!ModConfig.render.enableLatexRendering || !isFocused || text == null || text.isEmpty()) {
            LatexRenderContext.INSTANCE.clearEditingFormula();
            return;
        }
        LatexRenderContext.INSTANCE.setEditingFormula(MarkdownParser.getLatexSegmentAtCursor(text, cursorPosition));
    }

    @Inject(method = "drawTextBox", at = @At("RETURN"))
    private void latexnh$afterDrawTextBox(CallbackInfo ci) {
        TextSegment activeSegment = text == null ? null : MarkdownParser.getLatexSegmentAtCursor(text, cursorPosition);
        LatexRenderContext.INSTANCE.clearEditingFormula();
        if (!ModConfig.render.enableLatexRendering || !isFocused
            || text == null
            || text.isEmpty()
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
    }
}
