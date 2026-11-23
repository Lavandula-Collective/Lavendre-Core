package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {
    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState state) {
        super(client, connection, state);
    }

    @Inject(method = "onScreenHandlerSlotUpdate", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;isInHotbar(I)Z"))
    private void lavendre$syncToolSlot(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        // Like the hotbar, Tool Slot can receive updates while a different screen is open.
        PlayerScreenHandler handler = client.player.playerScreenHandler;
        if (DedicatedToolSlot.at(handler, packet.getSlot()) != null) {
            handler.setStackInSlot(packet.getSlot(), packet.getRevision(), packet.getStack());
            ci.cancel();
        }
    }
}
