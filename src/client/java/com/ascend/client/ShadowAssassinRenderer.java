package com.ascend.client;

import com.ascend.Ascend;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

/** Draws the Shadow Assassin with the zombie model and its own dark texture. */
public class ShadowAssassinRenderer extends ZombieEntityRenderer {
	private static final Identifier TEXTURE = Ascend.id("textures/entity/shadow_assassin.png");

	public ShadowAssassinRenderer(EntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public Identifier getTexture(ZombieEntity entity) {
		return TEXTURE;
	}
}
