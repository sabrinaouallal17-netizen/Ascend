package com.ascend;

import com.ascend.logic.Counter;
import com.ascend.logic.Skill;
import com.ascend.logic.SkillBook;
import com.ascend.network.SkillActionPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Server side of the skills: learning, binding and using them. */
public final class SkillSystem {
	private static final double DASH_STRENGTH = 1.6;
	private static final float HEAL_AMOUNT = 6.0F;
	private static final double HEAL_RADIUS = 6.0;
	private static final double SLAM_RADIUS = 4.0;
	private static final float SLAM_DAMAGE = 6.0F;
	private static final int BERSERK_TICKS = 10 * 20;
	private static final int LIFESTEAL_TICKS = 10 * 20;
	private static final float LIFESTEAL_RATIO = 0.3F;
	private static final double STEP_RANGE = 12.0;
	private static final double NOVA_RADIUS = 6.0;
	private static final float NOVA_DAMAGE = 6.0F;
	/** Players with Lifesteal active, and the server tick it ends. */
	private static final Map<UUID, Long> LIFESTEAL_UNTIL = new HashMap<>();

	private SkillSystem() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((target, source, amount) -> {
			if (source.getAttacker() instanceof ServerPlayerEntity player && target != player) {
				Long until = LIFESTEAL_UNTIL.get(player.getUuid());
				if (until != null) {
					if (until >= player.getServer().getTicks()) {
						player.heal(amount * LIFESTEAL_RATIO);
						player.getServerWorld().spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(),
								target.getBodyY(0.5), target.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
					} else {
						LIFESTEAL_UNTIL.remove(player.getUuid());
					}
				}
			}
			return true;
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LIFESTEAL_UNTIL.clear());
	}

	public static void handle(ServerPlayerEntity player, SkillActionPayload payload) {
		switch (payload.action()) {
			case SkillActionPayload.LEARN -> skill(payload.skill()).ifPresent(skill -> learn(player, skill));
			case SkillActionPayload.BIND -> skill(payload.skill()).ifPresent(skill -> {
				if (Progression.skills(player).bind(skill, payload.slot())) {
					Progression.syncSkills(player);
				}
			});
			case SkillActionPayload.USE -> use(player, payload.slot());
			default -> {
			}
		}
	}

	private static java.util.Optional<Skill> skill(int index) {
		return index >= 0 && index < Skill.values().length
				? java.util.Optional.of(Skill.values()[index])
				: java.util.Optional.empty();
	}

	private static void learn(ServerPlayerEntity player, Skill skill) {
		SkillBook book = Progression.skills(player);
		switch (book.learn(skill, Progression.get(player), Progression.quests(player))) {
			case LEARNED -> {
				player.sendMessage(Text.literal("Skill learned: ").formatted(Formatting.AQUA)
						.append(Text.literal(skill.title()).formatted(Formatting.WHITE, Formatting.BOLD)), false);
				Progression.syncProgress(player);
				Progression.syncSkills(player);
				Progression.count(player, Counter.SKILLS_LEARNED, 1);
			}
			case LEVEL_TOO_LOW -> player.sendMessage(Text.literal("Requires level " + skill.requiredLevel() + ".")
					.formatted(Formatting.RED), true);
			case NOT_ENOUGH_POINTS -> player.sendMessage(Text.literal("Not enough skill points (" + skill.cost() + " needed).")
					.formatted(Formatting.RED), true);
			case QUEST_INCOMPLETE -> player.sendMessage(Text.literal("Quest needed: " + skill.quest().task() + " ("
					+ Progression.quests(player).count(skill.quest()) + "/" + skill.questTarget() + ")").formatted(Formatting.RED), true);
			case ALREADY_LEARNED -> {
			}
		}
	}

	/** The living mob the player is aiming at, within {@link #STEP_RANGE}. */
	private static LivingEntity aimedMob(ServerPlayerEntity player) {
		Vec3d eye = player.getEyePos();
		Vec3d end = eye.add(player.getRotationVector().multiply(STEP_RANGE));
		Box box = player.getBoundingBox().stretch(player.getRotationVector().multiply(STEP_RANGE)).expand(1.0);
		EntityHitResult hit = ProjectileUtil.raycast(player, eye, end, box,
				entity -> entity instanceof LivingEntity living && living.isAlive() && !entity.isSpectator() && entity != player,
				STEP_RANGE * STEP_RANGE);
		if (hit == null || !player.canSee(hit.getEntity())) {
			return null;
		}
		return (LivingEntity) hit.getEntity();
	}

