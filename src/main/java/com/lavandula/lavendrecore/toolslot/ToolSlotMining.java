package com.lavandula.lavendrecore.toolslot;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class ToolSlotMining {
    private ToolSlotMining() {
    }

    @Nullable
    public static DedicatedToolSlot select(PlayerEntity player, BlockState state, BlockPos pos) {
        World world = player.getWorld();
        if (!ToolSlotUse.isAvailable(player) || state.isAir() || state.getHardness(world, pos) < 0.0F) {
            return null;
        }

        boolean toolRequired = state.isToolRequired();
        ItemStack held = ToolSlotUse.heldStack(player.getInventory());
        if (miningSpeed(held, state, toolRequired) != Float.NEGATIVE_INFINITY && held.getItem().canMine(state, world, pos, player)) {
            return null;
        }

        DedicatedToolSlot selected = null;
        float fastest = Float.NEGATIVE_INFINITY;
        var enabledFeatures = world.getEnabledFeatures();
        for (DedicatedToolSlot slot : ToolSlotScreenHandler.of(player).lavendre$getDedicatedToolSlots()) {
            ItemStack tool = slot.getStack();
            if (!slot.canInsert(tool)) {
                continue;
            }
            float speed = miningSpeed(tool, state, toolRequired);
            if (speed != Float.NEGATIVE_INFINITY && (selected == null || speed > fastest)
                    && tool.isItemEnabled(enabledFeatures) && tool.getItem().canMine(state, world, pos, player)) {
                selected = slot;
                fastest = speed;
            }
        }
        return selected;
    }

    private static float miningSpeed(ItemStack stack, BlockState state, boolean toolRequired) {
        if (stack.isEmpty()) {
            return Float.NEGATIVE_INFINITY;
        }
        boolean suitable = stack.isSuitableFor(state);
        if (toolRequired && !suitable) {
            return Float.NEGATIVE_INFINITY;
        }
        float speed = stack.getMiningSpeedMultiplier(state);
        return suitable || speed > 1.0F ? speed : Float.NEGATIVE_INFINITY;
    }
}
