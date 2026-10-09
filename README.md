# Ascend

An RPG leveling mod for **Minecraft 1.21.1** (Fabric).

## How it works

- **Earn XP** (separate from vanilla XP):
  - ⛏️ Mining ores: coal 2, copper 2, redstone 3, lapis 3, quartz 3, iron 4, gold 6, diamond 15, emerald 20, ancient debris 30
    (no XP with Silk Touch).
  - ⚔️ Killing mobs: half the mob's max health (zombie 10, enderman 20, wither 150).
  - 🌾 Harvesting fully grown crops: 1.
  - 🎣 Fishing: 5 · 💰 Trading with villagers: 3 · ✨ Enchanting: 2 per level · 🐄 Breeding: 4
  - 🧭 Discovering a new biome: 10 (once per biome).
- **Level up** to level **200**. Each level gives **1 skill point**, a title, a sound and particles.
- **Spend points** in the character screen (**K**). Each stat goes up to **75** points:

| Stat | Per point | Max (75 points) |
|---|---|---|
| Strength | +0.1 attack damage | +7.5 damage |
| Vitality | +0.4 max health | +30 health (15 hearts) |
| Agility | +0.4% movement speed | +30% |
| Mining | +1% mining speed | +75% |

- Your level and XP bar are shown in the bottom-left corner, and other players see **[Lv X]** above your head.
- The menu (**K**) has 4 tabs: **Stats** (type an amount to spend many points at once), **Skills**, **Milestones** and **Achievements**.

## Skills

Learn skills with skill points in the Skills tab, then put them in a slot. Use them with **R**, **G** and **V** (configurable).

| Skill | Level | Cost | Cooldown | Effect |
|---|---|---|---|---|
| Dash | 15 | 2 | 6s | Dash forward |
| Heal Pulse | 30 | 3 | 30s | Heal 6 HP to you and nearby players |
| Ground Slam | 45 | 3 | 15s | Damage and knock back nearby mobs |
| Berserk | 75 | 5 | 60s | Strength II and Speed II for 10s |

## Mob levels

Hostile mobs get a random level (shown above their heads): higher far from spawn (+1 per 150 blocks), +15 in the
Nether and +30 in the End, up to 100. Higher levels mean more health and damage, and more XP. Level 50+ mobs are elites.

## Achievements

14 achievements (First Blood, Shiny!, Explorer, Shadow Slayer, Collector...), each with an XP reward.

## Milestones

| Level | Milestone | Quest | Reward |
|---|---|---|---|
| 10 | Fast Learner | | +10% XP |
| 15 | Hunter | Kill 100 mobs | +1 attack damage |
| 25 | Night Eyes | | Permanent night vision |
| 35 | Prospector | Mine 200 ores | +10% mining speed |
| 50 | Sky Walker | | Double jump (press jump again in the air) |
| 60 | Wanderer | Find 25 biomes | +10% movement speed |
| 80 | Fisherman | Catch 50 fish | +2 luck |
| 100 | Night Owl | | Double XP at night |
| 125 | Shadowbane | Slay the Shadow Assassin | +4 max health |
| 150 | Veteran | Unlock 10 achievements | +10% XP |
| 200 | Ascended | | Permanent regeneration and a golden aura |

## Boss: Shadow Assassin

300 HP, teleports behind its target and gets faster below half health. A boss bar shows its health, and its drops
and drop rates are shown in chat when you meet it. Summon it with a **Shadow Sigil** (obsidian in the corners,
ender pearls on the sides, a Large EXP Bottle in the middle), its spawn egg, or `/summon ascend:shadow_assassin`.

| Drop | Chance | Effect |
|---|---|---|
| Shadow Dagger | 25% | 13 damage (+5 over a netherite sword). Right-click: **Shadow Dash** (dash 8 blocks, 3s invisibility, 5s cooldown) |
| Shadow Pickaxe | 15% | Mines a 6x6 area (sneak for one block) |
| Shadow Axe | 15% | Fells the whole tree (sneak for one log) |
| Shadow Helmet / Chestplate / Leggings / Boots | 20% each | A bit tougher than netherite. Full set: Speed I |
| Legendary EXP Bottle | 100% | +2,000 XP |

## Items

| Item | Effect | How to get it |
|---|---|---|
| Ascend EXP Bottle | +100 XP | Dungeon, mineshaft, temple, stronghold and shipwreck chests, or crafted (gold nuggets in the corners, lapis on the sides, glass bottle in the middle) |
| Large Ascend EXP Bottle | +500 XP | End city, ancient city and bastion chests, or 5 EXP Bottles (shapeless) |
| Legendary Ascend EXP Bottle | +2,000 XP | Dropped by the Wither, Ender Dragon, Warden and Elder Guardian; rarely in end city, ancient city and bastion chests |
| Tome of Rebirth | Refunds all spent skill points | Rarely in end city, ancient city and bastion chests, or crafted (book in the middle, diamonds on the sides, Large EXP Bottles in the corners) |

Bottles give you the empty glass bottle back and are not used up at max level.

## Settings

Operators can change these per world with `/ascend settings` (lists them) and `/ascend settings <name> <value>`:

| Setting | Default | What it does |
|---|---|---|
| `keepXp` | `true` | Keep level, XP and stats on death. `false`: dying resets you to level 1. |
| `xpMultiplier` | `1.0` | Multiplies XP from gameplay (0.1 to 10). Bottles and commands are not multiplied. |
| `milestones` | `true` | Enable milestone rewards. |
| `levelTags` | `true` | Show [Lv X] above players' heads. |
| `mobLevels` | `true` | Give new hostile mobs random levels. |

## Commands (operators)

- `/asxp add <amount> [player]`: give Ascend XP (levels up normally).
- `/asxp set <level> [player]`: set the level directly (1 to 200). Skill points are recalculated.
- `/ascend settings [name] [value]`: see or change the settings above.
- `/ascend reset`: reset your level, XP, stats and skills (achievements are kept).

Progress is saved with the player. Creative and spectator players don't earn XP from gameplay.

## Build

Requires Java 21.

```sh
./gradlew build
```

The jar is in `build/libs/`. Put it in your `mods` folder with [Fabric API](https://modrinth.com/mod/fabric-api).
The latest build is always at
https://github.com/sabrinaouallal17-netizen/Ascend/releases/download/dev/ascend.jar
