package com.ascend.item;

import com.ascend.entity.AscendEntities;
import java.util.List;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/** Shadow Sigil: use on a block to summon the Shadow Assassin. */
public class ShadowSigilItem extends Item {
	public ShadowSigilItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld() instanceof ServerWorld world) {
			BlockPos pos = context.getBlockPos().offset(context.getSide());
			if (AscendEntities.SHADOW_ASSASSIN.spawn(world, pos, SpawnReason.MOB_SUMMONED) == null) {
				return ActionResult.FAIL;
			}
			world.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0F, 1.4F);
			world.getPlayers(player -> player.squaredDistanceTo(pos.toCenterPos()) < 64 * 64).forEach(player ->
					player.sendMessage(Text.literal("The Shadow Assassin has come for you...")
							.formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), false));
			ItemStack stack = context.getStack();
			if (context.getPlayer() != null) {
				stack.decrementUnlessCreative(1, context.getPlayer());
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
		tooltip.add(Text.literal("Use on a block to summon the Shadow Assassin").formatted(Formatting.DARK_PURPLE));
	}
}
