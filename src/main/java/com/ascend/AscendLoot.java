package com.ascend;

import com.ascend.item.AscendItems;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;

/** Adds EXP bottles and the Tome of Rebirth to structure chests. */
public final class AscendLoot {
	private static final Set<RegistryKey<LootTable>> COMMON_CHESTS = Set.of(
			LootTables.SIMPLE_DUNGEON_CHEST,
			LootTables.ABANDONED_MINESHAFT_CHEST,
			LootTables.DESERT_PYRAMID_CHEST,
			LootTables.JUNGLE_TEMPLE_CHEST,
			LootTables.STRONGHOLD_CORRIDOR_CHEST,
			LootTables.SHIPWRECK_TREASURE_CHEST);
	private static final Set<RegistryKey<LootTable>> RARE_CHESTS = Set.of(
			LootTables.END_CITY_TREASURE_CHEST,
			LootTables.ANCIENT_CITY_CHEST,
			LootTables.BASTION_TREASURE_CHEST);

	private AscendLoot() {
	}

	public static void register() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) {
				return;
			}
			if (COMMON_CHESTS.contains(key)) {
				tableBuilder.pool(pool(AscendItems.EXP_BOTTLE, 0.35F, 1, 3));
			}
			if (RARE_CHESTS.contains(key)) {
				tableBuilder.pool(pool(AscendItems.LARGE_EXP_BOTTLE, 0.30F, 1, 2));
				tableBuilder.pool(pool(AscendItems.LEGENDARY_EXP_BOTTLE, 0.03F, 1, 1));
				tableBuilder.pool(pool(AscendItems.TOME_OF_REBIRTH, 0.05F, 1, 1));
			}
		});
	}

	private static LootPool.Builder pool(Item item, float chance, int min, int max) {
		return LootPool.builder()
				.rolls(ConstantLootNumberProvider.create(1))
				.conditionally(RandomChanceLootCondition.builder(chance))
				.with(ItemEntry.builder(item)
						.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max))));
	}
}
