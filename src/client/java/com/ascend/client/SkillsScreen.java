package com.ascend.client;

import com.ascend.logic.PlayerProgress;
import com.ascend.logic.Skill;
import com.ascend.logic.SkillBook;
import com.ascend.network.SkillActionPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Skills tab: learn skills with points and put them in the 3 skill slots. */
public class SkillsScreen extends AscendScreen {
	private static final int ROW_HEIGHT = 40;
	private static final int ROWS_TOP = 46;

	private final List<ButtonWidget> learnButtons = new ArrayList<>();
	private final List<ButtonWidget[]> slotButtons = new ArrayList<>();

	public SkillsScreen() {
		super("Ascend - Skills");
	}

	@Override
	protected Tab tab() {
		return Tab.SKILLS;
	}

	@Override
	protected void initContent() {
		learnButtons.clear();
		slotButtons.clear();
		for (Skill skill : Skill.values()) {
			int y = top() + ROWS_TOP + skill.ordinal() * ROW_HEIGHT;
			int right = left() + PANEL_WIDTH - 10;
			learnButtons.add(addDrawableChild(ButtonWidget.builder(Text.literal("Learn"),
					button -> send(SkillActionPayload.LEARN, skill, 0)).dimensions(right - 48, y, 48, 18).build()));
			ButtonWidget[] slots = new ButtonWidget[SkillBook.SLOTS];
			for (int slot = 0; slot < SkillBook.SLOTS; slot++) {
				int slotIndex = slot;
				String key = AscendClient.SKILL_KEYS[slot].getBoundKeyLocalizedText().getString();
				slots[slot] = addDrawableChild(ButtonWidget.builder(Text.literal(key),
						button -> send(SkillActionPayload.BIND, skill, slotIndex))
						.dimensions(right - 66 + slot * 22, y, 20, 18).build());
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
		int left = left() + 12;
		text(context, "Skill points: " + progress.points(), left, top() + CONTENT_TOP + 4, progress.points() > 0 ? GREEN : GRAY);
		rightText(context, "Learned skills: pick a key", left() + PANEL_WIDTH - 10, top() + CONTENT_TOP + 4, DARK_GRAY);

		for (Skill skill : Skill.values()) {
			int y = top() + ROWS_TOP + skill.ordinal() * ROW_HEIGHT;
			boolean known = book.knows(skill);
			boolean levelOk = progress.level() >= skill.requiredLevel();
			text(context, skill.title(), left, y, known ? AQUA : levelOk ? WHITE : DARK_GRAY);
			String info = known
					? "Cooldown " + skill.cooldownTicks() / 20 + "s" + cooldownText(skill)
					: "Lv " + skill.requiredLevel() + " · " + skill.cost() + " points · " + skill.cooldownTicks() / 20 + "s";
			text(context, info, left, y + 10, levelOk ? GRAY : DARK_GRAY);
			text(context, skill.description(), left, y + 20, DARK_GRAY);
		}

		int y = top() + ROWS_TOP + Skill.values().length * ROW_HEIGHT + 2;
		text(context, "Shadow Dash: hold the Shadow Dagger and right-click", left, y, 0xFFAA55FF);
	}

	private static String cooldownText(Skill skill) {
		long left = AscendClient.cooldownLeft(skill);
		return left > 0 ? " (ready in " + (left + 19) / 20 + "s)" : " (ready)";
	}
}
