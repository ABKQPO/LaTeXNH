package com.hfstudio.render.latex;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class LatexRenderTracker {

    public static final LatexRenderTracker INSTANCE = new LatexRenderTracker();

    private final LatexRenderEntryStore entries = new LatexRenderEntryStore();

    private LatexRenderTracker() {}

    public void beginFrame() {
        entries.beginFrame();
    }

    public void register(String formula, int x, int y, int width, int height) {
        registerProjected(formula, x, y, width, height, false);
    }

    public void registerFailed(String formula, int x, int y, int width, int height) {
        registerProjected(formula, x, y, width, height, true);
    }

    public LatexRenderEntry getEntryAt(int mx, int my) {
        return entries.getEntryAt(mx, my);
    }

    public String getFormulaAt(int mx, int my) {
        LatexRenderEntry e = getEntryAt(mx, my);
        return e != null ? e.formula : null;
    }

    private void registerProjected(String formula, int x, int y, int width, int height, boolean failed) {
        LatexScreenProjector.ScreenRect rect = projectGuiRect(x, y, width, height);
        if (rect == null) {
            entries.add(new LatexRenderEntry(formula, x, y, width, height, failed));
            return;
        }
        entries.add(new LatexRenderEntry(formula, rect.x, rect.y, rect.width, rect.height, failed));
    }

    private LatexScreenProjector.ScreenRect projectGuiRect(int x, int y, int width, int height) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft == null) {
            return null;
        }

        ScaledResolution scaledResolution = new ScaledResolution(
            minecraft,
            minecraft.displayWidth,
            minecraft.displayHeight);
        int scaleFactor = scaledResolution.getScaleFactor();
        int scaledHeight = scaledResolution.getScaledHeight();

        FloatBuffer modelViewBuffer = BufferUtils.createFloatBuffer(16);
        FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
        // LWJGL 2 checks glGetInteger buffers against the maximum 16-int return size,
        // even for GL_VIEWPORT which only writes 4 integers.
        IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);

        float[] modelView = new float[16];
        float[] projection = new float[16];
        int[] viewport = new int[4];
        modelViewBuffer.rewind();
        projectionBuffer.rewind();
        viewportBuffer.rewind();
        modelViewBuffer.get(modelView);
        projectionBuffer.get(projection);
        viewportBuffer.get(viewport);

        return LatexScreenProjector
            .projectGuiRect(x, y, width, height, modelView, projection, viewport, scaleFactor, scaledHeight);
    }
}
