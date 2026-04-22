package com.hfstudio.latexnh.render.latex;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.scilab.forge.jlatexmath.TeXFormula;

import com.hfstudio.latexnh.LaTeXNH;
import com.hfstudio.latexnh.config.ModConfig;

public final class LatexFontResolver {

    private static final Object LOCK = new Object();
    private static final int NO_TEX_TYPE = -1;
    private static final String BUNDLED_UNIFONT_RESOURCE = "/assets/latexnh/fonts/unifont-16.0.04.otf";

    private static final Set<Character.UnicodeBlock> ACTIVE_EXTERNAL_BLOCKS = new HashSet<>();

    private static String activeExternalFontFamily;
    private static String cachedConfigKey;
    private static Selection cachedSelection;
    private static Set<String> cachedAvailableFonts;
    private static boolean bundledUnifontLoadAttempted;
    private static String bundledUnifontFamily;

    private LatexFontResolver() {}

    public static Selection resolveConfiguredSelection() {
        synchronized (LOCK) {
            String configKey = buildConfigKey(ModConfig.render.fontFamily, ModConfig.render.customSystemFontName);
            if (cachedSelection != null && configKey.equals(cachedConfigKey)) {
                return cachedSelection;
            }

            cachedSelection = resolve(
                ModConfig.render.fontFamily,
                ModConfig.render.customSystemFontName,
                getAvailableFonts(),
                LatexFontResolver::ensureBundledUnifontLoaded);
            cachedConfigKey = configKey;
            return cachedSelection;
        }
    }

    public static Selection resolve(LatexFontFamily family, String customSystemFontName, Set<String> availableFonts,
        Supplier<String> bundledUnifontFamilySupplier) {
        LatexFontFamily safeFamily = family == null ? LatexFontFamily.DEFAULT : family;

        return switch (safeFamily) {
            case SERIF -> new Selection(TeXFormula.SERIF, null, "serif");
            case SANS_SERIF -> new Selection(TeXFormula.SANSSERIF, null, "sans_serif");
            case TYPEWRITER -> new Selection(TeXFormula.TYPEWRITER, null, "typewriter");
            case UNIFONT -> {
                String familyName = normalizeFamilyName(bundledUnifontFamilySupplier.get());
                yield familyName == null ? defaultSelection()
                    : new Selection(NO_TEX_TYPE, familyName, "unifont:" + familyName);
            }
            case SYSTEM -> {
                String requestedFamily = normalizeFamilyName(customSystemFontName);
                String matchedFamily = requestedFamily == null ? null
                    : findMatchingFontFamily(requestedFamily, availableFonts);
                yield matchedFamily == null ? defaultSelection()
                    : new Selection(NO_TEX_TYPE, matchedFamily, "system:" + matchedFamily);
            }
            case DEFAULT -> defaultSelection();
        };
    }

    public static void applyConfiguredSelection(String formula) {
        applySelection(formula, resolveConfiguredSelection());
    }

    public static void applySelection(String formula, Selection selection) {
        synchronized (LOCK) {
            if (selection == null || !selection.usesExternalFont()) {
                clearActiveExternalFonts();
                return;
            }

            String externalFontFamily = selection.getExternalFontFamily();
            if (!externalFontFamily.equals(activeExternalFontFamily)) {
                clearActiveExternalFonts();
                activeExternalFontFamily = externalFontFamily;
            }

            for (Character.UnicodeBlock block : collectUnicodeBlocks(formula)) {
                if (!ACTIVE_EXTERNAL_BLOCKS.add(block)) {
                    continue;
                }
                TeXFormula.registerExternalFont(block, externalFontFamily);
            }
        }
    }

    public static void invalidateRuntimeState() {
        synchronized (LOCK) {
            cachedSelection = null;
            cachedConfigKey = null;
            cachedAvailableFonts = null;
            clearActiveExternalFonts();
        }
    }

    private static Selection defaultSelection() {
        return new Selection(NO_TEX_TYPE, null, "default");
    }

