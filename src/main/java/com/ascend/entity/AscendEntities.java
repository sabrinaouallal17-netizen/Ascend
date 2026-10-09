package com.ascend.entity;

import com.ascend.Ascend;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class AscendEntities {
	public static final EntityType<ShadowAssassinEntity> SHADOW_ASSASSIN = Registry.register(Registries.ENTITY_TYPE,
			Ascend.id("shadow_assassin"),
			EntityType.Builder.create(ShadowAssassinEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.6F, 1.95F)
					.maxTrackingRange(10)
					.build("shadow_assassin"));

	private AscendEntities() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(SHADOW_ASSASSIN, ShadowAssassinEntity.createAttributes());
	}
}
