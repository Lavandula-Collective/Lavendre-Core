package com.lavandula.lavendrecore;

public record ToolSlotSettings(int indicatorX, int indicatorY, int inventoryX, int inventoryY,
                               Anchor inventoryAnchor, int indicatorHoldMs, boolean fade, boolean invertRightClick,
                               boolean hideInCreative) {
    public static final int MAX_OFFSET = 4096;
    public static final int MAX_HOLD_MS = 60000;

    public ToolSlotSettings {
        indicatorX = Math.clamp(indicatorX, -MAX_OFFSET, MAX_OFFSET);
        indicatorY = Math.clamp(indicatorY, -MAX_OFFSET, MAX_OFFSET);
        inventoryX = Math.clamp(inventoryX, -MAX_OFFSET, MAX_OFFSET);
        inventoryY = Math.clamp(inventoryY, -MAX_OFFSET, MAX_OFFSET);
        indicatorHoldMs = Math.clamp(indicatorHoldMs, 0, MAX_HOLD_MS);
        if (inventoryAnchor == null) {
            inventoryAnchor = Anchor.CENTER;
        }
    }

    public static ToolSlotSettings defaults() {
        return new ToolSlotSettings(0, 0, 0, 0, Anchor.CENTER, 260, true, false, true);
    }

    public enum Anchor {
        LEFT, CENTER, RIGHT
    }
}
