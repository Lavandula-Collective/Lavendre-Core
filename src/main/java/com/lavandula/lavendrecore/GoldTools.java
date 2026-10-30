package com.lavandula.lavendrecore;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Golden pickaxe breaks everything in #lavendre_core:gold_instabreak instantly
 * (and drops it). Edit that tag to change what counts as "gold stuff".
 */
public final class GoldTools {
    public static final TagKey<Block> GOLD_INSTABREAK =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(LavendreCore.MOD_ID, "gold_instabreak"));

    // Anything above ~150 is instant for every block in the tag.
    private static final float INSTANT_SPEED = 1000.0f;

    private GoldTools() {
    }

    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context ->
                context.modify(Items.GOLDEN_PICKAXE, builder -> {
                    ToolComponent original = Items.GOLDEN_PICKAXE.getComponents().get(DataComponentTypes.TOOL);
                    if (original == null) {
                        LavendreCore.LOGGER.warn("Golden pickaxe has no tool component, skipping gold tweak.");
                        return;
                    }

                    List<ToolComponent.Rule> rules = new ArrayList<>();
                    // Our rule goes FIRST. The first matching rule wins, and the vanilla
                    // "incorrect tool" rule would otherwise block gold from dropping.
                    rules.add(ToolComponent.Rule.ofAlwaysDropping(GOLD_INSTABREAK, INSTANT_SPEED));
                    rules.addAll(original.rules());

                    builder.add(DataComponentTypes.TOOL,
                            new ToolComponent(rules, original.defaultMiningSpeed(), original.damagePerBlock()));
                }));
    }
}