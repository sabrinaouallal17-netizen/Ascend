package com.ascend.item;

import com.ascend.Ascend;
import com.ascend.logic.XpBottle;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class AscendItems {
	public static final Item EXP_BOTTLE = register("exp_bottle",
			new ExpBottleItem(new Item.Settings().maxCount(64).rarity(Rarity.UNCOMMON), XpBottle.SMALL));
	public static final Item LARGE_EXP_BOTTLE = register("large_exp_bottle",
			new ExpBottleItem(new Item.Settings().maxCount(64).rarity(Rarity.RARE), XpBottle.LARGE));
	public static final Item LEGENDARY_EXP_BOTTLE = register("legendary_exp_bottle",
			new ExpBottleItem(new Item.Settings().maxCount(16).rarity(Rarity.EPIC), XpBottle.LEGENDARY));
	public static final Item TOME_OF_REBIRTH = register("tome_of_rebirth",
			new TomeOfRebirthItem(new Item.Settings().maxCount(16).rarity(Rarity.EPIC)));

	private AscendItems() {
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Ascend.id(name), item);
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(EXP_BOTTLE);
			entries.add(LARGE_EXP_BOTTLE);
			entries.add(LEGENDARY_EXP_BOTTLE);
			entries.add(TOME_OF_REBIRTH);
		});
	}
}
