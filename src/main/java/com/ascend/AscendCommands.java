package com.ascend;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Operator commands:
 * /asxp add <amount> [player], /asxp set <level> [player] and /ascend reset.
 */
public final class AscendCommands {
	private AscendCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("asxp")
					.requires(source -> source.hasPermissionLevel(2))
					.then(CommandManager.literal("add")
							.then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
									.executes(context -> add(context, context.getSource().getPlayerOrThrow()))
									.then(CommandManager.argument("player", EntityArgumentType.player())
											.executes(context -> add(context, EntityArgumentType.getPlayer(context, "player"))))))
					.then(CommandManager.literal("set")
							.then(CommandManager.argument("level", IntegerArgumentType.integer(1, com.ascend.logic.Leveling.MAX_LEVEL))
									.executes(context -> set(context, context.getSource().getPlayerOrThrow()))
									.then(CommandManager.argument("player", EntityArgumentType.player())
											.executes(context -> set(context, EntityArgumentType.getPlayer(context, "player")))))));

			dispatcher.register(CommandManager.literal("ascend")
					.requires(source -> source.hasPermissionLevel(2))
					.then(CommandManager.literal("reset")
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
								Progression.reset(player);
								context.getSource().sendFeedback(() -> Text.literal("Ascend progress reset."), false);
								return 1;
							})));
		});
	}

	private static int add(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Progression.addXp(player, amount);
		context.getSource().sendFeedback(() -> Text.literal("Gave " + amount + " Ascend XP to ")
				.append(player.getDisplayName()), true);
		return amount;
	}

	private static int set(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
		int level = IntegerArgumentType.getInteger(context, "level");
		Progression.setLevel(player, level);
		context.getSource().sendFeedback(() -> Text.literal("Set ").append(player.getDisplayName())
				.append(" to Ascend level " + level), true);
		return level;
	}
}
