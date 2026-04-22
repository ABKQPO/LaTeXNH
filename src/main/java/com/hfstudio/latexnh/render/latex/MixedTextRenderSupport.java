package com.hfstudio.latexnh.render.latex;

import java.util.List;

import com.hfstudio.latexnh.config.ModConfig;
import com.hfstudio.latexnh.render.markdown.MarkdownParser;
import com.hfstudio.latexnh.render.markdown.TextSegment;

public final class MixedTextRenderSupport {

    @FunctionalInterface
    public interface PlainRenderer {

        float render(String text, float x, float y, int color, boolean shadow);
    }

    @FunctionalInterface
    public interface PlainWidthMeasurer {

        int measure(String text);
    }

    private MixedTextRenderSupport() {}

    static boolean shouldRenderLatexInCurrentPass(boolean shadow, boolean combinedShadowPass) {
        return !shadow || combinedShadowPass;
    }

    public static float renderMixed(String text, float x, float y, int color, boolean shadow, int[] colorPalette,
        PlainRenderer plainRenderer) {
        return renderMixed(text, x, y, color, shadow, false, colorPalette, plainRenderer);
    }

    public static float renderMixed(String text, float x, float y, int color, boolean shadow,
        boolean combinedShadowPass, int[] colorPalette, PlainRenderer plainRenderer) {
        List<TextSegment> segments = MarkdownParser.parseSegments(text);
        MinecraftTextFormattingState formattingState = new MinecraftTextFormattingState(color, colorPalette);
        float currentX = x;

        for (TextSegment segment : segments) {
            if (!segment.isLatex()) {
                String formatted = MarkdownParser.toMinecraftFormatted(segment.content);
                if (!formatted.isEmpty()) {
                    currentX = plainRenderer.render(
                        withPrefix(formattingState.buildFormattingPrefix(), formatted),
                        currentX,
                        y,
                        color,
                        shadow);
                    formattingState.apply(formatted);
                }
                continue;
            }

            String sourceText = segment.toSourceText();
            if (LatexRenderContext.INSTANCE.shouldRenderSource(segment)) {
                currentX = renderSourceSegment(
                    sourceText,
                    formattingState,
                    currentX,
                    y,
                    color,
                    shadow,
                    plainRenderer,
                    false,
                    segment.content);
                continue;
            }

            float displayHeight = segment.isDisplayLatex() ? ModConfig.render.inlineHeight * 1.5f
                : ModConfig.render.inlineHeight;

            if (!shouldRenderLatexInCurrentPass(shadow, combinedShadowPass)) {
                int shadowWidth = LatexRenderer.INSTANCE.measureLatexWidth(
                    segment.content,
                    displayHeight,
                    formattingState.getCurrentColorArgb(),
                    colorPalette);
                if (shadowWidth > 0) {
                    currentX += shadowWidth + 1;
                    continue;
                }
                currentX = renderSourceSegment(
                    sourceText,
                    formattingState,
                    currentX,
                    y,
                    color,
                    true,
                    plainRenderer,
                    false,
                    segment.content);
                continue;
            }

            float topY = y - (displayHeight - 8f) / 2f;
            int renderedWidth = LatexRenderer.INSTANCE.drawLatex(
                segment.content,
                currentX,
                topY,
                displayHeight,
                formattingState.getCurrentColorArgb(),
                colorPalette);
            if (renderedWidth > 0) {
                LatexRenderTracker.INSTANCE.register(
                    segment.content,
                    Math.round(currentX),
                    Math.round(topY),
                    renderedWidth,
                    Math.round(displayHeight));
                currentX += renderedWidth + 1;
                continue;
            }

            currentX = renderSourceSegment(
                sourceText,
                formattingState,
                currentX,
                y,
                color,
                shadow,
                plainRenderer,
                shouldRenderLatexInCurrentPass(shadow, combinedShadowPass),
                segment.content);
        }

        return currentX;
    }

    public static int measureMixed(String text, int[] colorPalette, PlainWidthMeasurer plainWidthMeasurer) {
        List<TextSegment> segments = MarkdownParser.parseSegments(text);
        MinecraftTextFormattingState formattingState = new MinecraftTextFormattingState(
            LatexFormattingParser.DEFAULT_ARGB,
            colorPalette);
        int total = 0;

        for (TextSegment segment : segments) {
            if (!segment.isLatex()) {
                String formatted = MarkdownParser.toMinecraftFormatted(segment.content);
                if (!formatted.isEmpty()) {
                    total += plainWidthMeasurer.measure(withPrefix(formattingState.buildFormattingPrefix(), formatted));
                    formattingState.apply(formatted);
                }
                continue;
            }

            String sourceText = segment.toSourceText();
            if (LatexRenderContext.INSTANCE.shouldRenderSource(segment)
                || LatexTextureCache.INSTANCE.hasFailed(segment.content)) {
                total += plainWidthMeasurer.measure(withPrefix(formattingState.buildFormattingPrefix(), sourceText));
                formattingState.apply(sourceText);
                continue;
            }

            float displayHeight = segment.isDisplayLatex() ? ModConfig.render.inlineHeight * 1.5f
                : ModConfig.render.inlineHeight;
            int renderedWidth = LatexRenderer.INSTANCE
                .measureLatexWidth(segment.content, displayHeight, formattingState.getCurrentColorArgb(), colorPalette);
            if (renderedWidth > 0) {
                total += renderedWidth + 1;
                continue;
            }

            total += plainWidthMeasurer.measure(withPrefix(formattingState.buildFormattingPrefix(), sourceText));
            formattingState.apply(sourceText);
        }

        return total;
    }

    private static float renderSourceSegment(String sourceText, MinecraftTextFormattingState formattingState, float x,
        float y, int color, boolean shadow, PlainRenderer plainRenderer, boolean registerFailure, String formula) {
        float rawStart = x;
        float rawEnd = plainRenderer
            .render(withPrefix(formattingState.buildFormattingPrefix(), sourceText), x, y, color, shadow);
        formattingState.apply(sourceText);

        if (!shadow && registerFailure) {
            LatexRenderTracker.INSTANCE.registerFailed(
                formula,
                Math.round(rawStart),
                Math.round(y),
                Math.max(0, Math.round(rawEnd - rawStart)),
                9);
        }
        return rawEnd;
    }

    private static String withPrefix(String prefix, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (prefix == null || prefix.isEmpty()) {
            return text;
        }
        return prefix + text;
    }
}
