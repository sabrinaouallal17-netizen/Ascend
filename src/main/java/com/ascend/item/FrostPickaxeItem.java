package com.ascend.item;

import com.ascend.AscendParticles;
import com.ascend.AscendSounds;
import com.ascend.logic.FrostPickaxe;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Frost Pickaxe: mines an 8x8 area that grows to 10x10, 12x12 and 16x16 as it mines. */
public class FrostPickaxeItem extends PickaxeItem {
	private static final String BLOCKS_KEY = "AscendFrostBlocks";

	public FrostPickaxeItem(Settings settings) {
		super(FrostToolMaterial.INSTANCE, settings);
	}

	public static long blocksMined(ItemStack stack) {
		return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getLong(BLOCKS_KEY);
	}

	public static int level(ItemStack stack) {
		return FrostPickaxe.levelFor(blocksMined(stack));
	}

	/** Counts mined blocks and celebrates level ups. */
	public static void addBlocks(ServerPlayerEntity player, ItemStack stack, int blocks) {
		long before = blocksMined(stack);
		long after = before + blocks;
		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putLong(BLOCKS_KEY, after));
		int oldLevel = FrostPickaxe.levelFor(before);
		int newLevel = FrostPickaxe.levelFor(after);
		if (newLevel > oldLevel) {
			int area = FrostPickaxe.areaFor(newLevel);
			player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Frost Pickaxe Lv " + newLevel).formatted(Formatting.AQUA, Formatting.BOLD)));
			player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("Now mines " + area + "x" + area).formatted(Formatting.WHITE)));
			player.getServerWorld().playSound(null, player.getBlockPos(), AscendSounds.FROST_PICKAXE_LEVELUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
			player.getServerWorld().spawnParticles(AscendParticles.FROST_SPARK, player.getX(), player.getY() + 1.0, player.getZ(),
					60, 0.8, 1.0, 0.8, 0.3);
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		long mined = blocksMined(stack);
		int level = FrostPickaxe.levelFor(mined);
		int area = FrostPickaxe.areaFor(level);
		tooltip.add(Text.literal("Level " + level + " / " + FrostPickaxe.MAX_LEVEL + "  ·  mines " + area + "x" + area).formatted(Formatting.AQUA));
		long next = FrostPickaxe.nextThreshold(level);
		tooltip.add(Text.literal(next < 0 ? "Max level  ·  " + mined + " blocks mined" : mined + " / " + next + " blocks to next level")
				.formatted(Formatting.GRAY));
		tooltip.add(Text.literal("Sneak to mine a single block").formatted(Formatting.DARK_GRAY));
	}
}
