package com.ascend.item;

import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Sigil Piece: found at the top of the Shadow Towers. Three of them make a Shadow Sigil. */
public class SigilPieceItem extends Item {
	public SigilPieceItem(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Found at the top of the Shadow Towers").formatted(Formatting.DARK_PURPLE));
		tooltip.add(Text.literal("3 pieces + Shadow Ingots = Shadow Sigil").formatted(Formatting.GRAY));
	}
}
