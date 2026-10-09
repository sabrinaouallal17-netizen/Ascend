package com.ascend.world;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import terrablender.api.Region;
import terrablender.api.RegionType;

/** A TerraBlender region where some temperate biomes are replaced by the Shadow Wastes. */
public class ShadowRegion extends Region {
	public ShadowRegion(Identifier name, int weight) {
		super(name, RegionType.OVERWORLD, weight);
	}

	@Override
	public void addBiomes(Registry<Biome> registry, Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> mapper) {
		this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
			builder.replaceBiome(BiomeKeys.PLAINS, AscendBiomes.SHADOW_WASTES);
			builder.replaceBiome(BiomeKeys.FOREST, AscendBiomes.SHADOW_WASTES);
			builder.replaceBiome(BiomeKeys.DARK_FOREST, AscendBiomes.SHADOW_WASTES);
			builder.replaceBiome(BiomeKeys.SAVANNA, AscendBiomes.SHADOW_WASTES);
			builder.replaceBiome(BiomeKeys.TAIGA, AscendBiomes.SHADOW_WASTES);
		});
	}
}
