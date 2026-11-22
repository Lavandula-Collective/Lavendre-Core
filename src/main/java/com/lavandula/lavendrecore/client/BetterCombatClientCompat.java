package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreCore;
import com.lavandula.lavendrecore.compat.BetterCombatCompat;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class BetterCombatClientCompat {
    private record Access(Method upswingTicks, Method swingInProgress) {
    }

    private static final Identifier ATTACK_REQUEST = Identifier.of("bettercombat", "c2s_request_attack");
    private static @Nullable Access access = findAccess();

    private BetterCombatClientCompat() {
    }

    public static <T> T withAttackHand(MinecraftClient client, Supplier<T> attack) {
        ClientPlayerEntity player = client.player;
        if (access == null || player == null || !ToolSlotUse.isAvailable(player)
                || !ClientPlayNetworking.canSend(ATTACK_REQUEST)
                || !(client.interactionManager instanceof ActiveToolSlot.CombatHand toolSlots)) {
            return attack.get();
        }

        boolean windingUp = isWindingUp(client);
        HitResult target = client.crosshairTarget;
        // Keep the current swing on its original hand. Otherwise let block hits start mining as usual.
        DedicatedToolSlot sword = windingUp ? BetterCombatCompat.displayedSword(player)
                : target != null && target.getType() == HitResult.Type.BLOCK ? null : BetterCombatCompat.swordForAttack(player);
        T result = ToolSlotUse.withTool(player, sword, attack);
        if (!windingUp && isWindingUp(client)) {
            toolSlots.lavendre$recordCombatHand(player, sword);
        }
        return result;
    }

    private static boolean isWindingUp(MinecraftClient client) {
        Access api = access;
        return api != null && read(client, api.upswingTicks()) instanceof Integer ticks && ticks > 0;
    }

    public static boolean isSwingInProgress(MinecraftClient client) {
        Access api = access;
        return api != null && Boolean.TRUE.equals(read(client, api.swingInProgress()));
    }

    private static Object read(MinecraftClient client, Method method) {
        try {
            return method.invoke(client);
        } catch (ReflectiveOperationException | LinkageError e) {
            access = null;
            LavendreCore.LOGGER.warn("Could not read Better Combat swing state for Tool Slot.", e);
            return null;
        }
    }

    private static @Nullable Access findAccess() {
        if (!FabricLoader.getInstance().isModLoaded("bettercombat")) {
            return null;
        }
        try {
            Class<?> api = Class.forName("net.bettercombat.api.MinecraftClient_BetterCombat");
            return new Access(api.getMethod("getUpswingTicks"), api.getMethod("isWeaponSwingInProgress"));
        } catch (ReflectiveOperationException | LinkageError e) {
            LavendreCore.LOGGER.warn("The installed Better Combat version does not expose its swing state to Tool Slot.", e);
            return null;
        }
    }
}
