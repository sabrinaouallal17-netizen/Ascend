package com.ascend.logic;

/** How much RPG XP each action gives. */
public final class XpRewards {
	public static final int COAL = 2;
	public static final int COPPER = 2;
	public static final int IRON = 4;
	public static final int REDSTONE = 3;
	public static final int LAPIS = 3;
	public static final int QUARTZ = 3;
	public static final int GOLD = 6;
	public static final int DIAMOND = 15;
	public static final int EMERALD = 20;
	public static final int ANCIENT_DEBRIS = 30;
	public static final int CROP = 1;
	public static final int FISHING = 5;
	public static final int TRADE = 3;
	/** Per enchantment level spent at the enchanting table. */
	public static final int ENCHANT_PER_LEVEL = 2;
	public static final int BREEDING = 4;
	public static final int NEW_BIOME = 10;

	private XpRewards() {
	}

	/** Half the mob's max health, at least 1: zombie 10, enderman 20, wither 150, ender dragon 100. */
	public static int forMob(float maxHealth) {
		return Math.max(1, (int) Math.ceil(maxHealth / 2.0F));
	}
}
