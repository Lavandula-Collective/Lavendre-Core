package com.lavandula.lavendrecore.toolslot;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class ToolSlotUse {
    private record HandScope(PlayerInventory inventory, @Nullable DedicatedToolSlot slot) {
    }

    private static final ThreadLocal<HandScope> HAND_SCOPE = new ThreadLocal<>();

    private ToolSlotUse() {
    }

    public static boolean isAvailable(PlayerEntity player) {
        return player.getAbilities().allowModifyWorld && !player.isInCreativeMode() && !player.isSpectator()
                && !ToolSlotScreenHandler.of(player).lavendre$getDedicatedToolSlots().isEmpty();
    }

    @Nullable
    public static DedicatedToolSlot availableSlot(PlayerEntity player, ToolType type) {
        if (!isAvailable(player)) {
            return null;
        }
        // PlayerScreenHandler installs the slots in ToolType order.
        DedicatedToolSlot slot = ToolSlotScreenHandler.of(player).lavendre$getDedicatedToolSlots().get(type.ordinal());
        ItemStack stack = slot.getStack();
        return slot.canInsert(stack) && stack.isItemEnabled(player.getWorld().getEnabledFeatures()) ? slot : null;
    }

    public static ItemStack heldStack(PlayerInventory inventory) {
        return PlayerInventory.isValidHotbarIndex(inventory.selectedSlot) ? inventory.getStack(inventory.selectedSlot) : ItemStack.EMPTY;
    }

    @Nullable
    public static DedicatedToolSlot activeSlot(PlayerInventory inventory) {
        HandScope scope = HAND_SCOPE.get();
        return scope != null && scope.inventory() == inventory ? scope.slot() : null;
    }

    public static boolean hasHandScope(PlayerInventory inventory) {
        HandScope scope = HAND_SCOPE.get();
        return scope != null && scope.inventory() == inventory;
    }

    public static <T> T withTool(PlayerEntity player, @Nullable DedicatedToolSlot slot, Supplier<T> action) {
        // An explicit empty scope selects the physical hotbar hand for this interaction.
        PlayerInventory inventory = player.getInventory();
        HandScope previous = HAND_SCOPE.get();
        if (previous != null && previous.inventory() == inventory && previous.slot() == slot) {
            return action.get();
        }
        if (previous == null && slot == null && !player.getWorld().isClient()) {
            return action.get();
        }
        HAND_SCOPE.set(new HandScope(inventory, slot));
        try {
            return action.get();
        } finally {
            if (previous == null) {
                HAND_SCOPE.remove();
            } else {
                HAND_SCOPE.set(previous);
            }
        }
    }

    public static double attributeValue(PlayerEntity player, RegistryEntry<EntityAttribute> attribute, double value) {
        DedicatedToolSlot slot = activeSlot(player.getInventory());
        if (slot == null) {
            return value;
        }
        EntityAttributeInstance equipped = player.getAttributeInstance(attribute);
        if (equipped == null) {
            return value;
        }

        var substitution = new AttributeSubstitution(attribute, equipped);
        heldStack(player.getInventory()).applyAttributeModifiers(EquipmentSlot.MAINHAND, (type, modifier) -> {
            if (type.equals(attribute)) {
                substitution.remove(modifier);
            }
        });
        slot.getStack().applyAttributeModifiers(EquipmentSlot.MAINHAND, (type, modifier) -> {
            if (type.equals(attribute)) {
                substitution.replace(modifier);
            }
        });
        return substitution.adjust(value);
    }

    private static final class AttributeSubstitution {
        private final RegistryEntry<EntityAttribute> attribute;
        private final EntityAttributeInstance original;
        private EntityAttributeInstance effective;

        private AttributeSubstitution(RegistryEntry<EntityAttribute> attribute, EntityAttributeInstance original) {
            this.attribute = attribute;
            this.original = original;
        }

        private void remove(EntityAttributeModifier modifier) {
            editable().removeModifier(modifier.id());
        }

        private void replace(EntityAttributeModifier modifier) {
            EntityAttributeInstance instance = editable();
            instance.removeModifier(modifier.id());
            instance.addTemporaryModifier(modifier);
        }

        private EntityAttributeInstance editable() {
            if (effective == null) {
                effective = new EntityAttributeInstance(attribute, ignored -> {});
                effective.setFrom(original);
            }
            return effective;
        }

        private double adjust(double value) {
            return effective == null ? value : value + effective.getValue() - original.getValue();
        }
    }
}
