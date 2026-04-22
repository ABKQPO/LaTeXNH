package com.hfstudio.latexnh.mixins.early.minecraft;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;

import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.keybind.KeyBindings;
import com.hfstudio.latexnh.render.latex.LatexRenderEntry;
import com.hfstudio.latexnh.render.latex.LatexRenderTracker;
import com.hfstudio.latexnh.render.latex.LatexTextureCache;
import com.hfstudio.latexnh.render.markdown.MarkdownParser;
import com.hfstudio.latexnh.tooltip.TooltipRenderer;
import com.hfstudio.latexnh.tooltip.TooltipState;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    @Final
    private Minecraft mc;

    @Redirect(
        method = "updateCameraAndRender(F)V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getX()I", remap = false))
    private int latexnh$freezeRenderMouseX() {
        GuiScreen screen = mc.currentScreen;
        int rawMouseX = Mouse.getX();
        if (screen == null) {
            return rawMouseX;
        }

        int rawMouseY = Mouse.getY();
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        boolean freezeMouse = ModConfig.render.enableLatexRendering && ModConfig.render.enableHoverTooltip
            && KeyBindings.isShowLatexDown();

        TooltipState.INSTANCE.captureRaw(
            freezeMouse,
            screen,
            rawMouseX,
            rawMouseY,
            mc.displayWidth,
            mc.displayHeight,
            scaledResolution.getScaledWidth(),
            scaledResolution.getScaledHeight());

        if (freezeMouse) {
            TooltipState.INSTANCE.beginMouseOverride();
            return TooltipState.INSTANCE.getLogicalRawMouseX();
        }

        TooltipState.INSTANCE.endMouseOverride();
        return rawMouseX;
    }

    @Redirect(
        method = "updateCameraAndRender(F)V",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getY()I", remap = false))
    private int latexnh$freezeRenderMouseY() {
        if (TooltipState.INSTANCE.isMouseOverrideActive() && TooltipState.INSTANCE.isMouseFrozen()) {
            return TooltipState.INSTANCE.getLogicalRawMouseY();
        }
        return Mouse.getY();
    }

    @Redirect(
        method = "updateCameraAndRender(F)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V"))
    private void latexnh$drawScreenWithLatexTooltipOnTop(GuiScreen screen, int mouseX, int mouseY, float partialTicks) {
        screen.drawScreen(mouseX, mouseY, partialTicks);
        latexnh$renderHoveredLatexTooltip();
    }

    @Inject(method = "updateCameraAndRender(F)V", at = @At("RETURN"))
    private void latexnh$endMouseOverride(float partialTicks, CallbackInfo ci) {
        TooltipState.INSTANCE.endMouseOverride();
    }

    @Unique
    private void latexnh$renderHoveredLatexTooltip() {
        if (!ModConfig.render.enableLatexRendering || !ModConfig.render.enableHoverTooltip
            || !KeyBindings.isShowLatexDown()) {
            return;
        }

        int actualMouseX = TooltipState.INSTANCE.getActualMouseX();
        int actualMouseY = TooltipState.INSTANCE.getActualMouseY();
        LatexRenderEntry entry = LatexRenderTracker.INSTANCE.getEntryAt(actualMouseX, actualMouseY);
        if (entry == null) {
            return;
        }

        TooltipRenderer.INSTANCE.renderTextTooltip(latexnh$buildTooltipLines(entry), actualMouseX, actualMouseY);
    }

    @Unique
    private static List<String> latexnh$buildTooltipLines(LatexRenderEntry entry) {
        if (entry == null) {
            return Collections.emptyList();
        }
        if (entry.failed) {
            return MarkdownParser.buildErrorLines(entry.formula, LatexTextureCache.INSTANCE.getError(entry.formula));
        }
        return MarkdownParser.buildSourceLines(entry.formula);
    }
}
