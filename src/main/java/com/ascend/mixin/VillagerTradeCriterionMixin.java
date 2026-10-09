package com.ascend.mixin;

import com.ascend.XpSources;
import com.ascend.logic.XpRewards;
import net.minecraft.advancement.criterion.VillagerTradeCriterion;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** XP for trading with a villager or wandering trader. */
@Mixin(VillagerTradeCriterion.class)
public abstract class VillagerTradeCriterionMixin {
	@Inject(method = "trigger", at = @At("HEAD"))
	private void ascend$onTrade(ServerPlayerEntity player, MerchantEntity merchant, ItemStack stack, CallbackInfo ci) {
		XpSources.onActivity(player, XpRewards.TRADE);
	}
}
