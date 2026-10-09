package com.ascend.world;

import com.ascend.Ascend;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

/** Biome keys (biomes themselves are data files in data/ascend/worldgen/biome). */
public final class AscendBiomes {
	public static final RegistryKey<Biome> SHADOW_WASTES = RegistryKey.of(RegistryKeys.BIOME, Ascend.id("shadow_wastes"));

	private AscendBiomes() {
	}
}
