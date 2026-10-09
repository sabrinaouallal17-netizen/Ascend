package com.ascend.client;

import com.ascend.logic.Leveling;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Stat;
import com.ascend.network.SpendPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Stats tab: level, XP bar, skill points and the four stats, with an amount box to spend many points at once. */
public class CharacterScreen extends AscendScreen {
	private static final int ROW_HEIGHT = 26;
	private static final int STATS_TOP = 92;

	private final List<ButtonWidget> plusButtons = new ArrayList<>();
	private TextFieldWidget amountField;

	public CharacterScreen() {
		super("Ascend - Stats");
	}

	@Override
	protected Tab tab() {
		return Tab.STATS;
	}

	@Override
	protected void initContent() {
		amountField = new TextFieldWidget(textRenderer, left() + PANEL_WIDTH - 52, top() + 68, 34, 14, Text.literal("Amount"));
		amountField.setMaxLength(2);
		amountField.setTextPredicate(text -> text.matches("\\d*"));
		amountField.setText("1");
		addDrawableChild(amountField);

		plusButtons.clear();
		for (Stat stat : Stat.values()) {
			int y = top() + STATS_TOP + stat.ordinal() * ROW_HEIGHT;
			plusButtons.add(addDrawableChild(ButtonWidget.builder(Text.literal("+"),
					button -> ClientPlayNetworking.send(new SpendPayload(stat.ordinal(), amount())))
					.dimensions(left() + PANEL_WIDTH - 32, y - 5, 20, 18).build()));
		}
		refreshButtons();
	}

	private int amount() {
		try {
			return Math.max(1, Integer.parseInt(amountField.getText()));
		} catch (NumberFormatException empty) {
			return 1;
		}
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
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		PlayerProgress progress = AscendClient.progress;
		int left = left();
		int top = top() + CONTENT_TOP;

		context.drawCenteredTextWithShadow(textRenderer, Text.literal("Level " + progress.level() + " / " + Leveling.MAX_LEVEL),
				width / 2, top + 4, WHITE);
		int barLeft = left + 20;
		int barWidth = PANEL_WIDTH - 40;
		int barTop = top + 16;
		int needed = Leveling.xpToNextLevel(progress.level());
		int filled = progress.isMaxLevel() ? barWidth : (int) ((long) barWidth * progress.xp() / needed);
		context.fill(barLeft, barTop, barLeft + barWidth, barTop + 6, 0xFF222222);
		context.fill(barLeft, barTop, barLeft + filled, barTop + 6, 0xFF55DD55);
		String xpText = progress.isMaxLevel() ? "MAX LEVEL" : progress.xp() + " / " + needed + " XP";
		context.drawCenteredTextWithShadow(textRenderer, Text.literal(xpText), width / 2, barTop + 9, GRAY);

		text(context, "Skill points: " + progress.points(), left + 20, top() + 71, progress.points() > 0 ? GREEN : GRAY);
		rightText(context, "Amount:", left + PANEL_WIDTH - 56, top() + 71, GRAY);

		for (Stat stat : Stat.values()) {
			int y = top() + STATS_TOP + stat.ordinal() * ROW_HEIGHT;
			int points = progress.stat(stat);
			text(context, stat.displayName(), left + 20, y, WHITE);
			text(context, points + "/" + Stat.CAP, left + 95, y, GRAY);
			text(context, bonusText(stat, points), left + 140, y, GREEN);
		}
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
