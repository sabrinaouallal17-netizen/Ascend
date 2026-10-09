package com.ascend.logic;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Learned skills, the 3 skill slots, and cooldowns (cooldowns are not saved). */
public final class SkillBook {
	public static final int SLOTS = 3;

	public enum LearnResult {
		LEARNED,
		ALREADY_LEARNED,
		LEVEL_TOO_LOW,
		NOT_ENOUGH_POINTS
	}

	private final Set<Skill> learned = EnumSet.noneOf(Skill.class);
	private final Skill[] slots = new Skill[SLOTS];
	private final Map<Skill, Long> readyAt = new EnumMap<>(Skill.class);

	public SkillBook() {
	}

	public SkillBook(Set<Skill> learned, Skill[] slots) {
		this.learned.addAll(learned);
		for (int i = 0; i < SLOTS && i < slots.length; i++) {
			if (slots[i] != null && this.learned.contains(slots[i])) {
				this.slots[i] = slots[i];
			}
		}
	}

	public boolean knows(Skill skill) {
		return learned.contains(skill);
	}

	public Set<Skill> learned() {
		return learned.isEmpty() ? EnumSet.noneOf(Skill.class) : EnumSet.copyOf(learned);
	}

	public Skill[] slots() {
		return Arrays.copyOf(slots, SLOTS);
	}

	/** Learns a skill, paying its cost in skill points. The first free slot gets it. */
	public LearnResult learn(Skill skill, PlayerProgress progress) {
		if (learned.contains(skill)) {
			return LearnResult.ALREADY_LEARNED;
		}
		if (progress.level() < skill.requiredLevel()) {
			return LearnResult.LEVEL_TOO_LOW;
		}
		if (!progress.spendPoints(skill.cost())) {
			return LearnResult.NOT_ENOUGH_POINTS;
		}
		learned.add(skill);
		for (int i = 0; i < SLOTS; i++) {
			if (slots[i] == null) {
				slots[i] = skill;
				break;
			}
		}
		return LearnResult.LEARNED;
	}

	/** Puts a learned skill in a slot (removing it from any other slot). */
	public boolean bind(Skill skill, int slot) {
		if (slot < 0 || slot >= SLOTS || !learned.contains(skill)) {
			return false;
		}
		for (int i = 0; i < SLOTS; i++) {
			if (slots[i] == skill) {
				slots[i] = null;
			}
		}
		slots[slot] = skill;
		return true;
	}

	public Skill inSlot(int slot) {
		return slot >= 0 && slot < SLOTS ? slots[slot] : null;
	}

	/** Ticks left before the skill can be used again (0 = ready). */
	public long cooldownLeft(Skill skill, long nowTick) {
		return Math.max(0, readyAt.getOrDefault(skill, 0L) - nowTick);
	}

	/** Starts the cooldown if the skill is learned and ready. @return whether it may be used now */
	public boolean tryUse(Skill skill, long nowTick) {
		if (!learned.contains(skill) || cooldownLeft(skill, nowTick) > 0) {
			return false;
		}
		readyAt.put(skill, nowTick + skill.cooldownTicks());
		return true;
	}
}
