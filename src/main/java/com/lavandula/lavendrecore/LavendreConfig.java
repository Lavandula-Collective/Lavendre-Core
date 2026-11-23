package com.lavandula.lavendrecore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class LavendreConfig {
    public record Values(boolean developerMode, boolean copperProgression, boolean goldToolsInstantBreak,
                         boolean goldArmorSpeed, ToolSlot toolSlot) {
        public static Values defaults() {
            return new Values(false, true, true, true, new ToolSlot(true, ToolSlot.DEFAULT_EQUIP_MS, true));
        }
    }

    public record ToolSlot(boolean enabled, int equipMs, boolean pvpFlagAutoEquip) {
        public static final int DEFAULT_EQUIP_MS = 2000;
        public static final int MAX_DURATION_MS = 60000;

        public ToolSlot {
            equipMs = Math.clamp(equipMs, 0, MAX_DURATION_MS);
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String LEGACY_FILE = "lavendre_core.json";
    private static final String COMMON_FILE = "lavendre_core_common.json";
    private static final String CLIENT_FILE = "lavendre_core_client.json";

    private static Values session = Values.defaults();
    private static ToolSlotSettings clientToolSlot = ToolSlotSettings.defaults();

    private LavendreConfig() {
    }

    private static Path file(String name) {
        return FabricLoader.getInstance().getConfigDir().resolve(name);
    }

    public static void load() {
        session = readSaved();
        if (!Files.exists(file(COMMON_FILE))) {
            write(session);
        }
    }

    public static void loadClient() {
        clientToolSlot = readSavedClient();
        if (!Files.exists(file(CLIENT_FILE))) {
            writeClient(clientToolSlot);
        }
    }

    public static Values session() {
        return session;
    }

    public static boolean isDeveloperMode() {
        return session.developerMode();
    }

    public static ToolSlotSettings clientToolSlot() {
        return clientToolSlot;
    }

    public static boolean isRestartRequired() {
        return !readSaved().equals(session);
    }

    public static Values readSaved() {
        JsonObject json = readWithLegacyFallback(COMMON_FILE);
        JsonObject tool = toolSlotSection(json);
        Values defaults = Values.defaults();
        return new Values(
                bool(json, "developerMode", defaults.developerMode()),
                bool(json, "copperProgression", defaults.copperProgression()),
                bool(json, "goldToolsInstantBreak", defaults.goldToolsInstantBreak()),
                bool(json, "goldArmorSpeed", defaults.goldArmorSpeed()),
                new ToolSlot(bool(tool, "enabled", defaults.toolSlot().enabled()),
                        integer(tool, "equipMs", defaults.toolSlot().equipMs()),
                        bool(tool, "pvpFlagAutoEquip", defaults.toolSlot().pvpFlagAutoEquip())));
    }

    public static ToolSlotSettings readSavedClient() {
        JsonObject tool = toolSlotSection(readWithLegacyFallback(CLIENT_FILE));
        ToolSlotSettings defaults = ToolSlotSettings.defaults();
        ToolSlotSettings.Anchor anchor = defaults.inventoryAnchor();
        if (tool.has("inventoryAnchor") && tool.get("inventoryAnchor").isJsonPrimitive()) {
            try {
                anchor = ToolSlotSettings.Anchor.valueOf(tool.get("inventoryAnchor").getAsString());
            } catch (IllegalArgumentException ignored) {
                // Keep the default for an unrecognized anchor.
            }
        }
        return new ToolSlotSettings(
                integer(tool, "indicatorX", defaults.indicatorX()), integer(tool, "indicatorY", defaults.indicatorY()),
                integer(tool, "inventoryX", defaults.inventoryX()), integer(tool, "inventoryY", defaults.inventoryY()),
                anchor, integer(tool, "indicatorHoldMs", defaults.indicatorHoldMs()), bool(tool, "fade", defaults.fade()),
                bool(tool, "invertRightClick", defaults.invertRightClick()),
                bool(tool, "hideInCreative", defaults.hideInCreative()));
    }

    public static void write(Values values) {
        writeFile(COMMON_FILE, GSON.toJsonTree(values).getAsJsonObject());
    }

    public static void writeClient(ToolSlotSettings values) {
        JsonObject json = new JsonObject();
        json.add("toolSlot", GSON.toJsonTree(values));
        if (writeFile(CLIENT_FILE, json)) {
            clientToolSlot = values;
        }
    }

    private static JsonObject readWithLegacyFallback(String name) {
        Path path = file(name);
        if (!Files.exists(path)) {
            path = file(LEGACY_FILE);
        }
        if (!Files.exists(path)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            return json == null ? new JsonObject() : json;
        } catch (IOException | RuntimeException e) {
            LavendreCore.LOGGER.error("Could not read {}, using defaults.", path, e);
            return new JsonObject();
        }
    }

    private static boolean writeFile(String name, JsonObject json) {
        Path path = file(name);
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            temporary = Files.createTempFile(path.getParent(), name, ".tmp");
            Files.writeString(temporary, GSON.toJson(json));
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            LavendreCore.LOGGER.error("Could not write {}.", path, e);
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    LavendreCore.LOGGER.warn("Could not remove temporary config {}.", temporary, e);
                }
            }
        }
    }

    private static JsonObject toolSlotSection(JsonObject json) {
        return json.has("toolSlot") && json.get("toolSlot").isJsonObject() ? json.getAsJsonObject("toolSlot") : new JsonObject();
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsBoolean() : fallback;
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()
                ? json.getAsJsonPrimitive(key).getAsInt() : fallback;
    }
}
