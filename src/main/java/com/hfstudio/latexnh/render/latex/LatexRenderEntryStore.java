package com.hfstudio.latexnh.render.latex;

import java.util.ArrayList;
import java.util.List;

final class LatexRenderEntryStore {

    private final List<LatexRenderEntry> currentEntries = new ArrayList<>();
    private final List<LatexRenderEntry> previousEntries = new ArrayList<>();

    void beginFrame() {
        previousEntries.clear();
        previousEntries.addAll(currentEntries);
        currentEntries.clear();
    }

    void add(LatexRenderEntry entry) {
        currentEntries.add(entry);
    }

    LatexRenderEntry getEntryAt(int mouseX, int mouseY) {
        LatexRenderEntry currentHit = findEntryAt(currentEntries, mouseX, mouseY);
        return currentHit != null ? currentHit : findEntryAt(previousEntries, mouseX, mouseY);
    }

    private static LatexRenderEntry findEntryAt(List<LatexRenderEntry> entries, int mouseX, int mouseY) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            LatexRenderEntry entry = entries.get(i);
            if (mouseX >= entry.x && mouseX <= entry.x + entry.w && mouseY >= entry.y && mouseY <= entry.y + entry.h) {
                return entry;
            }
        }
        return null;
    }
}
