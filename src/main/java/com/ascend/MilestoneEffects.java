package com.ascend;

import com.ascend.logic.Milestone;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/** Applies the milestone effects: night vision (25) and Ascended regeneration and aura (200). */
public final class MilestoneEffects {
	private static final int CHECK_EVERY_TICKS = 10;
	private static final int EFFECT_TICKS = 400;
	/** Night vision flickers under 200 ticks, so refresh it before that. */
	private static final int REFRESH_BELOW_TICKS = 220;

	private MilestoneEffects() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % CHECK_EVERY_TICKS != 0 || !AscendSettings.get(server).milestones) {
				return;
			}
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (!player.isAlive() || player.isSpectator()) {
					continue;
				}
				int level = Progression.get(player).level();
				if (Milestone.NIGHT_EYES.unlockedAt(level)) {
					keep(player, StatusEffects.NIGHT_VISION);
				}
				if (Milestone.ASCENDED.unlockedAt(level)) {
					keep(player, StatusEffects.REGENERATION);
					player.getServerWorld().spawnParticles(ParticleTypes.END_ROD,
							player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.4, 0.6, 0.4, 0.01);
				}
			}
		});
	}

	private static void keep(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect) {
		StatusEffectInstance current = player.getStatusEffect(effect);
		if (current == null || current.getDuration() < REFRESH_BELOW_TICKS) {
			player.addStatusEffect(new StatusEffectInstance(effect, EFFECT_TICKS, 0, true, false, true));
		}
	}
}
