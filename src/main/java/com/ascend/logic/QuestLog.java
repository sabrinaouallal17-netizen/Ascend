package com.ascend.logic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A player's counters and unlocked achievements. */
public final class QuestLog {
	private final Map<Counter, Integer> counters = new EnumMap<>(Counter.class);
	private final Set<Achievement> unlocked = EnumSet.noneOf(Achievement.class);

	public QuestLog() {
	}

	public QuestLog(Map<Counter, Integer> counters, Set<Achievement> unlocked) {
		this.counters.putAll(counters);
		this.unlocked.addAll(unlocked);
	}

	public int count(Counter counter) {
		return counters.getOrDefault(counter, 0);
	}

	public Map<Counter, Integer> counters() {
		return new EnumMap<>(counters);
	}

	public boolean isUnlocked(Achievement achievement) {
		return unlocked.contains(achievement);
	}

	public Set<Achievement> unlocked() {
		return unlocked.isEmpty() ? EnumSet.noneOf(Achievement.class) : EnumSet.copyOf(unlocked);
	}

	/** Adds to a counter. @return achievements unlocked by this change */
	public List<Achievement> add(Counter counter, int amount) {
		if (amount <= 0) {
			return List.of();
		}
		counters.merge(counter, amount, Integer::sum);
		return checkUnlocks();
	}

	/** Raises a counter to at least {@code value} (for levels and biome counts). */
	public List<Achievement> setAtLeast(Counter counter, int value) {
		if (value <= count(counter)) {
			return List.of();
		}
		counters.put(counter, value);
		return checkUnlocks();
	}

	private List<Achievement> checkUnlocks() {
		List<Achievement> newlyUnlocked = new ArrayList<>();
		boolean changed = true;
		while (changed) {
			changed = false;
			for (Achievement achievement : Achievement.values()) {
				if (!unlocked.contains(achievement) && count(achievement.counter()) >= achievement.target()) {
					unlocked.add(achievement);
					newlyUnlocked.add(achievement);
					counters.put(Counter.ACHIEVEMENTS, unlocked.size());
					changed = true;
				}
			}
		}
		return newlyUnlocked;
	}
}
