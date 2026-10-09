package com.ascend.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Base for the Ascend menus: a panel with Stats / Skills / Milestones / Achievements tabs. */
public abstract class AscendScreen extends Screen {
	protected static final int PANEL_WIDTH = 280;
	protected static final int PANEL_HEIGHT = 240;
	protected static final int CONTENT_TOP = 28;
	protected static final int GOLD = 0xFFFFAA00;
	protected static final int WHITE = 0xFFFFFFFF;
	protected static final int GRAY = 0xFFAAAAAA;
	protected static final int DARK_GRAY = 0xFF666666;
	protected static final int GREEN = 0xFF55FF55;
	protected static final int AQUA = 0xFF55FFFF;

	public enum Tab {
		STATS("Stats"),
		SKILLS("Skills"),
		MILESTONES("Milestones"),
		ACHIEVEMENTS("Achievements");

		private final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	protected AscendScreen(String title) {
		super(Text.literal(title));
	}

	protected abstract Tab tab();

	/** Adds the tab's own widgets. */
	protected abstract void initContent();

	protected int left() {
		return (width - PANEL_WIDTH) / 2;
	}

	protected int top() {
		return (height - PANEL_HEIGHT) / 2;
	}

	@Override
	protected final void init() {
		int tabWidth = (PANEL_WIDTH - 12) / Tab.values().length;
		for (Tab tab : Tab.values()) {
			ButtonWidget button = ButtonWidget.builder(Text.literal(tab.label), b -> open(tab))
					.dimensions(left() + 6 + tab.ordinal() * tabWidth, top() + 5, tabWidth - 2, 18).build();
			button.active = tab != tab();
			addDrawableChild(button);
		}
		initContent();
	}

	public static void open(Tab tab) {
		MinecraftClient.getInstance().setScreen(switch (tab) {
			case STATS -> new CharacterScreen();
			case SKILLS -> new SkillsScreen();
			case MILESTONES -> new MilestonesScreen();
			case ACHIEVEMENTS -> new AchievementsScreen();
		});
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		int left = left();
		int top = top();
		context.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, GOLD);
		context.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF0101018);
	}

	protected void text(DrawContext context, String text, int x, int y, int color) {
		context.drawTextWithShadow(textRenderer, Text.literal(text), x, y, color);
	}

	protected void rightText(DrawContext context, String text, int rightX, int y, int color) {
		context.drawTextWithShadow(textRenderer, Text.literal(text), rightX - textRenderer.getWidth(text), y, color);
	}
}
