package com.lavandula.lavendrecore.client;

import com.lavandula.lavendrecore.LavendreConfig;
import com.lavandula.lavendrecore.LavendreCore;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.function.Consumer;

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
                .setTitle(Text.literal("Lavendre Core"))
                .setSavingRunnable(() -> {
                    LavendreConfig.write(new LavendreConfig.Values(draft[0], draft[1], draft[2], draft[3]));
                    if (LavendreConfig.isRestartRequired()) {
                        LavendreCore.LOGGER.info("Lavendre Core settings changed. A full game restart is required.");
                    }
                });

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory features = builder.getOrCreateCategory(Text.literal("Features"));
        ConfigCategory developer = builder.getOrCreateCategory(Text.literal("Developer"));

        if (LavendreConfig.isRestartRequired()) {
            Text note = Text.literal("Restart pending. Close the game fully to apply your changes.")
                    .formatted(Formatting.YELLOW);
            features.addEntry(entries.startTextDescription(note).build());
            developer.addEntry(entries.startTextDescription(note).build());
        }

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