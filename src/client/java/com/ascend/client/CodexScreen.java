package com.ascend.client;

import com.ascend.item.AscendItems;
import com.ascend.logic.BossLoot;
import com.ascend.logic.Counter;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Boss Codex tab: one page per Ascend boss with its stats, abilities, how to summon it and its drops. */
public class CodexScreen extends AscendScreen {
	private record Ability(String name, String description) {
	}

	private record Boss(String name, Item icon, int color, int health, String summon, List<Ability> abilities,
			List<BossLoot.Drop> drops, Counter kills) {
	}

	private static final List<Boss> BOSSES = List.of(
			new Boss("Shadow Assassin", AscendItems.SHADOW_SIGIL, 0xFFAA55FF, 300,
					"Shadow Sigil: 3 Sigil Pieces (Shadow Towers), 5 Shadow Ingots, 1 Eye of Ender",
					List.of(new Ability("Shadow Step", "teleports behind you"),
							new Ability("Shadow Strike", "charges, then dashes through you"),
							new Ability("Shadow Clones", "summons 2 clones"),
							new Ability("Shadow Storm", "enraged: spins and pulls everyone in")),
					BossLoot.SHADOW_ASSASSIN, Counter.SHADOW_KILLS),
			new Boss("Frost Warden", AscendItems.FROST_SIGIL, 0xFF7FD8FF, 450,
					"Frost Sigil: 1 Shadow Ingot, 4 Diamonds, 4 Blue Ice",
					List.of(new Ability("Ice Spikes", "spikes erupt under you and launch you"),
							new Ability("Frost Nova", "freezes and pushes back everyone close"),
							new Ability("Glacial Leap", "jumps on you and shatters the ground"),
							new Ability("Blizzard", "enraged: slows and freezes all around")),
					BossLoot.FROST_WARDEN, Counter.FROST_KILLS));

	private static int page;

	public CodexScreen() {
		super("Ascend - Boss Codex");
	}

	@Override
	protected Tab tab() {
		return Tab.CODEX;
	}

	@Override
	protected void initContent() {
		int y = top() + PANEL_HEIGHT - 24;
		addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> turn(-1))
				.dimensions(left() + PANEL_WIDTH / 2 - 50, y, 20, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> turn(1))
				.dimensions(left() + PANEL_WIDTH / 2 + 30, y, 20, 18).build());
	}

	private static void turn(int direction) {
		page = Math.floorMod(page + direction, BOSSES.size());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Boss boss = BOSSES.get(page);
		int left = left() + 12;
		int right = left() + PANEL_WIDTH - 12;
		int y = top() + CONTENT_TOP + 4;
		int kills = AscendClient.quests.count(boss.kills());
		boolean met = kills > 0;

		// Header: icon, name, health and kills.
		context.fill(left - 2, y - 2, right + 2, y + 22, 0xFF181822);
		drawBorder(context, left - 2, y - 2, right - left + 4, 24, boss.color());
		context.drawItem(new ItemStack(boss.icon()), left + 2, y + 2);
		text(context, boss.name(), left + 22, y + 2, boss.color());
		text(context, boss.health() + " HP", left + 22, y + 11, GRAY);
		rightText(context, met ? "Slain x" + kills : "Not slain yet", right - 2, y + 7, met ? GREEN : DARK_GRAY);
		y += 28;

		text(context, "Abilities", left, y, GOLD);
		y += 11;
		for (Ability ability : boss.abilities()) {
			text(context, ability.name(), left + 4, y, WHITE);
			text(context, ability.description(), left + 84, y, GRAY);
			y += 10;
		}
		y += 4;

		text(context, "Summon", left, y, GOLD);
		y += 11;
		for (OrderedText line : textRenderer.wrapLines(Text.literal(boss.summon()), right - left - 4)) {
			context.drawTextWithShadow(textRenderer, line, left + 4, y, GRAY);
			y += 10;
		}
		y += 4;

		text(context, "Drops", left, y, GOLD);
		y += 11;
		int columnWidth = (right - left) / 2;
		List<BossLoot.Drop> drops = boss.drops();
		for (int i = 0; i < drops.size(); i++) {
			BossLoot.Drop drop = drops.get(i);
			int x = left + (i % 2) * columnWidth;
			int rowY = y + (i / 2) * 12;
			Item item = Registries.ITEM.get(Identifier.of("ascend", drop.item()));
			context.getMatrices().push();
			context.getMatrices().translate(x, rowY - 2, 0.0F);
			context.getMatrices().scale(0.7F, 0.7F, 1.0F);
			context.drawItem(new ItemStack(item), 0, 0);
			context.getMatrices().pop();
			text(context, drop.name(), x + 14, rowY, WHITE);
			rightText(context, BossLoot.percent(drop), x + columnWidth - 6, rowY, drop.chance() >= 1.0 ? GREEN : AQUA);
		}

		centeredText(context, (page + 1) + " / " + BOSSES.size(), left() + PANEL_WIDTH / 2, top() + PANEL_HEIGHT - 19, WHITE);
	}
}
