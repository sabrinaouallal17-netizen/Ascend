package com.ascend.mixin;

import com.ascend.XpSources;
import com.ascend.logic.XpRewards;
import net.minecraft.advancement.criterion.BredAnimalsCriterion;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** XP for breeding animals. */
@Mixin(BredAnimalsCriterion.class)
public abstract class BredAnimalsCriterionMixin {
	@Inject(method = "trigger", at = @At("HEAD"))
	private void ascend$onBreed(ServerPlayerEntity player, AnimalEntity parent, AnimalEntity partner,
			PassiveEntity child, CallbackInfo ci) {
		XpSources.onActivity(player, XpRewards.BREEDING);
	}
}
