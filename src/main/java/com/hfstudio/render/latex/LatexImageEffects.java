package com.hfstudio.render.latex;

import java.awt.image.BufferedImage;

public final class LatexImageEffects {

    private LatexImageEffects() {}

    public static BufferedImage applyOutline(BufferedImage source, int outlineArgb, int outlineSizePx) {
        int clampedOutlineSize = Math.max(0, outlineSizePx);
        if (source == null || clampedOutlineSize == 0 || ((outlineArgb >>> 24) & 0xFF) == 0) {
            return source;
        }

        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        int width = sourceWidth + clampedOutlineSize * 2;
        int height = sourceHeight + clampedOutlineSize * 2;
        int[] sourcePixels = source.getRGB(0, 0, sourceWidth, sourceHeight, null, 0, sourceWidth);
        int[] resultPixels = new int[width * height];

        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                int sourcePixel = sourcePixels[(y * sourceWidth) + x];
                int sourceAlpha = (sourcePixel >>> 24) & 0xFF;
                if (sourceAlpha == 0) {
                    continue;
                }

                int outlinePixel = withScaledAlpha(outlineArgb, sourceAlpha);
                for (int dy = -clampedOutlineSize; dy <= clampedOutlineSize; dy++) {
                    for (int dx = -clampedOutlineSize; dx <= clampedOutlineSize; dx++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }

                        int targetX = x + clampedOutlineSize + dx;
                        int targetY = y + clampedOutlineSize + dy;
                        int targetIndex = (targetY * width) + targetX;
                        resultPixels[targetIndex] = blendSrcOver(outlinePixel, resultPixels[targetIndex]);
                    }
                }
            }
        }

        for (int y = 0; y < sourceHeight; y++) {
            for (int x = 0; x < sourceWidth; x++) {
                int sourcePixel = sourcePixels[(y * sourceWidth) + x];
                if (((sourcePixel >>> 24) & 0xFF) == 0) {
                    continue;
                }

                int targetIndex = ((y + clampedOutlineSize) * width) + (x + clampedOutlineSize);
                resultPixels[targetIndex] = blendSrcOver(sourcePixel, resultPixels[targetIndex]);
            }
        }

        BufferedImage outlined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        outlined.setRGB(0, 0, width, height, resultPixels, 0, width);
        return outlined;
    }

    private static int withScaledAlpha(int argb, int sourceAlpha) {
        int alpha = ((argb >>> 24) & 0xFF) * sourceAlpha / 255;
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    private static int blendSrcOver(int src, int dst) {
        int srcAlpha = (src >>> 24) & 0xFF;
        if (srcAlpha == 0) {
            return dst;
        }
        if (srcAlpha == 255) {
            return src;
        }

        int dstAlpha = (dst >>> 24) & 0xFF;
        int outAlpha = srcAlpha + dstAlpha * (255 - srcAlpha) / 255;
        if (outAlpha == 0) {
            return 0;
        }

        int srcRed = (src >>> 16) & 0xFF;
        int srcGreen = (src >>> 8) & 0xFF;
        int srcBlue = src & 0xFF;
        int dstRed = (dst >>> 16) & 0xFF;
        int dstGreen = (dst >>> 8) & 0xFF;
        int dstBlue = dst & 0xFF;

        int outRed = (srcRed * srcAlpha + dstRed * dstAlpha * (255 - srcAlpha) / 255) / outAlpha;
        int outGreen = (srcGreen * srcAlpha + dstGreen * dstAlpha * (255 - srcAlpha) / 255) / outAlpha;
        int outBlue = (srcBlue * srcAlpha + dstBlue * dstAlpha * (255 - srcAlpha) / 255) / outAlpha;

        return (outAlpha << 24) | (outRed << 16) | (outGreen << 8) | outBlue;
    }
}
