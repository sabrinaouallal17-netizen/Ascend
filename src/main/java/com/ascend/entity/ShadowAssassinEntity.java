package com.ascend.entity;

import com.ascend.Ascend;
import com.ascend.AscendParticles;
import com.ascend.AscendSounds;
import com.ascend.item.AscendItems;
import com.ascend.logic.BossLoot;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

/**
 * The Shadow Assassin boss.
 * <ul>
 * <li>Teleports behind its target.</li>
 * <li>Shadow Strike: winds up (1s), then dashes through its target.</li>
 * <li>Shadow Clones: summons 2 short-lived clones that have 1 hit point.</li>
 * <li>Shadow Storm (enraged only): spins in the air, pulling in and hurting nearby players.</li>
 * <li>Below half health it roars, turns the boss bar red, gets faster and uses abilities more often.</li>
 * </ul>
 * The current ability is synced to clients so the model can animate it.
 */
public class ShadowAssassinEntity extends ZombieEntity {
	public static final int IDLE = 0;
	public static final int WINDUP = 1;
	public static final int STRIKE = 2;
	public static final int STORM = 3;
	public static final int SUMMON = 4;

	private static final TrackedData<Integer> STATE =
			DataTracker.registerData(ShadowAssassinEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> CLONE =
			DataTracker.registerData(ShadowAssassinEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final Identifier ROOTED = Ascend.id("boss_rooted");
	private static final int WINDUP_TICKS = 20;
	private static final int STRIKE_TICKS = 8;
	private static final int STORM_TICKS = 50;
	private static final int SUMMON_TICKS = 20;
	private static final int CLONE_LIFETIME_TICKS = 200;
	private static final double STORM_RADIUS = 5.0;

	private final ServerBossBar bossBar = new ServerBossBar(
			Text.literal("Shadow Assassin").formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
			BossBar.Color.PURPLE, BossBar.Style.NOTCHED_10);
	private final Set<UUID> toldAboutDrops = new HashSet<>();
	private final Set<UUID> struckThisDash = new HashSet<>();
	private int teleportCooldown = 100;
	private int abilityCooldown = 60;
	private int stateTicks;
	private int cloneLife = CLONE_LIFETIME_TICKS;
	private boolean enraged;
	private boolean introDone;
	private Vec3d strikeDirection = Vec3d.ZERO;

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
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(STATE, IDLE);
		builder.add(CLONE, false);
	}

	public int getState() {
		return this.dataTracker.get(STATE);
	}

	private void setState(int state) {
		this.dataTracker.set(STATE, state);
		this.stateTicks = 0;
	}

	public boolean isClone() {
		return this.dataTracker.get(CLONE);
	}

	public void setClone(boolean clone) {
		this.dataTracker.set(CLONE, clone);
		if (clone) {
			this.experiencePoints = 0;
		}
	}

	@Override
	protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
		equipDagger();
	}

