package com.hfstudio.latexnh.render.latex;

import java.util.ArrayList;
import java.util.List;

public final class LatexFormattingParser {

    public static final int DEFAULT_ARGB = 0xFFFFFFFF;

    private LatexFormattingParser() {}

    public static String stripFormattingCodes(String formula) {
        StringBuilder stripped = new StringBuilder();
        for (FormattedRun run : parseRuns(formula)) {
            stripped.append(run.text);
        }
        return stripped.toString();
    }

    public static String toRenderableFormula(String formula) {
        return toRenderableFormula(formula, DEFAULT_ARGB);
    }

    public static String toRenderableFormula(String formula, int defaultArgb) {
        return toRenderableFormula(formula, defaultArgb, null);
    }

    public static String toRenderableFormula(String formula, int defaultArgb, int[] colorPalette) {
        return toRenderableFormula(formula, defaultArgb, colorPalette, true);
    }

    public static String toRenderableFormula(String formula, int defaultArgb, int[] colorPalette,
        boolean allowFormattingColor) {
        if (!allowFormattingColor) {
            return stripFormattingCodes(formula);
        }

        List<FormattedRun> runs = parseRuns(formula, defaultArgb, colorPalette);
        if (runs.isEmpty()) {
            return "";
        }

        StringBuilder colorDefinitions = new StringBuilder();
        StringBuilder body = new StringBuilder();
        List<Integer> definedColors = new ArrayList<>();

        for (FormattedRun run : runs) {
            if (run.text.isEmpty()) {
                continue;
            }
            if (run.argb == defaultArgb) {
                body.append(run.text);
                continue;
            }

            int colorIndex = definedColors.indexOf(run.argb);
            if (colorIndex < 0) {
                definedColors.add(run.argb);
                colorIndex = definedColors.size() - 1;
                String colorName = "latexnhcolor" + colorIndex;
                appendColorDefinition(colorDefinitions, colorName, run.argb);
                body.append("\\textcolor{")
                    .append(colorName)
                    .append("}{")
                    .append(run.text)
                    .append('}');
                continue;
            }
            String colorName = "latexnhcolor" + colorIndex;
            body.append("\\textcolor{")
                .append(colorName)
                .append("}{")
                .append(run.text)
                .append('}');
        }

        return colorDefinitions.append(body)
            .toString();
    }

    public static List<FormattedRun> parseRuns(String formula) {
        return parseRuns(formula, DEFAULT_ARGB);
    }

    public static List<FormattedRun> parseRuns(String formula, int defaultArgb) {
        return parseRuns(formula, defaultArgb, null);
    }

    public static List<FormattedRun> parseRuns(String formula, int defaultArgb, int[] colorPalette) {
        List<FormattedRun> runs = new ArrayList<>();
        if (formula == null || formula.isEmpty()) {
            return runs;
        }

        StringBuilder currentText = new StringBuilder();
        MinecraftTextFormattingState formattingState = new MinecraftTextFormattingState(defaultArgb, colorPalette);
        int currentColor = formattingState.getCurrentColorArgb();

        for (int i = 0; i < formula.length(); i++) {
            char ch = formula.charAt(i);
            if (ch == MinecraftTextFormattingState.FORMATTING_CHAR && i + 1 < formula.length()
                && MinecraftTextFormattingState.isFormattingCode(formula.charAt(i + 1))) {
                char code = formula.charAt(++i);
                if (MinecraftTextFormattingState.isColorCode(code) || Character.toLowerCase(code) == 'r') {
                    flushRun(runs, currentText, currentColor);
                }
                formattingState.applyFormattingCode(code);
                currentColor = formattingState.getCurrentColorArgb();
                continue;
            }
            currentText.append(ch);
        }

        flushRun(runs, currentText, currentColor);
        return runs;
    }

    private static void flushRun(List<FormattedRun> runs, StringBuilder currentText, int currentColor) {
        if (currentText.length() == 0) {
            return;
        }
        String text = currentText.toString();
        currentText.setLength(0);
        if (text.isEmpty()) {
            return;
        }
        if (!runs.isEmpty() && runs.get(runs.size() - 1).argb == currentColor) {
            FormattedRun previous = runs.remove(runs.size() - 1);
            runs.add(new FormattedRun(previous.text + text, currentColor));
            return;
        }
        runs.add(new FormattedRun(text, currentColor));
    }

    private static void appendColorDefinition(StringBuilder sb, String colorName, int argb) {
        sb.append("\\definecolor{")
            .append(colorName)
            .append("}{rgb}{")
            .append(formatColorComponent((argb >> 16) & 0xFF))
            .append(',')
            .append(formatColorComponent((argb >> 8) & 0xFF))
            .append(',')
            .append(formatColorComponent(argb & 0xFF))
            .append('}');
    }

    private static String formatColorComponent(int value) {
        if (value <= 0) {
            return "0";
        }
        if (value >= 255) {
            return "1";
        }
        String formatted = String.format(java.util.Locale.ROOT, "%.4f", value / 255.0f);
        int trim = formatted.length();
        while (trim > 0 && formatted.charAt(trim - 1) == '0') {
            trim--;
        }
        if (trim > 0 && formatted.charAt(trim - 1) == '.') {
            trim--;
        }
        return formatted.substring(0, trim);
    }

    public static final class FormattedRun {

        public final String text;
        public final int argb;

        public FormattedRun(String text, int argb) {
            this.text = text;
            this.argb = argb;
        }
    }
}
