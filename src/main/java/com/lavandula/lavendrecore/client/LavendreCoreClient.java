package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.toolslot.EnableToolSlotPayload;
import com.lavandula.lavendrecore.toolslot.ToolSlotHandPayload;
import com.lavandula.lavendrecore.toolslot.ToolSlotScreenHandler;
import com.lavandula.lavendrecore.toolslot.ToolSlotPreferencePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class LavendreCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LavendreConfig.loadClient();
        PunchyToolSlotCompat.initialize();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ToolSlotHud.close());
        ClientPlayNetworking.registerGlobalReceiver(ToolSlotHandPayload.ID, (payload, context) -> {
            if (context.player().getId() == payload.entityId()) {
                ToolSlotScreenHandler.of(context.player()).lavendre$setPvpSwordReady(payload.pvpSwordReady());
            } else if (context.player().getWorld().getEntityById(payload.entityId()) instanceof ActiveToolSlot.Remote remote) {
                remote.lavendre$setActiveToolSlot(payload.stack());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(EnableToolSlotPayload.ID, (payload, context) -> {
            int equipMs = Math.clamp(payload.equipMs(), 0, LavendreConfig.ToolSlot.MAX_DURATION_MS);
            ToolSlotScreenHandler.of(context.player()).lavendre$addToolSlot(equipMs);
            sendToolSlotPreference();
        });
    }

    public static void sendToolSlotPreference() {
        if (MinecraftClient.getInstance().getNetworkHandler() != null && ClientPlayNetworking.canSend(ToolSlotPreferencePayload.ID)) {
            ClientPlayNetworking.send(new ToolSlotPreferencePayload(LavendreConfig.clientToolSlot().invertRightClick()));
        }
    }
}
