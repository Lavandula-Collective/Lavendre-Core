package com.lavandula.lavendrecore.compat;

import com.lavandula.lavendrecore.LavendreCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.UUID;

public final class PvpFlagCompat {
    private record Access(Method state, Method isEnabled) {
    }

    private static @Nullable Access access = findAccess();

    private PvpFlagCompat() {
    }

    public static boolean isEnabled(ServerPlayerEntity player) {
        Access api = access;
        if (api == null) {
            return false;
        }
        try {
            Object state = api.state().invoke(null, player.getServerWorld());
            return (boolean) api.isEnabled().invoke(state, player.getUuid());
        } catch (ReflectiveOperationException | LinkageError e) {
            access = null;
            LavendreCore.LOGGER.warn("Could not read PvP Flag state for Tool Slot.", e);
            return false;
        }
    }

    private static @Nullable Access findAccess() {
        if (FabricLoader.getInstance().isModLoaded("pvpflag")) {
            try {
                Class<?> state = Class.forName("io.github.realkarmakun.pvpflag.PvpFlagState");
                return new Access(state.getMethod("get", ServerWorld.class),
                        state.getMethod("isEffectivelyEnabled", UUID.class));
            } catch (ReflectiveOperationException | LinkageError e) {
                LavendreCore.LOGGER.warn("The installed PvP Flag version does not expose its state to Tool Slot.", e);
            }
        }
        return null;
    }
}
