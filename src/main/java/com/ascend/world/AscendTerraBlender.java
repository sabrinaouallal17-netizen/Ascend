package com.ascend.world;

import com.ascend.Ascend;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;
import terrablender.api.TerraBlenderApi;

/** TerraBlender entrypoint: adds the Shadow Wastes region and its dark surface (blackstone over basalt). */
public class AscendTerraBlender implements TerraBlenderApi {
	private static final int REGION_WEIGHT = 2;

	@Override
	public void onTerraBlenderInitialized() {
		Regions.register(new ShadowRegion(Ascend.id("shadow_region"), REGION_WEIGHT));
		SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, Ascend.MOD_ID, surfaceRules());
	}

	private static MaterialRules.MaterialRule surfaceRules() {
		return MaterialRules.condition(MaterialRules.biome(AscendBiomes.SHADOW_WASTES),
				MaterialRules.condition(MaterialRules.surface(), MaterialRules.sequence(
						MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR,
								MaterialRules.block(Blocks.BLACKSTONE.getDefaultState())),
						MaterialRules.condition(MaterialRules.STONE_DEPTH_FLOOR_WITH_SURFACE_DEPTH,
								MaterialRules.block(Blocks.BASALT.getDefaultState())))));
	}
}
