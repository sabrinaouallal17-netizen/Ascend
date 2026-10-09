package com.ascend;

import com.ascend.item.AscendItems;
import com.ascend.logic.Milestone;
import com.ascend.network.DoubleJumpPayload;
import com.ascend.network.LevelTagPayload;
import com.ascend.network.SettingsPayload;
import com.ascend.network.SpendPayload;
import com.ascend.network.SyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
		AscendItems.init();
		PayloadTypeRegistry.playS2C().register(SyncPayload.ID, SyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(LevelTagPayload.ID, LevelTagPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(SettingsPayload.ID, SettingsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SpendPayload.ID, SpendPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DoubleJumpPayload.ID, DoubleJumpPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SpendPayload.ID,
				(payload, context) -> Progression.spend(context.player(), payload.stat()));
		ServerPlayNetworking.registerGlobalReceiver(DoubleJumpPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			if (AscendSettings.get(player.getServer()).milestones
					&& Milestone.SKY_WALKER.unlockedAt(Progression.get(player).level())) {
				player.fallDistance = 0.0F;
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Progression.onJoin(handler.getPlayer()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (!alive && !AscendSettings.get(newPlayer.getServer()).keepXp) {
				Progression.reset(newPlayer);
				newPlayer.sendMessage(Text.literal("You died and lost all your Ascend progress (keepXp is off).")
						.formatted(Formatting.RED), false);
			} else {
				Progression.refresh(newPlayer);
			}
		});

		XpSources.register();
		MilestoneEffects.register();
		AscendLoot.register();
		AscendCommands.register();

		LOGGER.info("Ascend loaded. Time to level up.");
	}
}
