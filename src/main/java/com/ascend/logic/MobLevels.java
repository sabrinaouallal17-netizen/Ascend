package com.ascend.logic;

/** Random mob levels: higher far from spawn and in the Nether and the End. */
public final class MobLevels {
	public static final int MAX_LEVEL = 100;
	public static final int ELITE_LEVEL = 50;
	/** Blocks from world spawn per extra level. */
	public static final double BLOCKS_PER_LEVEL = 150.0;
	public static final int NETHER_BONUS = 15;
	public static final int END_BONUS = 30;
	/** The random part is 0 to this value. */
	public static final int RANDOM_SPREAD = 4;

	private MobLevels() {
	}

	public static int levelFor(double distanceFromSpawn, int dimensionBonus, int randomRoll) {
		int level = 1 + (int) (distanceFromSpawn / BLOCKS_PER_LEVEL) + dimensionBonus + randomRoll;
		return Math.max(1, Math.min(MAX_LEVEL, level));
	}

	/** Max health multiplier: x1 at level 1, about x5 at level 100. */
	public static double healthMultiplier(int level) {
		return 1.0 + 0.04 * (level - 1);
	}

	/** Extra attack damage: +0 at level 1, about +8 at level 100. */
	public static double damageBonus(int level) {
		return 0.08 * (level - 1);
	}

	/** XP multiplier for killing the mob: x1 at level 1, about x6 at level 100. */
	public static double xpMultiplier(int level) {
		return 1.0 + 0.05 * (level - 1);
	}

	public static boolean isElite(int level) {
		return level >= ELITE_LEVEL;
	}
}
