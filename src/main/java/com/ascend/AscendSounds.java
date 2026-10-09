package com.ascend;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/** Custom sound effects (files in assets/ascend/sounds, listed in sounds.json). */
public final class AscendSounds {
	public static final SoundEvent BOSS_AMBIENT = register("shadow_assassin.ambient");
	public static final SoundEvent BOSS_HURT = register("shadow_assassin.hurt");
	public static final SoundEvent BOSS_DEATH = register("shadow_assassin.death");
	public static final SoundEvent BOSS_TELEPORT = register("shadow_assassin.teleport");
	public static final SoundEvent BOSS_SLASH = register("shadow_assassin.slash");
	public static final SoundEvent BOSS_CHARGE = register("shadow_assassin.charge");
	public static final SoundEvent BOSS_ROAR = register("shadow_assassin.roar");
	public static final SoundEvent BOSS_STORM = register("shadow_assassin.storm");
	public static final SoundEvent BOSS_SPAWN = register("shadow_assassin.spawn");
	public static final SoundEvent BOSS_CLONE = register("shadow_assassin.clone");
	public static final SoundEvent SHADOW_DASH = register("item.shadow_dagger.dash");
	public static final SoundEvent SHADOW_DAGGER_HIT = register("item.shadow_dagger.hit");
	public static final SoundEvent SHADOW_PICKAXE_SHATTER = register("item.shadow_pickaxe.shatter");
	public static final SoundEvent SHADOW_AXE_CLEAVE = register("item.shadow_axe.cleave");
	/** A registry entry, because armor materials need one for their equip sound. */
	public static final RegistryEntry<SoundEvent> SHADOW_ARMOR_EQUIP = Registry.registerReference(Registries.SOUND_EVENT,
			Ascend.id("item.shadow_armor.equip"), SoundEvent.of(Ascend.id("item.shadow_armor.equip")));

	private AscendSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = Ascend.id(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	/** Loads the class so the sounds are registered during mod initialization. */
	public static void init() {
	}
}
