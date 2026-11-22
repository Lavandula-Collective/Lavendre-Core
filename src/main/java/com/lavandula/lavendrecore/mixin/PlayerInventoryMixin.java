package com.lavandula.lavendrecore.mixin;

import com.lavandula.lavendrecore.toolslot.ToolSlotInventory;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import net.minecraft.block.BlockState;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PlayerInventory.class)
abstract class PlayerInventoryMixin implements ToolSlotInventory.Holder {
    @Shadow @Final public PlayerEntity player;
    @Unique private final ToolSlotInventory lavendre$toolSlot = new ToolSlotInventory((PlayerInventory) (Object) this);

    @Override
    public ToolSlotInventory lavendre$getToolSlot() {
        return lavendre$toolSlot;
    }

    @Inject(method = "getMainHandStack", at = @At("HEAD"), cancellable = true)
    private void lavendre$getMiningStack(CallbackInfoReturnable<ItemStack> cir) {
        DedicatedToolSlot slot = ToolSlotUse.activeSlot((PlayerInventory) (Object) this);
        if (slot != null) {
            cir.setReturnValue(slot.getStack());
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    private void lavendre$getMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        DedicatedToolSlot slot = ToolSlotUse.activeSlot((PlayerInventory) (Object) this);
        if (slot != null) {
            cir.setReturnValue(slot.getStack().getMiningSpeedMultiplier(state));
        }
    }

    @Inject(method = "clone", at = @At("TAIL"))
    private void lavendre$copyToolSlot(PlayerInventory other, CallbackInfo ci) {
        lavendre$toolSlot.copyFrom(ToolSlotInventory.of(other));
    }

    @Inject(method = "dropAll", at = @At("TAIL"))
    private void lavendre$dropTools(CallbackInfo ci) {
        for (int i = 0; i < lavendre$toolSlot.size(); i++) {
            ItemStack stack = lavendre$toolSlot.removeStack(i);
            if (!stack.isEmpty() && !EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                player.dropItem(stack, true, false);
            }
        }
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void lavendre$clearTools(CallbackInfo ci) {
        lavendre$toolSlot.clear();
    }

    @Inject(method = "remove(Ljava/util/function/Predicate;ILnet/minecraft/inventory/Inventory;)I", at = @At("RETURN"), cancellable = true)
    private void lavendre$removeTools(Predicate<ItemStack> predicate, int maxCount, Inventory craftingInventory,
                                      CallbackInfoReturnable<Integer> cir) {
        int removed = cir.getReturnValueI();
        if (maxCount > 0 && removed >= maxCount) {
            return;
        }
        int fromToolSlots = Inventories.remove(lavendre$toolSlot, predicate, maxCount < 0 ? -1 : maxCount - removed, maxCount == 0);
        if (fromToolSlots > 0) {
            if (maxCount != 0) {
                player.playerScreenHandler.sendContentUpdates();
            }
            cir.setReturnValue(removed + fromToolSlots);
        }
    }
}
