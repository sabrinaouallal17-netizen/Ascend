package com.ascend.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

/** Drop tables for Ascend bosses. Item ids are "ascend:..." paths. */
public final class BossLoot {
	public record Drop(String item, String name, double chance) {
	}

	public static final List<Drop> SHADOW_ASSASSIN = List.of(
			new Drop("shadow_dagger", "Shadow Dagger", 0.25),
			new Drop("shadow_pickaxe", "Shadow Pickaxe", 0.15),
			new Drop("shadow_axe", "Shadow Axe", 0.15),
			new Drop("shadow_helmet", "Shadow Helmet", 0.20),
			new Drop("shadow_chestplate", "Shadow Chestplate", 0.20),
			new Drop("shadow_leggings", "Shadow Leggings", 0.20),
			new Drop("shadow_boots", "Shadow Boots", 0.20),
			new Drop("legendary_exp_bottle", "Legendary EXP Bottle", 1.0));

	public static final List<Drop> FROST_WARDEN = List.of(
			new Drop("frost_sword", "Frost Sword", 0.25),
			new Drop("frost_pickaxe", "Frost Pickaxe", 0.15),
			new Drop("frost_axe", "Frost Axe", 0.15),
			new Drop("frost_helmet", "Frost Helmet", 0.20),
			new Drop("frost_chestplate", "Frost Chestplate", 0.20),
			new Drop("frost_leggings", "Frost Leggings", 0.20),
			new Drop("frost_boots", "Frost Boots", 0.20),
			new Drop("legendary_exp_bottle", "Legendary EXP Bottle", 1.0));

	private BossLoot() {
	}

	/** Rolls every drop independently. @param random returns values in [0, 1) */
	public static List<Drop> roll(List<Drop> table, DoubleSupplier random) {
		List<Drop> drops = new ArrayList<>();
		for (Drop drop : table) {
			if (random.getAsDouble() < drop.chance()) {
				drops.add(drop);
			}
		}
		return drops;
	}

	public static String percent(Drop drop) {
		return Math.round(drop.chance() * 100) + "%";
	}
}
