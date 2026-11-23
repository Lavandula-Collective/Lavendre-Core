package com.lavandula.lavendrecore.mixin;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.toolslot.EnableToolSlotPayload;
import com.lavandula.lavendrecore.toolslot.ServerToolSlotHand;
import com.lavandula.lavendrecore.toolslot.ToolSlotScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin implements ServerToolSlotHand.Holder {
    @Unique private final ServerToolSlotHand lavendre$toolSlotHand = new ServerToolSlotHand();

    @Override
    public ServerToolSlotHand lavendre$getToolSlotHand() {
        return lavendre$toolSlotHand;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lavendre$syncToolSlotHand(CallbackInfo ci) {
        lavendre$toolSlotHand.synchronize((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "onSpawn", at = @At("HEAD"))
    private void lavendre$enableToolSlotOnSpawn(CallbackInfo ci) {
        lavendre$enableToolSlot();
    }

    @Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendPlayerStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void lavendre$enableToolSlotAfterDimensionChange(TeleportTarget target, CallbackInfoReturnable<Entity> cir) {
        lavendre$enableToolSlot();
    }

    @Unique
    private void lavendre$enableToolSlot() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        LavendreConfig.ToolSlot settings = LavendreConfig.session().toolSlot();
        if (settings.enabled() && ServerPlayNetworking.canSend(player, EnableToolSlotPayload.ID)) {
            ToolSlotScreenHandler.of(player).lavendre$addToolSlot(settings.equipMs());
            // The client needs the matching slots before vanilla sends the inventory contents.
            ServerPlayNetworking.send(player, new EnableToolSlotPayload(settings.equipMs()));
        }
    }
}
