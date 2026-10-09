package com.ascend.client;

import com.ascend.item.AscendItems;
import com.ascend.logic.Achievement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Achievements tab: a card per achievement with an icon, a progress bar and its XP reward. */
public class AchievementsScreen extends AscendScreen {
	private static final int CARD_WIDTH = 128;
	private static final int CARD_HEIGHT = 25;
	private static final int GAP = 2;

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

	private static ItemStack icon(Achievement achievement) {
		return new ItemStack(switch (achievement) {
			case FIRST_BLOOD -> Items.IRON_SWORD;
			case SLAYER -> Items.DIAMOND_SWORD;
			case SHINY -> Items.DIAMOND;
			case MINER -> Items.IRON_PICKAXE;
			case EXPLORER -> Items.COMPASS;
			case WORLD_TRAVELER -> Items.FILLED_MAP;
			case ANGLER -> Items.FISHING_ROD;
			case MERCHANT -> Items.EMERALD;
			case ELITE_HUNTER -> Items.SKELETON_SKULL;
			case SHADOW_SLAYER -> AscendItems.SHADOW_DAGGER;
			case SKILLED -> Items.ENCHANTED_BOOK;
			case HALFWAY -> Items.EXPERIENCE_BOTTLE;
			case ASCENDANT -> Items.NETHER_STAR;
			case COLLECTOR -> Items.CHEST;
		});
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Achievement[] all = Achievement.values();
		int unlocked = AscendClient.quests.unlocked().size();
		int top = top() + CONTENT_TOP;

		text(context, "Achievements", left() + 12, top + 4, GOLD);
		int barX = left() + 90;
		int barWidth = PANEL_WIDTH - 100 - 40;
		context.fill(barX, top + 6, barX + barWidth, top + 10, 0xFF222222);
		context.fill(barX, top + 6, barX + barWidth * unlocked / all.length, top + 10, GOLD);
		rightText(context, unlocked + " / " + all.length, left() + PANEL_WIDTH - 12, top + 4, WHITE);

		int rowsPerColumn = (all.length + 1) / 2;
		for (Achievement achievement : all) {
			int column = achievement.ordinal() / rowsPerColumn;
			int row = achievement.ordinal() % rowsPerColumn;
			int x = left() + 10 + column * (CARD_WIDTH + 4);
			int y = top + 18 + row * (CARD_HEIGHT + GAP);
			drawCard(context, achievement, x, y);
		}
	}

	private void drawCard(DrawContext context, Achievement achievement, int x, int y) {
		boolean done = AscendClient.quests.isUnlocked(achievement);
		int count = Math.min(AscendClient.quests.count(achievement.counter()), achievement.target());
		int accent = done ? GREEN : 0xFF3A3A4A;
		context.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, done ? 0xFF132313 : 0xFF16161F);
		context.fill(x, y, x + 2, y + CARD_HEIGHT, accent);
		context.drawItem(icon(achievement), x + 5, y + 4);
		if (!done) {
			// Dim the icon of locked achievements.
			context.fill(x + 5, y + 4, x + 21, y + 20, 0xA016161F);
		}
		text(context, achievement.title(), x + 24, y + 3, done ? GREEN : WHITE);
		if (achievement.xpReward() > 0) {
			rightText(context, "+" + achievement.xpReward(), x + CARD_WIDTH - 3, y + 3, done ? DARK_GRAY : GOLD);
		}
		int barX = x + 24;
		int barWidth = CARD_WIDTH - 28;
		context.fill(barX, y + 14, barX + barWidth, y + 17, 0xFF222222);
		context.fill(barX, y + 14, barX + barWidth * count / achievement.target(), y + 17, done ? GREEN : AQUA);
		String detail = achievement.counter().task() + " " + count + "/" + achievement.target();
		context.getMatrices().push();
		context.getMatrices().translate(barX, y + 18.5F, 0.0F);
		context.getMatrices().scale(0.6F, 0.6F, 1.0F);
		context.drawTextWithShadow(textRenderer, detail, 0, 0, done ? GRAY : DARK_GRAY);
		context.getMatrices().pop();
	}
}
