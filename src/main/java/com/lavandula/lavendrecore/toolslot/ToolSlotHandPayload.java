package com.lavandula.lavendrecore.toolslot;

import com.lavandula.lavendrecore.LavendreCore;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ToolSlotHandPayload(int entityId, ItemStack stack, boolean pvpSwordReady) implements CustomPayload {
    public static final Id<ToolSlotHandPayload> ID = new Id<>(Identifier.of(LavendreCore.MOD_ID, "tool_slot_hand"));
    public static final PacketCodec<RegistryByteBuf, ToolSlotHandPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, ToolSlotHandPayload::entityId,
                    ItemStack.OPTIONAL_PACKET_CODEC, ToolSlotHandPayload::stack,
                    PacketCodecs.BOOL, ToolSlotHandPayload::pvpSwordReady, ToolSlotHandPayload::new);

    @Override
    public Id<ToolSlotHandPayload> getId() {
        return ID;
    }
}
