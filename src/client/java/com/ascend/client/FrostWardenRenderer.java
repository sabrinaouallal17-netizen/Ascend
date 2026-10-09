package com.ascend.client;

import com.ascend.Ascend;
import com.ascend.entity.FrostWardenEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Renders the Frost Warden: icy giant, glowing eyes, ability poses and a shiver when enraged. */
public class FrostWardenRenderer extends BipedEntityRenderer<FrostWardenEntity, FrostWardenModel> {
	private static final Identifier TEXTURE = Ascend.id("textures/entity/frost_warden.png");
	private static final RenderLayer EYES = RenderLayer.getEyes(Ascend.id("textures/entity/frost_warden_eyes.png"));
	private static final float SIZE = 1.45F;

	public FrostWardenRenderer(EntityRendererFactory.Context context) {
		super(context, new FrostWardenModel(context.getPart(EntityModelLayers.ZOMBIE)), 0.5F * SIZE);
		this.addFeature(new EyesFeatureRenderer<>(this) {
			@Override
			public RenderLayer getEyesTexture() {
				return EYES;
			}
		});
	}

	@Override
	public Identifier getTexture(FrostWardenEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(FrostWardenEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(SIZE, SIZE, SIZE);
	}

	@Override
	protected void setupTransforms(FrostWardenEntity entity, MatrixStack matrices, float animationProgress,
			float bodyYaw, float tickDelta, float scale) {
		float yaw = bodyYaw;
		if (entity.getState() == FrostWardenEntity.NOVA) {
			// Crouch before the burst.
			matrices.translate(0.0F, -0.1F, 0.0F);
		}
		if (entity.isEnraged() && entity.deathTime == 0) {
			// Blizzard shiver.
			yaw += MathHelper.sin(animationProgress * 1.7F) * 1.5F;
		}
		super.setupTransforms(entity, matrices, animationProgress, yaw, tickDelta, scale);
	}
}
