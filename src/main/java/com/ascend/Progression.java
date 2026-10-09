package com.ascend;

import com.ascend.logic.Leveling;
import com.ascend.logic.Milestone;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Stat;
import com.ascend.logic.XpMultiplier;
import com.ascend.network.LevelTagPayload;
import com.ascend.network.SettingsPayload;
import com.ascend.network.SyncPayload;
import com.ascend.stats.StatEffects;
import java.util.Arrays;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

/** Server side: gives XP, handles level ups and skill points, and keeps clients in sync. */
public final class Progression {
	private Progression() {
	}

	public static PlayerProgress get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(AscendData.PROGRESS);
	}

	/** XP from gameplay: multiplied by the world setting and milestone bonuses. */
	public static void addSourceXp(ServerPlayerEntity player, int base) {
		AscendSettings settings = AscendSettings.get(player.getServer());
		double multiplier = XpMultiplier.multiplier(get(player).level(), player.getServerWorld().isNight(),
				settings.xpMultiplier, settings.milestones);
		addXp(player, XpMultiplier.apply(base, multiplier));
	}

	/** Raw XP (bottles, commands): no multiplier. */
	public static void addXp(ServerPlayerEntity player, int amount) {
		PlayerProgress progress = get(player);
		if (progress.isMaxLevel() || amount <= 0) {
			return;
		}
		int levelBefore = progress.level();
		int gained = progress.addXp(amount);
		player.sendMessage(Text.literal("+" + amount + " XP").formatted(Formatting.GREEN)
				.append(Text.literal("  Lv " + progress.level() + "  " + progressText(progress)).formatted(Formatting.GRAY)), true);
		if (gained > 0) {
			celebrate(player, progress, gained);
			announceMilestones(player, levelBefore, progress.level());
			broadcastLevelTag(player);
		}
		sync(player, progress);
	}

	public static void spend(ServerPlayerEntity player, int statIndex) {
		if (statIndex < 0 || statIndex >= Stat.values().length) {
			return;
		}
		PlayerProgress progress = get(player);
		if (progress.spend(Stat.values()[statIndex])) {
			StatEffects.apply(player, progress);
			sync(player, progress);
		}
	}

	public static void setLevel(ServerPlayerEntity player, int level) {
		int levelBefore = get(player).level();
		get(player).setLevel(level);
		announceMilestones(player, levelBefore, get(player).level());
		refresh(player);
		broadcastLevelTag(player);
	}

	public static void reset(ServerPlayerEntity player) {
		player.setAttached(AscendData.PROGRESS, new PlayerProgress());
		refresh(player);
		broadcastLevelTag(player);
	}

	/** Re-applies stat bonuses and syncs the client (join, respawn, reset). */
	public static void refresh(ServerPlayerEntity player) {
		PlayerProgress progress = get(player);
		StatEffects.apply(player, progress);
		sync(player, progress);
	}

	/** Sends a joining player everyone's level and the settings, and tells everyone the joiner's level. */
	public static void onJoin(ServerPlayerEntity player) {
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

	/** Re-sends all level tags and settings to everyone, after a setting changed. */
	public static void broadcastAll(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			sendSettings(player);
			broadcastLevelTag(player);
		}
	}

	private static LevelTagPayload levelTag(ServerPlayerEntity player) {
		boolean shown = AscendSettings.get(player.getServer()).levelTags;
		return new LevelTagPayload(player.getUuid(), shown ? get(player).level() : 0);
	}

	private static void sendSettings(ServerPlayerEntity player) {
		send(player, new SettingsPayload(AscendSettings.get(player.getServer()).milestones));
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

	private static void announceMilestones(ServerPlayerEntity player, int fromLevel, int toLevel) {
		if (!AscendSettings.get(player.getServer()).milestones) {
			return;
		}
		for (Milestone milestone : Milestone.reachedBetween(fromLevel, toLevel)) {
			player.sendMessage(Text.literal("★ Milestone unlocked: ").formatted(Formatting.GOLD)
					.append(Text.literal(milestone.title()).formatted(Formatting.YELLOW, Formatting.BOLD))
					.append(Text.literal(" — " + milestone.description()).formatted(Formatting.GRAY)));
		}
	}

	private static void sync(ServerPlayerEntity player, PlayerProgress progress) {
		send(player, new SyncPayload(progress.level(), progress.xp(), progress.points(),
				Arrays.stream(progress.stats()).boxed().toList()));
	}

	private static void send(ServerPlayerEntity player, net.minecraft.network.packet.CustomPayload payload) {
		if (ServerPlayNetworking.canSend(player, payload.getId())) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
