package com.lavandula.lavendrecore.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface ActiveToolSlot {
    static ItemStack get(MinecraftClient client) {
        return client.interactionManager instanceof ActiveToolSlot toolSlots ? toolSlots.lavendre$getActiveToolSlot() : ItemStack.EMPTY;
    }

    ItemStack lavendre$getActiveToolSlot();

    static ItemStack renderMainHand(LivingEntity entity, ItemStack hotbarStack) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemStack tool = entity == client.player ? get(client)
                : entity instanceof Remote remote ? remote.lavendre$getActiveToolSlot() : ItemStack.EMPTY;
        return tool.isEmpty() ? hotbarStack : tool;
    }

    interface Remote extends ActiveToolSlot {
        void lavendre$setActiveToolSlot(ItemStack stack);
    }
}
