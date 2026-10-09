package com.ascend.logic;

/** Things counted per player, used by achievements and milestone quests. */
public enum Counter {
	MOBS_KILLED("Kill mobs"),
	ORES("Mine ores"),
	DIAMONDS("Mine diamonds"),
	BIOMES("Find biomes"),
	FISH("Catch fish"),
	TRADES("Trade"),
	ELITE_KILLS("Kill Lv 50+ mobs"),
	SHADOW_KILLS("Slay the Shadow"),
	LEVEL("Reach level"),
	SKILLS_LEARNED("Learn skills"),
	ACHIEVEMENTS("Achievements");

	private final String task;

	Counter(String task) {
		this.task = task;
	}

	/** Short description of what is counted, e.g. "Kill mobs". */
	public String task() {
		return task;
	}
}
