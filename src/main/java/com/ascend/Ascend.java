package com.ascend;

import com.ascend.network.SpendPayload;
import com.ascend.network.SyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ascend implements ModInitializer {
	public static final String MOD_ID = "ascend";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		AscendData.init();
		PayloadTypeRegistry.playS2C().register(SyncPayload.ID, SyncPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SpendPayload.ID, SpendPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SpendPayload.ID,
				(payload, context) -> Progression.spend(context.player(), payload.stat()));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Progression.refresh(handler.getPlayer()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> Progression.refresh(newPlayer));

		XpSources.register();
		AscendCommands.register();

		LOGGER.info("Ascend loaded. Time to level up.");
	}
}
