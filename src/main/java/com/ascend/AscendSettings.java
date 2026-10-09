package com.ascend;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

/** Per-world settings, changed with /ascend settings. */
public class AscendSettings extends PersistentState {
	public static final double MIN_MULTIPLIER = 0.1;
	public static final double MAX_MULTIPLIER = 10.0;

	private static final PersistentState.Type<AscendSettings> TYPE =
			new PersistentState.Type<>(AscendSettings::new, AscendSettings::fromNbt, null);

	/** Keep level, XP and stats on death. If false, dying resets you to level 1. */
	public boolean keepXp = true;
	/** Multiplies XP earned from gameplay. */
	public double xpMultiplier = 1.0;
	/** Milestone rewards (+10% XP, night vision, double jump, night XP, Ascended). */
	public boolean milestones = true;
	/** Show "[Lv X]" above players' heads. */
	public boolean levelTags = true;
	/** Give hostile mobs random levels. */
	public boolean mobLevels = true;
	/** Mob levels are shown above heads only within this many blocks (always when aimed at). */
	public int levelTagDistance = 16;
	public static final int MIN_TAG_DISTANCE = 4;
	public static final int MAX_TAG_DISTANCE = 64;

	public static AscendSettings get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "ascend_settings");
	}

	private static AscendSettings fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		AscendSettings settings = new AscendSettings();
		if (nbt.contains("keepXp")) {
			settings.keepXp = nbt.getBoolean("keepXp");
		}
		if (nbt.contains("xpMultiplier")) {
			settings.xpMultiplier = Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, nbt.getDouble("xpMultiplier")));
		}
		if (nbt.contains("milestones")) {
			settings.milestones = nbt.getBoolean("milestones");
		}
		if (nbt.contains("levelTags")) {
			settings.levelTags = nbt.getBoolean("levelTags");
		}
		if (nbt.contains("levelTagDistance")) {
			settings.levelTagDistance = Math.max(MIN_TAG_DISTANCE, Math.min(MAX_TAG_DISTANCE, nbt.getInt("levelTagDistance")));
		}
		if (nbt.contains("mobLevels")) {
			settings.mobLevels = nbt.getBoolean("mobLevels");
		}
		return settings;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		nbt.putBoolean("keepXp", keepXp);
		nbt.putDouble("xpMultiplier", xpMultiplier);
		nbt.putBoolean("milestones", milestones);
		nbt.putBoolean("levelTags", levelTags);
		nbt.putBoolean("mobLevels", mobLevels);
		nbt.putInt("levelTagDistance", levelTagDistance);
		return nbt;
	}
}
