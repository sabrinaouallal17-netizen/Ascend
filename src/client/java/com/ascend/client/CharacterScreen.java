package com.ascend.client;

import com.ascend.logic.Leveling;
import com.ascend.logic.Milestone;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Stat;
import com.ascend.network.SpendPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** The character screen (K): level, XP bar, skill points and the four stats. */
public class CharacterScreen extends Screen {
	private static final int PANEL_WIDTH = 260;
	private static final int PANEL_HEIGHT = 246;
	private static final int ROW_HEIGHT = 22;
	private static final int MILESTONE_ROW = 12;
	private static final int GOLD = 0xFFFFAA00;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFAAAAAA;
	private static final int GREEN = 0xFF55FF55;

	private final List<ButtonWidget> plusButtons = new ArrayList<>();

	public CharacterScreen() {
		super(Text.literal("Ascend"));
	}

	private int left() {
		return (width - PANEL_WIDTH) / 2;
	}

	private int top() {
		return (height - PANEL_HEIGHT) / 2;
	}

	@Override
	protected void init() {
		plusButtons.clear();
		for (Stat stat : Stat.values()) {
			int y = top() + 72 + stat.ordinal() * ROW_HEIGHT;
			plusButtons.add(addDrawableChild(ButtonWidget.builder(Text.literal("+"),
					button -> ClientPlayNetworking.send(new SpendPayload(stat.ordinal())))
					.dimensions(left() + PANEL_WIDTH - 32, y - 4, 20, 20).build()));
		}
		refreshButtons();
	}

	private void refreshButtons() {
		PlayerProgress progress = AscendClient.progress;
		for (Stat stat : Stat.values()) {
			plusButtons.get(stat.ordinal()).active = progress.canSpend(stat);
		}
	}

	@Override
	public void tick() {
		refreshButtons();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		PlayerProgress progress = AscendClient.progress;
		int left = left();
		int top = top();

		context.drawCenteredTextWithShadow(textRenderer, Text.literal("ASCEND"), width / 2, top + 8, GOLD);
		context.drawCenteredTextWithShadow(textRenderer,
				Text.literal("Level " + progress.level() + " / " + Leveling.MAX_LEVEL), width / 2, top + 24, WHITE);

		int barLeft = left + 20;
		int barWidth = PANEL_WIDTH - 40;
		int barTop = top + 38;
		int needed = Leveling.xpToNextLevel(progress.level());
		int filled = progress.isMaxLevel() ? barWidth : (int) ((long) barWidth * progress.xp() / needed);
		context.fill(barLeft, barTop, barLeft + barWidth, barTop + 6, 0xFF222222);
		context.fill(barLeft, barTop, barLeft + filled, barTop + 6, 0xFF55DD55);
		String xpText = progress.isMaxLevel() ? "MAX LEVEL" : progress.xp() + " / " + needed + " XP";
		context.drawCenteredTextWithShadow(textRenderer, Text.literal(xpText), width / 2, barTop + 9, GRAY);

		context.drawTextWithShadow(textRenderer, Text.literal("Skill points: " + progress.points()),
				left + 20, top + 56, progress.points() > 0 ? GREEN : GRAY);

		for (Stat stat : Stat.values()) {
			int y = top + 72 + stat.ordinal() * ROW_HEIGHT;
			int points = progress.stat(stat);
			context.drawTextWithShadow(textRenderer, Text.literal(stat.displayName()), left + 20, y, WHITE);
			context.drawTextWithShadow(textRenderer, Text.literal(points + "/" + Stat.CAP), left + 95, y, GRAY);
			context.drawTextWithShadow(textRenderer, Text.literal(bonusText(stat, points)), left + 135, y, GREEN);
		}

		int milestonesTop = top + 72 + Stat.values().length * ROW_HEIGHT + 4;
		context.drawTextWithShadow(textRenderer, Text.literal("Milestones"), left + 20, milestonesTop, GOLD);
		for (Milestone milestone : Milestone.values()) {
			int y = milestonesTop + 14 + milestone.ordinal() * MILESTONE_ROW;
			boolean unlocked = milestone.unlockedAt(progress.level());
			String line = (unlocked ? "✔ " : "Lv " + milestone.level() + " ") + milestone.title() + ": " + milestone.description();
			context.drawTextWithShadow(textRenderer, Text.literal(line), left + 20, y, unlocked ? GREEN : GRAY);
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		int left = left();
		int top = top();
		context.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, 0xFFFFAA00);
		context.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF0101018);
	}

	private static String bonusText(Stat stat, int points) {
		double bonus = stat.bonus(points);
		return switch (stat) {
			case STRENGTH -> String.format(Locale.ROOT, "+%.1f dmg", bonus);
			case VITALITY -> String.format(Locale.ROOT, "+%.1f HP", bonus);
			case AGILITY -> String.format(Locale.ROOT, "+%.1f%% speed", bonus * 100);
			case MINING -> String.format(Locale.ROOT, "+%.0f%% mining", bonus * 100);
		};
	}
}
