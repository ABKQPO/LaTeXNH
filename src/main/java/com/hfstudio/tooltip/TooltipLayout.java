package com.hfstudio.tooltip;

public final class TooltipLayout {

    private TooltipLayout() {}

    public static Position positionTextTooltip(int mouseX, int mouseY, int tooltipTextWidth, int tooltipHeight,
        int screenWidth, int screenHeight) {
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - tooltipHeight - 4;

        if (tooltipX + tooltipTextWidth > screenWidth - 4) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
        }
        if (tooltipX < 4) {
            tooltipX = 4;
        }
        if (tooltipY + tooltipHeight + 6 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 6;
        }
        if (tooltipY < 4) {
            tooltipY = 4;
        }

        return new Position(tooltipX, tooltipY);
    }

    public static FloatPosition positionLatexTooltip(float mouseX, float mouseY, float renderWidth, float renderHeight,
        int screenWidth, int screenHeight) {
        float tooltipX = mouseX + 14.0F;
        float tooltipY = mouseY - renderHeight - 5.0F;

        if (tooltipX + renderWidth + 10.0F > screenWidth) {
            tooltipX = mouseX - renderWidth - 14.0F;
        }
        if (tooltipY < 5.0F) {
            tooltipY = 5.0F;
        }
        if (tooltipY + renderHeight + 10.0F > screenHeight) {
            tooltipY = screenHeight - renderHeight - 10.0F;
        }

        return new FloatPosition(tooltipX, tooltipY);
    }

    public static final class Position {

        public final int x;
        public final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static final class FloatPosition {

        public final float x;
        public final float y;

        public FloatPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
