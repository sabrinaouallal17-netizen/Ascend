package com.ascend.stats;

import com.ascend.Ascend;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Stat;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Turns stat points into attribute bonuses. */
public final class StatEffects {
	private StatEffects() {
	}

	public static void apply(ServerPlayerEntity player, PlayerProgress progress) {
		set(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, Stat.STRENGTH, progress,
				EntityAttributeModifier.Operation.ADD_VALUE);
		set(player, EntityAttributes.GENERIC_MAX_HEALTH, Stat.VITALITY, progress,
				EntityAttributeModifier.Operation.ADD_VALUE);
		set(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, Stat.AGILITY, progress,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		set(player, EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, Stat.MINING, progress,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		if (player.getHealth() > player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
	}

	private static void set(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute, Stat stat,
			PlayerProgress progress, EntityAttributeModifier.Operation operation) {
		EntityAttributeInstance instance = player.getAttributeInstance(attribute);
		if (instance == null) {
			return;
		}
		Identifier id = Ascend.id(stat.name().toLowerCase(java.util.Locale.ROOT));
		instance.removeModifier(id);
		double bonus = stat.bonus(progress.stat(stat));
		if (bonus != 0.0) {
			instance.addPersistentModifier(new EntityAttributeModifier(id, bonus, operation));
		}
	}
}
