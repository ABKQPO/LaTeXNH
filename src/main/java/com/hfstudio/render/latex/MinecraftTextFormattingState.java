package com.hfstudio.render.latex;

public final class MinecraftTextFormattingState {

    public static final char FORMATTING_CHAR = '\u00a7';

    private static final int[] VANILLA_COLOR_PALETTE = createVanillaColorPalette();

    private final int baseColorArgb;
    private final int[] colorPalette;

    private int currentColorArgb;
    private char currentColorCode;
    private boolean obfuscated;
    private boolean bold;
    private boolean strikethrough;
    private boolean underline;
    private boolean italic;

    public MinecraftTextFormattingState(int baseColorArgb) {
        this(baseColorArgb, null);
    }

    public MinecraftTextFormattingState(int baseColorArgb, int[] colorPalette) {
        this.baseColorArgb = normalizeColor(baseColorArgb);
        this.colorPalette = colorPalette;
        this.currentColorArgb = this.baseColorArgb;
        this.currentColorCode = 0;
    }

    public void apply(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != FORMATTING_CHAR || i + 1 >= text.length()) {
                continue;
            }
            char code = text.charAt(++i);
            if (isFormattingCode(code)) {
                applyFormattingCode(code);
            }
        }
    }

    public void applyFormattingCode(char code) {
        char normalizedCode = Character.toLowerCase(code);
        if (isColorCode(normalizedCode)) {
            currentColorArgb = resolveColor(normalizedCode);
            currentColorCode = normalizedCode;
            clearStyleFlags();
            return;
        }

        switch (normalizedCode) {
            case 'k':
                obfuscated = true;
                break;
            case 'l':
                bold = true;
                break;
            case 'm':
                strikethrough = true;
                break;
            case 'n':
                underline = true;
                break;
            case 'o':
                italic = true;
                break;
            case 'r':
                currentColorArgb = baseColorArgb;
                currentColorCode = 0;
                clearStyleFlags();
                break;
            default:
                break;
        }
    }

    public int getCurrentColorArgb() {
        return currentColorArgb;
    }

    public String buildFormattingPrefix() {
        StringBuilder prefix = new StringBuilder();
        if (currentColorCode != 0) {
            prefix.append(FORMATTING_CHAR)
                .append(currentColorCode);
        }
        if (obfuscated) {
            prefix.append(FORMATTING_CHAR)
                .append('k');
        }
        if (bold) {
            prefix.append(FORMATTING_CHAR)
                .append('l');
        }
        if (strikethrough) {
            prefix.append(FORMATTING_CHAR)
                .append('m');
        }
        if (underline) {
            prefix.append(FORMATTING_CHAR)
                .append('n');
        }
        if (italic) {
            prefix.append(FORMATTING_CHAR)
                .append('o');
        }
        return prefix.toString();
    }

    public static int normalizeColor(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    public static boolean isFormattingCode(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r' -> true;
            default -> false;
        };
    }

    public static boolean isColorCode(char code) {
        char normalizedCode = Character.toLowerCase(code);
        return (normalizedCode >= '0' && normalizedCode <= '9') || (normalizedCode >= 'a' && normalizedCode <= 'f');
    }

    private int resolveColor(char colorCode) {
        int paletteIndex = colorCode <= '9' ? colorCode - '0' : colorCode - 'a' + 10;
        int[] palette = colorPalette != null && colorPalette.length > paletteIndex ? colorPalette
            : VANILLA_COLOR_PALETTE;
        int rgb = palette[paletteIndex] & 0x00FFFFFF;
        return (currentColorArgb & 0xFF000000) | rgb;
    }

    private void clearStyleFlags() {
        obfuscated = false;
        bold = false;
        strikethrough = false;
        underline = false;
        italic = false;
    }

    private static int[] createVanillaColorPalette() {
        int[] palette = new int[32];

        for (int i = 0; i < 32; ++i) {
            int offset = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + offset;
            int green = (i >> 1 & 1) * 170 + offset;
            int blue = (i & 1) * 170 + offset;

            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            palette[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }

        return palette;
    }
}
