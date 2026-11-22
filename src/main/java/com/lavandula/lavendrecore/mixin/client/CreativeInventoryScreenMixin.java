package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.ToolSlotRenderer;
import com.lavandula.lavendrecore.toolslot.ToolSlotScreenHandler;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolSlotInventory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CreativeInventoryScreen.class)
abstract class CreativeInventoryScreenMixin extends HandledScreen<ScreenHandler> {
    @Unique private static final int TOOL_ROW_Y = 166;

    @Shadow private boolean lastClickOutsideBounds;
    @Shadow public abstract boolean isInventoryTabSelected();

    protected CreativeInventoryScreenMixin(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void lavendre$drawToolSlotBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (isInventoryTabSelected() && !ToolSlotScreenHandler.of(client.player).lavendre$getDedicatedToolSlots().isEmpty()) {
            ToolSlotRenderer.drawInventory(context, ToolSlotInventory.of(client.player.getInventory()),
                    x + ToolSlotRenderer.inventoryFirstX(backgroundWidth), y + ToolSlotRenderer.inventoryY(TOOL_ROW_Y));
        }
    }

    @ModifyArgs(method = "setSelectedTab", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen$CreativeSlot;<init>(Lnet/minecraft/screen/slot/Slot;III)V"))
    private void lavendre$positionCreativeToolSlot(Args args) {
        Slot slot = args.get(0);
        if (slot instanceof DedicatedToolSlot toolSlot) {
            args.set(2, ToolSlotRenderer.inventoryFirstX(backgroundWidth) + toolSlot.type().ordinal() * DedicatedToolSlot.SPACING);
            args.set(3, ToolSlotRenderer.inventoryY(TOOL_ROW_Y));
        }
    }

    @Inject(method = "isClickOutsideBounds", at = @At("RETURN"), cancellable = true)
    private void lavendre$includeCreativeToolSlot(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
        if (isInventoryTabSelected() && ToolSlotRenderer.isWithinInventoryRow(mouseX - left, mouseY - top,
                ToolSlotRenderer.inventoryFirstX(backgroundWidth), ToolSlotRenderer.inventoryY(TOOL_ROW_Y),
                ToolSlotScreenHandler.of(client.player).lavendre$getDedicatedToolSlots())) {
            lastClickOutsideBounds = false;
            cir.setReturnValue(false);
        }
    }
}
