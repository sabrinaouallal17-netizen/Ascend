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
import net.minecraft.block.Blocks;
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
import net.minecraft.particle.BlockStateParticleEffect;
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
 * The Frost Warden boss: a slow, heavy ice giant.
 * <ul>
 * <li>Ice Spikes: raises its arms, then spikes of ice erupt under every nearby player.</li>
 * <li>Frost Nova: a freezing burst all around it.</li>
 * <li>Leap Slam: jumps onto its target and crashes down with a shockwave.</li>
 * <li>Enraged (below half health): roars and summons a permanent blizzard that slows and freezes players.</li>
 * </ul>
 */
public class FrostWardenEntity extends ZombieEntity {
	public static final int IDLE = 0;
	public static final int SPIKES = 1;
	public static final int NOVA = 2;
	public static final int LEAP = 3;

	private static final TrackedData<Integer> STATE =
			DataTracker.registerData(FrostWardenEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> ENRAGED =
			DataTracker.registerData(FrostWardenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final Identifier ROOTED = Ascend.id("boss_rooted");
	private static final int SPIKES_WINDUP = 20;
	private static final int NOVA_WINDUP = 14;
	private static final int LEAP_TIMEOUT = 40;
	private static final double SPIKES_RANGE = 12.0;
	private static final double NOVA_RADIUS = 6.0;
	private static final double SLAM_RADIUS = 4.5;
	private static final double BLIZZARD_RADIUS = 14.0;

	private final ServerBossBar bossBar = new ServerBossBar(
			Text.literal("Frost Warden").formatted(Formatting.AQUA, Formatting.BOLD),
			BossBar.Color.BLUE, BossBar.Style.NOTCHED_10);
	private final Set<UUID> toldAboutDrops = new HashSet<>();
	private int abilityCooldown = 60;
	private int stateTicks;
	private boolean introDone;

	public FrostWardenEntity(EntityType<? extends ZombieEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 150;
		this.setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return ZombieEntity.createZombieAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 450.0)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.24)
				.add(EntityAttributes.GENERIC_ARMOR, 14.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
				.add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(STATE, IDLE);
		builder.add(ENRAGED, false);
	}

	public int getState() {
		return this.dataTracker.get(STATE);
	}

	private void setState(int state) {
		this.dataTracker.set(STATE, state);
		this.stateTicks = 0;
	}

	public boolean isEnraged() {
		return this.dataTracker.get(ENRAGED);
	}

	@Override
	protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
		this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(AscendItems.FROST_SWORD));
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
	public boolean canFreeze() {
		return false;
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return AscendSounds.FROST_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return AscendSounds.FROST_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return AscendSounds.FROST_DEATH;
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.BLOCK_GLASS_STEP;
	}

