package com.lavandula.lavendrecore.compat;

import com.lavandula.lavendrecore.LavendreCore;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotActions;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.lavandula.lavendrecore.toolslot.ToolType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class BetterCombatCompat {
    private record Access(Method weaponAttributes, Method attacks) {
    }

    private static @Nullable Access access = findAccess();

    private BetterCombatCompat() {
    }

    @Nullable
    public static DedicatedToolSlot swordForAttack(PlayerEntity player) {
        ItemStack held = ToolSlotUse.heldStack(player.getInventory());
        if (player.isUsingRiptide() || ToolSlotActions.isWeapon(held)) {
            return null;
        }
        DedicatedToolSlot sword = ToolSlotUse.availableSlot(player, ToolType.SWORD);
        return sword != null && hasAttacks(sword.getStack()) ? sword : null;
    }

    @Nullable
    public static DedicatedToolSlot displayedSword(PlayerEntity player) {
        if (access == null) {
            return null;
        }
        DedicatedToolSlot sword = ToolSlotUse.availableSlot(player, ToolType.SWORD);
        return sword != null && player.getMainHandStack() == sword.getStack() ? sword : null;
    }

    public static boolean hasAttacks(ItemStack stack) {
        Access api = access;
        if (api == null || stack.isEmpty()) {
            return false;
        }
        try {
            Object attributes = api.weaponAttributes().invoke(null, stack);
            return attributes != null && api.attacks().invoke(attributes) instanceof Object[] attacks && attacks.length > 0;
        } catch (ReflectiveOperationException | LinkageError e) {
            access = null;
            LavendreCore.LOGGER.warn("Could not read Better Combat weapon attributes for Tool Slot.", e);
            return false;
        }
    }

    private static @Nullable Access findAccess() {
        if (!FabricLoader.getInstance().isModLoaded("bettercombat")) {
            return null;
        }
        try {
            Class<?> registry = Class.forName("net.bettercombat.logic.WeaponRegistry");
            Method attributes = registry.getMethod("getAttributes", ItemStack.class);
            return new Access(attributes, attributes.getReturnType().getMethod("attacks"));
        } catch (ReflectiveOperationException | LinkageError e) {
            LavendreCore.LOGGER.warn("The installed Better Combat version does not expose weapon attributes to Tool Slot.", e);
            return null;
        }
    }
}
