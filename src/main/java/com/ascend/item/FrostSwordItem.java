package com.ascend.item;

import com.ascend.AscendParticles;
import com.ascend.AscendSounds;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Frost Sword: 14 damage; every hit slows and freezes the target. */
public class FrostSwordItem extends SwordItem {
	public FrostSwordItem(Settings settings) {
		super(FrostToolMaterial.INSTANCE, settings);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1));
		target.setFrozenTicks(Math.min(target.getFrozenTicks() + 80, target.getMinFreezeDamageTicks() + 100));
		if (attacker.getWorld() instanceof ServerWorld world) {
			world.spawnParticles(AscendParticles.FROST_SPARK, target.getX(), target.getBodyY(0.6), target.getZ(), 8, 0.25, 0.3, 0.25, 0.12);
			world.spawnParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getBodyY(0.5), target.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
			world.playSound(null, target.getBlockPos(), AscendSounds.FROST_SWORD_HIT, SoundCategory.PLAYERS,
					0.8F, 0.9F + world.random.nextFloat() * 0.3F);
		}
		return super.postHit(stack, target, attacker);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("Frostbite: hits slow and freeze").formatted(Formatting.AQUA));
	}
}
