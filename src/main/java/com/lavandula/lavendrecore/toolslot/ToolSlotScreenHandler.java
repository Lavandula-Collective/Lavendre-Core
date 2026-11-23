package com.lavandula.lavendrecore.toolslot;

import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public interface ToolSlotScreenHandler {
    static ToolSlotScreenHandler of(PlayerEntity player) {
        return (ToolSlotScreenHandler) player.playerScreenHandler;
    }

    void lavendre$addToolSlot(int equipMs);

    int lavendre$getEquipMs();

    boolean lavendre$isPvpSwordReady();

    void lavendre$setPvpSwordReady(boolean ready);

    List<DedicatedToolSlot> lavendre$getDedicatedToolSlots();
}
