package com.ascend.logic;

/** Active skills, bought with skill points and used with the skill keys. */
public enum Skill {
	DASH("Dash", "Dash forward", 15, 2, 6 * 20),
	HEAL_PULSE("Heal Pulse", "Heal 6 HP to you and nearby players", 30, 3, 30 * 20),
	GROUND_SLAM("Ground Slam", "Damage and knock back nearby mobs", 45, 3, 15 * 20),
	BERSERK("Berserk", "Strength II and Speed II for 10s", 75, 5, 60 * 20);

	private final String title;
	private final String description;
	private final int requiredLevel;
	private final int cost;
	private final int cooldownTicks;

	Skill(String title, String description, int requiredLevel, int cost, int cooldownTicks) {
		this.title = title;
		this.description = description;
		this.requiredLevel = requiredLevel;
		this.cost = cost;
		this.cooldownTicks = cooldownTicks;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	public int requiredLevel() {
		return requiredLevel;
	}

	public int cost() {
		return cost;
	}

	public int cooldownTicks() {
		return cooldownTicks;
	}
}
