package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.toolslot.ToolSlotScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class CreativeInventoryAccess {
    private static final Identifier INVENTORY_TAB = Identifier.ofVanilla("inventory");

    private CreativeInventoryAccess() {
    }

    public static ItemGroup inventoryTab() {
        return Registries.ITEM_GROUP.get(INVENTORY_TAB);
    }

    public static boolean needsInventoryTab() {
        MinecraftClient client = MinecraftClient.getInstance();
        return !LavendreConfig.clientToolSlot().hideInCreative() && client != null && client.player != null
                && client.interactionManager != null && client.interactionManager.hasCreativeInventory()
                && ToolSlotRenderer.hasVisibleSlots(ToolSlotScreenHandler.of(client.player).lavendre$getDedicatedToolSlots());
    }
}
