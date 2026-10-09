package com.ascend.logic;

import java.util.ArrayList;
import java.util.List;

/** Permanent rewards unlocked at certain levels. */
public enum Milestone {
	FAST_LEARNER(10, "Fast Learner", "+10% XP"),
	NIGHT_EYES(25, "Night Eyes", "Night vision"),
	SKY_WALKER(50, "Sky Walker", "Double jump"),
	NIGHT_OWL(100, "Night Owl", "2x XP at night"),
	ASCENDED(200, "Ascended", "Regen + golden aura");

	private final int level;
	private final String title;
	private final String description;

	Milestone(int level, String title, String description) {
		this.level = level;
		this.title = title;
		this.description = description;
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

	public boolean unlockedAt(int playerLevel) {
		return playerLevel >= level;
	}

	/** Milestones reached when going from {@code fromLevel} (excluded) to {@code toLevel} (included). */
	public static List<Milestone> reachedBetween(int fromLevel, int toLevel) {
		List<Milestone> reached = new ArrayList<>();
		for (Milestone milestone : values()) {
			if (milestone.level > fromLevel && milestone.level <= toLevel) {
				reached.add(milestone);
			}
		}
		return reached;
	}
}
