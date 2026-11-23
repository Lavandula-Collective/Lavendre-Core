package com.lavandula.lavendrecore.mixin.client;

import com.lavandula.lavendrecore.client.CreativeInventoryAccess;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemGroups.class)
abstract class ItemGroupsMixin {
    @WrapMethod(method = "getGroupsToDisplay")
    private static List<ItemGroup> lavendre$includeCreativeInventoryTab(Operation<List<ItemGroup>> original) {
        List<ItemGroup> groups = original.call();
        if (!CreativeInventoryAccess.needsInventoryTab()) {
            return groups;
        }
        ItemGroup inventory = CreativeInventoryAccess.inventoryTab();
        if (inventory == null || groups.contains(inventory)) {
            return groups;
        }
        List<ItemGroup> visibleGroups = new ArrayList<>(groups);
        visibleGroups.add(inventory);
        return visibleGroups;
    }
}
