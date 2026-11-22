package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.ActiveToolSlot;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OtherClientPlayerEntity.class)
abstract class OtherClientPlayerEntityMixin implements ActiveToolSlot.Remote {
    @Unique private ItemStack lavendre$toolInHand = ItemStack.EMPTY;

    @Override
    public ItemStack lavendre$getActiveToolSlot() {
        return lavendre$toolInHand;
    }

    @Override
    public void lavendre$setActiveToolSlot(ItemStack stack) {
        lavendre$toolInHand = stack;
    }
}
