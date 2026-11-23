package com.lavandula.lavendrecore.toolslot;

import com.lavandula.lavendrecore.compat.BetterCombatCompat;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public final class ToolSlotActions {
    private static final List<ToolType> SHOVEL_FIRST = List.of(ToolType.AXE, ToolType.SHOVEL, ToolType.HOE);
    private static final List<ToolType> HOE_FIRST = List.of(ToolType.AXE, ToolType.HOE, ToolType.SHOVEL);

    private ToolSlotActions() {
    }

    public static boolean isWeapon(ItemStack stack) {
        return ToolType.SWORD.accepts(stack) || ToolType.AXE.accepts(stack) || stack.isOf(Items.TRIDENT)
                || stack.isOf(Items.MACE) || BetterCombatCompat.hasAttacks(stack);
    }

    public static ItemStack readySword(PlayerEntity player) {
        if (ToolType.SWORD.accepts(ToolSlotUse.heldStack(player.getInventory()))) {
            return ItemStack.EMPTY;
        }
        DedicatedToolSlot sword = ToolSlotUse.availableSlot(player, ToolType.SWORD);
        return sword == null ? ItemStack.EMPTY : sword.getStack();
    }

    @Nullable
    public static DedicatedToolSlot swordForAttack(PlayerEntity player, Entity target) {
        if (!(target instanceof LivingEntity || target instanceof EnderDragonPart) || !target.isAttackable()
                || player.isUsingRiptide() || isWeapon(ToolSlotUse.heldStack(player.getInventory()))) {
            return null;
        }
        return ToolSlotUse.availableSlot(player, ToolType.SWORD);
    }

    public static ActionResult useOnBlock(PlayerEntity player, Hand hand, BlockHitResult hit, boolean invert,
                                          @Nullable Consumer<DedicatedToolSlot> onToolUsed) {
        if (hand != Hand.MAIN_HAND || !ToolSlotUse.isAvailable(player)
                || !player.getWorld().getBlockState(hit.getBlockPos()).getBlock().isEnabled(player.getWorld().getEnabledFeatures())) {
            return ActionResult.PASS;
        }

        List<ToolType> order = player.isSneaking() != invert ? HOE_FIRST : SHOVEL_FIRST;
        for (ToolType type : order) {
            DedicatedToolSlot slot = ToolSlotUse.availableSlot(player, type);
            if (slot == null || player.getItemCooldownManager().isCoolingDown(slot.getStack().getItem())) {
                continue;
            }

            ItemStack tool = slot.getStack();
            ItemStack used = player instanceof ServerPlayerEntity ? tool.copy() : ItemStack.EMPTY;
            ActionResult result = ToolSlotUse.withTool(player, slot, () -> tool.useOnBlock(new ItemUsageContext(player, hand, hit)));
            if (result.isAccepted()) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayer, hit.getBlockPos(), used);
                }
                if (onToolUsed != null) {
                    onToolUsed.accept(slot);
                }
            }
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    }
}
