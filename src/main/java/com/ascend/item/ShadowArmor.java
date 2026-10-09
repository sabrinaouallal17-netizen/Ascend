package com.ascend.item;

import com.ascend.Ascend;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

/** Shadow armor material: a bit tougher than netherite. */
public final class ShadowArmor {
	public static final RegistryEntry<ArmorMaterial> MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL,
			Ascend.id("shadow"),
			new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class), defense -> {
						defense.put(ArmorItem.Type.BOOTS, 4);
						defense.put(ArmorItem.Type.LEGGINGS, 7);
						defense.put(ArmorItem.Type.CHESTPLATE, 9);
						defense.put(ArmorItem.Type.HELMET, 4);
						defense.put(ArmorItem.Type.BODY, 11);
					}),
					20,
					SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
					() -> Ingredient.ofItems(Items.NETHERITE_INGOT),
					List.of(new ArmorMaterial.Layer(Ascend.id("shadow"))),
					4.0F,
					0.15F));

	/** Durability multiplier (netherite is 37). */
	public static final int DURABILITY = 40;

	private ShadowArmor() {
	}
}
