package com.ascend.client;

import com.ascend.entity.ShadowAssassinEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;

/** Humanoid model with custom poses for each Shadow Assassin ability. */
public class ShadowAssassinModel extends BipedEntityModel<ShadowAssassinEntity> {
	public ShadowAssassinModel(ModelPart root) {
		super(root);
	}

	@Override
	public void setAngles(ShadowAssassinEntity entity, float limbAngle, float limbDistance, float animationProgress,
			float headYaw, float headPitch) {
		super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		float breathe = MathHelper.sin(animationProgress * 0.1F) * 0.06F;
		switch (entity.getState()) {
			case ShadowAssassinEntity.WINDUP -> {
				// Blade raised high, off hand pulled back, trembling with power.
				float shake = MathHelper.sin(animationProgress * 2.5F) * 0.04F;
				this.rightArm.pitch = -2.9F + shake;
				this.rightArm.yaw = -0.4F;
				this.rightArm.roll = 0.0F;
				this.leftArm.pitch = 0.6F;
				this.leftArm.roll = -0.35F;
				this.head.pitch = -0.2F;
			}
			case ShadowAssassinEntity.STRIKE -> {
				// Both arms thrust forward through the target.
				this.rightArm.pitch = -1.5F;
				this.rightArm.yaw = 0.3F;
				this.leftArm.pitch = -1.5F;
				this.leftArm.yaw = -0.3F;
				this.rightLeg.pitch = -0.6F;
				this.leftLeg.pitch = 0.6F;
			}
			case ShadowAssassinEntity.STORM -> {
				// Arms spread wide while spinning (the spin is done by the renderer).
				this.rightArm.pitch = breathe;
				this.leftArm.pitch = -breathe;
				this.rightArm.roll = 1.55F + breathe;
				this.leftArm.roll = -1.55F - breathe;
				this.rightLeg.roll = 0.15F;
				this.leftLeg.roll = -0.15F;
			}
			case ShadowAssassinEntity.SUMMON -> {
				// Arms raised to the sky, calling the clones.
				float wave = MathHelper.sin(animationProgress * 0.6F) * 0.15F;
				this.rightArm.pitch = -3.0F + wave;
				this.leftArm.pitch = -3.0F - wave;
				this.rightArm.roll = 0.3F;
				this.leftArm.roll = -0.3F;
				this.head.pitch = -0.5F;
			}
			default -> {
				// Stalking: blade held ready, slight breathing.
				this.rightArm.pitch = this.rightArm.pitch * 0.6F - 0.6F + breathe;
				this.rightArm.roll = 0.1F;
				this.leftArm.pitch = this.leftArm.pitch * 0.6F - 0.25F - breathe;
				this.leftArm.roll = -0.15F;
			}
		}
		this.hat.copyTransform(this.head);
	}
}
