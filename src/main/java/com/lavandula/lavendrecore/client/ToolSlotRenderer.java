package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreCore;
import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.ToolSlotSettings;
import com.lavandula.lavendrecore.toolslot.ToolSlotInventory;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;

public final class ToolSlotRenderer {
    private static final int ICON_SIZE = 16;
    private static final int ROW_WIDTH = 96;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_INSET = 4;
    static final int INDICATOR_SIZE = 22;
    private static final int INDICATOR_INSET = (INDICATOR_SIZE - ICON_SIZE) / 2;
    private static final Identifier INVENTORY = Identifier.of(LavendreCore.MOD_ID, "tool_slot/inventory");
    private static final Identifier OVERLAY = Identifier.of(LavendreCore.MOD_ID, "tool_slot/overlay");
    private static final Identifier INDICATOR = Identifier.of(LavendreCore.MOD_ID, "tool_slot/indicator");

    private ToolSlotRenderer() {
    }

    public static int inventoryFirstX(int width) {
        ToolSlotSettings settings = LavendreConfig.clientToolSlot();
        int space = width - ROW_WIDTH;
        int anchor = switch (settings.inventoryAnchor()) {
            case LEFT -> 0;
            case CENTER -> (space + 1) / 2;
            case RIGHT -> space;
        };
        return anchor + ROW_INSET + settings.inventoryX();
    }

    public static int inventoryY(int defaultY) {
        return defaultY + LavendreConfig.clientToolSlot().inventoryY();
    }

    public static boolean hasVisibleSlots(List<DedicatedToolSlot> slots) {
        return !slots.isEmpty() && slots.getFirst().isEnabled();
    }

    public static void drawInventory(DrawContext context, ToolSlotInventory inventory, int firstX, int y) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(INVENTORY, firstX - ROW_INSET, y - ROW_INSET, ROW_WIDTH, ROW_HEIGHT);
        for (int index = 0; index < inventory.size(); index++) {
            if (inventory.getStack(index).isEmpty()) {
                int offset = index * DedicatedToolSlot.SPACING;
                context.drawGuiTexture(OVERLAY, ROW_WIDTH, ROW_HEIGHT, ROW_INSET + offset, ROW_INSET,
                        firstX + offset, y, ICON_SIZE, ICON_SIZE);
            }
        }
        RenderSystem.disableBlend();
    }

    public static boolean isWithinInventoryRow(double x, double y, int firstX, int slotY, List<DedicatedToolSlot> slots) {
        int left = firstX - ROW_INSET;
        int top = slotY - ROW_INSET;
        return hasVisibleSlots(slots) && x >= left && x < left + ROW_WIDTH && y >= top && y < top + ROW_HEIGHT;
    }

    static void drawIndicator(DrawContext context, MinecraftClient client, PlayerEntity player, ItemStack tool, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.drawGuiTexture(INDICATOR, x, y, INDICATOR_SIZE, INDICATOR_SIZE);
        RenderSystem.disableBlend();
        context.drawItem(player, tool, x + INDICATOR_INSET, y + INDICATOR_INSET, 0);
        context.drawItemInSlot(client.textRenderer, tool, x + INDICATOR_INSET, y + INDICATOR_INSET);
    }
}
