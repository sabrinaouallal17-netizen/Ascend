package com.ascend.client;

import com.ascend.entity.FrostWardenEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;

/** Heavy humanoid model with custom poses for each Frost Warden ability. */
public class FrostWardenModel extends BipedEntityModel<FrostWardenEntity> {
	public FrostWardenModel(ModelPart root) {
		super(root);
	}

	@Override
	public void setAngles(FrostWardenEntity entity, float limbAngle, float limbDistance, float animationProgress,
			float headYaw, float headPitch) {
		super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		float breathe = MathHelper.sin(animationProgress * 0.08F) * 0.05F;
		switch (entity.getState()) {
			case FrostWardenEntity.SPIKES -> {
				// Sword raised in both hands, about to stab the ground.
				float shake = MathHelper.sin(animationProgress * 2.0F) * 0.03F;
				this.rightArm.pitch = -2.8F + shake;
				this.leftArm.pitch = -2.8F - shake;
				this.rightArm.yaw = -0.25F;
				this.leftArm.yaw = 0.25F;
				this.rightArm.roll = 0.0F;
				this.leftArm.roll = 0.0F;
				this.head.pitch = -0.3F;
			}
			case FrostWardenEntity.NOVA -> {
				// Arms spread, body tensed, releasing the cold.
				this.rightArm.pitch = -0.2F + breathe;
				this.leftArm.pitch = -0.2F - breathe;
				this.rightArm.roll = 1.3F;
				this.leftArm.roll = -1.3F;
				this.rightLeg.roll = 0.2F;
				this.leftLeg.roll = -0.2F;
				this.head.pitch = -0.4F;
			}
			case FrostWardenEntity.LEAP -> {
				// Airborne: sword overhead, knees tucked.
				this.rightArm.pitch = -2.6F;
				this.leftArm.pitch = -2.2F;
				this.rightLeg.pitch = -0.9F;
				this.leftLeg.pitch = -0.4F;
			}
			default -> {
				// Marching: heavy swing, sword lowered.
				this.rightArm.pitch = this.rightArm.pitch * 0.5F - 0.35F + breathe;
				this.rightArm.roll = 0.15F;
				this.leftArm.pitch = this.leftArm.pitch * 0.5F - breathe;
				this.leftArm.roll = -0.2F;
				if (entity.isEnraged()) {
					this.head.pitch -= 0.15F;
				}
			}
		}
		this.hat.copyTransform(this.head);
	}
}
