package com.lavandula.lavendrecore.toolslot;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

public final class ToolSlotInventory extends SimpleInventory {
    public ToolSlotInventory(PlayerInventory owner) {
        super(ToolType.values().length);
        addListener(inventory -> owner.markDirty());
    }

    public static ToolSlotInventory of(PlayerInventory inventory) {
        return ((Holder) inventory).lavendre$getToolSlot();
    }

    public interface Holder {
        ToolSlotInventory lavendre$getToolSlot();
    }

    public void copyFrom(ToolSlotInventory other) {
        for (int i = 0; i < size(); i++) {
            setStack(i, other.getStack(i).copy());
        }
    }

    public NbtCompound write(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        for (ToolType type : ToolType.values()) {
            ItemStack stack = getStack(type.ordinal());
            if (!stack.isEmpty()) {
                nbt.put(type.id(), stack.encode(registries));
            }
        }
        return nbt;
    }

    public void read(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        for (ToolType type : ToolType.values()) {
            ItemStack stack = nbt.contains(type.id(), NbtElement.COMPOUND_TYPE)
                    ? ItemStack.fromNbt(registries, nbt.getCompound(type.id())).orElse(ItemStack.EMPTY)
                    : ItemStack.EMPTY;
            // Let players recover tools if a data pack changes their tags after they are stored.
            setStack(type.ordinal(), stack);
        }
    }
}
