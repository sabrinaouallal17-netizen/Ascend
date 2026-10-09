package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MilestoneTest {
	@Test
	void unlockLevels() {
		assertFalse(Milestone.FAST_LEARNER.unlockedAt(9));
		assertTrue(Milestone.FAST_LEARNER.unlockedAt(10));
		assertTrue(Milestone.ASCENDED.unlockedAt(200));
	}

	@Test
	void reachedBetweenIsExclusiveThenInclusive() {
		assertEquals(List.of(Milestone.FAST_LEARNER), Milestone.reachedBetween(9, 10));
		assertTrue(Milestone.reachedBetween(10, 24).isEmpty());
		assertEquals(List.of(Milestone.NIGHT_EYES, Milestone.SKY_WALKER), Milestone.reachedBetween(20, 60));
		assertEquals(5, Milestone.reachedBetween(1, 200).size());
	}
}
