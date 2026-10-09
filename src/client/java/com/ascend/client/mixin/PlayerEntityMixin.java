package com.ascend.client.mixin;

import com.ascend.client.AscendClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shows "[Lv X]" before other players' names above their heads (client world only). */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
	private void ascend$addLevel(CallbackInfoReturnable<Text> cir) {
		PlayerEntity self = (PlayerEntity) (Object) this;
		if (!self.getWorld().isClient()) {
			return;
		}
		Integer level = AscendClient.LEVELS.get(self.getUuid());
		if (level == null || level <= 0) {
			return;
		}
		Formatting color = level >= 200 ? Formatting.GOLD : Formatting.YELLOW;
		cir.setReturnValue(Text.literal("[Lv " + level + "] ").formatted(color).append(cir.getReturnValue()));
	}
}
