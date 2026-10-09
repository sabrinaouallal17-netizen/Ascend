package com.ascend.item;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/** Shadow tools: a bit stronger than netherite. */
public enum ShadowToolMaterial implements ToolMaterial {
	INSTANCE;

	@Override
	public int getDurability() {
		return 4_500;
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return 12.0F;
	}

	@Override
	public float getAttackDamage() {
		return 4.0F;
	}

	@Override
	public TagKey<Block> getInverseTag() {
		return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
	}

	@Override
	public int getEnchantability() {
		return 20;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.ofItems(Items.NETHERITE_INGOT);
	}
}
