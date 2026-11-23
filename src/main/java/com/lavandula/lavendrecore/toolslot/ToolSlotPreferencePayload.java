package com.lavandula.lavendrecore.toolslot;

import com.lavandula.lavendrecore.LavendreCore;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

public record ToolSlotPreferencePayload(boolean invertRightClick) implements CustomPayload {
    public static final Id<ToolSlotPreferencePayload> ID = new Id<>(Identifier.of(LavendreCore.MOD_ID, "tool_slot_preference"));
    public static final PacketCodec<RegistryByteBuf, ToolSlotPreferencePayload> CODEC =
            PacketCodec.tuple(PacketCodecs.BOOL, ToolSlotPreferencePayload::invertRightClick, ToolSlotPreferencePayload::new);

    @Override
    public Id<ToolSlotPreferencePayload> getId() {
        return ID;
    }

    public interface Connection {
        static Connection of(ServerPlayNetworkHandler handler) {
            return (Connection) handler;
        }

        void lavendre$setInvertRightClick(boolean invert);

        boolean lavendre$invertRightClick();
    }
}
