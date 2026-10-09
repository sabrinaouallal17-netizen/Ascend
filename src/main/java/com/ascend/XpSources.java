package com.ascend;

import com.ascend.item.AscendItems;
import com.ascend.entity.ShadowAssassinEntity;
import com.ascend.logic.Counter;
import com.ascend.logic.MobLevels;
import com.ascend.logic.XpMultiplier;
import com.ascend.logic.XpRewards;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/** Where RPG XP comes from: mining, fighting, farming, exploring biomes, and boss drops. */
public final class XpSources {
	private static final int BIOME_CHECK_TICKS = 40;

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
				Progression.addSourceXp(serverPlayer, xp);
				if (!(state.getBlock() instanceof CropBlock)) {
					Progression.count(serverPlayer, Counter.ORES, 1);
					if (state.isIn(BlockTags.DIAMOND_ORES)) {
						Progression.count(serverPlayer, Counter.DIAMONDS, 1);
					}
				}
			}
		});

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (!(entity instanceof PlayerEntity)
					&& damageSource.getAttacker() instanceof ServerPlayerEntity player
					&& earnsXp(player)) {
				int level = MobLevelSystem.levelOf(entity);
				int base = XpRewards.forMob(entity.getMaxHealth() / (float) MobLevels.healthMultiplier(level));
				Progression.addSourceXp(player, XpMultiplier.apply(base, MobLevels.xpMultiplier(level)));
				Progression.count(player, Counter.MOBS_KILLED, 1);
				if (MobLevels.isElite(level)) {
					Progression.count(player, Counter.ELITE_KILLS, 1);
				}
				if (entity instanceof ShadowAssassinEntity) {
					Progression.count(player, Counter.SHADOW_KILLS, 1);
				}
			}
			if (entity instanceof WitherEntity || entity instanceof EnderDragonEntity
					|| entity instanceof WardenEntity || entity instanceof ElderGuardianEntity) {
				entity.dropStack(new ItemStack(AscendItems.LEGENDARY_EXP_BOTTLE));
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % BIOME_CHECK_TICKS != 0) {
				return;
			}
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (earnsXp(player)) {
					checkBiome(player);
				}
			}
		});
	}

	/** Called by the criterion mixins (fishing, trading, enchanting, breeding). */
	public static void onActivity(ServerPlayerEntity player, int baseXp, Counter counter) {
		if (earnsXp(player)) {
			Progression.addSourceXp(player, baseXp);
			if (counter != null) {
				Progression.count(player, counter, 1);
			}
		}
	}

	private static void checkBiome(ServerPlayerEntity player) {
		player.getServerWorld().getBiome(player.getBlockPos()).getKey().ifPresent(key -> {
			Set<String> discovered = player.getAttachedOrCreate(AscendData.BIOMES);
			String id = key.getValue().toString();
			if (discovered.add(id)) {
				player.sendMessage(Text.literal("New biome discovered: ").formatted(Formatting.AQUA)
						.append(Text.literal(prettyName(key.getValue().getPath())).formatted(Formatting.WHITE)));
				Progression.addSourceXp(player, XpRewards.NEW_BIOME);
				Progression.countAtLeast(player, Counter.BIOMES, discovered.size());
			}
		});
	}

	private static String prettyName(String path) {
		StringBuilder name = new StringBuilder();
		for (String word : path.split("_")) {
			if (!word.isEmpty()) {
				name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
			}
		}
		return name.toString().trim();
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
