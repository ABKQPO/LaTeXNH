package com.hfstudio.latexnh.render.latex;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;
import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import com.hfstudio.latexnh.LaTeXNH;

public final class LatexRenderer {

    public static final LatexRenderer INSTANCE = new LatexRenderer();

    private LatexRenderer() {}

    public int[] getOrCreateTexture(String formula) {
        return getOrCreateTexture(formula, LatexRenderStyle.fromConfig(), null);
    }

    public int[] getOrCreateTexture(String formula, int fillColorArgb, int[] colorPalette) {
        return getOrCreateTexture(formula, LatexRenderStyle.fromConfig(fillColorArgb), colorPalette);
    }

    private int[] getOrCreateTexture(String formula, LatexRenderStyle style, int[] colorPalette) {
        LatexTextureCache.INSTANCE.onStyleChanged(LatexRenderStyle.configCacheKey());

        if (LatexTextureCache.INSTANCE.hasFailed(formula)) {
            return null;
        }

        try {
            String strippedFormula = LatexFormattingParser.stripFormattingCodes(formula);
            LatexFontResolver.Selection fontSelection = LatexFontResolver.resolveConfiguredSelection();
            LatexFontResolver.applySelection(strippedFormula, fontSelection);
            new TeXFormula(strippedFormula);
            String renderableFormula = LatexFormattingParser
                .toRenderableFormula(formula, style.fillColorArgb, colorPalette, style.allowFormattingColor);
            String cacheFormula = renderableFormula;

            int[] cached = LatexTextureCache.INSTANCE.get(cacheFormula, style);
            if (cached != null) {
                return cached;
            }

            TeXFormula texFormula;
            try {
                texFormula = new TeXFormula(renderableFormula);
            } catch (ParseException ignored) {
                cacheFormula = strippedFormula;
                cached = LatexTextureCache.INSTANCE.get(cacheFormula, style);
                if (cached != null) {
                    return cached;
                }
                texFormula = new TeXFormula(strippedFormula);
            }
            TeXIcon icon = buildIcon(texFormula, style, fontSelection);
            icon.setInsets(new Insets(2, 2, 2, 2));
            icon.setForeground(new Color(style.fillColorArgb, true));

            BufferedImage image = renderFormulaImage(icon, style);
            int textureId = uploadTexture(image, image.getWidth(), image.getHeight(), style);
            LatexTextureCache.INSTANCE.put(cacheFormula, style, textureId, image.getWidth(), image.getHeight());
            return new int[] { textureId, image.getWidth(), image.getHeight() };
        } catch (ParseException e) {
            String errorMessage = e.getMessage();
            LaTeXNH.LOG.warn("[LaTeXNH] LaTeX parse failure: {} - {}", formula, errorMessage);
            LatexTextureCache.INSTANCE.markFailed(formula, errorMessage == null ? "" : errorMessage);
            return null;
        } catch (Exception e) {
            LaTeXNH.LOG.warn("[LaTeXNH] Unexpected error rendering LaTeX: {}", formula, e);
            LatexTextureCache.INSTANCE.markFailed(
                formula,
                e.getMessage() == null ? e.getClass()
                    .getSimpleName() : e.getMessage());
            return null;
        }
    }

    public int drawLatex(String formula, float x, float y, float displayH) {
        return drawLatex(formula, x, y, displayH, LatexRenderStyle.fromConfig().fillColorArgb, null);
    }

    public int drawLatex(String formula, float x, float y, float displayH, int fillColorArgb, int[] colorPalette) {
        int[] texture = getOrCreateTexture(formula, fillColorArgb, colorPalette);
        if (texture == null) {
            return 0;
        }

        float renderWidth = computeRenderWidth(texture, displayH);
        renderQuad(texture[0], x, y, renderWidth, displayH);
        return (int) Math.ceil(renderWidth);
    }

    public int measureLatexWidth(String formula, float displayH) {
        return measureLatexWidth(formula, displayH, LatexRenderStyle.fromConfig().fillColorArgb, null);
    }

    public int measureLatexWidth(String formula, float displayH, int fillColorArgb, int[] colorPalette) {
        int[] texture = getOrCreateTexture(formula, fillColorArgb, colorPalette);
        if (texture == null) {
            return 0;
        }
        return (int) Math.ceil(computeRenderWidth(texture, displayH));
    }

    public int drawLatexConstrained(String formula, float x, float y, float displayH, float maxWidth) {
        int[] texture = getOrCreateTexture(formula);
        if (texture == null) {
            return 0;
        }

        float srcWidth = texture[1];
        float srcHeight = texture[2];
        float scale = displayH / srcHeight;
        float renderWidth = srcWidth * scale;
        float renderHeight = displayH;

        if (renderWidth > maxWidth) {
            scale = maxWidth / srcWidth;
            renderWidth = maxWidth;
            renderHeight = srcHeight * scale;
        }

        renderQuad(texture[0], x, y, renderWidth, renderHeight);
        return (int) Math.ceil(renderWidth);
    }

    private float computeRenderWidth(int[] texture, float displayH) {
        float srcWidth = texture[1];
        float srcHeight = texture[2];
        float scale = displayH / srcHeight;
        return srcWidth * scale;
    }

    private TeXIcon buildIcon(TeXFormula texFormula, LatexRenderStyle style,
        LatexFontResolver.Selection fontSelection) {
        TeXFormula.TeXIconBuilder builder = texFormula.new TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY)
            .setSize(style.sourceRenderScale)
            .setFGColor(new Color(style.fillColorArgb, true));

        if (fontSelection.getTeXType() >= 0) {
            builder.setType(fontSelection.getTeXType());
        }

        return builder.build();
    }

    private void renderQuad(int textureId, float x, float y, float renderWidth, float renderHeight) {
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT | GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + renderHeight, 0, 0, 1);
        tessellator.addVertexWithUV(x + renderWidth, y + renderHeight, 0, 1, 1);
        tessellator.addVertexWithUV(x + renderWidth, y, 0, 1, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();

        GL11.glPopAttrib();
    }

    private BufferedImage renderFormulaImage(TeXIcon icon, LatexRenderStyle style) {
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, style.shapeAntialiasing.toGeneralAntialiasHint());
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, style.textAntialiasing.toTextAntialiasHint());
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, style.renderQuality.toRenderingHint());
        graphics
            .setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, style.renderQuality.toAlphaInterpolationHint());
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, style.renderQuality.toColorRenderingHint());
        graphics.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            style.enableFractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON
                : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fillRect(0, 0, width, height);
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();

        return LatexImageEffects.applyOutline(image, style.outlineColorArgb, style.outlineThicknessPx);
    }

    private static int uploadTexture(BufferedImage image, int width, int height, LatexRenderStyle style) {
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();

        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        int textureFilter = style.textureFiltering.getGlConstant();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, textureFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, textureFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA8,
            width,
            height,
            0,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return textureId;
    }
}
