package com.lavandula.lavendrecore.mixin;

import com.lavandula.lavendrecore.toolslot.ToolSlotActions;
import com.lavandula.lavendrecore.toolslot.ServerToolSlotHand;
import com.lavandula.lavendrecore.toolslot.ToolSlotInventory;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotUse;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin {
    @Unique private static final String TOOL_SLOT_NBT = "lavendre_core:tool_slot";
    @Unique private static final String LEGACY_TOOL_SLOT_NBT = "lavendre_core:tool_belt";

    @WrapMethod(method = "attack")
    private void lavendre$attackWithSword(Entity target, Operation<Void> original) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (ToolSlotUse.isSwordScoped(player)) {
            original.call(target);
            return;
        }
        DedicatedToolSlot slot = ToolSlotActions.swordForAttack(player, target);
        if (player instanceof ServerPlayerEntity serverPlayer && target.isAttackable()) {
            ServerToolSlotHand.of(serverPlayer).equip(serverPlayer, slot);
        }
        ToolSlotUse.withTool(player, slot, () -> original.call(target));
    }

    @WrapOperation(method = {"getBlockBreakingSpeed", "attack", "getAttackCooldownProgressPerTick"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeValue(Lnet/minecraft/registry/entry/RegistryEntry;)D"))
    private double lavendre$toolAttribute(PlayerEntity player, RegistryEntry<EntityAttribute> attribute, Operation<Double> original) {
        return ToolSlotUse.attributeValue(player, attribute, original.call(player, attribute));
    }

    @Inject(method = "equipStack", at = @At("HEAD"), cancellable = true)
    private void lavendre$updateToolSlotStack(EquipmentSlot equipmentSlot, ItemStack stack, CallbackInfo ci) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            DedicatedToolSlot slot = ToolSlotUse.activeSlot(player.getInventory());
            if (slot != null) {
                slot.setStack(stack);
                ci.cancel();
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void lavendre$writeToolSlot(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ToolSlotInventory toolSlots = ToolSlotInventory.of(player.getInventory());
        if (!toolSlots.isEmpty()) {
            nbt.put(TOOL_SLOT_NBT, toolSlots.write(player.getRegistryManager()));
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void lavendre$readToolSlot(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        String key = nbt.contains(TOOL_SLOT_NBT, NbtElement.COMPOUND_TYPE) ? TOOL_SLOT_NBT : LEGACY_TOOL_SLOT_NBT;
        ToolSlotInventory.of(player.getInventory()).read(nbt.getCompound(key), player.getRegistryManager());
    }
}
