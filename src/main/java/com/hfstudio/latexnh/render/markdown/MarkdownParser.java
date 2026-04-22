package com.hfstudio.latexnh.render.markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXFormula;

public final class MarkdownParser {

    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC = Pattern.compile("(?<![*_])\\*([^*\n]+?)\\*(?![*_])");
    private static final Pattern STRIKETHROUGH = Pattern.compile("~~(.+?)~~");
    private static final Pattern CODE = Pattern.compile("`([^`\n]+?)`");

    public static List<TextSegment> parseSegments(String text) {
        List<TextSegment> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        int plainStart = 0;
        int cursor = 0;

        while (cursor < text.length()) {
            char ch = text.charAt(cursor);
            if (ch == '\u00a7' && cursor + 1 < text.length()) {
                cursor += 2;
                continue;
            }
            if (ch == '$' && !isEscaped(text, cursor)) {
                int delimiterLength = cursor + 1 < text.length() && text.charAt(cursor + 1) == '$' ? 2 : 1;
                int contentStart = cursor + delimiterLength;
                int contentEnd = findClosingDelimiter(text, contentStart, delimiterLength);
                if (contentEnd >= 0) {
                    if (cursor > plainStart) {
                        result.add(
                            new TextSegment(
                                text.substring(plainStart, cursor),
                                TextSegment.SegmentType.PLAIN,
                                plainStart,
                                cursor));
                    }
                    result.add(
                        new TextSegment(
                            text.substring(contentStart, contentEnd),
                            delimiterLength == 2 ? TextSegment.SegmentType.LATEX_DISPLAY
                                : TextSegment.SegmentType.LATEX_INLINE,
                            cursor,
                            contentEnd + delimiterLength));
                    cursor = contentEnd + delimiterLength;
                    plainStart = cursor;
                    continue;
                }
            }
            cursor++;
        }

        if (plainStart < text.length()) {
            result.add(
                new TextSegment(text.substring(plainStart), TextSegment.SegmentType.PLAIN, plainStart, text.length()));
        }

        if (result.isEmpty()) {
            result.add(new TextSegment(text, TextSegment.SegmentType.PLAIN, 0, text.length()));
        }

        return result;
    }

    public static String toMinecraftFormatted(String text) {
        if (text == null) return "";
        text = replaceMatcher(text, BOLD, "\u00a7l", "\u00a7r");
        text = replaceMatcher(text, ITALIC, "\u00a7o", "\u00a7r");
        text = replaceMatcher(text, STRIKETHROUGH, "\u00a7m", "\u00a7r");
        text = replaceMatcher(text, CODE, "\u00a77", "\u00a7r");
        return text;
    }

    private static String replaceMatcher(String text, Pattern pattern, String openCode, String closeCode) {
        StringBuffer sb = new StringBuffer();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(openCode + m.group(1) + closeCode));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static boolean containsMarkup(String text) {
        if (text == null) return false;
        // Quick string check before full regex scan
        if (text.contains("$") || text.contains("**")
            || text.contains("~~")
            || text.contains("`")
            || text.contains("*")) {
            return hasLatexSegments(text) || BOLD.matcher(text)
                .find()
                || ITALIC.matcher(text)
                    .find()
                || STRIKETHROUGH.matcher(text)
                    .find()
                || CODE.matcher(text)
                    .find();
        }
        return false;
    }

    public static String getFormulaAtCursor(String text, int cursorPos) {
        TextSegment segment = getLatexSegmentAtCursor(text, cursorPos);
        return segment == null ? null : segment.content;
    }

    public static TextSegment getLatexSegmentAtCursor(String text, int cursorPos) {
        if (text == null || cursorPos < 0) return null;
        for (TextSegment segment : parseSegments(text)) {
            if (segment.containsCursor(cursorPos)) {
                return segment;
            }
        }
        return null;
    }

    public static boolean isDisplayFormulaAtCursor(String text, int cursorPos) {
        TextSegment segment = getLatexSegmentAtCursor(text, cursorPos);
        return segment != null && segment.isDisplayLatex();
    }

    public static int adjustSplitPoint(String text, int splitIdx) {
        if (text == null || splitIdx <= 0) return splitIdx;
        for (TextSegment segment : parseSegments(text)) {
            if (!segment.isLatex()) {
                continue;
            }
            if (splitIdx > segment.startIndex && splitIdx < segment.endIndex) {
                return hasRenderablePrefix(text, segment.startIndex) ? segment.startIndex : segment.endIndex;
            }
        }
        return splitIdx;
    }

    public static String adjustTrimmedResult(String text, String trimmed, boolean reverse) {
        if (text == null || trimmed == null || trimmed.isEmpty() || text.isEmpty()) {
            return trimmed;
        }

        if (reverse) {
            if (!text.endsWith(trimmed)) {
                return trimmed;
            }
            int start = text.length() - trimmed.length();
            for (TextSegment segment : parseSegments(text)) {
                if (!segment.isLatex()) {
                    continue;
                }
                if (start > segment.startIndex && start < segment.endIndex) {
                    if (segment.endIndex >= text.length()) {
                        return text.substring(segment.startIndex, text.length());
                    }
                    return text.substring(Math.min(segment.endIndex, text.length()));
                }
            }
            return trimmed;
        }

        if (!text.startsWith(trimmed)) {
            return trimmed;
        }
        int end = trimmed.length();
        for (TextSegment segment : parseSegments(text)) {
            if (!segment.isLatex()) {
                continue;
            }
            if (end > segment.startIndex && end < segment.endIndex) {
                if (segment.startIndex == 0) {
                    return text.substring(0, Math.min(segment.endIndex, text.length()));
                }
                return text.substring(0, segment.startIndex);
            }
        }
        return trimmed;
    }

