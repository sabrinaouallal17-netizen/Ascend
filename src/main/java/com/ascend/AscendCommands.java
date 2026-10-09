package com.ascend;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** Operator commands, handy for testing: /ascend addxp <amount> and /ascend reset. */
public final class AscendCommands {
	private AscendCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				CommandManager.literal("ascend")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.literal("addxp")
								.then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
										.executes(context -> {
											ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
											Progression.addXp(player, IntegerArgumentType.getInteger(context, "amount"));
											return 1;
										})))
						.then(CommandManager.literal("reset")
								.executes(context -> {
									ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
									Progression.reset(player);
									context.getSource().sendFeedback(() -> Text.literal("Ascend progress reset."), false);
									return 1;
								}))));
	}
}
