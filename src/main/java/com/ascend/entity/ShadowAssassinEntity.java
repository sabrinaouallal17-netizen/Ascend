package com.ascend.entity;

import com.ascend.Ascend;
import com.ascend.item.AscendItems;
import com.ascend.logic.BossLoot;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

/** The Shadow Assassin boss: teleports behind its target and gets faster below half health. */
public class ShadowAssassinEntity extends ZombieEntity {
	private final ServerBossBar bossBar = new ServerBossBar(
			Text.literal("Shadow Assassin").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
			BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);
	private final Set<UUID> toldAboutDrops = new HashSet<>();
	private int teleportCooldown = 100;

	public ShadowAssassinEntity(EntityType<? extends ZombieEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 100;
		this.setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return ZombieEntity.createZombieAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
				.add(EntityAttributes.GENERIC_ARMOR, 10.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
				.add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0);
	}

	@Override
	protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
		this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(AscendItems.SHADOW_DAGGER));
		this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
	}

	@Override
	protected boolean burnsInDaylight() {
		return false;
	}

	@Override
	protected boolean canConvertInWater() {
		return false;
	}

	@Override
	public void setBaby(boolean baby) {
		super.setBaby(false);
	}

	@Override
	public boolean canPickUpLoot() {
		return false;
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		bossBar.setPercent(this.getHealth() / this.getMaxHealth());
		if (this.getHealth() < this.getMaxHealth() / 2.0F && !this.hasStatusEffect(StatusEffects.SPEED)) {
			this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1));
		}
		LivingEntity target = this.getTarget();
		if (target != null && --teleportCooldown <= 0) {
			teleportCooldown = 80 + this.random.nextInt(60);
			teleportBehind(target);
		}
	}

	private void teleportBehind(LivingEntity target) {
		Vec3d facing = target.getRotationVector().multiply(1.0, 0.0, 1.0);
		if (facing.lengthSquared() < 1.0E-4 || this.squaredDistanceTo(target) < 9.0) {
			return;
		}
		Vec3d behind = target.getPos().subtract(facing.normalize().multiply(2.0));
		if (!this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(behind.subtract(this.getPos())))) {
			return;
		}
		if (this.getWorld() instanceof ServerWorld world) {
			world.spawnParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
			this.requestTeleport(behind.x, behind.y, behind.z);
			world.spawnParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.3, 0.6, 0.3, 0.2);
			world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 0.6F);
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		bossBar.addPlayer(player);
		if (toldAboutDrops.add(player.getUuid())) {
			MutableText message = Text.literal("☠ Shadow Assassin — possible drops:").formatted(Formatting.DARK_PURPLE, Formatting.BOLD);
			for (BossLoot.Drop drop : BossLoot.SHADOW_ASSASSIN) {
				message.append(Text.literal("\n  " + drop.name() + "  ").formatted(Formatting.LIGHT_PURPLE))
						.append(Text.literal(BossLoot.percent(drop)).formatted(Formatting.WHITE));
			}
			player.sendMessage(message, false);
		}
	}

	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		bossBar.removePlayer(player);
	}

	@Override
	protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
		for (BossLoot.Drop drop : BossLoot.roll(BossLoot.SHADOW_ASSASSIN, this.random::nextDouble)) {
			this.dropStack(new ItemStack(Registries.ITEM.get(Ascend.id(drop.item()))));
		}
	}
}
