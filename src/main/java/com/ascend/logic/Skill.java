package com.ascend.logic;

/** Active skills, bought with skill points (some also need a quest) and used with the skill keys. */
public enum Skill {
	DASH("Dash", "Dash forward", 15, 2, 6 * 20, null, 0),
	HEAL_PULSE("Heal Pulse", "Heal 6 HP to you and nearby players", 30, 3, 30 * 20, null, 0),
	GROUND_SLAM("Ground Slam", "Damage and knock back nearby mobs", 45, 3, 15 * 20, null, 0),
	BERSERK("Berserk", "Strength II and Speed II for 10s", 75, 5, 60 * 20, null, 0),
	LIFESTEAL("Lifesteal", "For 10s, your hits heal you for 30% of the damage", 40, 3, 45 * 20, Counter.MOBS_KILLED, 1_000),
	SHADOW_STEP("Shadow Step", "Teleport behind the mob you are looking at (12 blocks)", 60, 4, 10 * 20, Counter.SHADOW_KILLS, 1),
	FROST_NOVA("Frost Nova", "Freeze, slow and damage every mob around you", 90, 4, 25 * 20, Counter.FROST_KILLS, 1);

	private final String title;
	private final String description;
	private final int requiredLevel;
	private final int cost;
	private final int cooldownTicks;
	private final Counter quest;
	private final int questTarget;

	Skill(String title, String description, int requiredLevel, int cost, int cooldownTicks, Counter quest, int questTarget) {
		this.title = title;
		this.description = description;
		this.requiredLevel = requiredLevel;
		this.cost = cost;
		this.cooldownTicks = cooldownTicks;
		this.quest = quest;
		this.questTarget = questTarget;
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

	/** The quest needed to unlock this skill, or {@code null}. */
	public Counter quest() {
		return quest;
	}

	public int questTarget() {
		return questTarget;
	}

	public boolean questDone(QuestLog log) {
		return quest == null || log.count(quest) >= questTarget;
	}
}
