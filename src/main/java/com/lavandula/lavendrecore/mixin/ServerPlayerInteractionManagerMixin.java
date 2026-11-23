package com.lavandula.lavendrecore.mixin;

import com.lavandula.lavendrecore.toolslot.ToolSlotActions;
import com.lavandula.lavendrecore.toolslot.ServerToolSlotHand;
import com.lavandula.lavendrecore.toolslot.ToolSlotMining;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.lavandula.lavendrecore.toolslot.ToolSlotPreferencePayload;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
abstract class ServerPlayerInteractionManagerMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    @WrapMethod(method = "tryBreakBlock")
    private boolean lavendre$breakWithTool(BlockPos pos, Operation<Boolean> original) {
        DedicatedToolSlot slot = ToolSlotMining.select(player, world.getBlockState(pos), pos);
        boolean broke = ToolSlotUse.withTool(player, slot, () -> original.call(pos));
        if (broke) {
            ServerToolSlotHand.of(player).equip(player, slot);
        }
        return broke;
    }

    @ModifyExpressionValue(method = "processBlockBreakingAction", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack lavendre$applyMiningEnchantments(ItemStack held, BlockPos pos) {
        DedicatedToolSlot slot = ToolSlotMining.select(player, world.getBlockState(pos), pos);
        ServerToolSlotHand.of(player).equip(player, slot);
        return slot == null ? held : slot.getStack();
    }

    @Inject(method = "continueMining", at = @At("HEAD"))
    private void lavendre$keepMiningToolEquipped(BlockState state, BlockPos pos, int startTime, CallbackInfoReturnable<Float> cir) {
        ServerToolSlotHand.of(player).keepMining(player, ToolSlotMining.select(player, state, pos));
    }

    @Inject(method = "interactItem", at = @At("HEAD"))
    private void lavendre$clearToolSlotForItemUse(ServerPlayerEntity player, World world, ItemStack stack, Hand hand,
                                                 CallbackInfoReturnable<ActionResult> cir) {
        ServerToolSlotHand equipment = ServerToolSlotHand.of(player);
        if (hand == Hand.MAIN_HAND || equipment.isPvpSwordReady()) {
            equipment.equip(player, null);
        }
    }

    @WrapMethod(method = "interactBlock")
    private ActionResult lavendre$useToolSlot(ServerPlayerEntity player, World world, ItemStack held, Hand hand,
                                               BlockHitResult hit, Operation<ActionResult> original) {
        ServerToolSlotHand equipment = ServerToolSlotHand.of(player);
        if (equipment.isPvpSwordReady() && !held.isEmpty()) {
            equipment.equip(player, null);
        }
        ActionResult result = original.call(player, world, held, hand, hit);
        if (result != ActionResult.PASS) {
            if (result.isAccepted() && (hand == Hand.MAIN_HAND || equipment.isPvpSwordReady())) {
                equipment.equip(player, null);
            }
            return result;
        }
        boolean invert = ToolSlotPreferencePayload.Connection.of(player.networkHandler).lavendre$invertRightClick();
        return ToolSlotActions.useOnBlock(player, hand, hit, invert, slot -> equipment.equip(player, slot));
    }
}
