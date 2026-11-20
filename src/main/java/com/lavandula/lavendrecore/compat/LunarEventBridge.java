package com.lavandula.lavendrecore.compat;

import com.lavandula.lavendrecore.LavendreCore;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/** Optional, read-only observation of lunar events for advancement-based quests. */
public final class LunarEventBridge implements ModInitializer {
    private static final Identifier ANY_EVENT = Identifier.of(LavendreCore.MOD_ID, "lunar_events/any");

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("enhancedcelestials2core")) {
            EnhancedCelestialsIntegration.register();
            LavendreCore.LOGGER.info("Lunar event advancement bridge enabled.");
        }
    }

    // Keep optional mod classes out of the initializer's fields and signatures.
    private static final class EnhancedCelestialsIntegration {
        private static void register() {
            ServerTickEvents.END_WORLD_TICK.register(EnhancedCelestialsIntegration::tick);
        }

        private static void tick(ServerWorld world) {
            // Check once per second, including players who join an ongoing event.
            if (world.getTime() % 20 != 0 || world.getPlayers().isEmpty()) {
                return;
            }
            dev.corgitaco.enhancedcelestials2core.EnhancedCelestials.lunarForecastWorldData(world)
                    .filter(forecast -> forecast.isEventActive())
                    .flatMap(forecast -> forecast.currentLunarEventHolder().getKey())
                    .ifPresent(key -> {
                        Identifier event = key.getValue();
                        Identifier advancement = Identifier.of(LavendreCore.MOD_ID,
                                "lunar_events/" + event.getNamespace() + "/" + event.getPath());
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            grant(player, ANY_EVENT);
                            grant(player, advancement);
                        }
                    });
        }
    }

    private static void grant(ServerPlayerEntity player, Identifier id) {
        AdvancementEntry advancement = player.getServer().getAdvancementLoader().get(id);
        if (advancement != null && !player.getAdvancementTracker().getProgress(advancement).isDone()) {
            player.getAdvancementTracker().grantCriterion(advancement, "witnessed");
        }
    }
}
