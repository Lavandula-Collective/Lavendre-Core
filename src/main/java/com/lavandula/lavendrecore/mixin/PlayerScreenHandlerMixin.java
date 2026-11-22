package com.lavandula.lavendrecore.mixin;

import com.lavandula.lavendrecore.toolslot.ToolSlotInventory;
import com.lavandula.lavendrecore.toolslot.ToolSlotScreenHandler;
import com.lavandula.lavendrecore.toolslot.DedicatedToolSlot;
import com.lavandula.lavendrecore.toolslot.ToolType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerScreenHandler.class)
abstract class PlayerScreenHandlerMixin extends ScreenHandler implements ToolSlotScreenHandler {
    @Shadow @Final private PlayerEntity owner;
    @Unique private final List<DedicatedToolSlot> lavendre$toolSlotSlots = new ArrayList<>(ToolType.values().length);
    @Unique private int lavendre$equipMs;
    @Unique private boolean lavendre$pvpSwordReady;

    protected PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public void lavendre$addToolSlot(int equipMs) {
        lavendre$equipMs = equipMs;
        if (lavendre$toolSlotSlots.isEmpty()) {
            ToolSlotInventory toolSlots = ToolSlotInventory.of(owner.getInventory());
            for (ToolType type : ToolType.values()) {
                DedicatedToolSlot slot = new DedicatedToolSlot(toolSlots, type);
                addSlot(slot);
                lavendre$toolSlotSlots.add(slot);
            }
        }
    }

    @Override
    public int lavendre$getEquipMs() {
        return lavendre$equipMs;
    }

    @Override
    public boolean lavendre$isPvpSwordReady() {
        return lavendre$pvpSwordReady;
    }

    @Override
    public void lavendre$setPvpSwordReady(boolean ready) {
        lavendre$pvpSwordReady = ready;
    }

    @Override
    public List<DedicatedToolSlot> lavendre$getDedicatedToolSlots() {
        return lavendre$toolSlotSlots;
    }

    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void lavendre$quickMoveTool(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (lavendre$toolSlotSlots.isEmpty()) {
            return;
        }
        Slot source = getSlot(index);
        ItemStack stack = source.getStack();
        if (stack.isEmpty()) {
            return;
        }

        if (source instanceof DedicatedToolSlot) {
            ItemStack original = stack.copy();
            if (insertItem(stack, PlayerScreenHandler.INVENTORY_START, PlayerScreenHandler.HOTBAR_END, false)) {
                lavendre$finishToolMove(player, source, stack, original, cir);
            } else {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        } else if (index >= PlayerScreenHandler.INVENTORY_START && index < PlayerScreenHandler.HOTBAR_END) {
            for (DedicatedToolSlot target : lavendre$toolSlotSlots) {
                if (!target.hasStack() && target.canInsert(stack)) {
                    ItemStack original = stack.copy();
                    target.setStack(stack.split(1));
                    lavendre$finishToolMove(player, source, stack, original, cir);
                    return;
                }
            }
        }
    }

    @Unique
    private void lavendre$finishToolMove(PlayerEntity player, Slot source, ItemStack stack, ItemStack original,
                                          CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isEmpty()) {
            source.setStack(ItemStack.EMPTY, original);
        } else {
            source.markDirty();
        }
        source.onTakeItem(player, stack);
        cir.setReturnValue(original);
    }
}
