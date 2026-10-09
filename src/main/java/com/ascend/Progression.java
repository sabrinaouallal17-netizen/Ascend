package com.ascend;

import com.ascend.logic.Leveling;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Stat;
import com.ascend.network.SyncPayload;
import com.ascend.stats.StatEffects;
import java.util.Arrays;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Server side: gives XP, handles level ups and skill points, and keeps the client in sync. */
public final class Progression {
	private Progression() {
	}

	public static PlayerProgress get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(AscendData.PROGRESS);
	}

	public static void addXp(ServerPlayerEntity player, int amount) {
		PlayerProgress progress = get(player);
		if (progress.isMaxLevel()) {
			return;
		}
		int gained = progress.addXp(amount);
		player.sendMessage(Text.literal("+" + amount + " XP").formatted(Formatting.GREEN)
				.append(Text.literal("  Lv " + progress.level() + "  " + progressText(progress)).formatted(Formatting.GRAY)), true);
		if (gained > 0) {
			celebrate(player, progress, gained);
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
		get(player).setLevel(level);
		refresh(player);
	}

	public static void reset(ServerPlayerEntity player) {
		player.setAttached(AscendData.PROGRESS, new PlayerProgress());
		refresh(player);
	}

	/** Re-applies stat bonuses and syncs the client (join, respawn, reset). */
	public static void refresh(ServerPlayerEntity player) {
		PlayerProgress progress = get(player);
		StatEffects.apply(player, progress);
		sync(player, progress);
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

	private static void sync(ServerPlayerEntity player, PlayerProgress progress) {
		if (ServerPlayNetworking.canSend(player, SyncPayload.ID)) {
			ServerPlayNetworking.send(player, new SyncPayload(progress.level(), progress.xp(), progress.points(),
					Arrays.stream(progress.stats()).boxed().toList()));
		}
	}
}
