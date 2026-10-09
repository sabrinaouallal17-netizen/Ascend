package com.ascend.mixin;

import com.ascend.XpSources;
import com.ascend.logic.XpRewards;
import net.minecraft.advancement.criterion.EnchantedItemCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** XP for enchanting an item at an enchanting table. */
@Mixin(EnchantedItemCriterion.class)
public abstract class EnchantedItemCriterionMixin {
	@Inject(method = "trigger", at = @At("HEAD"))
	private void ascend$onEnchant(ServerPlayerEntity player, ItemStack stack, int levels, CallbackInfo ci) {
		XpSources.onActivity(player, levels * XpRewards.ENCHANT_PER_LEVEL);
	}
}
