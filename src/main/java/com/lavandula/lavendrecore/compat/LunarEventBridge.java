package com.lavandula.lavendrecore.compat;

import com.lavandula.lavendrecore.LavendreCore;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

import java.util.List;
import java.util.UUID;

/** Optional, read-only observation of lunar events for advancement-based quests. */
public final class LunarEventBridge implements ModInitializer {
    private static final Identifier ANY_EVENT = Identifier.of(LavendreCore.MOD_ID, "lunar_events/any");
    private static final List<String> ENCOUNTER_STAGES = List.of("", "/echo_i", "/echo_ii", "/echo_iii");

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
                    .ifPresent(forecast -> forecast.currentLunarEventHolder().getKey().ifPresent(key ->
                            recordEncounter(world, key.getValue(), forecast.currentDay())));
        }
    }

    private static void recordEncounter(ServerWorld world, Identifier event, long night) {
        String path = "lunar_events/" + event.getNamespace() + "/" + event.getPath();
        EncounterHistory history = world.getServer().getOverworld().getPersistentStateManager()
                .getOrCreate(EncounterHistory.TYPE, "lavendre_core_lunar_encounters");
        for (ServerPlayerEntity player : world.getPlayers()) {
            grant(player, ANY_EVENT);
            UUID playerId = player.getUuid();
            if (history.hasSeenNight(playerId, event, night)) {
                continue;
            }
            var loader = world.getServer().getAdvancementLoader();
            var tracker = player.getAdvancementTracker();
            for (String suffix : ENCOUNTER_STAGES) {
                AdvancementEntry stage = loader.get(Identifier.of(LavendreCore.MOD_ID, path + suffix));
                if (stage == null) {
                    break; // An event can still provide just its original discovery.
                }
                if (tracker.getProgress(stage).isDone()) {
                    continue;
                }
                if (tracker.grantCriterion(stage, "witnessed")) {
                    history.recordNight(playerId, event, night);
                }
                break;
            }
        }
    }

    private static void grant(ServerPlayerEntity player, Identifier id) {
        AdvancementEntry advancement = player.getServer().getAdvancementLoader().get(id);
        if (advancement != null && !player.getAdvancementTracker().getProgress(advancement).isDone()) {
            player.getAdvancementTracker().grantCriterion(advancement, "witnessed");
        }
    }

    /** The last credited night for each player and event, shared across dimensions. */
    static final class EncounterHistory extends PersistentState {
        static final Type<EncounterHistory> TYPE = new Type<>(
                () -> new EncounterHistory(new NbtCompound()),
                (nbt, registries) -> new EncounterHistory(nbt.getCompound("players")),
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

        private final NbtCompound players;

        private EncounterHistory(NbtCompound players) {
            this.players = players;
        }

        boolean hasSeenNight(UUID player, Identifier event, long night) {
            NbtCompound events = players.getCompound(player.toString());
            String key = event.toString();
            return events.contains(key) && events.getLong(key) >= night;
        }

        void recordNight(UUID player, Identifier event, long night) {
            String playerKey = player.toString();
            NbtCompound events = players.getCompound(playerKey);
            events.putLong(event.toString(), night);
            players.put(playerKey, events);
            markDirty();
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            nbt.put("players", players.copy());
            return nbt;
        }
    }
}
