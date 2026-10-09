package com.ascend.block;

import com.ascend.Ascend;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public final class AscendBlocks {
	/** Found in the Shadow Wastes. Needs a diamond pickaxe; drops Raw Shadow. Glows faintly. */
	public static final Block SHADOW_ORE = Registry.register(Registries.BLOCK, Ascend.id("shadow_ore"),
			new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), AbstractBlock.Settings.create()
					.mapColor(MapColor.PURPLE)
					.strength(4.5F, 6.0F)
					.requiresTool()
					.sounds(BlockSoundGroup.DEEPSLATE)
					.luminance(state -> 5)));

	private AscendBlocks() {
	}

	public static void init() {
	}
}