	private static void use(ServerPlayerEntity player, int slot) {
		SkillBook book = Progression.skills(player);
		Skill skill = book.inSlot(slot);
		if (skill == null || !player.isAlive() || player.isSpectator()) {
			return;
		}
		long now = player.getServer().getTicks();
		LivingEntity stepTarget = null;
		if (skill == Skill.SHADOW_STEP && book.cooldownLeft(skill, now) <= 0) {
			stepTarget = aimedMob(player);
			if (stepTarget == null) {
				player.sendMessage(Text.literal("No target in sight.").formatted(Formatting.GRAY), true);
				return;
			}
		}
		if (!book.tryUse(skill, now)) {
			player.sendMessage(Text.literal(skill.title() + " is recharging (" + (book.cooldownLeft(skill, now) + 19) / 20 + "s)")
					.formatted(Formatting.GRAY), true);
			return;
		}
		ServerWorld world = player.getServerWorld();
		switch (skill) {
			case DASH -> {
				Vec3d look = player.getRotationVector().multiply(1.0, 0.0, 1.0).normalize();
				player.addVelocity(look.x * DASH_STRENGTH, 0.3, look.z * DASH_STRENGTH);
				player.velocityModified = true;
				player.fallDistance = 0.0F;
				world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2, player.getZ(), 15, 0.3, 0.1, 0.3, 0.02);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0F, 1.4F);
			}
			case HEAL_PULSE -> {
				for (ServerPlayerEntity other : world.getPlayers(p -> p.squaredDistanceTo(player) <= HEAL_RADIUS * HEAL_RADIUS)) {
					other.heal(HEAL_AMOUNT);
					world.spawnParticles(ParticleTypes.HEART, other.getX(), other.getY() + 1.8, other.getZ(), 5, 0.4, 0.3, 0.4, 0.0);
				}
				world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.5F, 1.2F);
			}
			case GROUND_SLAM -> {
				Box area = player.getBoundingBox().expand(SLAM_RADIUS, 1.5, SLAM_RADIUS);
				for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area,
						entity -> entity instanceof Monster && entity.isAlive())) {
					target.damage(world.getDamageSources().playerAttack(player), SLAM_DAMAGE);
					target.takeKnockback(1.2, player.getX() - target.getX(), player.getZ() - target.getZ());
					target.addVelocity(0.0, 0.4, 0.0);
				}
				world.spawnParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 6, 1.5, 0.2, 1.5, 0.0);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.8F, 1.2F);
			}
			case BERSERK -> {
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, BERSERK_TICKS, 1));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, BERSERK_TICKS, 1));
				world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.5, 0.5, 0.5, 0.0);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_RAVAGER_ROAR, SoundCategory.PLAYERS, 0.8F, 1.3F);
			}
			case LIFESTEAL -> {
				LIFESTEAL_UNTIL.put(player.getUuid(), now + LIFESTEAL_TICKS);
				world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, player.getX(), player.getY() + 1.0, player.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_WITHER_AMBIENT, SoundCategory.PLAYERS, 0.5F, 1.6F);
			}
			case SHADOW_STEP -> {
				Vec3d behind = stepTarget.getPos().subtract(stepTarget.getRotationVector().multiply(1.0, 0.0, 1.0).normalize().multiply(1.5));
				world.spawnParticles(AscendParticles.SHADOW_WISP, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
				player.teleport(world, behind.x, stepTarget.getY(), behind.z, stepTarget.getHeadYaw(), player.getPitch());
				player.fallDistance = 0.0F;
				world.spawnParticles(AscendParticles.SHADOW_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.3, 0.6, 0.3, 0.1);
				world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.7F);
			}
			case FROST_NOVA -> {
				Box area = player.getBoundingBox().expand(NOVA_RADIUS, 2.0, NOVA_RADIUS);
				for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area,
						entity -> entity instanceof Monster && entity.isAlive())) {
					target.damage(world.getDamageSources().freeze(), NOVA_DAMAGE);
					target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
					target.setFrozenTicks(Math.max(target.getFrozenTicks(), target.getMinFreezeDamageTicks() + 100));
					world.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getBodyY(0.5), target.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
				}
				world.spawnParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.5, player.getZ(), 80, NOVA_RADIUS / 2, 0.4,
						NOVA_RADIUS / 2, 0.05);
				world.spawnParticles(AscendParticles.FROST_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 25, 1.0, 0.5, 1.0, 0.25);
				world.playSound(null, player.getBlockPos(), AscendSounds.FROST_NOVA, SoundCategory.PLAYERS, 1.0F, 1.2F);
			}
		}
		Progression.syncSkills(player);
	}
}
