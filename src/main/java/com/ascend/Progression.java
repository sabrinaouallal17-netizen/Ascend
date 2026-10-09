package com.ascend;

import com.ascend.logic.Achievement;
import com.ascend.logic.Counter;
import com.ascend.logic.Leveling;
import com.ascend.logic.Milestone;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.QuestLog;
import com.ascend.logic.Skill;
import com.ascend.logic.SkillBook;
import com.ascend.logic.Stat;
import com.ascend.logic.XpMultiplier;
import com.ascend.network.LevelTagPayload;
import com.ascend.network.QuestPayload;
import com.ascend.network.SettingsPayload;
import com.ascend.network.SkillsPayload;
import com.ascend.network.SyncPayload;
import com.ascend.stats.StatEffects;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Server side: XP, levels, points, counters, achievements and milestones, and keeping clients in sync. */
public final class Progression {
	private Progression() {
	}

	public static PlayerProgress get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(AscendData.PROGRESS);
	}

	public static SkillBook skills(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(AscendData.SKILLS);
	}

	public static QuestLog quests(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(AscendData.QUESTS);
	}

	/** The player's unlocked milestones, or none if milestones are disabled in this world. */
	public static Set<Milestone> milestones(ServerPlayerEntity player) {
		if (!AscendSettings.get(player.getServer()).milestones) {
			return EnumSet.noneOf(Milestone.class);
		}
		return Milestone.unlocked(get(player).level(), quests(player));
	}

	/** XP from gameplay: multiplied by the world setting and milestone bonuses. */
	public static void addSourceXp(ServerPlayerEntity player, int base) {
		double multiplier = XpMultiplier.multiplier(milestones(player), player.getServerWorld().isNight(),
				AscendSettings.get(player.getServer()).xpMultiplier);
		addXp(player, XpMultiplier.apply(base, multiplier));
	}

	/** Raw XP (bottles, commands, achievement rewards): no multiplier. */
	public static void addXp(ServerPlayerEntity player, int amount) {
		PlayerProgress progress = get(player);
		if (progress.isMaxLevel() || amount <= 0) {
			return;
		}
		Set<Milestone> before = milestones(player);
		int gained = progress.addXp(amount);
		player.sendMessage(Text.literal("+" + amount + " XP").formatted(Formatting.GREEN)
				.append(Text.literal("  Lv " + progress.level() + "  " + progressText(progress)).formatted(Formatting.GRAY)), true);
		if (gained > 0) {
			celebrate(player, progress, gained);
			broadcastLevelTag(player);
			rewardAchievements(player, quests(player).setAtLeast(Counter.LEVEL, progress.level()));
		}
		afterChange(player, before);
		syncProgress(player);
	}

	/** Adds to a quest/achievement counter. */
	public static void count(ServerPlayerEntity player, Counter counter, int amount) {
		Set<Milestone> before = milestones(player);
		rewardAchievements(player, quests(player).add(counter, amount));
		afterChange(player, before);
		syncQuests(player);
	}

	/** Raises a counter to at least a value (biome count, level). */
	public static void countAtLeast(ServerPlayerEntity player, Counter counter, int value) {
		Set<Milestone> before = milestones(player);
		rewardAchievements(player, quests(player).setAtLeast(counter, value));
		afterChange(player, before);
		syncQuests(player);
	}

	public static void spend(ServerPlayerEntity player, int statIndex, int amount) {
		if (statIndex < 0 || statIndex >= Stat.values().length || amount <= 0) {
			return;
		}
		PlayerProgress progress = get(player);
		if (progress.spend(Stat.values()[statIndex], Math.min(amount, Stat.CAP)) > 0) {
			StatEffects.apply(player);
			syncProgress(player);
		}
	}

	public static void setLevel(ServerPlayerEntity player, int level) {
		Set<Milestone> before = milestones(player);
		get(player).setLevel(level);
		rewardAchievements(player, quests(player).setAtLeast(Counter.LEVEL, get(player).level()));
		afterChange(player, before);
		refresh(player);
		broadcastLevelTag(player);
	}

	/** Resets level, XP, stats and skills (achievements and quests are kept). */
	public static void reset(ServerPlayerEntity player) {
		player.setAttached(AscendData.PROGRESS, new PlayerProgress());
		player.setAttached(AscendData.SKILLS, new SkillBook());
		refresh(player);
		broadcastLevelTag(player);
	}

	/** Re-applies bonuses and syncs everything to the client (join, respawn, reset). */
	public static void refresh(ServerPlayerEntity player) {
		StatEffects.apply(player);
		syncProgress(player);
		syncSkills(player);
		syncQuests(player);
	}

	/** Sends a joining player everyone's level and the settings, and tells everyone the joiner's level. */
	public static void onJoin(ServerPlayerEntity player) {
		quests(player).setAtLeast(Counter.LEVEL, get(player).level());
		refresh(player);
		sendSettings(player);
		for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
			send(player, levelTag(other));
		}
		broadcastLevelTag(player);
	}

	public static void broadcastLevelTag(ServerPlayerEntity player) {
		LevelTagPayload payload = levelTag(player);
		for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
			send(other, payload);
		}
	}

	/** Re-sends all level tags, settings and bonuses to everyone, after a setting changed. */
	public static void broadcastAll(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			sendSettings(player);
			broadcastLevelTag(player);
			refresh(player);
		}
	}

	private static void rewardAchievements(ServerPlayerEntity player, List<Achievement> unlocked) {
		for (Achievement achievement : unlocked) {
			player.sendMessage(Text.literal("🏆 Achievement unlocked: ").formatted(Formatting.GOLD)
					.append(Text.literal(achievement.title()).formatted(Formatting.YELLOW, Formatting.BOLD))
					.append(Text.literal(achievement.xpReward() > 0 ? "  +" + achievement.xpReward() + " XP" : "")
							.formatted(Formatting.GREEN)));
			player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
					SoundCategory.PLAYERS, 0.7F, 1.0F);
			addXp(player, achievement.xpReward());
		}
	}

	/** Announces milestones unlocked since {@code before} and re-applies bonuses if they changed. */
	private static void afterChange(ServerPlayerEntity player, Set<Milestone> before) {
		Set<Milestone> after = milestones(player);
		if (after.equals(before)) {
			return;
		}
		List<Milestone> fresh = new ArrayList<>(after);
		fresh.removeAll(before);
		for (Milestone milestone : fresh) {
			player.sendMessage(Text.literal("★ Milestone unlocked: ").formatted(Formatting.GOLD)
					.append(Text.literal(milestone.title()).formatted(Formatting.YELLOW, Formatting.BOLD))
					.append(Text.literal(" — " + milestone.description()).formatted(Formatting.GRAY)));
		}
		StatEffects.apply(player);
	}

	private static LevelTagPayload levelTag(ServerPlayerEntity player) {
		boolean shown = AscendSettings.get(player.getServer()).levelTags;
		return new LevelTagPayload(player.getUuid(), shown ? get(player).level() : 0);
	}

	private static void sendSettings(ServerPlayerEntity player) {
		AscendSettings settings = AscendSettings.get(player.getServer());
		send(player, new SettingsPayload(settings.milestones, settings.levelTagDistance));
	}

	private static String progressText(PlayerProgress progress) {
		return progress.isMaxLevel() ? "(MAX)" : "(" + progress.xp() + "/" + Leveling.xpToNextLevel(progress.level()) + ")";
	}

	private static void celebrate(ServerPlayerEntity player, PlayerProgress progress, int gained) {
		String subtitle = progress.isMaxLevel()
				? "MAX LEVEL! You have ascended."
				: "Level " + progress.level() + "  •  +" + gained + (gained == 1 ? " skill point" : " skill points");
		player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 40, 15));
		player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("LEVEL UP!").formatted(Formatting.GOLD, Formatting.BOLD)));
		player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(subtitle).formatted(Formatting.YELLOW)));
		player.getServerWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.getServerWorld().spawnParticles(ParticleTypes.HAPPY_VILLAGER,
				player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 1.0, 0.5, 0.1);
	}

	public static void syncProgress(ServerPlayerEntity player) {
		PlayerProgress progress = get(player);
		send(player, new SyncPayload(progress.level(), progress.xp(), progress.points(),
				Arrays.stream(progress.stats()).boxed().toList()));
	}

	public static void syncSkills(ServerPlayerEntity player) {
		SkillBook book = skills(player);
		long now = player.getServer().getTicks();
		List<Integer> learned = book.learned().stream().map(Enum::ordinal).toList();
		List<Integer> slots = Arrays.stream(book.slots()).map(skill -> skill == null ? -1 : skill.ordinal()).toList();
		List<Integer> cooldowns = Arrays.stream(Skill.values()).map(skill -> (int) book.cooldownLeft(skill, now)).toList();
		send(player, new SkillsPayload(learned, slots, cooldowns));
	}

	public static void syncQuests(ServerPlayerEntity player) {
		QuestLog log = quests(player);
		List<Integer> counters = Arrays.stream(Counter.values()).map(log::count).toList();
		List<Integer> achievements = log.unlocked().stream().map(Enum::ordinal).toList();
		send(player, new QuestPayload(counters, achievements));
	}

	private static void send(ServerPlayerEntity player, CustomPayload payload) {
		if (ServerPlayNetworking.canSend(player, payload.getId())) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
