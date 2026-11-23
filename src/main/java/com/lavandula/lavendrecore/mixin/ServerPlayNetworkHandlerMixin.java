package com.lavandula.lavendrecore.mixin;

import com.lavandula.lavendrecore.toolslot.ServerToolSlotHand;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotPreferencePayload;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayNetworkHandlerMixin implements ToolSlotPreferencePayload.Connection {
    @Shadow public ServerPlayerEntity player;
    @Unique private boolean lavendre$invertRightClick;

    @Override
    public void lavendre$setInvertRightClick(boolean invert) {
        lavendre$invertRightClick = invert;
    }

    @Override
    public boolean lavendre$invertRightClick() {
        return lavendre$invertRightClick;
    }

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V"))
    private void lavendre$resetToolSlotForEntityInteraction(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        ServerToolSlotHand.of(player).equip(player, null);
    }

    @Inject(method = "onUpdateSelectedSlot", at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", opcode = org.objectweb.asm.Opcodes.PUTFIELD))
    private void lavendre$resetToolSlotForHotbarChange(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        if (player.getInventory().selectedSlot != packet.getSelectedSlot()) {
            ServerToolSlotHand.of(player).equip(player, null);
        }
    }

    @Inject(method = "onCreativeInventoryAction", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"))
    private void lavendre$handleCreativeToolSlot(CreativeInventoryActionC2SPacket packet, CallbackInfo ci) {
        // This point is after vanilla checks the server thread and creative mode.
        DedicatedToolSlot slot = DedicatedToolSlot.at(player.playerScreenHandler, packet.slot());
        if (slot == null) {
            return;
        }
        ItemStack stack = packet.stack();
        if (stack.isEmpty() || (slot.canInsert(stack) && stack.getCount() <= slot.getMaxItemCount(stack)
                && stack.isItemEnabled(player.getWorld().getEnabledFeatures()))) {
            slot.setStack(stack);
            player.playerScreenHandler.sendContentUpdates();
        } else {
            player.playerScreenHandler.syncState();
        }
        ci.cancel();
    }
}