    public static java.util.List<String> buildErrorLines(String formula, String errMsg) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        ErrorLocation location = findErrorLocation(formula, errMsg);
        lines.add(
            EnumChatFormatting.RED + translate(
                "latexnh.tooltip.error.summary",
                Integer.valueOf(location.line),
                Integer.valueOf(location.column)));
        lines.add(EnumChatFormatting.GRAY + location.lineText);
        lines.add(EnumChatFormatting.YELLOW + buildColumnIndicator(location.lineText, location.column));
        String message = sanitiseErrorMessage(errMsg);
        if (!message.isEmpty()) {
            lines.add(EnumChatFormatting.RED + translate("latexnh.tooltip.error.message", message));
        }
        return lines;
    }

    public static java.util.List<String> buildSourceLines(String formula) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add(EnumChatFormatting.GOLD + translate("latexnh.tooltip.source.title"));
        lines.add(EnumChatFormatting.WHITE + formula);
        return lines;
    }

    private static boolean hasLatexSegments(String text) {
        for (TextSegment segment : parseSegments(text)) {
            if (segment.isLatex()) {
                return true;
            }
        }
        return false;
    }

    private static int findClosingDelimiter(String text, int start, int delimiterLength) {
        for (int i = start; i < text.length(); i++) {
            if (text.charAt(i) != '$' || isEscaped(text, i)) {
                continue;
            }
            if (delimiterLength == 2) {
                if (i + 1 < text.length() && text.charAt(i + 1) == '$' && !isEscaped(text, i + 1)) {
                    return i;
                }
                continue;
            }
            return i;
        }
        return -1;
    }

    private static boolean isEscaped(String text, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && text.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return (backslashes & 1) == 1;
    }

    private static boolean hasRenderablePrefix(String text, int endExclusive) {
        for (int i = 0; i < endExclusive; i++) {
            char ch = text.charAt(i);
            if (ch == '\u00a7' && i + 1 < endExclusive) {
                i++;
                continue;
            }
            return true;
        }
        return false;
    }

    private static ErrorLocation findErrorLocation(String formula, String errMsg) {
        int index = extractIndexFromMessage(errMsg);
        if (index < 0) {
            index = findFailureIndex(formula);
        }
        return ErrorLocation.fromIndex(formula, index);
    }

    private static int findFailureIndex(String formula) {
        try {
            new TeXFormula(formula);
            return Math.max(0, formula.length() - 1);
        } catch (ParseException ignored) {}
        for (int i = 1; i <= formula.length(); i++) {
            try {
                new TeXFormula(formula.substring(0, i));
            } catch (ParseException e) {
                return Math.max(0, i - 1);
            }
        }
        return 0;
    }

    private static int extractIndexFromMessage(String errMsg) {
        if (errMsg == null || errMsg.trim()
            .isEmpty()) {
            return -1;
        }
        Matcher matcher = Pattern.compile("(?i)(?:column|col|position|pos)\\s+(\\d+)")
            .matcher(errMsg);
        if (!matcher.find()) {
            return -1;
        }
        int value = Integer.parseInt(matcher.group(1));
        return Math.max(0, value - 1);
    }

    private static String buildColumnIndicator(String lineText, int column) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < column; i++) {
            sb.append(' ');
        }
        sb.append('^');
        return sb.toString();
    }

    private static String sanitiseErrorMessage(String errMsg) {
        if (errMsg == null) {
            return "";
        }
        String[] parts = errMsg.split("\\R");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return "";
    }

    private static String translate(String key, Object... args) {
        return args.length == 0 ? StatCollector.translateToLocal(key)
            : StatCollector.translateToLocalFormatted(key, args);
    }

    private MarkdownParser() {}

    private static final class ErrorLocation {

        private final int line;
        private final int column;
        private final String lineText;

        private ErrorLocation(int line, int column, String lineText) {
            this.line = line;
            this.column = column;
            this.lineText = lineText;
        }

        private static ErrorLocation fromIndex(String formula, int index) {
            if (formula == null || formula.isEmpty()) {
                return new ErrorLocation(1, 1, "");
            }

            int clampedIndex = Math.max(0, Math.min(index, formula.length() - 1));
            int line = 1;
            int column = 1;
            int lineStart = 0;

            for (int i = 0; i < clampedIndex; i++) {
                if (formula.charAt(i) == '\n') {
                    line++;
                    column = 1;
                    lineStart = i + 1;
                } else {
                    column++;
                }
            }

            int lineEnd = formula.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = formula.length();
            }

            return new ErrorLocation(line, column, formula.substring(lineStart, lineEnd));
        }
    }
}
