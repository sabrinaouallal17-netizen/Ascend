package com.ascend.logic;

/** The XP curve. */
public final class Leveling {
	public static final int MAX_LEVEL = 200;

	private Leveling() {
	}

	/** XP needed to go from {@code level} to {@code level + 1}: 26 at level 1, about 5,000 at level 199. */
	public static int xpToNextLevel(int level) {
		return 20 + 5 * level + level * level / 10;
	}
}
