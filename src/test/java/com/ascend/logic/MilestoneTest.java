package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MilestoneTest {
	@Test
	void levelOnlyMilestones() {
		QuestLog log = new QuestLog();
		assertFalse(Milestone.FAST_LEARNER.isUnlocked(9, log));
		assertTrue(Milestone.FAST_LEARNER.isUnlocked(10, log));
	}

	@Test
	void questMilestonesNeedLevelAndQuest() {
		QuestLog log = new QuestLog();
		assertFalse(Milestone.HUNTER.isUnlocked(20, log));
		log.add(Counter.MOBS_KILLED, 100);
		assertTrue(Milestone.HUNTER.isUnlocked(20, log));
		assertFalse(Milestone.HUNTER.isUnlocked(14, log));
	}

	@Test
	void sortedByLevel() {
		Milestone[] all = Milestone.values();
		for (int i = 1; i < all.length; i++) {
			assertTrue(all[i].level() > all[i - 1].level());
		}
		assertEquals(11, all.length);
	}

	@Test
	void unlockedSet() {
		QuestLog log = new QuestLog();
		assertEquals(3, Milestone.unlocked(55, log).size());
	}
}
