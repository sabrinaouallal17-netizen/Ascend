package com.ascend.logic;

import java.util.Set;

/** Multiplies XP earned from gameplay (not from bottles, commands or achievement rewards). */
public final class XpMultiplier {
	private XpMultiplier() {
	}

	/**
	 * @param unlocked the player's unlocked milestones (empty when milestones are disabled)
	 * @param setting  the world's xpMultiplier setting
	 */
	public static double multiplier(Set<Milestone> unlocked, boolean night, double setting) {
		double multiplier = setting;
		if (unlocked.contains(Milestone.FAST_LEARNER)) {
			multiplier *= 1.1;
		}
		if (unlocked.contains(Milestone.VETERAN)) {
			multiplier *= 1.1;
		}
		if (night && unlocked.contains(Milestone.NIGHT_OWL)) {
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
