package com.hfstudio.render.latex;

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
        List<FormattedRun> runs = parseRuns(formula);
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
            if (run.argb == DEFAULT_ARGB) {
                body.append(run.text);
                continue;
            }

            int colorIndex = definedColors.indexOf(Integer.valueOf(run.argb));
            if (colorIndex < 0) {
                definedColors.add(Integer.valueOf(run.argb));
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
        List<FormattedRun> runs = new ArrayList<>();
        if (formula == null || formula.isEmpty()) {
            return runs;
        }

        StringBuilder currentText = new StringBuilder();
        int currentColor = DEFAULT_ARGB;

        for (int i = 0; i < formula.length(); i++) {
            char ch = formula.charAt(i);
            if (ch == '\u00a7' && i + 1 < formula.length() && isFormattingCode(formula.charAt(i + 1))) {
                flushRun(runs, currentText, currentColor);
                currentColor = applyFormattingCode(currentColor, formula.charAt(++i));
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

    private static int applyFormattingCode(int currentColor, char code) {
        switch (Character.toLowerCase(code)) {
            case '0':
                return 0xFF000000;
            case '1':
                return 0xFF0000AA;
            case '2':
                return 0xFF00AA00;
            case '3':
                return 0xFF00AAAA;
            case '4':
                return 0xFFAA0000;
            case '5':
                return 0xFFAA00AA;
            case '6':
                return 0xFFFFAA00;
            case '7':
                return 0xFFAAAAAA;
            case '8':
                return 0xFF555555;
            case '9':
                return 0xFF5555FF;
            case 'a':
                return 0xFF55FF55;
            case 'b':
                return 0xFF55FFFF;
            case 'c':
                return 0xFFFF5555;
            case 'd':
                return 0xFFFF55FF;
            case 'e':
                return 0xFFFFFF55;
            case 'f':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'r':
                return DEFAULT_ARGB;
            default:
                return currentColor;
        }
    }

    private static boolean isFormattingCode(char code) {
        switch (Character.toLowerCase(code)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'r':
                return true;
            default:
                return false;
        }
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
