package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MobLevelsTest {
	@Test
	void growsWithDistanceAndDimension() {
		assertEquals(1, MobLevels.levelFor(0, 0, 0));
		assertEquals(11, MobLevels.levelFor(1_500, 0, 0));
		assertEquals(1 + MobLevels.NETHER_BONUS + 2, MobLevels.levelFor(0, MobLevels.NETHER_BONUS, 2));
	}

	@Test
	void isCapped() {
		assertEquals(MobLevels.MAX_LEVEL, MobLevels.levelFor(1_000_000, MobLevels.END_BONUS, 4));
	}

	@Test
	void scaling() {
		assertEquals(1.0, MobLevels.healthMultiplier(1), 1e-9);
		assertEquals(4.96, MobLevels.healthMultiplier(100), 1e-9);
		assertEquals(0.0, MobLevels.damageBonus(1), 1e-9);
		assertTrue(MobLevels.isElite(50));
		assertFalse(MobLevels.isElite(49));
	}
}
