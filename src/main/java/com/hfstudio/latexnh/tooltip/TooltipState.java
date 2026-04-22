package com.hfstudio.latexnh.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hfstudio.latexnh.render.latex.LatexRenderEntry;

public final class TooltipState {

    public static final TooltipState INSTANCE = new TooltipState();

    private static final int HOVER_STICKY_MARGIN = 2;
    private static final int HOVER_STICKY_MISSES = 1;

    private Object ownerScreen;
    private boolean mouseFrozen;
    private boolean mouseOverrideActive;
    private boolean inlineTooltipRendered;
    private int logicalMouseX;
    private int logicalMouseY;
    private int actualMouseX;
    private int actualMouseY;
    private int logicalRawMouseX;
    private int logicalRawMouseY;
    private int actualRawMouseX;
    private int actualRawMouseY;
    private LatexRenderEntry stableHoveredEntry;
    private int stableHoveredMissesRemaining;
    private SelectedTooltipRequest selectedTooltipRequest;

    public void beginFrame() {
        inlineTooltipRendered = false;
        selectedTooltipRequest = null;
    }

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
            stableHoveredEntry = null;
            stableHoveredMissesRemaining = 0;
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
        inlineTooltipRendered = false;
        logicalMouseX = 0;
        logicalMouseY = 0;
        actualMouseX = 0;
        actualMouseY = 0;
        logicalRawMouseX = 0;
        logicalRawMouseY = 0;
        actualRawMouseX = 0;
        actualRawMouseY = 0;
        stableHoveredEntry = null;
        stableHoveredMissesRemaining = 0;
        selectedTooltipRequest = null;
    }

    public void markInlineTooltipRendered() {
        inlineTooltipRendered = true;
    }

    public boolean hasInlineTooltipRendered() {
        return inlineTooltipRendered;
    }

    public LatexRenderEntry resolveStableHoveredEntry(LatexRenderEntry currentEntry, int mouseX, int mouseY) {
        if (currentEntry != null) {
            stableHoveredEntry = currentEntry;
            stableHoveredMissesRemaining = HOVER_STICKY_MISSES;
            return currentEntry;
        }

        if (stableHoveredEntry != null && stableHoveredMissesRemaining > 0
            && mouseX >= stableHoveredEntry.x - HOVER_STICKY_MARGIN
            && mouseX <= stableHoveredEntry.x + stableHoveredEntry.w + HOVER_STICKY_MARGIN
            && mouseY >= stableHoveredEntry.y - HOVER_STICKY_MARGIN
            && mouseY <= stableHoveredEntry.y + stableHoveredEntry.h + HOVER_STICKY_MARGIN) {
            stableHoveredMissesRemaining--;
            return stableHoveredEntry;
        }

        stableHoveredEntry = null;
        stableHoveredMissesRemaining = 0;
        return null;
    }

    public void requestSelectedLatexTooltip(String formula, float renderScale, int anchorX, int anchorY,
        boolean followMouse) {
        if (formula == null || formula.isEmpty()) {
            return;
        }
        selectedTooltipRequest = SelectedTooltipRequest.latex(formula, renderScale, anchorX, anchorY, followMouse);
    }

    public void requestSelectedTextTooltip(List<String> lines, int anchorX, int anchorY, boolean followMouse) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        selectedTooltipRequest = SelectedTooltipRequest.text(lines, anchorX, anchorY, followMouse);
    }

    public SelectedTooltipRequest getSelectedTooltipRequest() {
        return selectedTooltipRequest;
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

    public static final class SelectedTooltipRequest {

        private final boolean renderLatex;
        private final String formula;
        private final List<String> textLines;
        private final float renderScale;
        private final int anchorX;
        private final int anchorY;
        private final boolean followMouse;

        private SelectedTooltipRequest(boolean renderLatex, String formula, List<String> textLines, float renderScale,
            int anchorX, int anchorY, boolean followMouse) {
            this.renderLatex = renderLatex;
            this.formula = formula;
            this.textLines = textLines;
            this.renderScale = renderScale > 0.0f ? renderScale : 1.0f;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.followMouse = followMouse;
        }

        public static SelectedTooltipRequest latex(String formula, float renderScale, int anchorX, int anchorY,
            boolean followMouse) {
            return new SelectedTooltipRequest(
                true,
                formula,
                Collections.emptyList(),
                renderScale,
                anchorX,
                anchorY,
                followMouse);
        }

        public static SelectedTooltipRequest text(List<String> textLines, int anchorX, int anchorY,
            boolean followMouse) {
            return new SelectedTooltipRequest(
                false,
                "",
                Collections.unmodifiableList(new ArrayList<>(textLines)),
                1.0f,
                anchorX,
                anchorY,
                followMouse);
        }

        public boolean renderLatex() {
            return renderLatex;
        }

        public String formula() {
            return formula;
        }

        public List<String> textLines() {
            return textLines;
        }

        public float renderScale() {
            return renderScale;
        }

        public int anchorX() {
            return anchorX;
        }

        public int anchorY() {
            return anchorY;
        }

        public boolean followMouse() {
            return followMouse;
        }
    }
}
