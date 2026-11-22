package com.lavandula.lavendrecore.mixin.client;

import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotPositionAccessor {
    @Mutable @Accessor("x")
    void lavendre$setX(int x);

    @Mutable @Accessor("y")
    void lavendre$setY(int y);
}
