package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.client.ActiveToolSlot;
import com.lavandula.lavendrecore.client.BetterCombatClientCompat;
import com.lavandula.lavendrecore.toolslot.ToolSlotActions;
import com.lavandula.lavendrecore.toolslot.ToolSlotHandState;
import com.lavandula.lavendrecore.toolslot.ToolSlotMining;
import com.lavandula.lavendrecore.toolslot.ToolSlotScreenHandler;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
abstract class ClientPlayerInteractionManagerMixin implements ActiveToolSlot.CombatHand {
    @Shadow @Final private MinecraftClient client;
    @Shadow private boolean breakingBlock;
    @Shadow private BlockPos currentBreakingPos;
    @Shadow private int lastSelectedSlot;

    @Shadow protected abstract void syncSelectedSlot();

    @Unique private final ToolSlotHandState lavendre$hand = new ToolSlotHandState();

    @Inject(method = "syncSelectedSlot", at = @At("HEAD"))
    private void lavendre$clearToolSlotOnHotbarChange(CallbackInfo ci) {
        if (lastSelectedSlot >= 0 && lastSelectedSlot != client.player.getInventory().selectedSlot) {
            lavendre$hand.recordUse(client.player, null);
        }
    }

    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void lavendre$syncHeldToolBeforeMining(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ToolSlotUse.isAvailable(client.player)) {
            syncSelectedSlot();
        }
    }

    @Inject(method = {"attackBlock", "updateBlockBreakingProgress"}, at = @At("RETURN"))
    private void lavendre$trackMiningTool(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && breakingBlock && pos.equals(currentBreakingPos)) {
            DedicatedToolSlot slot = ToolSlotMining.select(client.player, client.world.getBlockState(pos), pos);
            lavendre$hand.recordUse(client.player, slot);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void lavendre$startUsingSelectedItem(ClientPlayerEntity player, Hand hand, BlockHitResult hit,
                                                  CallbackInfoReturnable<ActionResult> cir) {
        lavendre$clearForHandUse(player, hand);
    }

    @WrapMethod(method = "breakBlock")
    private boolean lavendre$predictToolBreak(BlockPos pos, Operation<Boolean> original) {
        ClientPlayerEntity player = client.player;
        BlockState state = client.world.getBlockState(pos);
        DedicatedToolSlot slot = ToolSlotMining.select(player, state, pos);
        boolean broke = ToolSlotUse.withTool(player, slot, () -> original.call(pos));
        if (broke) {
            lavendre$hand.recordUse(player, slot);
        }
        return broke;
    }

    @WrapMethod(method = "attackEntity")
    private void lavendre$showAttackWeapon(PlayerEntity player, Entity target, Operation<Void> original) {
        DedicatedToolSlot slot = ToolSlotActions.swordForAttack(player, target);
        original.call(player, target);
        if (target.isAttackable()) {
            lavendre$hand.recordUse(player, slot);
        }
    }

    @WrapMethod(method = "interactBlockInternal")
    private ActionResult lavendre$predictToolSlotUse(ClientPlayerEntity player, Hand hand, BlockHitResult hit, Operation<ActionResult> original) {
        ActionResult result = ToolSlotUse.withTool(player, null, () -> original.call(player, hand, hit));
        if (result != ActionResult.PASS) {
            if (result.isAccepted() && (hand == Hand.MAIN_HAND || ToolSlotScreenHandler.of(player).lavendre$isPvpSwordReady())) {
                lavendre$hand.recordUse(player, null);
            }
            return result;
        }
        return ToolSlotActions.useOnBlock(player, hand, hit, LavendreConfig.clientToolSlot().invertRightClick(),
                slot -> lavendre$hand.recordUse(player, slot));
    }

    @WrapMethod(method = "interactItem")
    private ActionResult lavendre$useSelectedItem(PlayerEntity player, Hand hand, Operation<ActionResult> original) {
        lavendre$clearForHandUse(player, hand);
        return ToolSlotUse.withTool(player, null, () -> original.call(player, hand));
    }

    @WrapMethod(method = "interactEntity")
    private ActionResult lavendre$useSelectedItemOnEntity(PlayerEntity player, Entity entity, Hand hand,
                                                          Operation<ActionResult> original) {
        lavendre$clearForHandUse(player, hand);
        return ToolSlotUse.withTool(player, null, () -> original.call(player, entity, hand));
    }

    @WrapMethod(method = "interactEntityAtLocation")
    private ActionResult lavendre$useSelectedItemAtEntity(PlayerEntity player, Entity entity, EntityHitResult hit,
                                                          Hand hand, Operation<ActionResult> original) {
        lavendre$clearForHandUse(player, hand);
        return ToolSlotUse.withTool(player, null, () -> original.call(player, entity, hit, hand));
    }

    @WrapOperation(method = {"isCurrentlyBreaking", "method_41930"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack lavendre$comparePhysicalMiningHand(ClientPlayerEntity player, Operation<ItemStack> original) {
        return ToolSlotUse.withTool(player, null, () -> original.call(player));
    }

    @Unique
    private void lavendre$clearForHandUse(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND && !ToolSlotScreenHandler.of(player).lavendre$isPvpSwordReady()) {
            return;
        }
        ItemStack held = hand == Hand.MAIN_HAND ? ToolSlotUse.heldStack(player.getInventory()) : player.getOffHandStack();
        if (!held.isEmpty()) {
            lavendre$hand.recordUse(player, null);
        }
    }

    @Override
    public void lavendre$recordCombatHand(PlayerEntity player, @Nullable DedicatedToolSlot slot) {
        syncSelectedSlot();
        lavendre$hand.recordUse(player, slot);
    }

    @Override
    public ItemStack lavendre$getActiveToolSlot() {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.currentScreen != null) {
            return ItemStack.EMPTY;
        }
        var handler = ToolSlotScreenHandler.of(player);
        boolean usingTool = breakingBlock || BetterCombatClientCompat.isSwingInProgress(client);
        return lavendre$hand.displayedStack(player, ToolSlotUse.isAvailable(player), usingTool, handler.lavendre$isPvpSwordReady());
    }
}
