package com.ascend.item;

import com.ascend.AscendParticles;
import com.ascend.AscendSounds;
import com.ascend.logic.FrostPickaxe;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Area mining (Shadow 6x6, Frost 8x8 up to 16x16) and tree felling for the boss tools. */
public final class ShadowTools {
	private static final int MAX_TREE_LOGS = 256;
	private static final int SHADOW_AREA = 6;
	/** Set while we break the extra blocks, so they don't trigger more area breaks. */
	private static boolean breaking;

	private ShadowTools() {
	}

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (breaking || !(world instanceof ServerWorld serverWorld)
					|| !(player instanceof ServerPlayerEntity serverPlayer) || player.isSneaking()) {
				return;
			}
			ItemStack held = player.getMainHandStack();
			breaking = true;
			try {
				if (held.isOf(AscendItems.SHADOW_PICKAXE)) {
					mineSquare(serverWorld, serverPlayer, pos, SHADOW_AREA);
					serverWorld.playSound(null, pos, AscendSounds.SHADOW_PICKAXE_SHATTER, SoundCategory.PLAYERS, 1.0F,
							0.9F + serverWorld.random.nextFloat() * 0.2F);
					serverWorld.spawnParticles(AscendParticles.SHADOW_WISP, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							25, 1.4, 1.4, 1.4, 0.02);
					serverWorld.spawnParticles(AscendParticles.SHADOW_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							12, 0.6, 0.6, 0.6, 0.15);
				} else if (held.isOf(AscendItems.FROST_PICKAXE)) {
					int area = FrostPickaxe.areaFor(FrostPickaxeItem.level(held));
					int broken = mineSquare(serverWorld, serverPlayer, pos, area);
					FrostPickaxeItem.addBlocks(serverPlayer, held, broken + 1);
					serverWorld.playSound(null, pos, AscendSounds.FROST_PICKAXE_SHATTER, SoundCategory.PLAYERS, 1.0F,
							0.9F + serverWorld.random.nextFloat() * 0.2F);
					double spread = area / 4.0;
					serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							20 + area * 2, spread, spread, spread, 0.03);
					serverWorld.spawnParticles(AscendParticles.FROST_SPARK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							12, 0.6, 0.6, 0.6, 0.15);
				} else if ((held.isOf(AscendItems.SHADOW_AXE) || held.isOf(AscendItems.FROST_AXE)) && state.isIn(BlockTags.LOGS)) {
					boolean frost = held.isOf(AscendItems.FROST_AXE);
					int felled = fellTree(serverWorld, serverPlayer, pos, frost);
					if (felled > 0) {
						serverWorld.playSound(null, pos, frost ? AscendSounds.FROST_SWORD_HIT : AscendSounds.SHADOW_AXE_CLEAVE,
								SoundCategory.PLAYERS, 1.0F, 1.0F);
					}
				}
			} finally {
				breaking = false;
			}
		});
	}

	/** Breaks an area x area square around the mined block, facing the player. @return how many extra blocks broke */
	private static int mineSquare(ServerWorld world, ServerPlayerEntity player, BlockPos center, int area) {
		int[] range = FrostPickaxe.offsets(area);
		int broken = 0;
		boolean lookingVertically = Math.abs(player.getPitch()) > 45.0F;
		Direction.Axis facingAxis = player.getHorizontalFacing().getAxis();
		for (int a = range[0]; a <= range[1]; a++) {
			for (int b = range[0]; b <= range[1]; b++) {
				BlockPos target;
				if (lookingVertically) {
					target = center.add(a, 0, b);
				} else if (facingAxis == Direction.Axis.X) {
					target = center.add(0, b, a);
				} else {
					target = center.add(a, b, 0);
				}
				if (!target.equals(center) && tryBreak(world, player, target, true)) {
					broken++;
				}
				if (player.getMainHandStack().isEmpty()) {
					return broken;
				}
			}
		}
		return broken;
	}

	/** Breaks every log connected to the broken one, leaving a trail of shadow or frost. @return how many logs were felled */
	private static int fellTree(ServerWorld world, ServerPlayerEntity player, BlockPos start, boolean frost) {
		int felled = 0;
		Set<BlockPos> visited = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(start);
		visited.add(start);
		while (!queue.isEmpty() && visited.size() < MAX_TREE_LOGS) {
			BlockPos log = queue.poll();
			for (BlockPos neighbor : BlockPos.iterate(log.add(-1, -1, -1), log.add(1, 1, 1))) {
				BlockPos next = neighbor.toImmutable();
				if (visited.add(next) && world.getBlockState(next).isIn(BlockTags.LOGS)) {
					queue.add(next);
					tryBreak(world, player, next, false);
					felled++;
					world.spawnParticles(frost ? ParticleTypes.SNOWFLAKE : AscendParticles.SHADOW_WISP, next.getX() + 0.5, next.getY() + 0.5, next.getZ() + 0.5,
							2, 0.3, 0.3, 0.3, 0.01);
					if (player.getMainHandStack().isEmpty()) {
						return felled;
					}
				}
			}
		}
		return felled;
	}

	private static boolean tryBreak(ServerWorld world, ServerPlayerEntity player, BlockPos pos, boolean needsSuitableTool) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir() || state.getHardness(world, pos) < 0.0F) {
			return false;
		}
		if (needsSuitableTool && !player.getMainHandStack().isSuitableFor(state)) {
			return false;
		}
		return player.interactionManager.tryBreakBlock(pos);
	}
}
