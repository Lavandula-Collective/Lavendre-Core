package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.ToolSlotRenderer;
import com.lavandula.lavendrecore.toolslot.ToolSlotScreenHandler;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotInventory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    @Shadow private boolean narrow;
    @Shadow @Final private RecipeBookWidget recipeBook;

    protected InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void lavendre$drawToolSlotBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if ((!narrow || !recipeBook.isOpen()) && !ToolSlotScreenHandler.of(client.player).lavendre$getDedicatedToolSlots().isEmpty()) {
            ToolSlotRenderer.drawInventory(context, ToolSlotInventory.of(client.player.getInventory()),
                    x + ToolSlotRenderer.inventoryFirstX(backgroundWidth), y + ToolSlotRenderer.inventoryY(DedicatedToolSlot.Y));
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void lavendre$positionToolSlots(CallbackInfo ci) {
        int firstX = ToolSlotRenderer.inventoryFirstX(backgroundWidth);
        int slotY = ToolSlotRenderer.inventoryY(DedicatedToolSlot.Y);
        for (DedicatedToolSlot slot : ((ToolSlotScreenHandler) handler).lavendre$getDedicatedToolSlots()) {
            SlotPositionAccessor position = (SlotPositionAccessor) (Object) slot;
            position.lavendre$setX(firstX + slot.type().ordinal() * DedicatedToolSlot.SPACING);
            position.lavendre$setY(slotY);
        }
    }

    @Inject(method = "isClickOutsideBounds", at = @At("RETURN"), cancellable = true)
    private void lavendre$includeToolSlot(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
        if ((!narrow || !recipeBook.isOpen()) && ToolSlotRenderer.isWithinInventoryRow(mouseX - left, mouseY - top,
                ToolSlotRenderer.inventoryFirstX(backgroundWidth), ToolSlotRenderer.inventoryY(DedicatedToolSlot.Y),
                ((ToolSlotScreenHandler) handler).lavendre$getDedicatedToolSlots())) {
            cir.setReturnValue(false);
        }
    }
}
