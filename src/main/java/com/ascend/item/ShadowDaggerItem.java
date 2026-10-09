package com.ascend.item;

import com.ascend.AscendParticles;
import com.ascend.AscendSounds;
import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
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
			Vec3d from = user.getPos();
			world.playSound(null, user.getBlockPos(), AscendSounds.SHADOW_DASH, SoundCategory.PLAYERS, 1.0F, 1.0F);
			user.requestTeleport(target.x, user.getY(), target.z);
			// Trail of shadow wisps along the dash.
			for (int i = 0; i <= 10; i++) {
				Vec3d point = from.lerp(user.getPos(), i / 10.0);
				serverWorld.spawnParticles(AscendParticles.SHADOW_WISP, point.x, point.y + 1.0, point.z, 3, 0.2, 0.4, 0.2, 0.01);
			}
			serverWorld.spawnParticles(AscendParticles.SHADOW_SPARK, user.getX(), user.getY() + 1.0, user.getZ(), 12, 0.3, 0.5, 0.3, 0.1);
			user.fallDistance = 0.0F;
			user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 60, 0, false, false, true));
		}
		user.getItemCooldownManager().set(this, DASH_COOLDOWN_TICKS);
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public boolean postHit(ItemStack stack, net.minecraft.entity.LivingEntity target, net.minecraft.entity.LivingEntity attacker) {
		if (attacker.getWorld() instanceof ServerWorld world) {
			world.spawnParticles(AscendParticles.SHADOW_SPARK, target.getX(), target.getBodyY(0.6), target.getZ(), 6, 0.25, 0.3, 0.25, 0.12);
		}
		return super.postHit(stack, target, attacker);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.literal("+5 damage over a Netherite Sword").formatted(Formatting.DARK_PURPLE));
		tooltip.add(Text.literal("Skill: Shadow Dash").formatted(Formatting.LIGHT_PURPLE));
		tooltip.add(Text.literal("Right-click: dash 8 blocks and vanish for 3s (5s cooldown)").formatted(Formatting.GRAY));
	}
}
