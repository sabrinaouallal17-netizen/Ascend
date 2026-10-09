package com.ascend.logic;

/** One-time goals that give an XP reward. */
public enum Achievement {
	FIRST_BLOOD("First Blood", Counter.MOBS_KILLED, 1, 25),
	SLAYER("Slayer", Counter.MOBS_KILLED, 500, 500),
	SHINY("Shiny!", Counter.DIAMONDS, 1, 50),
	MINER("Miner", Counter.ORES, 250, 300),
	EXPLORER("Explorer", Counter.BIOMES, 10, 150),
	WORLD_TRAVELER("World Traveler", Counter.BIOMES, 30, 500),
	ANGLER("Angler", Counter.FISH, 25, 150),
	MERCHANT("Merchant", Counter.TRADES, 50, 200),
	ELITE_HUNTER("Elite Hunter", Counter.ELITE_KILLS, 1, 200),
	SHADOW_SLAYER("Shadow Slayer", Counter.SHADOW_KILLS, 1, 1_000),
	SKILLED("Skilled", Counter.SKILLS_LEARNED, 1, 100),
	HALFWAY("Halfway There", Counter.LEVEL, 100, 1_000),
	ASCENDANT("Ascendant", Counter.LEVEL, 200, 0),
	COLLECTOR("Collector", Counter.ACHIEVEMENTS, 10, 500);

	private final String title;
	private final Counter counter;
	private final int target;
	private final int xpReward;

	Achievement(String title, Counter counter, int target, int xpReward) {
		this.title = title;
		this.counter = counter;
		this.target = target;
		this.xpReward = xpReward;
	}

	public String title() {
		return title;
	}

	public Counter counter() {
		return counter;
	}

	public int target() {
		return target;
	}

	public int xpReward() {
		return xpReward;
	}

	/** e.g. "Kill mobs: 500". */
	public String description() {
		return counter.task() + ": " + target;
	}
}
