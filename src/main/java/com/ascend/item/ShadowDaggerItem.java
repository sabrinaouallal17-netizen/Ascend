package com.ascend.item;

import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/** Shadow Dagger: 13 damage, and right-click for Shadow Dash. */
public class ShadowDaggerItem extends SwordItem {
	public static final int DASH_COOLDOWN_TICKS = 5 * 20;
	private static final double DASH_DISTANCE = 8.0;

	public ShadowDaggerItem(Settings settings) {
		super(ShadowToolMaterial.INSTANCE, settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (hand != Hand.MAIN_HAND) {
			return TypedActionResult.pass(stack);
		}
		if (world instanceof ServerWorld serverWorld) {
			Vec3d direction = user.getRotationVector().multiply(1.0, 0.0, 1.0);
			if (direction.lengthSquared() < 1.0E-4) {
				return TypedActionResult.fail(stack);
			}
			direction = direction.normalize();
			Vec3d start = user.getEyePos();
			Vec3d end = start.add(direction.multiply(DASH_DISTANCE));
			HitResult hit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE, user));
			Vec3d target = hit.getPos().subtract(direction.multiply(0.6));
			serverWorld.spawnParticles(ParticleTypes.LARGE_SMOKE, user.getX(), user.getY() + 1.0, user.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
			user.requestTeleport(target.x, user.getY(), target.z);
			user.fallDistance = 0.0F;
			user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false, true));
			world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.4F);
		}
		user.getItemCooldownManager().set(this, DASH_COOLDOWN_TICKS);
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("+5 damage over a Netherite Sword").formatted(Formatting.DARK_PURPLE));
		tooltip.add(Text.literal("Skill: Shadow Dash").formatted(Formatting.LIGHT_PURPLE));
		tooltip.add(Text.literal("Right-click: dash 8 blocks and vanish for 3s (5s cooldown)").formatted(Formatting.GRAY));
	}
}