	@Override
	protected float getSoundVolume() {
		return 2.5F;
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
		if (this.random.nextInt(2) == 0) {
			world.addParticle(ParticleTypes.SNOWFLAKE,
					this.getX() + (this.random.nextDouble() - 0.5) * 1.6, this.getY() + this.random.nextDouble() * 3.0,
					this.getZ() + (this.random.nextDouble() - 0.5) * 1.6, 0.0, -0.02, 0.0);
		}
		switch (getState()) {
			case SPIKES -> {
				for (int i = 0; i < 3; i++) {
					double a = this.random.nextDouble() * Math.PI * 2;
					world.addParticle(AscendParticles.FROST_SPARK, this.getX() + Math.cos(a) * 2.0, this.getY() + 3.2,
							this.getZ() + Math.sin(a) * 2.0, -Math.cos(a) * 0.1, 0.0, -Math.sin(a) * 0.1);
				}
			}
			case NOVA -> {
				double a = this.age * 0.7;
				world.addParticle(AscendParticles.FROST_SPARK, this.getX() + Math.cos(a) * 1.5, this.getY() + 1.5,
						this.getZ() + Math.sin(a) * 1.5, 0.0, 0.05, 0.0);
			}
			default -> {
			}
		}
		if (isEnraged()) {
			for (int i = 0; i < 6; i++) {
				double a = this.random.nextDouble() * Math.PI * 2;
				double r = 2.0 + this.random.nextDouble() * BLIZZARD_RADIUS * 0.7;
				world.addParticle(ParticleTypes.SNOWFLAKE, this.getX() + Math.cos(a) * r, this.getY() + 0.5 + this.random.nextDouble() * 4.0,
						this.getZ() + Math.sin(a) * r, -Math.sin(a) * 0.3, -0.05, Math.cos(a) * 0.3);
			}
		}
		if (this.deathTime > 0) {
			for (int i = 0; i < 5; i++) {
				world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.PACKED_ICE.getDefaultState()),
						this.getX() + (this.random.nextDouble() - 0.5) * 1.5, this.getY() + this.random.nextDouble() * 3.0,
						this.getZ() + (this.random.nextDouble() - 0.5) * 1.5, 0.0, 0.1, 0.0);
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
		if (!introDone) {
			introDone = true;
			playIntro(world);
		}
		bossBar.setPercent(this.getHealth() / this.getMaxHealth());
		if (!isEnraged() && this.getHealth() < this.getMaxHealth() / 2.0F) {
			enrage(world);
		}
		if (isEnraged()) {
			tickBlizzard(world);
		}
		LivingEntity target = this.getTarget();
		switch (getState()) {
			case IDLE -> tickIdle(world, target);
			case SPIKES -> tickSpikes(world);
			case NOVA -> tickNova(world);
			case LEAP -> tickLeap(world);
			default -> setState(IDLE);
		}
	}

	private void tickIdle(ServerWorld world, LivingEntity target) {
		if (target == null || --abilityCooldown > 0 || this.squaredDistanceTo(target) > 20 * 20) {
			return;
		}
		double distance = this.distanceTo(target);
		int roll = this.random.nextInt(100);
		if (distance > 6 && roll < 45 && this.isOnGround()) {
			startLeap(world, target);
		} else if (distance < NOVA_RADIUS && roll < 60) {
			startAbility(world, NOVA, AscendSounds.FROST_NOVA, 0.6F);
		} else {
			startAbility(world, SPIKES, AscendSounds.FROST_SPIKES, 0.7F);
		}
	}

	private void startAbility(ServerWorld world, int state, SoundEvent sound, float pitch) {
		setState(state);
		setRooted(true);
		world.playSound(null, this.getBlockPos(), sound, SoundCategory.HOSTILE, 1.5F, pitch);
	}

	private void endAbility(int cooldown) {
		setState(IDLE);
		setRooted(false);
		abilityCooldown = isEnraged() ? cooldown * 3 / 5 : cooldown;
	}

