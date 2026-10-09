package com.ascend.logic;

/** The stats a player can spend skill points on. */
public enum Stat {
	/** +0.1 attack damage per point. */
	STRENGTH("Strength", 0.1),
	/** +0.4 max health per point (50 points = 10 hearts). */
	VITALITY("Vitality", 0.4),
	/** +0.4% movement speed per point. */
	AGILITY("Agility", 0.004),
	/** +1% block breaking speed per point. */
	MINING("Mining", 0.01);

	/** Maximum points in one stat. */
	public static final int CAP = 50;

	private final String displayName;
	private final double perPoint;

	Stat(String displayName, double perPoint) {
		this.displayName = displayName;
		this.perPoint = perPoint;
	}

	public String displayName() {
		return displayName;
	}

	/** The total bonus given by {@code points} points in this stat. */
	public double bonus(int points) {
		return perPoint * Math.min(points, CAP);
	}
}
