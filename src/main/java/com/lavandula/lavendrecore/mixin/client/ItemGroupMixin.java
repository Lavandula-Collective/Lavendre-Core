package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.CreativeInventoryAccess;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemGroup.class)
abstract class ItemGroupMixin {
    @Unique private static final int VANILLA_INVENTORY_COLUMN = 6;
    @Shadow @Final private int column;

    @WrapMethod(method = "shouldDisplay")
    private boolean lavendre$allowCreativeInventoryTab(Operation<Boolean> original) {
        ItemGroup group = (ItemGroup) (Object) this;
        return group.getType() == ItemGroup.Type.INVENTORY && group == CreativeInventoryAccess.inventoryTab()
                && CreativeInventoryAccess.needsInventoryTab() || original.call();
    }

    @ModifyReturnValue(method = "getColumn", at = @At("RETURN"))
    private int lavendre$keepInventoryTabOnScreen(int position) {
        ItemGroup group = (ItemGroup) (Object) this;
        if (position > VANILLA_INVENTORY_COLUMN && group == CreativeInventoryAccess.inventoryTab()) {
            return VANILLA_INVENTORY_COLUMN;
        }
        return position;
    }

    @ModifyReturnValue(method = "getRow", at = @At("RETURN"))
    private ItemGroup.Row lavendre$useInventoryTabRow(ItemGroup.Row row) {
        // Creative tab customizers can move removed tabs to an off-screen column.
        ItemGroup group = (ItemGroup) (Object) this;
        if (column > VANILLA_INVENTORY_COLUMN && group == CreativeInventoryAccess.inventoryTab()) {
            return ItemGroup.Row.BOTTOM;
        }
        return row;
    }
}
