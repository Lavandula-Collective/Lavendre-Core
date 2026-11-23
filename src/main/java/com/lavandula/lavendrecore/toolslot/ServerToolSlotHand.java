package com.lavandula.lavendrecore.toolslot;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.compat.PvpFlagCompat;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public final class ServerToolSlotHand {
    private final Set<ServerPlayerEntity> newObservers = new HashSet<>();
    private final ToolSlotHandState hand = new ToolSlotHandState();
    private ItemStack published = ItemStack.EMPTY;
    private boolean miningThisTick;
    private boolean pvpSwordReady;

    public static ServerToolSlotHand of(ServerPlayerEntity player) {
        return ((Holder) player).lavendre$getToolSlotHand();
    }

    public void equip(ServerPlayerEntity player, @Nullable DedicatedToolSlot slot) {
        hand.recordUse(player, slot);
        miningThisTick = false;
    }

    public void keepMining(ServerPlayerEntity player, @Nullable DedicatedToolSlot miningTool) {
        miningThisTick = hand.refreshMining(player, miningTool);
    }

    public boolean isPvpSwordReady() {
        return pvpSwordReady;
    }

    public void synchronize(ServerPlayerEntity player) {
        boolean available = ToolSlotUse.isAvailable(player);
        boolean ready = available && LavendreConfig.session().toolSlot().pvpFlagAutoEquip() && PvpFlagCompat.isEnabled(player);
        boolean flagChanged = ready != pvpSwordReady;
        pvpSwordReady = ready;
        ItemStack stack = hand.displayedStack(player, available, miningThisTick, ready);
        miningThisTick = false;
        boolean changed = !ItemStack.areEqual(stack, published);
        if (!changed && !flagChanged && newObservers.isEmpty()) {
            return;
        }
        if (changed) {
            published = stack.copy();
        }
        var payload = new ToolSlotHandPayload(player.getId(), published, ready);
        if (flagChanged && ServerPlayNetworking.canSend(player, ToolSlotHandPayload.ID)) {
            ServerPlayNetworking.send(player, payload);
        }
        if (changed || !newObservers.isEmpty()) {
            for (ServerPlayerEntity observer : PlayerLookup.tracking(player)) {
                if (observer != player && (changed || newObservers.contains(observer))
                        && ServerPlayNetworking.canSend(observer, ToolSlotHandPayload.ID)) {
                    ServerPlayNetworking.send(observer, payload);
                }
            }
            newObservers.clear();
        }
    }

    public void startTracking(ServerPlayerEntity observer) {
        // Fabric fires START_TRACKING before the vanilla spawn packet is sent.
        if (!published.isEmpty() && ServerPlayNetworking.canSend(observer, ToolSlotHandPayload.ID)) {
            newObservers.add(observer);
        }
    }

    public interface Holder {
        ServerToolSlotHand lavendre$getToolSlotHand();
    }
}
