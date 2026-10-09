package com.ascend.stats;

import com.ascend.Ascend;
import com.ascend.Progression;
import com.ascend.logic.Milestone;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Stat;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Turns stat points and milestone rewards into attribute bonuses. */
public final class StatEffects {
	private StatEffects() {
	}

	public static void apply(ServerPlayerEntity player) {
		PlayerProgress progress = Progression.get(player);
		Set<Milestone> milestones = Progression.milestones(player);

		set(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, statId(Stat.STRENGTH),
				Stat.STRENGTH.bonus(progress.stat(Stat.STRENGTH)), EntityAttributeModifier.Operation.ADD_VALUE);
		set(player, EntityAttributes.GENERIC_MAX_HEALTH, statId(Stat.VITALITY),
				Stat.VITALITY.bonus(progress.stat(Stat.VITALITY)), EntityAttributeModifier.Operation.ADD_VALUE);
		set(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, statId(Stat.AGILITY),
				Stat.AGILITY.bonus(progress.stat(Stat.AGILITY)), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		set(player, EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, statId(Stat.MINING),
				Stat.MINING.bonus(progress.stat(Stat.MINING)), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);

		milestone(player, milestones, Milestone.HUNTER, EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0,
				EntityAttributeModifier.Operation.ADD_VALUE);
		milestone(player, milestones, Milestone.PROSPECTOR, EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, 0.1,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		milestone(player, milestones, Milestone.WANDERER, EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		milestone(player, milestones, Milestone.FISHERMAN, EntityAttributes.GENERIC_LUCK, 2.0,
				EntityAttributeModifier.Operation.ADD_VALUE);
		milestone(player, milestones, Milestone.SHADOWBANE, EntityAttributes.GENERIC_MAX_HEALTH, 4.0,
				EntityAttributeModifier.Operation.ADD_VALUE);

		if (player.getHealth() > player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
	}

	private static Identifier statId(Stat stat) {
		return Ascend.id(stat.name().toLowerCase(Locale.ROOT));
	}

	private static void milestone(ServerPlayerEntity player, Set<Milestone> unlocked, Milestone milestone,
			RegistryEntry<EntityAttribute> attribute, double value, EntityAttributeModifier.Operation operation) {
		set(player, attribute, Ascend.id("milestone_" + milestone.name().toLowerCase(Locale.ROOT)),
				unlocked.contains(milestone) ? value : 0.0, operation);
	}

	private static void set(ServerPlayerEntity player, RegistryEntry<EntityAttribute> attribute, Identifier id,
			double bonus, EntityAttributeModifier.Operation operation) {
		EntityAttributeInstance instance = player.getAttributeInstance(attribute);
		if (instance == null) {
			return;
		}
		instance.removeModifier(id);
		if (bonus != 0.0) {
			instance.addPersistentModifier(new EntityAttributeModifier(id, bonus, operation));
		}
	}
}
