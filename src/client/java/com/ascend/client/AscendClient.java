package com.ascend.client;

import com.ascend.logic.PlayerProgress;
import com.ascend.network.SyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AscendClient implements ClientModInitializer {
	private static final KeyBinding CHARACTER_KEY = new KeyBinding(
			"key.ascend.character", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.ascend");

	/** The last progress received from the server. */
	public static PlayerProgress progress = new PlayerProgress();

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(CHARACTER_KEY);

		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) ->
				progress = new PlayerProgress(payload.level(), payload.xp(), payload.points(),
						payload.stats().stream().mapToInt(Integer::intValue).toArray()));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (CHARACTER_KEY.wasPressed()) {
				if (client.currentScreen == null && client.player != null) {
					client.setScreen(new CharacterScreen());
				}
			}
		});
	}
}
