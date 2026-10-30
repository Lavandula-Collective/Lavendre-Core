<div align="center">
<img src="https://cdn.modrinth.com/data/cached_images/f2b475ff899ec173519de3d54a333de5ce7a98bf.png"width="100"> 


# Lavendre Core

Lavendre Core is the core mod for the [Lavendre](https://modrinth.com/modpack/lavendre) modpack, providing essential resources, built-in datapacks, and additional features that support the modpack's gameplay and overall experience.

This mod is designed specifically for Lavendre and is not intended for standalone use. However, its modular datapacks can be individually enabled or disabled through the configuration.

Issues and compatibility fixes will only be addressed when they affect the modpack.

</div>

---

<details>
<summary>List of Features (CONTAINS SPOILERS)</summary>

# 🎮 Gameplay

### Copper Progression

- **Copper Progression:** Introduces copper equipment as an intermediate progression tier between stone and iron, providing an additional step in early-game progression.
- **Gold Tools:** Gold tools can now instantly break gold-related blocks, including gold blocks and other gold-based blocks.
- **Gold Armor:** Each equipped gold armor piece grants a 10% movement speed bonus, stacking up to 40% when wearing the full set.

# 🎨 Resources

### Mizuno's 16 Craft

- Adds [Mizuno's 16 Craft](https://mizunomcmemo.blogspot.com/) GUI textures compat vanilla minecraft and the following mods:
  - [Backpacked](https://mrcrayfish.com/mods/backpacked)
  - [\[Let's Do\] Bakery - Farm&Charm Compat](https://modrinth.com/mod/lets-do-bakery-farmcharm-compat)
  - [\[Let's Do\] Farm & Charm](https://modrinth.com/mod/lets-do-farm-charm)
  - [\[Let's Do\] Herbal Brews](https://modrinth.com/mod/lets-do-herbalbrews)
  - [\[Let's Do\] Lili's Pottery](https://modrinth.com/mod/lets-do-lilis-pottery)
  - [\[Let's Do\] Vinery](https://modrinth.com/mod/lets-do-vinery)
  - [Farmer's Delight Refabricated](https://modrinth.com/mod/farmers-delight-refabricated)
  - [Field Guide](https://modrinth.com/mod/field-guide)
  - [Modulation](https://modrinth.com/mod/modulation)
  - [Polymorph](https://modrinth.com/mod/polymorph)
  - [Reliable EMI (REMI)](https://modrinth.com/mod/reliable-emi)
  - [Trinkets](https://modrinth.com/mod/trinkets)
- Adds and modifies language entries in selected packs for Lavendre.
- Adds custom GUI textures for [Questlog](https://modrinth.com/mod/questlog)
- Adds custom AFK music for [Ji AFK Cinematic](https://modrinth.com/mod/ji-afk-cinematic)
- Enables the existing 3D lantern models from [Mizuno's 16 Craft](https://mizunomcmemo.blogspot.com/) to display correctly in the inventory and when held in the player's hand.
- Adds new banners for [Modern World Creation](https://modrinth.com/mod/modern-world-creation).

# 📦 Datapack

### **[Biomes O' Plenty](https://modrinth.com/mod/biomes-o-plenty)**
- Adjusts wild barley generation to use the equivalent wild barley from Let's Do: Farm & Charm for consistency across Lavendre's farming systems.

### **[Farmer's Delight](https://modrinth.com/mod/farmers-delight-refabricated) & [Let's Do: Farm & Charm](https://modrinth.com/mod/lets-do-farm-charm)**
- Implements extensive compatibility changes to ensure both mods work together without overlapping or redundant content.
- Prioritizes Farmer's Delight items over their Let's Do counterparts to prevent duplicate items and recipes.
- Adjusts wild crop generation to use Farm & Charm's equivalent crops while retaining the corresponding Farmer's Delight drops.

### **[Let's Do: Hearth & Timber](https://modrinth.com/mod/lets-do-hearth-timber)**
- Disables the world generation of groutless rubblestone to prevent unwanted terrain generation.

### **[Let's Do: Herbal Brews](https://modrinth.com/mod/lets-do-herbalbrews) & [Rustic Delight](https://modrinth.com/mod/rustic-delight)**
- Changes Rustic Delight coffee preparation to use the kettle from Let's Do: Herbal Brews, consolidating coffee preparation around a shared workstation.
- Updates Herbal Brews tea and coffee recipes, along with their associated stat bonuses, to maintain consistency across beverages.
- Aligns Herbal Brews coffee-mixing recipes with Rustic Delight's manual mixing logic for a consistent preparation experience.
- Replaces Herbal Brews' wild coffee generation with Rustic Delight's wild coffee plant to avoid redundant world-generation content.
- Adds `#minecraft:flower` tag to Herbal Brews plants.

### Lullaby of the New Moon

- Added 7 new lore-friendly lunar events with [Enhanced Celestials 2](https://modrinth.com/mod/enhanced-celestials-2-core):
  - **Blue Moon** (Uncommon): Bonus XP, cheaper anvil and enchanting costs, a stronger beacon range, and a little luck.
  - **Eternal Moon** (Uncommon): Crops drop double.
  - **Frost Moon** (Uncommon): A quiet night with far fewer monsters.
  - **Iridescent Moon** (Super Rare): Appears only on a new moon night. You get night vision, speed, and slow falling, plus more amethyst shards and lapis from mining.
  - **Super Blue Moon** (Rare): A stronger Blue Moon with much more XP, much cheaper anvil and enchanting costs, a bigger beacon range, and more luck.
  - **Super Frost Moon** (Rare): No monsters spawn at all, and you take less damage.
  - **Super Eternal Moon** (Rare): Crops drop four times as much.

</details>

<details>
<summary>Configuration</summary>

Lavendre Core includes configurable options for managing its features and modular datapacks. Individual features can be enabled or disabled through the mod configuration menu.

Some options are intended for development and testing and are not recommended for regular gameplay. By default, you can find this in `config/lavendre_core.json`:

```groovy
{
  "developerMode": false,
  "copperProgression": true,
  "goldToolsInstantBreak": true,
  "goldArmorSpeed": true
}
```

</details>

---

<div align="center">

Credits to https://pixelrepo.com/ for select assets.

</div>
