package com.hfstudio.latexnh.render.latex;

import java.util.Locale;

import com.hfstudio.latexnh.config.ModConfig;

public final class LatexRenderStyle {

    public static final int DEFAULT_FILL_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_OUTLINE_COLOR = 0xFF000000;
    public static final String DEFAULT_FILL_COLOR_HEX = "#FFFFFF";
    public static final String DEFAULT_OUTLINE_COLOR_HEX = "#000000";
    public static final int DEFAULT_OUTLINE_THICKNESS = 2;

    public final int fillColorArgb;
    public final int outlineColorArgb;
    public final int outlineThicknessPx;

    public LatexRenderStyle(int fillColorArgb, int outlineColorArgb, int outlineThicknessPx) {
        this.fillColorArgb = fillColorArgb;
        this.outlineColorArgb = outlineColorArgb;
        this.outlineThicknessPx = Math.max(0, outlineThicknessPx);
    }

    public static LatexRenderStyle fromConfig() {
        return fromConfig(parseColorOrDefault(ModConfig.render.formulaColor, DEFAULT_FILL_COLOR));
    }

    public static LatexRenderStyle fromConfig(int fillColorArgb) {
        return new LatexRenderStyle(
            MinecraftTextFormattingState.normalizeColor(fillColorArgb),
            parseColorOrDefault(ModConfig.render.outlineColor, DEFAULT_OUTLINE_COLOR),
            ModConfig.render.outlineThickness);
    }

    public static String configCacheKey() {
        return fromConfig().cacheKey();
    }

    public String cacheKey() {
        return String.format(Locale.ROOT, "%08x:%08x:%d", fillColorArgb, outlineColorArgb, outlineThicknessPx);
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
