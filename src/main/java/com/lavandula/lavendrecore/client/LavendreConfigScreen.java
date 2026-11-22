package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.LavendreCore;
import com.lavandula.lavendrecore.ToolSlotSettings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LavendreConfigScreen {
    private LavendreConfigScreen() {
    }

    public static Screen create(Screen parent) {
        LavendreConfig.Values saved = LavendreConfig.readSaved();

        boolean[] draft = {
                saved.developerMode(),
                saved.copperProgression(),
                saved.goldToolsInstantBreak(),
                saved.goldArmorSpeed()
        };

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Lavendre Core"));

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory features = builder.getOrCreateCategory(Text.literal("Features"));
        ConfigCategory toolSlot = builder.getOrCreateCategory(Text.literal("Tool Slot"));
        ConfigCategory developer = builder.getOrCreateCategory(Text.literal("Developer"));
        Supplier<ToolSlotSettings> savedToolSlot = addToolSlotSettings(toolSlot, entries, LavendreConfig.readSavedClient());

        var enabled = entries.startBooleanToggle(Text.literal("Enable Tool Slot"), saved.toolSlot().enabled())
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enables the dedicated slots and automatic tool use."),
                        Text.literal("Requires a game restart for single-player or a dedicated server restart.").formatted(Formatting.RED),
                        Text.literal("In multiplayer, the server controls availability."))
                .requireRestart().build();
        var equip = entries.startIntField(Text.literal("Tool Slot active duration (ms)"), saved.toolSlot().equipMs())
                .setDefaultValue(LavendreConfig.ToolSlot.DEFAULT_EQUIP_MS)
                .setMin(0).setMax(LavendreConfig.ToolSlot.MAX_DURATION_MS)
                .setTooltip(Text.literal("How long a Tool Slot tool remains visible in hand after use."),
                        Text.literal("Uses the server's value in multiplayer. Requires a game or server restart."))
                .requireRestart().build();
        var pvpFlag = entries.startBooleanToggle(Text.literal("PvP Flag Sword Auto-Equip"), saved.toolSlot().pvpFlagAutoEquip())
                .setDefaultValue(true)
                .setTooltip(Text.literal("Readies the Tool Slot sword when idle with PvP Flag enabled."),
                        Text.literal("Requires PvP Flag. The server controls this setting in multiplayer."),
                        Text.literal("Requires a game or server restart.").formatted(Formatting.RED))
                .requireRestart().build();

        builder.setSavingRunnable(() -> {
            LavendreConfig.write(new LavendreConfig.Values(draft[0], draft[1], draft[2], draft[3],
                    new LavendreConfig.ToolSlot(enabled.getValue(), equip.getValue(), pvpFlag.getValue())));
            LavendreConfig.writeClient(savedToolSlot.get());
            LavendreCoreClient.sendToolSlotPreference();
            if (LavendreConfig.isRestartRequired()) {
                LavendreCore.LOGGER.info("Lavendre Core has settings that require a game or server restart.");
            }
        });

        if (LavendreConfig.isRestartRequired()) {
            Text note = Text.literal("Restart pending for common settings.")
                    .formatted(Formatting.YELLOW);
            features.addEntry(entries.startTextDescription(note).build());
            developer.addEntry(entries.startTextDescription(note).build());
        }
        features.addEntry(enabled);
        features.addEntry(equip);
        features.addEntry(pvpFlag);

        addToggle(features, entries, "Copper Progression", saved.copperProgression(), true,
                value -> draft[1] = value,
                "Iron ore needs a copper pickaxe or better.",
                "Stone tools can still break it, but it drops nothing.",
                "§cRequires a full game restart.");

        addToggle(features, entries, "Gold Tools Instant Break", saved.goldToolsInstantBreak(), true,
                value -> draft[2] = value,
                "The golden pickaxe instantly breaks gold blocks, ores and related blocks.",
                "§cRequires a full game restart.");

        addToggle(features, entries, "Gold Armor Speed Bonus", saved.goldArmorSpeed(), true,
                value -> draft[3] = value,
                "Each golden armor piece gives +10% speed, up to +40% for a full set.",
                "§cRequires a full game restart.");

        addToggle(developer, entries, "Developer Mode", saved.developerMode(), false,
                value -> draft[0] = value,
                "Splits the Lavendre Core datapack into one pack per module.",
                "The packs are off by default, so enable them in the datapack screen.",
                "§cRequires a full game restart.");

        return builder.build();
    }

    private static Supplier<ToolSlotSettings> addToolSlotSettings(ConfigCategory category, ConfigEntryBuilder entries,
                                                                 ToolSlotSettings saved) {
        ToolSlotSettings defaults = ToolSlotSettings.defaults();
        var indicatorX = addNumber(category, entries, "Indicator horizontal offset", saved.indicatorX(), defaults.indicatorX(),
                -ToolSlotSettings.MAX_OFFSET, ToolSlotSettings.MAX_OFFSET, "GUI pixels. Positive values move right.");
        var indicatorY = addNumber(category, entries, "Indicator vertical offset", saved.indicatorY(), defaults.indicatorY(),
                -ToolSlotSettings.MAX_OFFSET, ToolSlotSettings.MAX_OFFSET, "GUI pixels. Positive values move down.");
        var anchor = entries.startEnumSelector(Text.literal("Inventory row anchor"), ToolSlotSettings.Anchor.class, saved.inventoryAnchor())
                .setDefaultValue(defaults.inventoryAnchor())
                .setEnumNameProvider(value -> Text.literal(value.name().charAt(0) + value.name().substring(1).toLowerCase(Locale.ROOT)))
                .setTooltip(Text.literal("Aligns the row to the left, center, or right of the inventory panel.")).build();
        category.addEntry(anchor);
        var inventoryX = addNumber(category, entries, "Inventory row horizontal offset", saved.inventoryX(), defaults.inventoryX(),
                -ToolSlotSettings.MAX_OFFSET, ToolSlotSettings.MAX_OFFSET, "GUI pixels from the anchor. Positive values move right.");
        var inventoryY = addNumber(category, entries, "Inventory row vertical offset", saved.inventoryY(), defaults.inventoryY(),
                -ToolSlotSettings.MAX_OFFSET, ToolSlotSettings.MAX_OFFSET, "GUI pixels from the default row. Positive values move down.");
        var hold = addNumber(category, entries, "Delay before hiding (ms)", saved.indicatorHoldMs(), defaults.indicatorHoldMs(),
                0, ToolSlotSettings.MAX_HOLD_MS, "How long the indicator stays after the active tool is put away.");
        var fade = entries.startBooleanToggle(Text.literal("Fade animation"), saved.fade())
                .setDefaultValue(defaults.fade()).setTooltip(Text.literal("Fades the indicator in and out.")).build();
        category.addEntry(fade);
        var invert = entries.startBooleanToggle(Text.literal("Invert shovel and hoe priority"), saved.invertRightClick())
                .setDefaultValue(defaults.invertRightClick())
                .setTooltip(Text.literal("Default: shovel when standing and hoe when sneaking."),
                        Text.literal("Inverted: hoe when standing and shovel when sneaking.")).build();
        category.addEntry(invert);
        return () -> new ToolSlotSettings(indicatorX.getValue(), indicatorY.getValue(),
                inventoryX.getValue(), inventoryY.getValue(), anchor.getValue(), hold.getValue(), fade.getValue(), invert.getValue());
    }

    private static IntegerListEntry addNumber(ConfigCategory category, ConfigEntryBuilder entries, String label,
                                              int current, int defaults, int min, int max, String tooltip) {
        var entry = entries.startIntField(Text.literal(label), current).setDefaultValue(defaults)
                .setMin(min).setMax(max).setTooltip(Text.literal(tooltip)).build();
        category.addEntry(entry);
        return entry;
    }

    private static void addToggle(ConfigCategory category, ConfigEntryBuilder entries, String name,
                                  boolean current, boolean defaultValue, Consumer<Boolean> onSave,
                                  String... tooltipLines) {
        Text[] tooltip = Arrays.stream(tooltipLines).map(Text::literal).toArray(Text[]::new);
        category.addEntry(entries.startBooleanToggle(Text.literal(name), current)
                .setDefaultValue(defaultValue)
                .setTooltip(tooltip)
                .setSaveConsumer(onSave)
                .build());
    }
}