    private static String buildConfigKey(LatexFontFamily family, String customSystemFontName) {
        LatexFontFamily safeFamily = family == null ? LatexFontFamily.DEFAULT : family;
        return safeFamily.name() + ':' + (customSystemFontName == null ? "" : customSystemFontName.trim());
    }

    private static Set<String> getAvailableFonts() {
        synchronized (LOCK) {
            if (cachedAvailableFonts != null) {
                return cachedAvailableFonts;
            }

            try {
                String[] familyNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames(Locale.ROOT);
                cachedAvailableFonts = new HashSet<>(Arrays.asList(familyNames));
            } catch (Throwable throwable) {
                LaTeXNH.LOG.warn(
                    "[LaTeXNH] Failed to enumerate system fonts, falling back to JLaTeXMath defaults.",
                    throwable);
                cachedAvailableFonts = new HashSet<>();
            }

            return cachedAvailableFonts;
        }
    }

    private static String ensureBundledUnifontLoaded() {
        synchronized (LOCK) {
            if (bundledUnifontLoadAttempted) {
                return bundledUnifontFamily;
            }

            bundledUnifontLoadAttempted = true;

            try (InputStream stream = LatexFontResolver.class.getResourceAsStream(BUNDLED_UNIFONT_RESOURCE)) {
                if (stream == null) {
                    LaTeXNH.LOG.warn("[LaTeXNH] Bundled Unifont resource is missing: {}", BUNDLED_UNIFONT_RESOURCE);
                    return null;
                }

                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .registerFont(font);
                bundledUnifontFamily = normalizeFamilyName(font.getFamily(Locale.ROOT));
                if (bundledUnifontFamily == null) {
                    bundledUnifontFamily = normalizeFamilyName(font.getFontName(Locale.ROOT));
                }
                if (bundledUnifontFamily != null && cachedAvailableFonts != null) {
                    cachedAvailableFonts.add(bundledUnifontFamily);
                }
                return bundledUnifontFamily;
            } catch (Exception exception) {
                LaTeXNH.LOG
                    .warn("[LaTeXNH] Failed to load bundled Unifont, falling back to JLaTeXMath defaults.", exception);
                return null;
            }
        }
    }

    private static String findMatchingFontFamily(String requestedFamily, Set<String> availableFonts) {
        for (String family : availableFonts == null ? Collections.<String>emptySet() : availableFonts) {
            if (family.equalsIgnoreCase(requestedFamily)) {
                return family;
            }
        }
        return null;
    }

    private static Set<Character.UnicodeBlock> collectUnicodeBlocks(String formula) {
        Set<Character.UnicodeBlock> blocks = new HashSet<>();
        if (formula != null && !formula.isEmpty()) {
            formula.codePoints()
                .mapToObj(Character.UnicodeBlock::of)
                .filter(Objects::nonNull)
                .forEach(blocks::add);
        }

        if (blocks.isEmpty()) {
            blocks.add(Character.UnicodeBlock.BASIC_LATIN);
        }
        return blocks;
    }

    private static void clearActiveExternalFonts() {
        if (ACTIVE_EXTERNAL_BLOCKS.isEmpty()) {
            activeExternalFontFamily = null;
            return;
        }

        for (Character.UnicodeBlock block : ACTIVE_EXTERNAL_BLOCKS) {
            TeXFormula.registerExternalFont(block, null, null);
        }
        ACTIVE_EXTERNAL_BLOCKS.clear();
        activeExternalFontFamily = null;
    }

    private static String normalizeFamilyName(String familyName) {
        if (familyName == null) {
            return null;
        }

        String normalized = familyName.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static final class Selection {

        private final int teXType;
        private final String externalFontFamily;
        private final String cacheToken;

        private Selection(int teXType, String externalFontFamily, String cacheToken) {
            this.teXType = teXType;
            this.externalFontFamily = externalFontFamily;
            this.cacheToken = cacheToken;
        }

        public int getTeXType() {
            return teXType;
        }

        public String getExternalFontFamily() {
            return externalFontFamily;
        }

        public String getCacheToken() {
            return cacheToken;
        }

        public boolean usesExternalFont() {
            return externalFontFamily != null && !externalFontFamily.isEmpty();
        }
    }
}
