package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.ActiveToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
abstract class HeldItemRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyExpressionValue(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;mainHand:Lnet/minecraft/item/ItemStack;"))
    private ItemStack lavendre$renderActiveTool(ItemStack held) {
        return ActiveToolSlot.renderMainHand(client.player, held);
    }

    @WrapOperation(method = "updateHeldItems", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack lavendre$cachePhysicalHand(ClientPlayerEntity player, Operation<ItemStack> original) {
        return ToolSlotUse.withTool(player, null, () -> original.call(player));
    }
}
