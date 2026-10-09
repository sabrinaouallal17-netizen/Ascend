package com.ascend.item;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Shadow Pickaxe: mines a 6x6 area (see ShadowTools). */
public class ShadowPickaxeItem extends PickaxeItem {
	public ShadowPickaxeItem(Settings settings) {
		super(ShadowToolMaterial.INSTANCE, settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Mines a 6x6 area").formatted(Formatting.DARK_PURPLE));
		tooltip.add(Text.literal("Sneak to mine a single block").formatted(Formatting.GRAY));
	}
}
