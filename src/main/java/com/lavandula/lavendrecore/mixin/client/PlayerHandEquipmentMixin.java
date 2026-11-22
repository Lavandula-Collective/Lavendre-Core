package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.ActiveToolSlot;
import com.lavandula.lavendrecore.compat.BetterCombatCompat;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
abstract class PlayerHandEquipmentMixin {
    @WrapMethod(method = "getAttackCooldownProgressPerTick")
    private float lavendre$useDisplayedSwordCooldown(Operation<Float> original) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player instanceof ClientPlayerEntity && !ToolSlotUse.hasHandScope(player.getInventory())) {
            DedicatedToolSlot sword = BetterCombatCompat.displayedSword(player);
            if (sword != null) {
                return ToolSlotUse.withTool(player, sword, original::call);
            }
        }
        return original.call();
    }

    @ModifyReturnValue(method = "getEquippedStack", at = @At("RETURN"))
    private ItemStack lavendre$getVisibleMainHand(ItemStack equipped, EquipmentSlot slot) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (slot == EquipmentSlot.MAINHAND && player.getWorld().isClient() && !ToolSlotUse.hasHandScope(player.getInventory())) {
            return ActiveToolSlot.renderMainHand(player, equipped);
        }
        return equipped;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack lavendre$trackPhysicalHotbarChanges(PlayerEntity player, Operation<ItemStack> original) {
        return ToolSlotUse.withTool(player, null, () -> original.call(player));
    }
}
