package com.ascend.mixin;

import com.ascend.XpSources;
import com.ascend.logic.XpRewards;
import java.util.Collection;
import net.minecraft.advancement.criterion.FishingRodHookedCriterion;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** XP for catching something with a fishing rod. */
@Mixin(FishingRodHookedCriterion.class)
public abstract class FishingRodHookedCriterionMixin {
	@Inject(method = "trigger", at = @At("HEAD"))
	private void ascend$onFish(ServerPlayerEntity player, ItemStack rod, FishingBobberEntity bobber,
			Collection<ItemStack> fishingLoots, CallbackInfo ci) {
		if (!fishingLoots.isEmpty()) {
			XpSources.onActivity(player, XpRewards.FISHING);
		}
	}
}
