package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.BetterCombatClientCompat;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
    @WrapMethod(method = "doAttack")
    private boolean lavendre$selectCombatHand(Operation<Boolean> original) {
        return BetterCombatClientCompat.withAttackHand((MinecraftClient) (Object) this, original::call);
    }

    @WrapMethod(method = "handleBlockBreaking")
    private void lavendre$selectHeldAttackHand(boolean breaking, Operation<Void> original) {
        BetterCombatClientCompat.withAttackHand((MinecraftClient) (Object) this, () -> original.call(breaking));
    }

    @WrapOperation(method = "doItemUse", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack lavendre$selectPhysicalItemForUse(ClientPlayerEntity player, Hand hand, Operation<ItemStack> original) {
        // Other mods can wrap this lookup to record the hand and stack used by right-click.
        return hand == Hand.MAIN_HAND
                ? ToolSlotUse.withTool(player, null, () -> original.call(player, hand))
                : original.call(player, hand);
    }

    @WrapOperation(method = "handleInputEvents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropSelectedItem(Z)Z"))
    private boolean lavendre$dropPhysicalItem(ClientPlayerEntity player, boolean entireStack, Operation<Boolean> original) {
        return ToolSlotUse.withTool(player, null, () -> original.call(player, entireStack));
    }
}
