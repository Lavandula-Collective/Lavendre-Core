package com.lavandula.lavendrecore.mixin.compat;

import com.lavandula.lavendrecore.compat.BetterCombatCompat;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ServerToolSlotHand;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "net.bettercombat.network.ServerNetwork", remap = false)
abstract class BetterCombatServerNetworkMixin {
    @WrapMethod(method = "handleAttackRequest", remap = false)
    private static void lavendre$handleAttackWithToolSlot(@Coerce CustomPayload request, MinecraftServer server,
                                                           ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                                           Operation<Void> original) {
        // Fabric delivers the packet on the server thread, so Better Combat's executeSync runs inline.
        if (!ToolSlotUse.isAvailable(player)) {
            original.call(request, server, player, handler);
            return;
        }
        DedicatedToolSlot sword = BetterCombatCompat.swordForAttack(player);
        ToolSlotUse.withTool(player, sword, () -> original.call(request, server, player, handler));
        ServerToolSlotHand.of(player).equip(player, sword);
    }
}
