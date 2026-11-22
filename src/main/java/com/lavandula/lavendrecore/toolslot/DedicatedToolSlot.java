package com.lavandula.lavendrecore.toolslot;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class DedicatedToolSlot extends Slot {
    public static final int FIRST_X = 44;
    public static final int Y = 169;
    public static final int SPACING = 18;

    private final ToolType type;

    public DedicatedToolSlot(ToolSlotInventory inventory, ToolType type) {
        super(inventory, type.ordinal(), FIRST_X + type.ordinal() * SPACING, Y);
        this.type = type;
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
        return type.accepts(stack);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }
}
