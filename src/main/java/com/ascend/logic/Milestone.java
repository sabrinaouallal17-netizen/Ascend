package com.ascend.logic;

import java.util.EnumSet;
import java.util.Set;

/** Permanent rewards unlocked at a level, some also needing a quest. */
public enum Milestone {
	FAST_LEARNER(10, "Fast Learner", "+10% XP", null, 0),
	HUNTER(15, "Hunter", "+1 attack damage", Counter.MOBS_KILLED, 100),
	NIGHT_EYES(25, "Night Eyes", "Night vision", null, 0),
	PROSPECTOR(35, "Prospector", "+10% mining speed", Counter.ORES, 200),
	SKY_WALKER(50, "Sky Walker", "Double jump", null, 0),
	WANDERER(60, "Wanderer", "+10% speed", Counter.BIOMES, 25),
	FISHERMAN(80, "Fisherman", "+2 luck", Counter.FISH, 50),
	NIGHT_OWL(100, "Night Owl", "2x XP at night", null, 0),
	SHADOWBANE(125, "Shadowbane", "+4 max health", Counter.SHADOW_KILLS, 1),
	VETERAN(150, "Veteran", "+10% XP", Counter.ACHIEVEMENTS, 10),
	ASCENDED(200, "Ascended", "Regen + golden aura", null, 0);

	private final int level;
	private final String title;
	private final String description;
	private final Counter quest;
	private final int questTarget;

	Milestone(int level, String title, String description, Counter quest, int questTarget) {
		this.level = level;
		this.title = title;
		this.description = description;
		this.quest = quest;
		this.questTarget = questTarget;
	}

	public int level() {
		return level;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	/** The quest counter, or {@code null} when only the level is needed. */
	public Counter quest() {
		return quest;
	}

	public int questTarget() {
		return questTarget;
	}

	public boolean isUnlocked(int playerLevel, QuestLog log) {
		return playerLevel >= level && (quest == null || log.count(quest) >= questTarget);
	}

	public static Set<Milestone> unlocked(int playerLevel, QuestLog log) {
		Set<Milestone> unlocked = EnumSet.noneOf(Milestone.class);
		for (Milestone milestone : values()) {
			if (milestone.isUnlocked(playerLevel, log)) {
				unlocked.add(milestone);
			}
		}
		return unlocked;
	}
}
