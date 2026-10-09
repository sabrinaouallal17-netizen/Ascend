package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LevelingTest {
	@Test
	void curveGrows() {
		assertEquals(25, Leveling.xpToNextLevel(1));
		for (int level = 1; level < Leveling.MAX_LEVEL; level++) {
			assertTrue(Leveling.xpToNextLevel(level + 1) > Leveling.xpToNextLevel(level));
		}
	}

	@Test
	void lastLevelStaysReasonable() {
		assertEquals(5_000, Leveling.xpToNextLevel(199), 100);
	}
}
