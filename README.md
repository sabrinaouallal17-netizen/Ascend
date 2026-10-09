# Ascend

An RPG leveling mod for **Minecraft 1.21.1** (Fabric).

## How it works

- **Earn XP** (separate from vanilla XP):
  - ⛏️ Mining ores: coal 2, copper 2, redstone 3, lapis 3, quartz 3, iron 4, gold 6, diamond 15, emerald 20, ancient debris 30
    (no XP with Silk Touch).
  - ⚔️ Killing mobs: half the mob's max health (zombie 10, enderman 20, wither 150).
  - 🌾 Harvesting fully grown crops: 1.
- **Level up** up to level **200**. Each level gives **1 skill point**, a title, a sound and particles.
- **Spend points** in the character screen (**K**, configurable in Controls). Each stat goes up to 50 points:

| Stat | Per point | Max (50 points) |
|---|---|---|
| Strength | +0.1 attack damage | +5 damage |
| Vitality | +0.4 max health | +20 health (10 hearts) |
| Agility | +0.4% movement speed | +20% |
| Mining | +1% mining speed | +50% |

## Ascend EXP Bottle

Right-click to drink: **+100 Ascend XP**, and you get the empty glass bottle back (not used up at max level).
Found in the Tools & Utilities creative tab, or crafted:

```
Gold nugget | Lapis lazuli | Gold nugget
Lapis lazuli | Glass bottle | Lapis lazuli
Gold nugget | Lapis lazuli | Gold nugget
```

Progress is saved with the player and kept on death. Creative and spectator players don't earn XP.

### Commands (operators)

- `/asxp add <amount> [player]`: give Ascend XP (levels up normally).
- `/asxp set <level> [player]`: set the level directly (1 to 200). Skill points are recalculated.
- `/ascend reset`: reset your level, XP and stats.

## Build

Requires Java 21.

```sh
./gradlew build
```

The jar is in `build/libs/`. Put it in your `mods` folder with [Fabric API](https://modrinth.com/mod/fabric-api).
The latest build is always at
https://github.com/sabrinaouallal17-netizen/Ascend/releases/download/dev/ascend.jar
