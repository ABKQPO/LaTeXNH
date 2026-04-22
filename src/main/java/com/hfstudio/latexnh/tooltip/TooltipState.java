package com.hfstudio.latexnh.tooltip;

public final class TooltipState {

    public static final TooltipState INSTANCE = new TooltipState();

    private Object ownerScreen;
    private boolean mouseFrozen;
    private boolean mouseOverrideActive;
    private int logicalMouseX;
    private int logicalMouseY;
    private int actualMouseX;
    private int actualMouseY;
    private int logicalRawMouseX;
    private int logicalRawMouseY;
    private int actualRawMouseX;
    private int actualRawMouseY;

    public void captureRaw(boolean altDown, Object screen, int rawMouseX, int rawMouseY, int displayWidth,
        int displayHeight, int scaledWidth, int scaledHeight) {
        int mouseX = displayWidth <= 0 ? 0 : rawMouseX * scaledWidth / displayWidth;
        int mouseY = displayHeight <= 0 ? 0 : scaledHeight - rawMouseY * scaledHeight / displayHeight - 1;
        capture(altDown, screen, mouseX, mouseY, rawMouseX, rawMouseY);
    }

    public void capture(boolean altDown, Object screen, int mouseX, int mouseY, int rawMouseX, int rawMouseY) {
        actualMouseX = mouseX;
        actualMouseY = mouseY;
        actualRawMouseX = rawMouseX;
        actualRawMouseY = rawMouseY;

        if (!altDown) {
            mouseFrozen = false;
            ownerScreen = screen;
            logicalMouseX = mouseX;
            logicalMouseY = mouseY;
            logicalRawMouseX = rawMouseX;
            logicalRawMouseY = rawMouseY;
            return;
        }

        if (!mouseFrozen || ownerScreen != screen) {
            mouseFrozen = true;
            ownerScreen = screen;
            logicalMouseX = mouseX;
            logicalMouseY = mouseY;
            logicalRawMouseX = rawMouseX;
            logicalRawMouseY = rawMouseY;
        }
    }

    public void beginMouseOverride() {
        mouseOverrideActive = true;
    }

    public void endMouseOverride() {
        mouseOverrideActive = false;
    }

    public void clear() {
        ownerScreen = null;
        mouseFrozen = false;
        mouseOverrideActive = false;
        logicalMouseX = 0;
        logicalMouseY = 0;
        actualMouseX = 0;
        actualMouseY = 0;
        logicalRawMouseX = 0;
        logicalRawMouseY = 0;
        actualRawMouseX = 0;
        actualRawMouseY = 0;
    }

    public boolean isMouseFrozen() {
        return mouseFrozen;
    }

    public boolean isMouseOverrideActive() {
        return mouseOverrideActive;
    }

    public int getLogicalMouseX() {
        return logicalMouseX;
    }

    public int getLogicalMouseY() {
        return logicalMouseY;
    }

    public int getActualMouseX() {
        return actualMouseX;
    }

    public int getActualMouseY() {
        return actualMouseY;
    }

    public int getLogicalRawMouseX() {
        return logicalRawMouseX;
    }

    public int getLogicalRawMouseY() {
        return logicalRawMouseY;
    }

    public int getActualRawMouseX() {
        return actualRawMouseX;
    }

    public int getActualRawMouseY() {
        return actualRawMouseY;
    }
}
