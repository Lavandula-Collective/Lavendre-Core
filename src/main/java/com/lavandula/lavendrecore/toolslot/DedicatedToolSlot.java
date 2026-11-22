package com.lavandula.lavendrecore.toolslot;

import com.lavandula.lavendrecore.LavendreConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class DedicatedToolSlot extends Slot {
    public static final int FIRST_X = 44;
    public static final int Y = 169;
    public static final int SPACING = 18;

    private final ToolType type;
    private final PlayerEntity owner;

    public DedicatedToolSlot(ToolSlotInventory inventory, ToolType type, PlayerEntity owner) {
        super(inventory, type.ordinal(), FIRST_X + type.ordinal() * SPACING, Y);
        this.type = type;
        this.owner = owner;
    }

    public static DedicatedToolSlot at(ScreenHandler handler, int index) {
        if (index >= 0 && index < handler.slots.size() && handler.getSlot(index) instanceof DedicatedToolSlot slot) {
            return slot;
        }
        return null;
    }

    public ToolType type() {
        return type;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return isEnabled() && type.accepts(stack);
    }

    @Override
    public boolean canTakeItems(PlayerEntity player) {
        return isEnabled();
    }

    @Override
    public boolean isEnabled() {
        return !(owner.getWorld().isClient() && owner.isInCreativeMode() && LavendreConfig.clientToolSlot().hideInCreative());
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }
}
