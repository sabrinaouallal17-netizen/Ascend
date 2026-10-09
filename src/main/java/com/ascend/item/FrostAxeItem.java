package com.ascend.item;

import java.util.List;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Frost Axe: fells the whole tree, hits hard and slow. */
public class FrostAxeItem extends AxeItem {
	public FrostAxeItem(Settings settings) {
		super(FrostToolMaterial.INSTANCE, settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Fells the whole tree").formatted(Formatting.AQUA));
		tooltip.add(Text.literal("Sneak to break a single log").formatted(Formatting.GRAY));
	}
}
