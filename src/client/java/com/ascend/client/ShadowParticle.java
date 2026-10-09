package com.ascend.client;

import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

/** Glowing shadow wisps (rising smoke) and sparks (quick glints). */
public class ShadowParticle extends SpriteBillboardParticle {
	private static final int FULL_BRIGHT = 15728880;
	private final SpriteProvider sprites;
	private final float startAlpha;

	protected ShadowParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz,
			SpriteProvider sprites, boolean spark) {
		super(world, x, y, z, vx, vy, vz);
		this.sprites = sprites;
		this.velocityX = vx;
		this.velocityY = vy;
		this.velocityZ = vz;
		this.maxAge = spark ? 8 + this.random.nextInt(6) : 18 + this.random.nextInt(14);
		this.scale *= spark ? 0.7F : 1.3F + this.random.nextFloat() * 0.6F;
		this.gravityStrength = spark ? 0.0F : -0.015F;
		this.velocityMultiplier = spark ? 0.85F : 0.92F;
		this.startAlpha = spark ? 1.0F : 0.85F;
		this.alpha = startAlpha;
		this.setSpriteForAge(sprites);
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteForAge(sprites);
		this.alpha = startAlpha * (1.0F - (float) this.age / this.maxAge);
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getBrightness(float tint) {
		return FULL_BRIGHT;
	}

	public static class WispFactory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider sprites;

		public WispFactory(SpriteProvider sprites) {
			this.sprites = sprites;
		}

		@Override
		public ShadowParticle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
				double vx, double vy, double vz) {
			return new ShadowParticle(world, x, y, z, vx, vy, vz, sprites, false);
		}
	}

	public static class SparkFactory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider sprites;

		public SparkFactory(SpriteProvider sprites) {
			this.sprites = sprites;
		}

		@Override
		public ShadowParticle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z,
				double vx, double vy, double vz) {
			return new ShadowParticle(world, x, y, z, vx, vy, vz, sprites, true);
		}
	}
}
