package com.ascend.logic;

/** The Frost Pickaxe levels up with the blocks it mines, growing its mining area. */
public final class FrostPickaxe {
	public static final int MAX_LEVEL = 3;
	/** Blocks needed to reach levels 1, 2 and 3. */
	private static final int[] THRESHOLDS = {5_000, 25_000, 100_000};
	/** Mining area side for levels 0 to 3: 8x8, 10x10, 12x12, 16x16. */
	private static final int[] AREA = {8, 10, 12, 16};

	private FrostPickaxe() {
	}

	public static int levelFor(long blocksMined) {
		int level = 0;
		while (level < MAX_LEVEL && blocksMined >= THRESHOLDS[level]) {
			level++;
		}
		return level;
	}

	public static int areaFor(int level) {
		return AREA[Math.max(0, Math.min(MAX_LEVEL, level))];
	}

	/** Blocks needed for the next level, or -1 at max level. */
	public static long nextThreshold(int level) {
		return level >= MAX_LEVEL ? -1 : THRESHOLDS[level];
	}

	/** First and last offsets of an even- or odd-sized square centered on the mined block. */
	public static int[] offsets(int area) {
		int low = -(area - 1) / 2;
		return new int[] {low, low + area - 1};
	}
}
