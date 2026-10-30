package com.lavandula.lavendrecore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes config/lavendre_core.json.
 * Uses plain Gson so Cloth Config is only needed on the client, for the screen.
 *
 * Everything here is read once at startup (the "session" values), so every setting
 * needs a full game restart to take effect.
 */
public final class LavendreConfig {
    public record Values(
            boolean developerMode,
            boolean copperProgression,
            boolean goldToolsInstantBreak,
            boolean goldArmorSpeed) {

        public static Values defaults() {
            return new Values(false, true, true, true);
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** What this game session started with. */
    private static Values session = Values.defaults();

    private LavendreConfig() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("lavendre_core.json");
    }

    public static void load() {
        session = readSaved();
        if (!Files.exists(file())) {
            write(session);
        }
    }

    /** The values this session is running with. */
    public static Values session() {
        return session;
    }

    public static boolean isDeveloperMode() {
        return session.developerMode();
    }

    /** True when the saved settings differ from what this session started with. */
    public static boolean isRestartRequired() {
        return !readSaved().equals(session);
    }

    public static Values readSaved() {
        Path file = file();
        Values defaults = Values.defaults();
        if (!Files.exists(file)) {
            return defaults;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) {
                return defaults;
            }
            return new Values(
                    bool(json, "developerMode", defaults.developerMode()),
                    bool(json, "copperProgression", defaults.copperProgression()),
                    bool(json, "goldToolsInstantBreak", defaults.goldToolsInstantBreak()),
                    bool(json, "goldArmorSpeed", defaults.goldArmorSpeed()));
        } catch (IOException | RuntimeException e) {
            LavendreCore.LOGGER.error("Could not read {}, using defaults.", file, e);
            return defaults;
        }
    }

    public static void write(Values values) {
        JsonObject json = new JsonObject();
        json.addProperty("developerMode", values.developerMode());
        json.addProperty("copperProgression", values.copperProgression());
        json.addProperty("goldToolsInstantBreak", values.goldToolsInstantBreak());
        json.addProperty("goldArmorSpeed", values.goldArmorSpeed());
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(json));
        } catch (IOException e) {
            LavendreCore.LOGGER.error("Could not write {}.", file(), e);
        }
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }
}