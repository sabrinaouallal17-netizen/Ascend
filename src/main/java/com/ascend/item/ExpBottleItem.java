package com.ascend.item;

import com.ascend.Progression;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Ascend EXP Bottles (small, large, legendary): right-click to gain Ascend XP and get the empty bottle back. */
public class ExpBottleItem extends Item {
	private final int xp;

	public ExpBottleItem(Settings settings, int xp) {
		super(settings);
		this.xp = xp;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user instanceof ServerPlayerEntity player) {
			if (Progression.get(player).isMaxLevel()) {
				player.sendMessage(Text.literal("You are already max level!").formatted(Formatting.GOLD), true);
				return TypedActionResult.fail(stack);
			}
			Progression.addXp(player, xp);
			world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 0.8F);
		}
		return TypedActionResult.success(ItemUsage.exchangeStack(stack, user, new ItemStack(Items.GLASS_BOTTLE)), world.isClient());
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("+" + xp + " Ascend XP").formatted(Formatting.GREEN));
		tooltip.add(Text.literal("Right-click to drink").formatted(Formatting.GRAY));
	}
}
