package com.ascend.client;

import com.ascend.logic.Milestone;
import java.util.Set;
import net.minecraft.client.gui.DrawContext;

/** Milestones tab: every milestone, its level, its quest progress and its reward. */
public class MilestonesScreen extends AscendScreen {
	public MilestonesScreen() {
		super("Ascend - Milestones");
	}

	@Override
	protected Tab tab() {
		return Tab.MILESTONES;
	}

	@Override
	protected void initContent() {
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Set<Milestone> unlocked = AscendClient.milestones();
		int left = left() + 12;
		int right = left() + PANEL_WIDTH - 12;
		int y = top() + CONTENT_TOP + 4;
		if (!AscendClient.milestonesEnabled) {
			text(context, "Milestones are disabled in this world.", left, y, DARK_GRAY);
			return;
		}
		for (Milestone milestone : Milestone.values()) {
			boolean done = unlocked.contains(milestone);
			boolean levelOk = AscendClient.progress.level() >= milestone.level();
			text(context, (done ? "✔ " : "• ") + milestone.title(), left, y, done ? GREEN : levelOk ? WHITE : GRAY);
			rightText(context, milestone.description(), right, y, done ? GOLD : DARK_GRAY);
			String requirement = "Lv " + milestone.level();
			if (milestone.quest() != null) {
				int count = Math.min(AscendClient.quests.count(milestone.quest()), milestone.questTarget());
				requirement += "  ·  " + milestone.quest().task() + " " + count + "/" + milestone.questTarget();
			}
			y += 9;
			text(context, "   " + requirement, left, y, levelOk ? GRAY : DARK_GRAY);
			y += 9;
		}
	}
}
