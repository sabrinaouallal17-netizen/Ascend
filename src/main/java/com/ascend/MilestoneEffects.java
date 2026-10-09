package com.ascend;

import com.ascend.item.AscendItems;
import com.ascend.logic.Milestone;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/** Applies effects: Night Eyes, Ascended (regeneration and aura) and the full Shadow set (Speed I). */
public final class MilestoneEffects {
	private static final int CHECK_EVERY_TICKS = 10;
	private static final int EFFECT_TICKS = 400;
	/** Night vision flickers under 200 ticks, so refresh it before that. */
	private static final int REFRESH_BELOW_TICKS = 220;

	private MilestoneEffects() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % CHECK_EVERY_TICKS != 0) {
				return;
			}
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (!player.isAlive() || player.isSpectator()) {
					continue;
				}
				if (AscendItems.wearsFullShadowSet(player)) {
					keep(player, StatusEffects.SPEED);
					// Shadow aura around the wearer, visible to everyone.
					player.getServerWorld().spawnParticles(AscendParticles.SHADOW_WISP, player.getX(), player.getY() + 0.2,
							player.getZ(), 2, 0.35, 0.1, 0.35, 0.01);
				}
				if (AscendItems.wearsFullFrostSet(player)) {
					keep(player, StatusEffects.RESISTANCE);
					player.setFrozenTicks(0);
					player.getServerWorld().spawnParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.2,
							player.getZ(), 2, 0.35, 0.1, 0.35, 0.01);
				}
				Set<Milestone> milestones = Progression.milestones(player);
				if (milestones.contains(Milestone.NIGHT_EYES)) {
					keep(player, StatusEffects.NIGHT_VISION);
				}
				if (milestones.contains(Milestone.ASCENDED)) {
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
