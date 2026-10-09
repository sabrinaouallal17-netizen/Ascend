package com.ascend.item;

import java.util.List;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Shadow Axe: fells a whole tree at once (see ShadowTools). */
public class ShadowAxeItem extends AxeItem {
	public ShadowAxeItem(Settings settings) {
		super(ShadowToolMaterial.INSTANCE, settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Fells the whole tree").formatted(Formatting.DARK_PURPLE));
		tooltip.add(Text.literal("Sneak to break a single log").formatted(Formatting.GRAY));
	}
}
