package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.toolslot.ToolSlotInventory;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
abstract class HandledScreenMixin {
    @Shadow @Final protected ScreenHandler handler;

    @Shadow
    protected abstract boolean isPointWithinBounds(int x, int y, int width, int height, double mouseX, double mouseY);

    @Inject(method = "getSlotAt", at = @At("RETURN"), cancellable = true)
    private void lavendre$preferToolSlot(double mouseX, double mouseY, CallbackInfoReturnable<Slot> cir) {
        Slot hovered = cir.getReturnValue();
        if (hovered == null || hovered.inventory instanceof ToolSlotInventory) {
            return;
        }
        for (Slot slot : handler.slots) {
            if (slot.inventory instanceof ToolSlotInventory && slot.isEnabled()
                    && isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                cir.setReturnValue(slot);
                return;
            }
        }
    }
}
