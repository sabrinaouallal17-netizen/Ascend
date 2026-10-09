package com.ascend.logic;

import java.util.Arrays;

/** A player's level, XP, unspent points and stats. */
public final class PlayerProgress {
	private int level;
	private int xp;
	private int points;
	private final int[] stats;

	public PlayerProgress() {
		this(1, 0, 0, new int[Stat.values().length]);
	}

	public PlayerProgress(int level, int xp, int points, int[] stats) {
		this.level = Math.max(1, Math.min(Leveling.MAX_LEVEL, level));
		this.xp = Math.max(0, xp);
		this.points = Math.max(0, points);
		this.stats = Arrays.copyOf(stats, Stat.values().length);
	}

	public int level() {
		return level;
	}

	public int xp() {
		return xp;
	}

	public int points() {
		return points;
	}

	public int stat(Stat stat) {
		return stats[stat.ordinal()];
	}

	public int[] stats() {
		return stats.clone();
	}

	public boolean isMaxLevel() {
		return level >= Leveling.MAX_LEVEL;
	}

	/**
	 * Adds XP, levelling up as many times as needed. Each level gives one skill point.
	 *
	 * @return the number of levels gained
	 */
	public int addXp(int amount) {
		if (amount <= 0 || isMaxLevel()) {
			return 0;
		}
		xp += amount;
		int gained = 0;
		while (!isMaxLevel() && xp >= Leveling.xpToNextLevel(level)) {
			xp -= Leveling.xpToNextLevel(level);
			level++;
			points++;
			gained++;
		}
		if (isMaxLevel()) {
			xp = 0;
		}
		return gained;
	}

	/**
	 * Sets the level directly (XP into the level goes back to 0). Points are recalculated so the player
	 * always has one point per level gained; if more were already spent, the stats are refunded.
	 */
	public void setLevel(int newLevel) {
		level = Math.max(1, Math.min(Leveling.MAX_LEVEL, newLevel));
		xp = 0;
		int earned = level - 1;
		int spent = spentPoints();
		if (spent > earned) {
			Arrays.fill(stats, 0);
			spent = 0;
		}
		points = earned - spent;
	}

	/** Total points currently spent in stats. */
	public int spentPoints() {
		return Arrays.stream(stats).sum();
	}

	/** Gives back every spent point (Tome of Rebirth). */
	public void respec() {
		points += spentPoints();
		Arrays.fill(stats, 0);
	}

	public boolean canSpend(Stat stat) {
		return points > 0 && stats[stat.ordinal()] < Stat.CAP;
	}

	/** Spends one point in a stat. */
	public boolean spend(Stat stat) {
		if (!canSpend(stat)) {
			return false;
		}
		points--;
		stats[stat.ordinal()]++;
		return true;
	}
}
