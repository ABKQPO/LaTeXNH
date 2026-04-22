package com.hfstudio.latexnh.render.latex;

public final class LatexFontRenderGuards {

    private static final ThreadLocal<Integer> MIXED_RENDER_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> PENDING_SMOOTHFONT_EXIT_BYPASS = ThreadLocal.withInitial(() -> 0);

    private LatexFontRenderGuards() {}

    public static boolean isMixedRenderActive() {
        return MIXED_RENDER_DEPTH.get() > 0;
    }

    public static void beginMixedRender() {
        MIXED_RENDER_DEPTH.set(MIXED_RENDER_DEPTH.get() + 1);
    }

    public static void endMixedRender() {
        int nextDepth = MIXED_RENDER_DEPTH.get() - 1;
        if (nextDepth <= 0) {
            MIXED_RENDER_DEPTH.remove();
            return;
        }
        MIXED_RENDER_DEPTH.set(nextDepth);
    }

    public static void scheduleSmoothFontExitBypass() {
        PENDING_SMOOTHFONT_EXIT_BYPASS.set(PENDING_SMOOTHFONT_EXIT_BYPASS.get() + 1);
    }

    public static boolean consumeSmoothFontExitBypass() {
        int pending = PENDING_SMOOTHFONT_EXIT_BYPASS.get();
        if (pending <= 0) {
            return false;
        }
        if (pending == 1) {
            PENDING_SMOOTHFONT_EXIT_BYPASS.remove();
        } else {
            PENDING_SMOOTHFONT_EXIT_BYPASS.set(pending - 1);
        }
        return true;
    }
}
