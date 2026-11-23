package com.lavandula.lavendrecore.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

/** Records hotbar bounds for the current HUD render pass. */
public final class ToolSlotHotbarLayout {
    private static final Identifier HOTBAR = Identifier.ofVanilla("hud/hotbar");
    private static final Identifier OFFHAND_LEFT = Identifier.ofVanilla("hud/hotbar_offhand_left");
    private static final Identifier OFFHAND_RIGHT = Identifier.ofVanilla("hud/hotbar_offhand_right");
    private static final Identifier ATTACK_BACKGROUND = Identifier.ofVanilla("hud/hotbar_attack_indicator_background");
    private static @Nullable ToolSlotHotbarLayout current;

    private final DrawContext context;
    private @Nullable Bounds hotbar;
    private @Nullable Bounds offhand;
    private @Nullable Bounds attack;

    public ToolSlotHotbarLayout(DrawContext context) {
        this.context = context;
    }

    public void capture(Runnable drawHud) {
        ToolSlotHotbarLayout previous = current;
        current = this;
        try {
            drawHud.run();
        } finally {
            current = previous;
        }
    }

    /** A replacement hotbar can report its rectangle while drawing the HUD, using its current GUI transform. */
    public static void reportHotbar(DrawContext context, float x, float y, float width, float height) {
        if (current != null && current.context == context && width > 0 && height > 0) {
            current.hotbar = Bounds.transformed(context, x, y, width, height);
        }
    }

    public static void onGuiSprite(DrawContext context, Identifier sprite, int x, int y, int width, int height) {
        if (current == null || current.context != context) {
            return;
        }
        if (HOTBAR.equals(sprite)) {
            reportHotbar(context, x, y, width, height);
        } else if (OFFHAND_LEFT.equals(sprite) || OFFHAND_RIGHT.equals(sprite)) {
            current.offhand = Bounds.transformed(context, x, y, width, height);
        } else if (ATTACK_BACKGROUND.equals(sprite)) {
            current.attack = Bounds.transformed(context, x, y, width, height);
        }
    }

    @Nullable Position indicatorPosition(int size, int gap) {
        if (hotbar == null) {
            return null;
        }
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int y = Math.round((hotbar.top + hotbar.bottom - size) / 2);
        y = Math.clamp(y, 0, Math.max(0, height - size));
        float right = Math.max(rightEdge(offhand, y, size), rightEdge(attack, y, size));
        int x = (int) Math.ceil(right) + gap;
        if (x + size > width) {
            x = Math.clamp((int) Math.ceil(hotbar.right) - size, 0, Math.max(0, width - size));
            int above = (int) Math.floor(hotbar.top) - size - gap;
            int below = (int) Math.ceil(hotbar.bottom) + gap;
            y = above >= 0 ? above : Math.min(below, Math.max(0, height - size));
        }
        return new Position(Math.max(0, x), y);
    }

    private float rightEdge(@Nullable Bounds accessory, int y, int size) {
        if (accessory != null && accessory.left >= hotbar.right - 1 && accessory.top < y + size && accessory.bottom > y) {
            return Math.max(hotbar.right, accessory.right);
        }
        return hotbar.right;
    }

    record Position(int x, int y) {
    }

    private record Bounds(float left, float top, float right, float bottom) {
        static Bounds transformed(DrawContext context, float x, float y, float width, float height) {
            Matrix4fc matrix = context.getMatrices().peek().getPositionMatrix();
            float originX = matrix.m00() * x + matrix.m10() * y + matrix.m30();
            float originY = matrix.m01() * x + matrix.m11() * y + matrix.m31();
            float widthX = matrix.m00() * width;
            float widthY = matrix.m01() * width;
            float heightX = matrix.m10() * height;
            float heightY = matrix.m11() * height;
            return new Bounds(originX + Math.min(0.0F, widthX) + Math.min(0.0F, heightX),
                    originY + Math.min(0.0F, widthY) + Math.min(0.0F, heightY),
                    originX + Math.max(0.0F, widthX) + Math.max(0.0F, heightX),
                    originY + Math.max(0.0F, widthY) + Math.max(0.0F, heightY));
        }
    }
}
