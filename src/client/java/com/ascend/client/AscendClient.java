package com.ascend.client;

import com.ascend.entity.AscendEntities;
import com.ascend.logic.Achievement;
import com.ascend.logic.Counter;
import com.ascend.logic.Leveling;
import com.ascend.logic.Milestone;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.QuestLog;
import com.ascend.logic.Skill;
import com.ascend.logic.SkillBook;
import com.ascend.network.DoubleJumpPayload;
import com.ascend.network.LevelTagPayload;
import com.ascend.network.QuestPayload;
import com.ascend.network.SettingsPayload;
import com.ascend.network.SkillActionPayload;
import com.ascend.network.SkillsPayload;
import com.ascend.network.SyncPayload;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
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
	public static final KeyBinding[] SKILL_KEYS = {
			new KeyBinding("key.ascend.skill_1", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.ascend"),
			new KeyBinding("key.ascend.skill_2", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.ascend"),
			new KeyBinding("key.ascend.skill_3", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.ascend")};
	private static final double DOUBLE_JUMP_VELOCITY = 0.55;

	/** The last data received from the server. */
	public static PlayerProgress progress = new PlayerProgress();
	public static SkillBook skills = new SkillBook();
	public static QuestLog quests = new QuestLog();
	/** Client tick at which each skill is ready again. */
	public static final Map<Skill, Long> SKILL_READY_AT = new EnumMap<>(Skill.class);
	/** Levels of the players around, shown above their heads (0 = hidden). */
	public static final Map<UUID, Integer> LEVELS = new ConcurrentHashMap<>();
	public static boolean milestonesEnabled = true;
	public static long clientTicks;

	private boolean jumpWasPressed;
	private boolean doubleJumpUsed;

	public static Set<Milestone> milestones() {
		return milestonesEnabled ? Milestone.unlocked(progress.level(), quests) : EnumSet.noneOf(Milestone.class);
	}

	public static long cooldownLeft(Skill skill) {
		return Math.max(0, SKILL_READY_AT.getOrDefault(skill, 0L) - clientTicks);
	}

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(CHARACTER_KEY);
		for (KeyBinding key : SKILL_KEYS) {
			KeyBindingHelper.registerKeyBinding(key);
		}
		EntityRendererRegistry.register(AscendEntities.SHADOW_ASSASSIN, ShadowAssassinRenderer::new);

		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (payload, context) ->
				progress = new PlayerProgress(payload.level(), payload.xp(), payload.points(),
						payload.stats().stream().mapToInt(Integer::intValue).toArray()));
		ClientPlayNetworking.registerGlobalReceiver(SkillsPayload.ID, (payload, context) -> {
			Set<Skill> learned = EnumSet.noneOf(Skill.class);
			payload.learned().forEach(index -> learned.add(Skill.values()[index]));
			Skill[] slots = new Skill[SkillBook.SLOTS];
			for (int i = 0; i < slots.length && i < payload.slots().size(); i++) {
				int index = payload.slots().get(i);
				slots[i] = index >= 0 ? Skill.values()[index] : null;
			}
			skills = new SkillBook(learned, slots);
			for (int i = 0; i < Skill.values().length && i < payload.cooldowns().size(); i++) {
				SKILL_READY_AT.put(Skill.values()[i], clientTicks + payload.cooldowns().get(i));
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(QuestPayload.ID, (payload, context) -> {
			Map<Counter, Integer> counters = new EnumMap<>(Counter.class);
			for (int i = 0; i < Counter.values().length && i < payload.counters().size(); i++) {
				counters.put(Counter.values()[i], payload.counters().get(i));
			}
			Set<Achievement> unlocked = EnumSet.noneOf(Achievement.class);
			payload.achievements().forEach(index -> unlocked.add(Achievement.values()[index]));
			quests = new QuestLog(counters, unlocked);
		});
		ClientPlayNetworking.registerGlobalReceiver(LevelTagPayload.ID, (payload, context) ->
				LEVELS.put(payload.player(), payload.level()));
		ClientPlayNetworking.registerGlobalReceiver(SettingsPayload.ID, (payload, context) ->
				milestonesEnabled = payload.milestones());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			LEVELS.clear();
			SKILL_READY_AT.clear();
			progress = new PlayerProgress();
			skills = new SkillBook();
			quests = new QuestLog();
			milestonesEnabled = true;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			clientTicks++;
			while (CHARACTER_KEY.wasPressed()) {
				if (client.currentScreen == null && client.player != null) {
					client.setScreen(new CharacterScreen());
				}
			}
			for (int slot = 0; slot < SKILL_KEYS.length; slot++) {
				while (SKILL_KEYS[slot].wasPressed()) {
					if (client.player != null && skills.inSlot(slot) != null
							&& ClientPlayNetworking.canSend(SkillActionPayload.ID)) {
						ClientPlayNetworking.send(new SkillActionPayload(SkillActionPayload.USE, -1, slot));
					}
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
		boolean canDoubleJump = milestones().contains(Milestone.SKY_WALKER)
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

	/** Bottom-left corner: skill slots with cooldowns, then "Lv X" and the XP bar. */
	private static void renderHud(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}
		int x = 4;
		int y = context.getScaledWindowHeight() - 16;
		int line = y - 12;
		for (int slot = SkillBook.SLOTS - 1; slot >= 0; slot--) {
			Skill skill = skills.inSlot(slot);
			if (skill == null) {
				continue;
			}
			long left = cooldownLeft(skill);
			String key = SKILL_KEYS[slot].getBoundKeyLocalizedText().getString();
			String text = "[" + key + "] " + skill.title() + (left > 0 ? " " + (left + 19) / 20 + "s" : "");
			context.drawTextWithShadow(client.textRenderer, text, x, line, left > 0 ? 0xFF888888 : 0xFF55FFFF);
			line -= 10;
		}
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
