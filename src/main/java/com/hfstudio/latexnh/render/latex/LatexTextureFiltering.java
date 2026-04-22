package com.hfstudio.latexnh.render.latex;

import org.lwjgl.opengl.GL11;

public enum LatexTextureFiltering {

    LINEAR(GL11.GL_LINEAR),
    NEAREST(GL11.GL_NEAREST);

    private final int glConstant;

    LatexTextureFiltering(int glConstant) {
        this.glConstant = glConstant;
    }

    public int getGlConstant() {
        return glConstant;
    }
}
