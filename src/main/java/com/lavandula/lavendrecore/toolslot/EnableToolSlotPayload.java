package com.lavandula.lavendrecore.toolslot;

import com.lavandula.lavendrecore.LavendreCore;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EnableToolSlotPayload(int equipMs) implements CustomPayload {
    public static final Id<EnableToolSlotPayload> ID = new Id<>(Identifier.of(LavendreCore.MOD_ID, "tool_slot_v5"));
    public static final PacketCodec<RegistryByteBuf, EnableToolSlotPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, EnableToolSlotPayload::equipMs, EnableToolSlotPayload::new);

    @Override
    public Id<EnableToolSlotPayload> getId() {
        return ID;
    }
}
