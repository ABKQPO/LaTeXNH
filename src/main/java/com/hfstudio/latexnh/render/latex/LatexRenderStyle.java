package com.hfstudio.latexnh.render.latex;

import java.util.Locale;

import com.hfstudio.latexnh.config.ModConfig;

public final class LatexRenderStyle {

    public static final int DEFAULT_FILL_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_OUTLINE_COLOR = 0xFF000000;
    public static final String DEFAULT_FILL_COLOR_HEX = "#FFFFFF";
    public static final String DEFAULT_OUTLINE_COLOR_HEX = "#000000";
    public static final int DEFAULT_OUTLINE_THICKNESS = 0;

    public final int fillColorArgb;
    public final int outlineColorArgb;
    public final int outlineThicknessPx;
    public final String fontCacheToken;
    public final boolean allowFormattingColor;
    public final float sourceRenderScale;
    public final LatexTextureFiltering textureFiltering;
    public final LatexHintMode shapeAntialiasing;
    public final LatexHintMode textAntialiasing;
    public final LatexRenderQuality renderQuality;
    public final boolean enableFractionalMetrics;

    public LatexRenderStyle(int fillColorArgb, int outlineColorArgb, int outlineThicknessPx, String fontCacheToken,
        boolean allowFormattingColor, float sourceRenderScale, LatexTextureFiltering textureFiltering,
        LatexHintMode shapeAntialiasing, LatexHintMode textAntialiasing, LatexRenderQuality renderQuality,
        boolean enableFractionalMetrics) {
        this.fillColorArgb = fillColorArgb;
        this.outlineColorArgb = outlineColorArgb;
        this.outlineThicknessPx = Math.max(0, outlineThicknessPx);
        this.fontCacheToken = fontCacheToken == null || fontCacheToken.isEmpty() ? "default" : fontCacheToken;
        this.allowFormattingColor = allowFormattingColor;
        this.sourceRenderScale = Math.max(16.0f, sourceRenderScale);
        this.textureFiltering = textureFiltering == null ? LatexTextureFiltering.LINEAR : textureFiltering;
        this.shapeAntialiasing = shapeAntialiasing == null ? LatexHintMode.ON : shapeAntialiasing;
        this.textAntialiasing = textAntialiasing == null ? LatexHintMode.ON : textAntialiasing;
        this.renderQuality = renderQuality == null ? LatexRenderQuality.QUALITY : renderQuality;
        this.enableFractionalMetrics = enableFractionalMetrics;
    }

    public static LatexRenderStyle fromConfig() {
        return fromConfig(parseColorOrDefault(ModConfig.render.formulaColor, DEFAULT_FILL_COLOR));
    }

    public static LatexRenderStyle fromConfig(int fillColorArgb) {
        int configuredFillColor = parseColorOrDefault(ModConfig.render.formulaColor, DEFAULT_FILL_COLOR);
        boolean allowFormattingColor = ModConfig.render.allowFormattingColor;
        LatexFontResolver.Selection fontSelection = LatexFontResolver.resolveConfiguredSelection();
        return new LatexRenderStyle(
            MinecraftTextFormattingState.normalizeColor(allowFormattingColor ? fillColorArgb : configuredFillColor),
            parseColorOrDefault(ModConfig.render.outlineColor, DEFAULT_OUTLINE_COLOR),
            ModConfig.render.outlineThickness,
            fontSelection.getCacheToken(),
            allowFormattingColor,
            ModConfig.render.sourceRenderScale,
            ModConfig.render.textureFiltering,
            ModConfig.render.shapeAntialiasing,
            ModConfig.render.textAntialiasing,
            ModConfig.render.renderQuality,
            ModConfig.render.enableFractionalMetrics);
    }

    public static String configCacheKey() {
        return fromConfig().cacheKey();
    }

    public String cacheKey() {
        return String.format(
            Locale.ROOT,
            "%08x:%08x:%d:%s:%s:%.2f:%s:%s:%s:%s:%s",
            fillColorArgb,
            outlineColorArgb,
            outlineThicknessPx,
            allowFormattingColor ? "t" : "f",
            fontCacheToken,
            sourceRenderScale,
            textureFiltering.name(),
            shapeAntialiasing.name(),
            textAntialiasing.name(),
            renderQuality.name(),
            enableFractionalMetrics ? "fm1" : "fm0");
    }

    public static int parseColorOrDefault(String value, int fallbackArgb) {
        if (value == null) {
            return fallbackArgb;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return fallbackArgb;
        }
        if (normalized.charAt(0) == '#') {
            normalized = normalized.substring(1);
        }

        try {
            if (normalized.length() == 6) {
                return 0xFF000000 | Integer.parseUnsignedInt(normalized, 16);
            }
            if (normalized.length() == 8) {
                return (int) Long.parseLong(normalized, 16);
            }
        } catch (NumberFormatException ignored) {}

        return fallbackArgb;
    }
}
