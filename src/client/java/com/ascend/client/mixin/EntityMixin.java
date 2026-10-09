package com.ascend.client.mixin;

import com.ascend.client.AscendClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mob "[Lv X]" tags are only shown near the player (or when aimed at, which vanilla handles). */
@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "isCustomNameVisible", at = @At("RETURN"), cancellable = true)
	private void ascend$limitLevelTagDistance(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			return;
		}
		Entity self = (Entity) (Object) this;
		if (!self.getWorld().isClient() || !(self instanceof Monster)) {
			return;
		}
		Text name = self.getCustomName();
		PlayerEntity player = MinecraftClient.getInstance().player;
		if (name == null || player == null || !name.getString().startsWith("[Lv ")) {
			return;
		}
		double max = AscendClient.levelTagDistance;
		if (self.squaredDistanceTo(player) > max * max) {
			cir.setReturnValue(false);
		}
	}
}
