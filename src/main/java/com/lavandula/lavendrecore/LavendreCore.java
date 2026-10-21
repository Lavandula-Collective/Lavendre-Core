package com.lavandula.lavendrecore;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LavendreCore implements ModInitializer {
    public static final String MOD_ID = "lavendre_core";
    public static final Logger LOGGER = LoggerFactory.getLogger("Lavendre Core");

    // Folder names inside src/main/resources/resourcepacks/
    public static final Identifier RESOURCE_PACK_ID = Identifier.of(MOD_ID, "lavendre_core_resources");
    public static final Identifier DATA_PACK_ID = Identifier.of(MOD_ID, "lavendre_core_data");

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
            // Runs on client and server, so both packs register everywhere.
            // They show up in the pack selectors. Ordering and forcing is handled
            // by the modpack (Resource Pack Overrides), not by this mod.
            ResourceManagerHelper.registerBuiltinResourcePack(
                    RESOURCE_PACK_ID, container,
                    Text.literal("Lavendre Core Resources"),
                    ResourcePackActivationType.DEFAULT_ENABLED);

            ResourceManagerHelper.registerBuiltinResourcePack(
                    DATA_PACK_ID, container,
                    Text.literal("Lavendre Core Data"),
                    ResourcePackActivationType.DEFAULT_ENABLED);
        });

        LOGGER.info("Lavendre Core loaded.");
    }
}
