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

    private LatexTextureCache() {}

    public int[] get(String formula) {
        return cache.get(formula);
    }

    public void put(String formula, int textureId, int widthPx, int heightPx) {
        cache.put(formula, new int[] { textureId, widthPx, heightPx });
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

    public void clearAll() {
        for (int[] v : cache.values()) {
            GL11.glDeleteTextures(v[0]);
        }
        cache.clear();
        errorMessages.clear();
    }
}
