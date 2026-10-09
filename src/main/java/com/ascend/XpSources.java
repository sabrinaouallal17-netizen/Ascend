package com.ascend;

import com.ascend.logic.XpRewards;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/** Where RPG XP comes from: mining ores, killing mobs and harvesting crops. */
public final class XpSources {
	private XpSources() {
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!(player instanceof ServerPlayerEntity serverPlayer) || !earnsXp(serverPlayer)) {
				return;
			}
			int xp = state.getBlock() instanceof CropBlock crop
					? (crop.isMature(state) ? XpRewards.CROP : 0)
					: (hasSilkTouch(world, serverPlayer) ? 0 : oreXp(state));
			if (xp > 0) {
				Progression.addXp(serverPlayer, xp);
			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(entity instanceof PlayerEntity)
					&& damageSource.getAttacker() instanceof ServerPlayerEntity player
					&& earnsXp(player)) {
				Progression.addXp(player, XpRewards.forMob(entity.getMaxHealth()));
			}
		});
	}

	private static boolean earnsXp(ServerPlayerEntity player) {
		return !player.isCreative() && !player.isSpectator();
	}

	/** Silk-touched ores give no XP, so placing and re-mining them can't be farmed. */
	private static boolean hasSilkTouch(World world, ServerPlayerEntity player) {
		return world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.SILK_TOUCH)
				.map(silkTouch -> EnchantmentHelper.getLevel(silkTouch, player.getMainHandStack()) > 0)
				.orElse(false);
	}

	private static int oreXp(BlockState state) {
		if (state.isIn(BlockTags.COAL_ORES)) {
			return XpRewards.COAL;
		}
		if (state.isIn(BlockTags.COPPER_ORES)) {
			return XpRewards.COPPER;
		}
		if (state.isIn(BlockTags.IRON_ORES)) {
			return XpRewards.IRON;
		}
		if (state.isIn(BlockTags.REDSTONE_ORES)) {
			return XpRewards.REDSTONE;
		}
		if (state.isIn(BlockTags.LAPIS_ORES)) {
			return XpRewards.LAPIS;
		}
		if (state.isIn(BlockTags.GOLD_ORES)) {
			return XpRewards.GOLD;
		}
		if (state.isIn(BlockTags.DIAMOND_ORES)) {
			return XpRewards.DIAMOND;
		}
		if (state.isIn(BlockTags.EMERALD_ORES)) {
			return XpRewards.EMERALD;
		}
		if (state.isOf(Blocks.NETHER_QUARTZ_ORE)) {
			return XpRewards.QUARTZ;
		}
		if (state.isOf(Blocks.ANCIENT_DEBRIS)) {
			return XpRewards.ANCIENT_DEBRIS;
		}
		return 0;
	}
}
