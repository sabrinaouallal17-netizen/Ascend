package com.ascend.client;

import com.ascend.item.AscendItems;
import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Skill;
import com.ascend.logic.SkillBook;
import com.ascend.network.SkillActionPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/** Skills tab: the 3 equipped slots on top, then one card per skill (learn it, or pick its key). */
public class SkillsScreen extends AscendScreen {
	private static final int CARD_WIDTH = 128;
	private static final int CARD_HEIGHT = 84;
	private static final int CARDS_TOP = 50;
	private static final int GAP = 4;

	private final List<ButtonWidget> learnButtons = new ArrayList<>();
	private final List<ButtonWidget[]> slotButtons = new ArrayList<>();

	public SkillsScreen() {
		super("Ascend - Skills");
	}

	@Override
	protected Tab tab() {
		return Tab.SKILLS;
	}

	private static ItemStack icon(Skill skill) {
		return new ItemStack(switch (skill) {
			case DASH -> Items.FEATHER;
			case HEAL_PULSE -> Items.GOLDEN_APPLE;
			case GROUND_SLAM -> Items.MACE;
			case BERSERK -> Items.BLAZE_POWDER;
		});
	}

	private int cardX(Skill skill) {
		return left() + 10 + (skill.ordinal() % 2) * (CARD_WIDTH + GAP);
	}

	private int cardY(Skill skill) {
		return top() + CARDS_TOP + (skill.ordinal() / 2) * (CARD_HEIGHT + GAP);
	}

	@Override
	protected void initContent() {
		learnButtons.clear();
		slotButtons.clear();
		for (Skill skill : Skill.values()) {
			int x = cardX(skill);
			int buttonsY = cardY(skill) + CARD_HEIGHT - 22;
			learnButtons.add(addDrawableChild(ButtonWidget.builder(Text.literal("Learn (" + skill.cost() + " pts)"),
					button -> send(SkillActionPayload.LEARN, skill, 0)).dimensions(x + 6, buttonsY, CARD_WIDTH - 12, 18).build()));
			ButtonWidget[] slots = new ButtonWidget[SkillBook.SLOTS];
			for (int slot = 0; slot < SkillBook.SLOTS; slot++) {
				int slotIndex = slot;
				String key = AscendClient.SKILL_KEYS[slot].getBoundKeyLocalizedText().getString();
				slots[slot] = addDrawableChild(ButtonWidget.builder(Text.literal(key),
						button -> send(SkillActionPayload.BIND, skill, slotIndex))
						.dimensions(x + 6 + slot * 40, buttonsY, 38, 18).build());
			}
			slotButtons.add(slots);
		}
		refreshButtons();
	}

	private static void send(int action, Skill skill, int slot) {
		ClientPlayNetworking.send(new SkillActionPayload(action, skill.ordinal(), slot));
	}

	private void refreshButtons() {
		SkillBook book = AscendClient.skills;
		PlayerProgress progress = AscendClient.progress;
		for (Skill skill : Skill.values()) {
			boolean known = book.knows(skill);
			ButtonWidget learn = learnButtons.get(skill.ordinal());
			learn.visible = !known;
			learn.active = progress.level() >= skill.requiredLevel() && progress.points() >= skill.cost();
			ButtonWidget[] slots = slotButtons.get(skill.ordinal());
			for (int slot = 0; slot < slots.length; slot++) {
				slots[slot].visible = known;
				slots[slot].active = book.inSlot(slot) != skill;
			}
		}
	}

	@Override
	public void tick() {
		refreshButtons();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		SkillBook book = AscendClient.skills;
		PlayerProgress progress = AscendClient.progress;
		int top = top() + CONTENT_TOP;

		text(context, "Skill points: " + progress.points(), left() + 12, top + 6, progress.points() > 0 ? GREEN : GRAY);
		// Equipped slots bar.
		for (int slot = 0; slot < SkillBook.SLOTS; slot++) {
			int x = left() + PANEL_WIDTH - 10 - (SkillBook.SLOTS - slot) * 56;
			Skill skill = book.inSlot(slot);
			context.fill(x, top + 1, x + 54, top + 19, skill == null ? 0xFF1A1A24 : 0xFF14303A);
			drawBorder(context, x, top + 1, 54, 18, skill == null ? 0xFF333344 : AQUA);
			String key = AscendClient.SKILL_KEYS[slot].getBoundKeyLocalizedText().getString();
			if (skill != null) {
				context.drawItem(icon(skill), x + 1, top + 2);
				text(context, key, x + 20, top + 6, WHITE);
				long cd = AscendClient.cooldownLeft(skill);
				if (cd > 0) {
					rightText(context, (cd + 19) / 20 + "s", x + 52, top + 6, GRAY);
				}
			} else {
				text(context, key + " empty", x + 4, top + 6, DARK_GRAY);
			}
		}

		for (Skill skill : Skill.values()) {
			drawCard(context, skill, book.knows(skill), progress);
		}
		int hintY = top() + CARDS_TOP + 2 * (CARD_HEIGHT + GAP) + 1;
		context.drawItem(new ItemStack(AscendItems.SHADOW_DAGGER), left() + 10, hintY - 4);
		text(context, "Shadow Dash: right-click while holding the Shadow Dagger", left() + 30, hintY, 0xFFAA55FF);
	}

	private void drawCard(DrawContext context, Skill skill, boolean known, PlayerProgress progress) {
		int x = cardX(skill);
		int y = cardY(skill);
		boolean levelOk = progress.level() >= skill.requiredLevel();
		int accent = known ? AQUA : levelOk ? GOLD : 0xFF444455;
		context.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, known ? 0xFF102028 : 0xFF16161F);
		drawBorder(context, x, y, CARD_WIDTH, CARD_HEIGHT, accent);
		context.fill(x + 1, y + 1, x + CARD_WIDTH - 1, y + 20, 0x30FFFFFF & accent | 0x20000000);
		context.drawItem(icon(skill), x + 3, y + 2);
		text(context, skill.title(), x + 22, y + 6, known ? AQUA : levelOk ? WHITE : GRAY);
		String status = known ? "Learned" : "Lv " + skill.requiredLevel();
		rightText(context, status, x + CARD_WIDTH - 4, y + 6, known ? GREEN : levelOk ? GOLD : DARK_GRAY);

		text(context, "Cooldown " + skill.cooldownTicks() / 20 + "s", x + 5, y + 24, GRAY);
		int lineY = y + 34;
		for (OrderedText line : textRenderer.wrapLines(Text.literal(skill.description()), CARD_WIDTH - 10)) {
			if (lineY > y + CARD_HEIGHT - 32) {
				break;
			}
			context.drawTextWithShadow(textRenderer, line, x + 5, lineY, DARK_GRAY);
			lineY += 9;
		}
		long cd = known ? AscendClient.cooldownLeft(skill) : 0;
		if (cd > 0) {
			int barWidth = CARD_WIDTH - 12;
			int filled = (int) (barWidth * (1.0 - (double) cd / skill.cooldownTicks()));
			context.fill(x + 6, y + CARD_HEIGHT - 27, x + 6 + barWidth, y + CARD_HEIGHT - 25, 0xFF222222);
			context.fill(x + 6, y + CARD_HEIGHT - 27, x + 6 + filled, y + CARD_HEIGHT - 25, AQUA);
		}
	}

	private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}
}
