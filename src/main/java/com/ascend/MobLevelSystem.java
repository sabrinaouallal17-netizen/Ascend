package com.ascend;

import com.ascend.entity.FrostWardenEntity;
import com.ascend.entity.ShadowAssassinEntity;
import com.ascend.logic.MobLevels;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Gives hostile mobs a random level when they first appear, shown above their heads. */
public final class MobLevelSystem {
	private MobLevelSystem() {
	}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof MobEntity mob
					&& entity instanceof Monster
					&& !isBoss(entity)
					&& !mob.hasAttached(AscendData.MOB_LEVEL)
					&& !mob.hasCustomName()
					&& AscendSettings.get(world.getServer()).mobLevels) {
				assignLevel(mob, world);
			}
		});
	}

	public static int levelOf(Entity entity) {
		Integer level = entity.getAttached(AscendData.MOB_LEVEL);
		return level == null ? 1 : level;
	}

	private static boolean isBoss(Entity entity) {
		return entity instanceof WitherEntity || entity instanceof EnderDragonEntity || entity instanceof WardenEntity
				|| entity instanceof ElderGuardianEntity || entity instanceof ShadowAssassinEntity
				|| entity instanceof FrostWardenEntity;
	}

	private static void assignLevel(MobEntity mob, ServerWorld world) {
		BlockPos spawn = world.getSpawnPos();
		double distance = Math.sqrt(Math.pow(mob.getX() - spawn.getX(), 2) + Math.pow(mob.getZ() - spawn.getZ(), 2));
		int bonus = world.getRegistryKey() == World.NETHER ? MobLevels.NETHER_BONUS
				: world.getRegistryKey() == World.END ? MobLevels.END_BONUS : 0;
		int level = MobLevels.levelFor(distance, bonus, world.random.nextInt(MobLevels.RANDOM_SPREAD + 1));
		mob.setAttached(AscendData.MOB_LEVEL, level);

		modify(mob, EntityAttributes.GENERIC_MAX_HEALTH, MobLevels.healthMultiplier(level) - 1.0,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		modify(mob, EntityAttributes.GENERIC_ATTACK_DAMAGE, MobLevels.damageBonus(level),
				EntityAttributeModifier.Operation.ADD_VALUE);
		mob.setHealth(mob.getMaxHealth());

		Formatting color = MobLevels.isElite(level) ? Formatting.RED : level >= 25 ? Formatting.GOLD : Formatting.YELLOW;
		mob.setCustomName(Text.literal("[Lv " + level + "] ").formatted(color).append(mob.getType().getName()));
		mob.setCustomNameVisible(true);
	}

	private static void modify(MobEntity mob, RegistryEntry<EntityAttribute> attribute, double value,
			EntityAttributeModifier.Operation operation) {
		EntityAttributeInstance instance = mob.getAttributeInstance(attribute);
		if (instance == null || value == 0.0) {
			return;
		}
		instance.removeModifier(Ascend.id("mob_level"));
		instance.addPersistentModifier(new EntityAttributeModifier(Ascend.id("mob_level"), value, operation));
	}
}
