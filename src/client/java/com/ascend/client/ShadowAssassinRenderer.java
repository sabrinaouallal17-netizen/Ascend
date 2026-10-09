package com.ascend.client;

import com.ascend.Ascend;
import com.ascend.entity.ShadowAssassinEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Renders the Shadow Assassin: dark texture, glowing eyes, ability poses, spinning storm and hovering. */
public class ShadowAssassinRenderer extends BipedEntityRenderer<ShadowAssassinEntity, ShadowAssassinModel> {
	private static final Identifier TEXTURE = Ascend.id("textures/entity/shadow_assassin.png");
	private static final RenderLayer EYES = RenderLayer.getEyes(Ascend.id("textures/entity/shadow_assassin_eyes.png"));

	public ShadowAssassinRenderer(EntityRendererFactory.Context context) {
		super(context, new ShadowAssassinModel(context.getPart(EntityModelLayers.ZOMBIE)), 0.5F);
		this.addFeature(new EyesFeatureRenderer<>(this) {
			@Override
			public RenderLayer getEyesTexture() {
				return EYES;
			}
		});
	}

	@Override
	public Identifier getTexture(ShadowAssassinEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(ShadowAssassinEntity entity, MatrixStack matrices, float amount) {
		float size = entity.isClone() ? 0.95F : 1.1F;
		matrices.scale(size, size, size);
	}

	@Override
	protected void setupTransforms(ShadowAssassinEntity entity, MatrixStack matrices, float animationProgress,
			float bodyYaw, float tickDelta, float scale) {
		float yaw = bodyYaw;
		switch (entity.getState()) {
			case ShadowAssassinEntity.STORM -> {
				// Hover and spin.
				matrices.translate(0.0F, 0.35F + MathHelper.sin(animationProgress * 0.3F) * 0.1F, 0.0F);
				yaw += (animationProgress * 40.0F) % 360.0F;
			}
			case ShadowAssassinEntity.SUMMON -> matrices.translate(0.0F, 0.2F, 0.0F);
			default -> {
				// Gentle floating bob.
				matrices.translate(0.0F, MathHelper.sin(animationProgress * 0.12F) * 0.04F, 0.0F);
			}
		}
		if (entity.deathTime > 0) {
			// Spin as it dies.
			yaw += (entity.deathTime + tickDelta) * 25.0F;
		}
		super.setupTransforms(entity, matrices, animationProgress, yaw, tickDelta, scale);
	}
}