	private void tickSpikes(ServerWorld world) {
		if (++stateTicks < SPIKES_WINDUP) {
			return;
		}
		world.playSound(null, this.getBlockPos(), AscendSounds.FROST_SPIKES, SoundCategory.HOSTILE, 2.5F, 1.0F);
		BlockStateParticleEffect ice = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.ICE.getDefaultState());
		for (ServerPlayerEntity player : playersAround(world, SPIKES_RANGE)) {
			player.damage(this.getDamageSources().mobAttack(this), 8.0F);
			player.addVelocity(0.0, 0.8, 0.0);
			player.velocityModified = true;
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
			for (int y = 0; y < 4; y++) {
				world.spawnParticles(ice, player.getX(), player.getY() + y * 0.5, player.getZ(), 12, 0.25, 0.1, 0.25, 0.2);
			}
			world.spawnParticles(AscendParticles.FROST_SPARK, player.getX(), player.getY() + 0.5, player.getZ(), 15, 0.3, 0.8, 0.3, 0.2);
		}
		endAbility(100);
	}

	private void tickNova(ServerWorld world) {
		if (++stateTicks < NOVA_WINDUP) {
			return;
		}
		for (int i = 0; i < 48; i++) {
			double a = i * Math.PI * 2 / 48;
			world.spawnParticles(ParticleTypes.SNOWFLAKE, this.getX() + Math.cos(a) * 1.5, this.getY() + 1.0, this.getZ() + Math.sin(a) * 1.5,
					0, Math.cos(a), 0.05, Math.sin(a), 0.6);
		}
		world.spawnParticles(AscendParticles.FROST_SPARK, this.getX(), this.getY() + 1.5, this.getZ(), 40, 1.5, 1.0, 1.5, 0.3);
		for (ServerPlayerEntity player : playersAround(world, NOVA_RADIUS)) {
			player.damage(this.getDamageSources().mobAttack(this), 6.0F);
			player.setFrozenTicks(player.getMinFreezeDamageTicks() + 120);
			Vec3d push = player.getPos().subtract(this.getPos()).multiply(1.0, 0.0, 1.0).normalize().multiply(1.2);
			player.addVelocity(push.x, 0.4, push.z);
			player.velocityModified = true;
		}
		endAbility(120);
	}

	private void startLeap(ServerWorld world, LivingEntity target) {
		setState(LEAP);
		Vec3d toTarget = target.getPos().subtract(this.getPos());
		Vec3d horizontal = toTarget.multiply(1.0, 0.0, 1.0);
		double distance = Math.min(horizontal.length(), 14.0);
		Vec3d direction = horizontal.normalize().multiply(distance / 9.0);
		this.setVelocity(direction.x, 1.0, direction.z);
		this.velocityModified = true;
		world.playSound(null, this.getBlockPos(), AscendSounds.FROST_ROAR, SoundCategory.HOSTILE, 1.0F, 1.6F);
		world.spawnParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 30, 1.0, 0.2, 1.0, 0.1);
	}

	private void tickLeap(ServerWorld world) {
		stateTicks++;
		boolean landed = stateTicks > 5 && this.isOnGround();
		if (!landed && stateTicks < LEAP_TIMEOUT) {
			return;
		}
		world.playSound(null, this.getBlockPos(), AscendSounds.FROST_SLAM, SoundCategory.HOSTILE, 3.0F, 1.0F);
		world.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 4, 1.5, 0.1, 1.5, 0.0);
		world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.PACKED_ICE.getDefaultState()),
				this.getX(), this.getY() + 0.1, this.getZ(), 80, 2.5, 0.1, 2.5, 0.3);
		for (ServerPlayerEntity player : playersAround(world, SLAM_RADIUS)) {
			player.damage(this.getDamageSources().mobAttack(this), 10.0F);
			Vec3d push = player.getPos().subtract(this.getPos()).multiply(1.0, 0.0, 1.0).normalize().multiply(1.0);
			player.addVelocity(push.x, 0.6, push.z);
			player.velocityModified = true;
		}
		endAbility(80);
	}

	private void tickBlizzard(ServerWorld world) {
		if (this.age % 10 != 0) {
			return;
		}
		for (ServerPlayerEntity player : playersAround(world, BLIZZARD_RADIUS)) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0, false, false));
			player.setFrozenTicks(Math.min(player.getFrozenTicks() + 25, player.getMinFreezeDamageTicks() + 60));
		}
		if (!this.hasStatusEffect(StatusEffects.STRENGTH)) {
			this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 0, false, false));
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

	private List<ServerPlayerEntity> playersAround(ServerWorld world, double radius) {
		return world.getPlayers(player -> player.squaredDistanceTo(this) < radius * radius
				&& !player.isSpectator() && !player.isCreative());
	}

	private void playIntro(ServerWorld world) {
		world.playSound(null, this.getBlockPos(), AscendSounds.FROST_SPAWN, SoundCategory.HOSTILE, 4.0F, 1.0F);
		world.spawnParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.5, this.getZ(), 200, 3.0, 2.0, 3.0, 0.1);
		world.spawnParticles(AscendParticles.FROST_SPARK, this.getX(), this.getY() + 1.5, this.getZ(), 60, 1.0, 1.5, 1.0, 0.3);
		for (ServerPlayerEntity player : playersAround(world, 32)) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, false, false));
		}
	}

	private void enrage(ServerWorld world) {
		this.dataTracker.set(ENRAGED, true);
		abilityCooldown = Math.min(abilityCooldown, 20);
		bossBar.setColor(BossBar.Color.WHITE);
		bossBar.setName(Text.literal("Frost Warden — Blizzard").formatted(Formatting.WHITE, Formatting.BOLD));
		world.playSound(null, this.getBlockPos(), AscendSounds.FROST_ROAR, SoundCategory.HOSTILE, 4.0F, 1.0F);
		world.spawnParticles(AscendParticles.FROST_SPARK, this.getX(), this.getY() + 1.5, this.getZ(), 100, 1.5, 1.5, 1.5, 0.4);
		for (ServerPlayerEntity player : playersAround(world, 32)) {
			player.sendMessage(Text.literal("The Frost Warden calls a blizzard!").formatted(Formatting.AQUA, Formatting.ITALIC), true);
		}
	}

	// ---- Combat ----

	@Override
	public boolean tryAttack(Entity target) {
		boolean hit = super.tryAttack(target);
		if (hit && this.getWorld() instanceof ServerWorld world) {
			world.playSound(null, this.getBlockPos(), AscendSounds.FROST_SWORD_HIT, SoundCategory.HOSTILE, 1.4F, 0.7F);
			world.spawnParticles(AscendParticles.FROST_SPARK, target.getX(), target.getBodyY(0.6), target.getZ(), 10, 0.3, 0.3, 0.3, 0.15);
			if (target instanceof LivingEntity living) {
				living.setFrozenTicks(Math.min(living.getFrozenTicks() + 60, living.getMinFreezeDamageTicks() + 80));
			}
		}
		return hit;
	}

	@Override
	public void onDeath(DamageSource damageSource) {
		super.onDeath(damageSource);
		if (this.getWorld() instanceof ServerWorld world) {
			world.spawnParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.5, this.getZ(), 250, 2.0, 2.0, 2.0, 0.2);
			world.spawnParticles(AscendParticles.FROST_SPARK, this.getX(), this.getY() + 1.5, this.getZ(), 120, 1.5, 2.0, 1.5, 0.4);
			world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.BLUE_ICE.getDefaultState()),
					this.getX(), this.getY() + 1.5, this.getZ(), 150, 1.0, 1.5, 1.0, 0.3);
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		bossBar.addPlayer(player);
		if (toldAboutDrops.add(player.getUuid())) {
			MutableText message = Text.literal("❄ Frost Warden — possible drops:").formatted(Formatting.AQUA, Formatting.BOLD);
			for (BossLoot.Drop drop : BossLoot.FROST_WARDEN) {
				message.append(Text.literal("\n  " + drop.name() + "  ").formatted(Formatting.BLUE))
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
		for (BossLoot.Drop drop : BossLoot.roll(BossLoot.FROST_WARDEN, this.random::nextDouble)) {
			this.dropStack(new ItemStack(Registries.ITEM.get(Ascend.id(drop.item()))));
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("AscendEnraged", isEnraged());
		nbt.putBoolean("AscendIntro", introDone);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		introDone = nbt.getBoolean("AscendIntro");
		if (nbt.getBoolean("AscendEnraged")) {
			this.dataTracker.set(ENRAGED, true);
			bossBar.setColor(BossBar.Color.WHITE);
			bossBar.setName(Text.literal("Frost Warden — Blizzard").formatted(Formatting.WHITE, Formatting.BOLD));
		}
	}
}
