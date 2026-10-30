package com.lavandula.lavendrecore;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Each golden armor piece worn gives +10% movement speed. Full set is +40%. */
public final class GoldArmorSpeed {
    private static final Identifier MODIFIER_ID = Identifier.of(LavendreCore.MOD_ID, "gold_armor_speed");
    private static final double PER_PIECE = 0.10;

    private GoldArmorSpeed() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                update(player);
            }
        });
    }

    private static void update(ServerPlayerEntity player) {
        EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }

        int pieces = 0;
        if (player.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.GOLDEN_HELMET)) pieces++;
        if (player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.GOLDEN_CHESTPLATE)) pieces++;
        if (player.getEquippedStack(EquipmentSlot.LEGS).isOf(Items.GOLDEN_LEGGINGS)) pieces++;
        if (player.getEquippedStack(EquipmentSlot.FEET).isOf(Items.GOLDEN_BOOTS)) pieces++;

        EntityAttributeModifier current = speed.getModifier(MODIFIER_ID);
        double wanted = pieces * PER_PIECE;

        if (pieces == 0) {
            if (current != null) {
                speed.removeModifier(MODIFIER_ID);
            }
            return;
        }

        // Nothing to do if the right amount is already applied.
        if (current != null && Math.abs(current.value() - wanted) < 1.0E-6) {
            return;
        }

        speed.removeModifier(MODIFIER_ID);
        speed.addTemporaryModifier(new EntityAttributeModifier(
                MODIFIER_ID, wanted, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }
}
