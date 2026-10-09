package com.ascend.item;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/** A sigil that summons a boss when used on a block (Shadow Sigil, Frost Sigil). */
public class BossSigilItem extends Item {
	private final Supplier<EntityType<? extends MobEntity>> boss;
	private final String bossName;
	private final String arrivalMessage;
	private final Formatting color;

	public BossSigilItem(Settings settings, Supplier<EntityType<? extends MobEntity>> boss, String bossName,
			String arrivalMessage, Formatting color) {
		super(settings);
		this.boss = boss;
		this.bossName = bossName;
		this.arrivalMessage = arrivalMessage;
		this.color = color;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld() instanceof ServerWorld world) {
			BlockPos pos = context.getBlockPos().offset(context.getSide());
			if (boss.get().spawn(world, pos, SpawnReason.MOB_SUMMONED) == null) {
				return ActionResult.FAIL;
			}
			world.getPlayers(player -> player.squaredDistanceTo(pos.toCenterPos()) < 64 * 64).forEach(player ->
					player.sendMessage(Text.literal(arrivalMessage).formatted(color, Formatting.ITALIC), false));
			if (context.getPlayer() != null) {
				context.getStack().decrementUnlessCreative(1, context.getPlayer());
			}
		}
		return ActionResult.success(context.getWorld().isClient());
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Use on a block to summon the " + bossName).formatted(color));
	}
}
