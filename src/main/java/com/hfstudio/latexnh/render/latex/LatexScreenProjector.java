package com.hfstudio.latexnh.render.latex;

public final class LatexScreenProjector {

    private static final float EPSILON = 0.001f;

    public static ScreenRect projectGuiRect(float left, float top, float width, float height, float[] modelView,
        float[] projection, int[] viewport, int scaleFactor, int scaledHeight) {
        if (modelView == null || projection == null
            || viewport == null
            || modelView.length < 16
            || projection.length < 16
            || viewport.length < 4
            || scaleFactor <= 0
            || scaledHeight <= 0) {
            return null;
        }

        ScreenPoint topLeft = projectPoint(left, top, modelView, projection, viewport, scaleFactor, scaledHeight);
        ScreenPoint bottomRight = projectPoint(
            left + width,
            top + height,
            modelView,
            projection,
            viewport,
            scaleFactor,
            scaledHeight);
        if (topLeft == null || bottomRight == null) {
            return null;
        }

        float minX = Math.min(topLeft.x, bottomRight.x);
        float minY = Math.min(topLeft.y, bottomRight.y);
        float maxX = Math.max(topLeft.x, bottomRight.x);
        float maxY = Math.max(topLeft.y, bottomRight.y);

        return new ScreenRect(
            (int) Math.floor(minX + EPSILON),
            (int) Math.floor(minY + EPSILON),
            Math.max(1, (int) Math.ceil(Math.max(0f, maxX - minX) - EPSILON)),
            Math.max(1, (int) Math.ceil(Math.max(0f, maxY - minY) - EPSILON)));
    }

    private static ScreenPoint projectPoint(float x, float y, float[] modelView, float[] projection, int[] viewport,
        int scaleFactor, int scaledHeight) {
        float[] point = new float[] { x, y, 0f, 1f };
        float[] eye = multiply(modelView, point);
        float[] clip = multiply(projection, eye);
        float w = clip[3];
        if (Math.abs(w) < 1.0e-6f) {
            return null;
        }

        float ndcX = clip[0] / w;
        float ndcY = clip[1] / w;
        float windowX = viewport[0] + (1f + ndcX) * viewport[2] * 0.5f;
        float windowY = viewport[1] + (1f + ndcY) * viewport[3] * 0.5f;

        float guiX = windowX / scaleFactor;
        float guiY = scaledHeight - windowY / scaleFactor;
        return new ScreenPoint(guiX, guiY);
    }

    private static float[] multiply(float[] matrix, float[] vector) {
        return new float[] {
            matrix[0] * vector[0] + matrix[4] * vector[1] + matrix[8] * vector[2] + matrix[12] * vector[3],
            matrix[1] * vector[0] + matrix[5] * vector[1] + matrix[9] * vector[2] + matrix[13] * vector[3],
            matrix[2] * vector[0] + matrix[6] * vector[1] + matrix[10] * vector[2] + matrix[14] * vector[3],
            matrix[3] * vector[0] + matrix[7] * vector[1] + matrix[11] * vector[2] + matrix[15] * vector[3] };
    }

    public static final class ScreenRect {

        public final int x;
        public final int y;
        public final int width;
        public final int height;

        public ScreenRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static final class ScreenPoint {

        private final float x;
        private final float y;

        private ScreenPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private LatexScreenProjector() {}
}