	private void equipDagger() {
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

	// ---- Sounds ----

	@Override
	protected SoundEvent getAmbientSound() {
		return isClone() ? null : AscendSounds.BOSS_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return AscendSounds.BOSS_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return isClone() ? AscendSounds.BOSS_CLONE : AscendSounds.BOSS_DEATH;
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.BLOCK_WOOL_STEP;
	}

	@Override
	protected float getSoundVolume() {
		return isClone() ? 0.6F : 2.0F;
	}

	// ---- Client visuals ----

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient()) {
			spawnClientParticles();
		}
	}

	private void spawnClientParticles() {
		World world = this.getWorld();
		if (this.random.nextInt(isClone() ? 4 : 2) == 0) {
			world.addParticle(AscendParticles.SHADOW_WISP,
					this.getX() + (this.random.nextDouble() - 0.5) * 0.8, this.getY() + this.random.nextDouble() * 0.4,
					this.getZ() + (this.random.nextDouble() - 0.5) * 0.8, 0.0, 0.03, 0.0);
		}
		int state = getState();
		if (state == WINDUP) {
			// Sparks spiral into the raised blade.
			double angle = this.age * 0.8;
			for (int i = 0; i < 2; i++) {
				double a = angle + i * Math.PI;
				world.addParticle(AscendParticles.SHADOW_SPARK,
						this.getX() + Math.cos(a) * 1.2, this.getY() + 2.2, this.getZ() + Math.sin(a) * 1.2,
						-Math.cos(a) * 0.08, 0.02, -Math.sin(a) * 0.08);
			}
		} else if (state == STORM) {
			for (int i = 0; i < 4; i++) {
				double a = this.age * 0.5 + i * Math.PI / 2;
				double r = STORM_RADIUS * (0.4 + 0.6 * this.random.nextDouble());
				world.addParticle(AscendParticles.SHADOW_WISP,
						this.getX() + Math.cos(a) * r, this.getY() + 0.3 + this.random.nextDouble() * 1.5,
						this.getZ() + Math.sin(a) * r, -Math.sin(a) * 0.25, 0.02, Math.cos(a) * 0.25);
			}
		} else if (state == SUMMON) {
			world.addParticle(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 2.4, this.getZ(),
					(this.random.nextDouble() - 0.5) * 0.4, 0.2, (this.random.nextDouble() - 0.5) * 0.4);
		}
		if (this.deathTime > 0) {
			for (int i = 0; i < 4; i++) {
				world.addParticle(AscendParticles.SHADOW_WISP,
						this.getX() + (this.random.nextDouble() - 0.5), this.getY() + this.random.nextDouble() * 2.0,
						this.getZ() + (this.random.nextDouble() - 0.5), 0.0, 0.08, 0.0);
			}
		}
	}

	// ---- AI ----

	@Override
	protected void mobTick() {
		super.mobTick();
		if (!(this.getWorld() instanceof ServerWorld world)) {
			return;
		}
		if (isClone()) {
			if (--cloneLife <= 0) {
				vanish(world);
			}
			return;
		}
		if (!introDone) {
			introDone = true;
			playIntro(world);
		}
		bossBar.setPercent(this.getHealth() / this.getMaxHealth());
		if (!enraged && this.getHealth() < this.getMaxHealth() / 2.0F) {
			enrage(world);
		}
		if (enraged && !this.hasStatusEffect(StatusEffects.SPEED)) {
			this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 1, false, false));
		}

		LivingEntity target = this.getTarget();
		switch (getState()) {
			case IDLE -> tickIdle(world, target);
			case WINDUP -> tickWindup(world, target);
			case STRIKE -> tickStrike(world);
			case STORM -> tickStorm(world);
			case SUMMON -> tickSummon(world, target);
			default -> setState(IDLE);
		}
	}

	private void tickIdle(ServerWorld world, LivingEntity target) {
		if (target == null) {
			return;
		}
		if (--teleportCooldown <= 0) {
			teleportCooldown = (enraged ? 60 : 90) + this.random.nextInt(60);
			teleportBehind(world, target);
		}
		if (--abilityCooldown <= 0 && this.squaredDistanceTo(target) < 16 * 16) {
			int roll = this.random.nextInt(100);
			if (enraged && roll < 40) {
				startAbility(world, STORM, AscendSounds.BOSS_STORM);
			} else if (roll < (enraged ? 80 : 70)) {
				startAbility(world, WINDUP, AscendSounds.BOSS_CHARGE);
			} else {
				startAbility(world, SUMMON, AscendSounds.BOSS_CLONE);
			}
		}
	}

	private void startAbility(ServerWorld world, int state, SoundEvent sound) {
		setState(state);
		struckThisDash.clear();
		setRooted(state != STRIKE);
		world.playSound(null, this.getBlockPos(), sound, SoundCategory.HOSTILE, 2.0F, 1.0F);
	}

	private void endAbility(int cooldown) {
		setState(IDLE);
		setRooted(false);
		abilityCooldown = enraged ? cooldown * 3 / 5 : cooldown;
	}

	private void tickWindup(ServerWorld world, LivingEntity target) {
		if (target != null) {
			this.getLookControl().lookAt(target, 60.0F, 60.0F);
		}
		if (++stateTicks < WINDUP_TICKS) {
			return;
		}
		Vec3d toTarget = target == null ? this.getRotationVector() : target.getPos().subtract(this.getPos());
		toTarget = toTarget.multiply(1.0, 0.0, 1.0);
		strikeDirection = toTarget.lengthSquared() < 1.0E-4 ? Vec3d.ZERO : toTarget.normalize();
		setState(STRIKE);
		setRooted(false);
		world.playSound(null, this.getBlockPos(), AscendSounds.BOSS_SLASH, SoundCategory.HOSTILE, 2.0F, 0.8F);
	}

	private void tickStrike(ServerWorld world) {
		this.setVelocity(strikeDirection.x * 1.4, this.getVelocity().y, strikeDirection.z * 1.4);
		this.velocityModified = true;
		world.spawnParticles(AscendParticles.SHADOW_WISP, this.getX(), this.getY() + 1.0, this.getZ(), 6, 0.3, 0.5, 0.3, 0.01);
		for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class,
				this.getBoundingBox().expand(0.8), player -> !player.isSpectator() && !player.isCreative())) {
			if (struckThisDash.add(player.getUuid())) {
				player.damage(this.getDamageSources().mobAttack(this), 10.0F);
				player.takeKnockback(1.0, -strikeDirection.x, -strikeDirection.z);
				world.spawnParticles(AscendParticles.SHADOW_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.3, 0.5, 0.3, 0.2);
			}
		}
		if (++stateTicks >= STRIKE_TICKS) {
			endAbility(100);
		}
	}

	private void tickStorm(ServerWorld world) {
		if (stateTicks % 4 == 0) {
			for (ServerPlayerEntity player : world.getEntitiesByClass(ServerPlayerEntity.class,
					this.getBoundingBox().expand(STORM_RADIUS, 2.0, STORM_RADIUS),
					player -> !player.isSpectator() && !player.isCreative())) {
				Vec3d pull = this.getPos().subtract(player.getPos()).multiply(0.12);
				player.addVelocity(pull.x, 0.05, pull.z);
				player.velocityModified = true;
				player.damage(this.getDamageSources().mobAttack(this), 3.0F);
			}
			world.spawnParticles(AscendParticles.SHADOW_SPARK, this.getX(), this.getY() + 1.2, this.getZ(), 10, 1.5, 0.6, 1.5, 0.1);
		}
		if (++stateTicks >= STORM_TICKS) {
			endAbility(140);
		}
	}

	private void tickSummon(ServerWorld world, LivingEntity target) {
		if (++stateTicks == SUMMON_TICKS / 2 && target != null) {
			for (int i = 0; i < 2; i++) {
				ShadowAssassinEntity clone = AscendEntities.SHADOW_ASSASSIN.create(world);
				if (clone == null) {
					continue;
				}
				double angle = this.random.nextDouble() * Math.PI * 2;
				clone.setClone(true);
				clone.refreshPositionAndAngles(target.getX() + Math.cos(angle) * 3, target.getY(),
						target.getZ() + Math.sin(angle) * 3, this.getYaw(), 0.0F);
				clone.setHealth(1.0F);
				clone.equipDagger();
				clone.setTarget(target);
				world.spawnEntity(clone);
				world.spawnParticles(AscendParticles.SHADOW_WISP, clone.getX(), clone.getY() + 1.0, clone.getZ(), 30, 0.4, 0.8, 0.4, 0.02);
			}
		}
		if (stateTicks >= SUMMON_TICKS) {
			endAbility(120);
		}
	}

	private void setRooted(boolean rooted) {
		EntityAttributeInstance speed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		speed.removeModifier(ROOTED);
		if (rooted) {
			speed.addTemporaryModifier(new EntityAttributeModifier(ROOTED, -1.0,
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
			this.getNavigation().stop();
		}
	}

	private void teleportBehind(ServerWorld world, LivingEntity target) {
		Vec3d facing = target.getRotationVector().multiply(1.0, 0.0, 1.0);
		if (facing.lengthSquared() < 1.0E-4 || this.squaredDistanceTo(target) < 9.0) {
			return;
		}
		Vec3d behind = target.getPos().subtract(facing.normalize().multiply(2.0));
		if (!world.isSpaceEmpty(this, this.getBoundingBox().offset(behind.subtract(this.getPos())))) {
			return;
		}
		world.spawnParticles(AscendParticles.SHADOW_WISP, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.3, 0.8, 0.3, 0.03);
		world.playSound(null, this.getBlockPos(), AscendSounds.BOSS_TELEPORT, SoundCategory.HOSTILE, 1.5F, 1.0F);
		this.requestTeleport(behind.x, behind.y, behind.z);
		world.spawnParticles(AscendParticles.SHADOW_WISP, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.3, 0.8, 0.3, 0.03);
		world.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.3, 0.8, 0.3, 0.05);
		world.playSound(null, this.getBlockPos(), AscendSounds.BOSS_TELEPORT, SoundCategory.HOSTILE, 1.5F, 1.3F);
	}

	private List<ServerPlayerEntity> playersAround(ServerWorld world, double radius) {
		return world.getPlayers(player -> player.squaredDistanceTo(this) < radius * radius && !player.isSpectator());
	}

	private void playIntro(ServerWorld world) {
		world.playSound(null, this.getBlockPos(), AscendSounds.BOSS_SPAWN, SoundCategory.HOSTILE, 4.0F, 1.0F);
		world.spawnParticles(AscendParticles.SHADOW_WISP, this.getX(), this.getY() + 1.0, this.getZ(), 120, 0.6, 1.5, 0.6, 0.05);
		world.spawnParticles(ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + 1.0, this.getZ(), 80, 0.5, 1.5, 0.5, 0.1);
		for (ServerPlayerEntity player : playersAround(world, 32)) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0, false, false));
		}
	}

	private void enrage(ServerWorld world) {
		enraged = true;
		abilityCooldown = Math.min(abilityCooldown, 20);
		bossBar.setColor(BossBar.Color.RED);
		bossBar.setName(Text.literal("Shadow Assassin — Enraged").formatted(Formatting.DARK_RED, Formatting.BOLD));
		world.playSound(null, this.getBlockPos(), AscendSounds.BOSS_ROAR, SoundCategory.HOSTILE, 4.0F, 1.0F);
		world.spawnParticles(AscendParticles.SHADOW_SPARK, this.getX(), this.getY() + 1.0, this.getZ(), 80, 1.0, 1.0, 1.0, 0.3);
		for (ServerPlayerEntity player : playersAround(world, 32)) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0, false, false));
			player.sendMessage(Text.literal("The Shadow Assassin is enraged!").formatted(Formatting.DARK_RED, Formatting.ITALIC), true);
		}
	}

	private void vanish(ServerWorld world) {
		world.spawnParticles(AscendParticles.SHADOW_WISP, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.4, 0.8, 0.4, 0.03);
		world.playSound(null, this.getBlockPos(), AscendSounds.BOSS_CLONE, SoundCategory.HOSTILE, 0.8F, 1.4F);
		this.discard();
	}

	// ---- Combat ----

	@Override
	public boolean tryAttack(Entity target) {
		boolean hit = super.tryAttack(target);
		if (hit && this.getWorld() instanceof ServerWorld world) {
			world.playSound(null, this.getBlockPos(), AscendSounds.BOSS_SLASH, SoundCategory.HOSTILE, 1.2F,
					0.9F + this.random.nextFloat() * 0.3F);
			world.spawnParticles(AscendParticles.SHADOW_SPARK, target.getX(), target.getBodyY(0.6), target.getZ(),
					8, 0.3, 0.3, 0.3, 0.15);
			if (isClone()) {
				vanish(world);
			}
		}
		return hit;
	}

	@Override
	public void onDeath(DamageSource damageSource) {
		super.onDeath(damageSource);
		if (this.getWorld() instanceof ServerWorld world && !isClone()) {
			world.spawnParticles(AscendParticles.SHADOW_WISP, this.getX(), this.getY() + 1.0, this.getZ(), 200, 1.0, 1.5, 1.0, 0.1);
			world.spawnParticles(AscendParticles.SHADOW_SPARK, this.getX(), this.getY() + 1.0, this.getZ(), 100, 1.0, 1.5, 1.0, 0.4);
			world.spawnParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY() + 1.0, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		if (isClone()) {
			return;
		}
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
		if (isClone()) {
			return;
		}
		for (BossLoot.Drop drop : BossLoot.roll(BossLoot.SHADOW_ASSASSIN, this.random::nextDouble)) {
			this.dropStack(new ItemStack(Registries.ITEM.get(Ascend.id(drop.item()))));
		}
	}

	// ---- Saving ----

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("AscendClone", isClone());
		nbt.putBoolean("AscendEnraged", enraged);
		nbt.putBoolean("AscendIntro", introDone);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setClone(nbt.getBoolean("AscendClone"));
		introDone = nbt.getBoolean("AscendIntro");
		if (nbt.getBoolean("AscendEnraged")) {
			enraged = true;
			bossBar.setColor(BossBar.Color.RED);
			bossBar.setName(Text.literal("Shadow Assassin — Enraged").formatted(Formatting.DARK_RED, Formatting.BOLD));
		}
	}
}
