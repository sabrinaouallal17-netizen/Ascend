package com.ascend.item;

import com.ascend.Progression;
import com.ascend.logic.PlayerProgress;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Tome of Rebirth: right-click to get back every spent skill point. */
public class TomeOfRebirthItem extends Item {
	public TomeOfRebirthItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user instanceof ServerPlayerEntity player) {
			PlayerProgress progress = Progression.get(player);
			int refunded = progress.spentPoints();
			if (refunded == 0) {
				player.sendMessage(Text.literal("You have no points to refund.").formatted(Formatting.GRAY), true);
				return TypedActionResult.fail(stack);
			}
			progress.respec();
			Progression.refresh(player);
			world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
			player.sendMessage(Text.literal("Reborn! " + refunded + " skill points refunded.").formatted(Formatting.LIGHT_PURPLE), false);
		}
		stack.decrementUnlessCreative(1, user);
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Refunds all your spent skill points").formatted(Formatting.LIGHT_PURPLE));
		tooltip.add(Text.literal("Right-click to use").formatted(Formatting.GRAY));
	}
}
