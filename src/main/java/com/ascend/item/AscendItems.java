package com.ascend.item;

import com.ascend.Ascend;
import com.ascend.block.AscendBlocks;
import com.ascend.entity.AscendEntities;
import com.ascend.logic.XpBottle;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Formatting;
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

	public static final Item SHADOW_DAGGER = register("shadow_dagger", new ShadowDaggerItem(shadow()
			.attributeModifiers(SwordItem.createAttributeModifiers(ShadowToolMaterial.INSTANCE, 8, -2.0F))));
	public static final Item SHADOW_PICKAXE = register("shadow_pickaxe", new ShadowPickaxeItem(shadow()
			.attributeModifiers(MiningToolItem.createAttributeModifiers(ShadowToolMaterial.INSTANCE, 1.0F, -2.8F))));
	public static final Item SHADOW_AXE = register("shadow_axe", new ShadowAxeItem(shadow()
			.attributeModifiers(MiningToolItem.createAttributeModifiers(ShadowToolMaterial.INSTANCE, 5.0F, -3.0F))));
	public static final Item SHADOW_HELMET = register("shadow_helmet", armor(ArmorItem.Type.HELMET));
	public static final Item SHADOW_CHESTPLATE = register("shadow_chestplate", armor(ArmorItem.Type.CHESTPLATE));
	public static final Item SHADOW_LEGGINGS = register("shadow_leggings", armor(ArmorItem.Type.LEGGINGS));
	public static final Item SHADOW_BOOTS = register("shadow_boots", armor(ArmorItem.Type.BOOTS));

	public static final Item RAW_SHADOW = register("raw_shadow", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
	public static final Item SHADOW_INGOT = register("shadow_ingot", new Item(new Item.Settings().rarity(Rarity.RARE).fireproof()));
	public static final Item SIGIL_PIECE = register("sigil_piece", new SigilPieceItem(new Item.Settings().maxCount(16).rarity(Rarity.EPIC)));
	public static final Item SHADOW_ORE = register("shadow_ore", new BlockItem(AscendBlocks.SHADOW_ORE, new Item.Settings()));

	public static final Item SHADOW_SIGIL = register("shadow_sigil", new BossSigilItem(
			new Item.Settings().maxCount(16).rarity(Rarity.EPIC), () -> AscendEntities.SHADOW_ASSASSIN,
			"Shadow Assassin", "The Shadow Assassin has come for you...", Formatting.DARK_PURPLE));

	public static final Item FROST_SWORD = register("frost_sword", new FrostSwordItem(frost()
			.attributeModifiers(SwordItem.createAttributeModifiers(FrostToolMaterial.INSTANCE, 8, -2.4F))));
	public static final Item FROST_PICKAXE = register("frost_pickaxe", new FrostPickaxeItem(frost()
			.attributeModifiers(MiningToolItem.createAttributeModifiers(FrostToolMaterial.INSTANCE, 1.0F, -2.8F))));
	public static final Item FROST_AXE = register("frost_axe", new FrostAxeItem(frost()
			.attributeModifiers(MiningToolItem.createAttributeModifiers(FrostToolMaterial.INSTANCE, 5.0F, -3.0F))));
	public static final Item FROST_HELMET = register("frost_helmet", frostArmor(ArmorItem.Type.HELMET));
	public static final Item FROST_CHESTPLATE = register("frost_chestplate", frostArmor(ArmorItem.Type.CHESTPLATE));
	public static final Item FROST_LEGGINGS = register("frost_leggings", frostArmor(ArmorItem.Type.LEGGINGS));
	public static final Item FROST_BOOTS = register("frost_boots", frostArmor(ArmorItem.Type.BOOTS));
	public static final Item FROST_SIGIL = register("frost_sigil", new BossSigilItem(
			new Item.Settings().maxCount(16).rarity(Rarity.EPIC), () -> AscendEntities.FROST_WARDEN,
			"Frost Warden", "The air freezes... the Frost Warden awakens.", Formatting.AQUA));
	public static final Item FROST_WARDEN_SPAWN_EGG = register("frost_warden_spawn_egg",
			new SpawnEggItem(AscendEntities.FROST_WARDEN, 0x9FD8FF, 0x1E3A8A, new Item.Settings()));
	public static final Item SHADOW_ASSASSIN_SPAWN_EGG = register("shadow_assassin_spawn_egg",
			new SpawnEggItem(AscendEntities.SHADOW_ASSASSIN, 0x1A1026, 0x8A2BE2, new Item.Settings()));

	private AscendItems() {
	}

	/** Shadow gear: fireproof, epic and unbreakable. */
	private static Item.Settings shadow() {
		return new Item.Settings().fireproof().rarity(Rarity.EPIC)
				.component(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
	}

	/** Frost gear: like Shadow gear (fireproof, epic, unbreakable). */
	private static Item.Settings frost() {
		return shadow();
	}

	private static Item frostArmor(ArmorItem.Type type) {
		return new ArmorItem(ShadowArmor.FROST, type, frost().maxDamage(type.getMaxDamage(ShadowArmor.DURABILITY)));
	}

	private static Item armor(ArmorItem.Type type) {
		return new ArmorItem(ShadowArmor.MATERIAL, type, shadow().maxDamage(type.getMaxDamage(ShadowArmor.DURABILITY)));
	}

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, Ascend.id(name), item);
	}

	public static boolean wearsFullShadowSet(net.minecraft.entity.LivingEntity entity) {
		return entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).isOf(SHADOW_HELMET)
				&& entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isOf(SHADOW_CHESTPLATE)
				&& entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS).isOf(SHADOW_LEGGINGS)
				&& entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET).isOf(SHADOW_BOOTS);
	}

	public static boolean wearsFullFrostSet(net.minecraft.entity.LivingEntity entity) {
		return entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).isOf(FROST_HELMET)
				&& entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isOf(FROST_CHESTPLATE)
				&& entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS).isOf(FROST_LEGGINGS)
				&& entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET).isOf(FROST_BOOTS);
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(EXP_BOTTLE);
			entries.add(LARGE_EXP_BOTTLE);
			entries.add(LEGENDARY_EXP_BOTTLE);
			entries.add(TOME_OF_REBIRTH);
			entries.add(SIGIL_PIECE);
			entries.add(SHADOW_SIGIL);
			entries.add(SHADOW_PICKAXE);
			entries.add(SHADOW_AXE);
			entries.add(FROST_SIGIL);
			entries.add(FROST_PICKAXE);
			entries.add(FROST_AXE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
			entries.add(SHADOW_DAGGER);
			entries.add(SHADOW_HELMET);
			entries.add(SHADOW_CHESTPLATE);
			entries.add(SHADOW_LEGGINGS);
			entries.add(SHADOW_BOOTS);
			entries.add(FROST_SWORD);
			entries.add(FROST_HELMET);
			entries.add(FROST_CHESTPLATE);
			entries.add(FROST_LEGGINGS);
			entries.add(FROST_BOOTS);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
			entries.add(SHADOW_ASSASSIN_SPAWN_EGG);
			entries.add(FROST_WARDEN_SPAWN_EGG);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
			entries.add(RAW_SHADOW);
			entries.add(SHADOW_INGOT);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> entries.add(SHADOW_ORE));
	}
}
