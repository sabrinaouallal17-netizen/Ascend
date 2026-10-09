package com.ascend.client;

import com.ascend.logic.Leveling;
import com.ascend.logic.Milestone;
import com.ascend.logic.PlayerProgress;
import com.ascend.network.DoubleJumpPayload;
import com.ascend.network.LevelTagPayload;
import com.ascend.network.SettingsPayload;
import com.ascend.network.SyncPayload;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AscendClient implements ClientModInitializer {
	private static final KeyBinding CHARACTER_KEY = new KeyBinding(
			"key.ascend.character", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.ascend");
	private static final double DOUBLE_JUMP_VELOCITY = 0.55;

	/** The last progress received from the server. */
	public static PlayerProgress progress = new PlayerProgress();
	/** Levels of the players around, shown above their heads (0 = hidden). */
	public static final Map<UUID, Integer> LEVELS = new ConcurrentHashMap<>();
	public static boolean milestonesEnabled = true;

	private boolean jumpWasPressed;
	private boolean doubleJumpUsed;

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(CHARACTER_KEY);

		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) ->
				progress = new PlayerProgress(payload.level(), payload.xp(), payload.points(),
						payload.stats().stream().mapToInt(Integer::intValue).toArray()));
		ClientPlayNetworking.registerGlobalReceiver(LevelTagPayload.ID, (payload, context) ->
				LEVELS.put(payload.player(), payload.level()));
		ClientPlayNetworking.registerGlobalReceiver(SettingsPayload.ID, (payload, context) ->
				milestonesEnabled = payload.milestones());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			LEVELS.clear();
			progress = new PlayerProgress();
			milestonesEnabled = true;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (CHARACTER_KEY.wasPressed()) {
				if (client.currentScreen == null && client.player != null) {
					client.setScreen(new CharacterScreen());
				}
			}
			tickDoubleJump(client);
		});

		HudRenderCallback.EVENT.register((context, tickCounter) -> renderHud(context));
	}

	/** Sky Walker milestone: press jump again in the air to jump once more. */
	private void tickDoubleJump(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		boolean pressed = client.options.jumpKey.isPressed();
		if (player == null) {
			jumpWasPressed = pressed;
			return;
		}
		if (player.isOnGround() || player.isClimbing() || player.isTouchingWater()) {
			doubleJumpUsed = false;
		}
		boolean canDoubleJump = milestonesEnabled
				&& Milestone.SKY_WALKER.unlockedAt(progress.level())
				&& !doubleJumpUsed
				&& !player.isOnGround()
				&& !player.isTouchingWater()
				&& !player.isFallFlying()
				&& !player.getAbilities().flying
				&& !player.hasVehicle()
				&& client.currentScreen == null;
		if (pressed && !jumpWasPressed && canDoubleJump) {
			Vec3d velocity = player.getVelocity();
			player.setVelocity(velocity.x, DOUBLE_JUMP_VELOCITY, velocity.z);
			player.fallDistance = 0.0F;
			doubleJumpUsed = true;
			if (ClientPlayNetworking.canSend(DoubleJumpPayload.ID)) {
				ClientPlayNetworking.send(DoubleJumpPayload.INSTANCE);
			}
		}
		jumpWasPressed = pressed;
	}

	/** Small "Lv X" and XP bar in the bottom-left corner. */
	private static void renderHud(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}
		int x = 4;
		int y = context.getScaledWindowHeight() - 16;
		int barWidth = 60;
		String label = "Lv " + progress.level();
		context.drawTextWithShadow(client.textRenderer, label, x, y - 2, progress.isMaxLevel() ? 0xFFFFAA00 : 0xFFFFFF55);
		int barX = x + client.textRenderer.getWidth(label) + 4;
		int filled = progress.isMaxLevel()
				? barWidth
				: (int) ((long) barWidth * progress.xp() / Leveling.xpToNextLevel(progress.level()));
		context.fill(barX, y + 1, barX + barWidth, y + 4, 0xC0222222);
		context.fill(barX, y + 1, barX + filled, y + 4, 0xFF55DD55);
	}
}
