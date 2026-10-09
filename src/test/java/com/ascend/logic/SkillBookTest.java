package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SkillBookTest {
	private static PlayerProgress atLevel(int level) {
		PlayerProgress progress = new PlayerProgress();
		progress.setLevel(level);
		return progress;
	}

	@Test
	void learningCostsPointsAndFillsASlot() {
		PlayerProgress progress = atLevel(20);
		SkillBook book = new SkillBook();
		assertEquals(SkillBook.LearnResult.LEARNED, book.learn(Skill.DASH, progress, new QuestLog()));
		assertEquals(19 - Skill.DASH.cost(), progress.points());
		assertEquals(Skill.DASH, book.inSlot(0));
		assertEquals(SkillBook.LearnResult.ALREADY_LEARNED, book.learn(Skill.DASH, progress, new QuestLog()));
	}

	@Test
	void levelAndPointsAreChecked() {
		SkillBook book = new SkillBook();
		assertEquals(SkillBook.LearnResult.LEVEL_TOO_LOW, book.learn(Skill.BERSERK, atLevel(74), new QuestLog()));
		PlayerProgress broke = atLevel(80);
		broke.spend(Stat.STRENGTH, 79);
		assertEquals(SkillBook.LearnResult.NOT_ENOUGH_POINTS, book.learn(Skill.BERSERK, broke, new QuestLog()));
		assertFalse(book.knows(Skill.BERSERK));
	}

	@Test
	void bindingMovesTheSkill() {
		PlayerProgress progress = atLevel(50);
		SkillBook book = new SkillBook();
		book.learn(Skill.DASH, progress, new QuestLog());
		assertTrue(book.bind(Skill.DASH, 2));
		assertNull(book.inSlot(0));
		assertEquals(Skill.DASH, book.inSlot(2));
		assertFalse(book.bind(Skill.BERSERK, 1));
	}

	@Test
	void cooldowns() {
		PlayerProgress progress = atLevel(20);
		SkillBook book = new SkillBook();
		book.learn(Skill.DASH, progress, new QuestLog());
		assertTrue(book.tryUse(Skill.DASH, 100));
		assertFalse(book.tryUse(Skill.DASH, 101));
		assertEquals(Skill.DASH.cooldownTicks() - 1, book.cooldownLeft(Skill.DASH, 101));
		assertTrue(book.tryUse(Skill.DASH, 100 + Skill.DASH.cooldownTicks()));
	}

	@Test
	void savedSlotsMustBeLearned() {
		SkillBook book = new SkillBook(java.util.Set.of(), new Skill[] {Skill.DASH, null, null});
		assertNull(book.inSlot(0));
	}

	@Test
	void questSkillsNeedTheirQuest() {
		PlayerProgress progress = atLevel(100);
		SkillBook book = new SkillBook();
		QuestLog log = new QuestLog();
		assertEquals(SkillBook.LearnResult.QUEST_INCOMPLETE, book.learn(Skill.FROST_NOVA, progress, log));
		log.add(Counter.FROST_KILLS, 1);
		assertEquals(SkillBook.LearnResult.LEARNED, book.learn(Skill.FROST_NOVA, progress, log));
		assertEquals(SkillBook.LearnResult.QUEST_INCOMPLETE, book.learn(Skill.SHADOW_STEP, progress, log));
	}
}
