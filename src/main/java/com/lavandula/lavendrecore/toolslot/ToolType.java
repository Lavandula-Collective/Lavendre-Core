package com.lavandula.lavendrecore.toolslot;

import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

public enum ToolType {
    PICKAXE("pickaxe", ItemTags.PICKAXES, PickaxeItem.class),
    AXE("axe", ItemTags.AXES, AxeItem.class),
    SHOVEL("shovel", ItemTags.SHOVELS, ShovelItem.class),
    SWORD("sword", ItemTags.SWORDS, SwordItem.class),
    HOE("hoe", ItemTags.HOES, HoeItem.class);

    private final String id;
    private final TagKey<Item> tag;
    private final Class<? extends Item> itemClass;

    ToolType(String id, TagKey<Item> tag, Class<? extends Item> itemClass) {
        this.id = id;
        this.tag = tag;
        this.itemClass = itemClass;
    }

    public String id() {
        return id;
    }

    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && (stack.isIn(tag) || itemClass.isInstance(stack.getItem()));
    }
}
