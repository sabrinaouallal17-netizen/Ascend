package com.ascend.item;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/** Frost tools: the tier above Shadow. */
public enum FrostToolMaterial implements ToolMaterial {
	INSTANCE;

	@Override
	public int getDurability() {
		return 6_000;
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return 14.0F;
	}

	@Override
	public float getAttackDamage() {
		return 5.0F;
	}

	@Override
	public TagKey<Block> getInverseTag() {
		return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
	}

	@Override
	public int getEnchantability() {
		return 22;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.ofItems(Items.BLUE_ICE);
	}
}
