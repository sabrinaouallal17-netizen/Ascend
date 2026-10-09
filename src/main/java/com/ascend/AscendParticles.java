package com.ascend;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Custom particles: glowing shadow wisps and sparks. */
public final class AscendParticles {
	public static final SimpleParticleType SHADOW_WISP = Registry.register(Registries.PARTICLE_TYPE,
			Ascend.id("shadow_wisp"), FabricParticleTypes.simple());
	public static final SimpleParticleType SHADOW_SPARK = Registry.register(Registries.PARTICLE_TYPE,
			Ascend.id("shadow_spark"), FabricParticleTypes.simple());

	private AscendParticles() {
	}

	/** Loads the class so the particles are registered during mod initialization. */
	public static void init() {
	}
}
