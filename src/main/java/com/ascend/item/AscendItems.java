package com.ascend.item;

import com.ascend.Ascend;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class AscendItems {
	public static final Item EXP_BOTTLE = Registry.register(Registries.ITEM, Ascend.id("exp_bottle"),
			new ExpBottleItem(new Item.Settings().maxCount(64).rarity(Rarity.UNCOMMON)));

	private AscendItems() {
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(EXP_BOTTLE));
	}
}
