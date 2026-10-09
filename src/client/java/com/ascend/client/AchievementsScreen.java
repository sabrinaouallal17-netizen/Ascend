package com.ascend.client;

import com.ascend.logic.Achievement;
import net.minecraft.client.gui.DrawContext;

/** Achievements tab: two columns of achievements with their progress and XP reward. */
public class AchievementsScreen extends AscendScreen {
	private static final int CELL_HEIGHT = 28;

	public AchievementsScreen() {
		super("Ascend - Achievements");
	}

	@Override
	protected Tab tab() {
		return Tab.ACHIEVEMENTS;
	}

	@Override
	protected void initContent() {
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Achievement[] all = Achievement.values();
		int unlockedCount = AscendClient.quests.unlocked().size();
		text(context, "Unlocked: " + unlockedCount + " / " + all.length, left() + 12, top() + CONTENT_TOP + 4, GOLD);
		int columnWidth = (PANEL_WIDTH - 24) / 2;
		int rowsPerColumn = (all.length + 1) / 2;
		for (Achievement achievement : all) {
			int column = achievement.ordinal() / rowsPerColumn;
			int row = achievement.ordinal() % rowsPerColumn;
			int x = left() + 12 + column * columnWidth;
			int y = top() + CONTENT_TOP + 18 + row * CELL_HEIGHT;
			boolean done = AscendClient.quests.isUnlocked(achievement);
			text(context, (done ? "✔ " : "") + achievement.title(), x, y, done ? GREEN : WHITE);
			int count = Math.min(AscendClient.quests.count(achievement.counter()), achievement.target());
			String detail = achievement.counter().task() + " " + count + "/" + achievement.target();
			text(context, detail, x, y + 10, done ? GRAY : DARK_GRAY);
			if (achievement.xpReward() > 0) {
				int labelX = x + textRenderer.getWidth((done ? "✔ " : "") + achievement.title()) + 4;
				text(context, "+" + achievement.xpReward(), labelX, y, done ? DARK_GRAY : GOLD);
			}
		}
	}
}
