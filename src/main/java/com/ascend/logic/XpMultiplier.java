package com.ascend.logic;

/** Multiplies XP earned from gameplay (not from bottles or commands). */
public final class XpMultiplier {
	private XpMultiplier() {
	}

	/**
	 * @param setting           the world's xpMultiplier setting
	 * @param milestonesEnabled whether milestone rewards are enabled in this world
	 */
	public static double multiplier(int level, boolean night, double setting, boolean milestonesEnabled) {
		double multiplier = setting;
		if (milestonesEnabled && Milestone.FAST_LEARNER.unlockedAt(level)) {
			multiplier *= 1.1;
		}
		if (milestonesEnabled && night && Milestone.NIGHT_OWL.unlockedAt(level)) {
			multiplier *= 2.0;
		}
		return multiplier;
	}

	/** Applies a multiplier to a base reward; any positive reward stays at least 1. */
	public static int apply(int base, double multiplier) {
		if (base <= 0) {
			return 0;
		}
		return Math.max(1, (int) Math.round(base * multiplier));
	}
}
