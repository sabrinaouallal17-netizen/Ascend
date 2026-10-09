package com.ascend;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Operator commands:
 * /asxp add <amount> [player], /asxp set <level> [player], /ascend settings [name value] and /ascend reset.
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
					.then(CommandManager.literal("settings")
							.executes(AscendCommands::listSettings)
							.then(CommandManager.literal("keepXp")
									.then(CommandManager.argument("value", BoolArgumentType.bool())
											.executes(context -> changeSetting(context, "keepXp", settings ->
													settings.keepXp = BoolArgumentType.getBool(context, "value")))))
							.then(CommandManager.literal("xpMultiplier")
									.then(CommandManager.argument("value", DoubleArgumentType.doubleArg(
													AscendSettings.MIN_MULTIPLIER, AscendSettings.MAX_MULTIPLIER))
											.executes(context -> changeSetting(context, "xpMultiplier", settings ->
													settings.xpMultiplier = DoubleArgumentType.getDouble(context, "value")))))
							.then(CommandManager.literal("milestones")
									.then(CommandManager.argument("value", BoolArgumentType.bool())
											.executes(context -> changeSetting(context, "milestones", settings ->
													settings.milestones = BoolArgumentType.getBool(context, "value")))))
							.then(CommandManager.literal("levelTags")
									.then(CommandManager.argument("value", BoolArgumentType.bool())
											.executes(context -> changeSetting(context, "levelTags", settings ->
													settings.levelTags = BoolArgumentType.getBool(context, "value")))))
							.then(CommandManager.literal("levelTagDistance")
									.then(CommandManager.argument("value", IntegerArgumentType.integer(
													AscendSettings.MIN_TAG_DISTANCE, AscendSettings.MAX_TAG_DISTANCE))
											.executes(context -> changeSetting(context, "levelTagDistance", settings ->
													settings.levelTagDistance = IntegerArgumentType.getInteger(context, "value")))))
							.then(CommandManager.literal("mobLevels")
									.then(CommandManager.argument("value", BoolArgumentType.bool())
											.executes(context -> changeSetting(context, "mobLevels", settings ->
													settings.mobLevels = BoolArgumentType.getBool(context, "value"))))))
					.then(CommandManager.literal("reset")
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
								Progression.reset(player);
								context.getSource().sendFeedback(() -> Text.literal("Ascend progress reset."), false);
								return 1;
							})));
		});
	}

	private static int listSettings(CommandContext<ServerCommandSource> context) {
		AscendSettings settings = AscendSettings.get(context.getSource().getServer());
		context.getSource().sendFeedback(() -> Text.literal("Ascend Settings").formatted(Formatting.GOLD, Formatting.BOLD)
				.append(line("keepXp", settings.keepXp, "keep level and stats on death"))
				.append(line("xpMultiplier", settings.xpMultiplier, "XP multiplier (0.1 to 10)"))
				.append(line("milestones", settings.milestones, "milestone rewards"))
				.append(line("levelTags", settings.levelTags, "level above players' heads"))
				.append(line("mobLevels", settings.mobLevels, "random levels for new hostile mobs"))
				.append(line("levelTagDistance", settings.levelTagDistance, "blocks within which mob levels show (4 to 64)"))
				.append(Text.literal("\nChange one with /ascend settings <name> <value>").formatted(Formatting.GRAY)), false);
		return 1;
	}

	private static Text line(String name, Object value, String description) {
		return Text.literal("\n " + name + ": ").formatted(Formatting.YELLOW)
				.append(Text.literal(String.valueOf(value)).formatted(Formatting.WHITE))
				.append(Text.literal("  (" + description + ")").formatted(Formatting.GRAY));
	}

	private static int changeSetting(CommandContext<ServerCommandSource> context, String name,
			Consumer<AscendSettings> change) {
		MinecraftServer server = context.getSource().getServer();
		AscendSettings settings = AscendSettings.get(server);
		change.accept(settings);
		settings.markDirty();
		Progression.broadcastAll(server);
		context.getSource().sendFeedback(() -> Text.literal("Ascend setting " + name + " updated."), true);
		return 1;
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
