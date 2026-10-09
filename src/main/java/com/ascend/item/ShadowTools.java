package com.ascend.item;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Shadow Pickaxe 6x6 mining and Shadow Axe tree felling. */
public final class ShadowTools {
	private static final int MAX_TREE_LOGS = 256;
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
					mineArea(serverWorld, serverPlayer, pos);
				} else if (held.isOf(AscendItems.SHADOW_AXE) && state.isIn(BlockTags.LOGS)) {
					fellTree(serverWorld, serverPlayer, pos);
				}
			} finally {
				breaking = false;
			}
		});
	}

	/** Breaks a 6x6 square around the mined block, facing the player. */
	private static void mineArea(ServerWorld world, ServerPlayerEntity player, BlockPos center) {
		boolean lookingVertically = Math.abs(player.getPitch()) > 45.0F;
		Direction.Axis facingAxis = player.getHorizontalFacing().getAxis();
		for (int a = -2; a <= 3; a++) {
			for (int b = -2; b <= 3; b++) {
				BlockPos target;
				if (lookingVertically) {
					target = center.add(a, 0, b);
				} else if (facingAxis == Direction.Axis.X) {
					target = center.add(0, b, a);
				} else {
					target = center.add(a, b, 0);
				}
				if (!target.equals(center)) {
					tryBreak(world, player, target, true);
				}
				if (player.getMainHandStack().isEmpty()) {
					return;
				}
			}
		}
	}

	/** Breaks every log connected to the broken one. */
	private static void fellTree(ServerWorld world, ServerPlayerEntity player, BlockPos start) {
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
					if (player.getMainHandStack().isEmpty()) {
						return;
					}
				}
			}
		}
	}

	private static void tryBreak(ServerWorld world, ServerPlayerEntity player, BlockPos pos, boolean needsSuitableTool) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir() || state.getHardness(world, pos) < 0.0F) {
			return;
		}
		if (needsSuitableTool && !player.getMainHandStack().isSuitableFor(state)) {
			return;
		}
		player.interactionManager.tryBreakBlock(pos);
	}
}
