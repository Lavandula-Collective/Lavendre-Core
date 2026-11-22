package com.lavandula.lavendrecore.toolslot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

/** Shared timing and priority rules for the predicted client hand and the server's tracked hand. */
public final class ToolSlotHandState {
    private PlayerEntity owner;
    private DedicatedToolSlot slot;
    private Item tool;
    private int hotbarSlot;
    private long lastUse;

    public void recordUse(PlayerEntity player, @Nullable DedicatedToolSlot usedSlot) {
        owner = player;
        slot = usedSlot;
        tool = usedSlot == null ? null : usedSlot.getStack().getItem();
        hotbarSlot = player.getInventory().selectedSlot;
        lastUse = Util.getMeasuringTimeMs();
    }

    public boolean refreshMining(PlayerEntity player, @Nullable DedicatedToolSlot miningSlot) {
        if (owner != player || hotbarSlot != player.getInventory().selectedSlot || slot != miningSlot) {
            return false;
        }
        lastUse = Util.getMeasuringTimeMs();
        return true;
    }

    public ItemStack displayedStack(PlayerEntity player, boolean toolSlotAvailable, boolean mining, boolean pvpSwordReady) {
        if (!toolSlotAvailable || !player.isAlive() || player.isUsingItem()) {
            return ItemStack.EMPTY;
        }
        if (owner == player) {
            if (hotbarSlot != player.getInventory().selectedSlot) {
                recordUse(player, null);
            }
            if (mining || Util.getMeasuringTimeMs() - lastUse < ToolSlotScreenHandler.of(player).lavendre$getEquipMs()) {
                ItemStack active = slot == null ? ItemStack.EMPTY : slot.getStack();
                return !active.isEmpty() && active.isOf(tool) ? active : ItemStack.EMPTY;
            }
        }
        return pvpSwordReady ? ToolSlotActions.readySword(player) : ItemStack.EMPTY;
    }
}
