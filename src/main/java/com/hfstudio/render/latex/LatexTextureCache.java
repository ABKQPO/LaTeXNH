package com.hfstudio.render.latex;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

public final class LatexTextureCache {

    public static final LatexTextureCache INSTANCE = new LatexTextureCache();

    private static final int MAX_SIZE = 128;

    private final Map<String, int[]> cache = new LinkedHashMap<>(MAX_SIZE + 1, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, int[]> eldest) {
            if (size() > MAX_SIZE) {
                GL11.glDeleteTextures(eldest.getValue()[0]);
                return true;
            }
            return false;
        }
    };

    private final Map<String, String> errorMessages = new HashMap<>();
    private String activeStyleKey;

    private LatexTextureCache() {}

    public int[] get(String formula, LatexRenderStyle style) {
        return cache.get(buildCacheKey(formula, style));
    }

    public void put(String formula, LatexRenderStyle style, int textureId, int widthPx, int heightPx) {
        cache.put(buildCacheKey(formula, style), new int[] { textureId, widthPx, heightPx });
    }

    public boolean hasFailed(String formula) {
        return errorMessages.containsKey(formula);
    }

    public void markFailed(String formula, String errorMsg) {
        errorMessages.put(formula, errorMsg);
    }

    public String getError(String formula) {
        return errorMessages.get(formula);
    }

    public void onStyleChanged(String styleKey) {
        if (styleKey == null || styleKey.isEmpty()) {
            return;
        }
        if (activeStyleKey == null) {
            activeStyleKey = styleKey;
            return;
        }
        if (activeStyleKey.equals(styleKey)) {
            return;
        }

        activeStyleKey = styleKey;
        if (!cache.isEmpty()) {
            LatexCacheCleanupHandler.INSTANCE.scheduleClear();
        }
    }

    public void clearAll() {
        for (int[] v : cache.values()) {
            GL11.glDeleteTextures(v[0]);
        }
        cache.clear();
        errorMessages.clear();
    }

    private static String buildCacheKey(String formula, LatexRenderStyle style) {
        return style.cacheKey() + '\u0000' + formula;
    }
}
