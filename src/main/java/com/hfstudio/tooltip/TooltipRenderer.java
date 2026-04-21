package com.hfstudio.tooltip;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.hfstudio.config.ModConfig;
import com.hfstudio.render.latex.LatexRenderer;

public final class TooltipRenderer {

    public static final TooltipRenderer INSTANCE = new TooltipRenderer();

    private static final int TEXT_LINE_HEIGHT = 10;
    private static final int TOOLTIP_MARGIN = 24;
    private static final float TOOLTIP_Z = 400.0F;

    private TooltipRenderer() {}

    public void renderTextTooltip(List<String> lines, int mouseX, int mouseY) {
        if (lines == null || lines.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer fontRenderer = minecraft.fontRenderer;
        ScaledResolution scaledResolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();

        List<String> wrappedLines = wrapLines(lines, fontRenderer, Math.max(40, screenWidth - TOOLTIP_MARGIN));
        int tooltipTextWidth = 0;
        for (String line : wrappedLines) {
            tooltipTextWidth = Math.max(tooltipTextWidth, fontRenderer.getStringWidth(line));
        }

        int tooltipHeight = 8;
        if (wrappedLines.size() > 1) {
            tooltipHeight += 2 + (wrappedLines.size() - 1) * TEXT_LINE_HEIGHT;
        }
        TooltipLayout.Position position = TooltipLayout
            .positionTextTooltip(mouseX, mouseY, tooltipTextWidth, tooltipHeight, screenWidth, screenHeight);
        int tooltipX = position.x;
        int tooltipY = position.y;

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, TOOLTIP_Z);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        drawBackground(tooltipX - 3, tooltipY - 4, tooltipTextWidth + 6, tooltipHeight + 8);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        int textY = tooltipY;
        for (int i = 0; i < wrappedLines.size(); i++) {
            fontRenderer.drawStringWithShadow(wrappedLines.get(i), tooltipX, textY, 0xFFFFFF);
            textY += i == 0 ? 12 : TEXT_LINE_HEIGHT;
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    public void renderLatexTooltip(String formula, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();

        int[] texture = LatexRenderer.INSTANCE.getOrCreateTexture(formula);
        if (texture == null) {
            return;
        }

        float srcWidth = texture[1];
        float srcHeight = texture[2];
        float maxWidth = screenWidth * 0.55f;
        float displayHeight = ModConfig.render.tooltipScale * 30f;
        float scale = displayHeight / srcHeight;
        float renderWidth = srcWidth * scale;
        float renderHeight = displayHeight;

        if (renderWidth > maxWidth) {
            scale = maxWidth / srcWidth;
            renderWidth = maxWidth;
            renderHeight = srcHeight * scale;
        }

        TooltipLayout.FloatPosition position = TooltipLayout
            .positionLatexTooltip(mouseX, mouseY, renderWidth, renderHeight, screenWidth, screenHeight);
        float tooltipX = position.x;
        float tooltipY = position.y;

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, TOOLTIP_Z);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        drawBackground(tooltipX - 5, tooltipY - 5, renderWidth + 10, renderHeight + 10);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture[0]);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(tooltipX, tooltipY + renderHeight, 0, 0, 1);
        tessellator.addVertexWithUV(tooltipX + renderWidth, tooltipY + renderHeight, 0, 1, 1);
        tessellator.addVertexWithUV(tooltipX + renderWidth, tooltipY, 0, 1, 0);
        tessellator.addVertexWithUV(tooltipX, tooltipY, 0, 0, 0);
        tessellator.draw();

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private List<String> wrapLines(List<String> lines, FontRenderer fontRenderer, int maxWidth) {
        List<String> wrapped = new ArrayList<>();
        for (String line : lines) {
            if (fontRenderer.getStringWidth(line) <= maxWidth) {
                wrapped.add(line);
            } else {
                wrapped.addAll(fontRenderer.listFormattedStringToWidth(line, maxWidth));
            }
        }
        return wrapped;
    }

    private static void drawBackground(float x, float y, float width, float height) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tessellator = Tessellator.instance;

        GL11.glColor4f(0.063f, 0.0f, 0.063f, 0.94f);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x, y + height, 0);
        tessellator.addVertex(x + width, y + height, 0);
        tessellator.addVertex(x + width, y, 0);
        tessellator.addVertex(x, y, 0);
        tessellator.draw();

        GL11.glColor4f(0.31f, 0.0f, 1.0f, 0.31f);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x + 1, y, 0);
        tessellator.addVertex(x + width - 1, y, 0);
        tessellator.addVertex(x + width - 1, y + 1, 0);
        tessellator.addVertex(x + 1, y + 1, 0);
        tessellator.draw();

        GL11.glColor4f(0.17f, 0.0f, 0.5f, 0.31f);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x + 1, y + height - 1, 0);
        tessellator.addVertex(x + width - 1, y + height - 1, 0);
        tessellator.addVertex(x + width - 1, y + height, 0);
        tessellator.addVertex(x + 1, y + height, 0);
        tessellator.draw();

        GL11.glColor4f(0.31f, 0.0f, 1.0f, 0.31f);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x, y + 1, 0);
        tessellator.addVertex(x + 1, y + 1, 0);
        tessellator.addVertex(x + 1, y + height - 1, 0);
        tessellator.addVertex(x, y + height - 1, 0);
        tessellator.draw();

        tessellator.startDrawingQuads();
        tessellator.addVertex(x + width - 1, y + 1, 0);
        tessellator.addVertex(x + width, y + 1, 0);
        tessellator.addVertex(x + width, y + height - 1, 0);
        tessellator.addVertex(x + width - 1, y + height - 1, 0);
        tessellator.draw();
    }
}
