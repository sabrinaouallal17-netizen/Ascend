package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class XpMultiplierTest {
	@Test
	void lowLevelUsesTheSetting() {
		assertEquals(1.0, XpMultiplier.multiplier(5, true, 1.0, true), 1e-9);
		assertEquals(2.5, XpMultiplier.multiplier(5, false, 2.5, true), 1e-9);
	}

	@Test
	void milestonesStack() {
		assertEquals(1.1, XpMultiplier.multiplier(10, true, 1.0, true), 1e-9);
		assertEquals(1.1, XpMultiplier.multiplier(100, false, 1.0, true), 1e-9);
		assertEquals(2.2, XpMultiplier.multiplier(100, true, 1.0, true), 1e-9);
	}

	@Test
	void disabledMilestonesGiveNoBonus() {
		assertEquals(1.0, XpMultiplier.multiplier(150, true, 1.0, false), 1e-9);
	}

	@Test
	void applyRoundsAndKeepsAtLeastOne() {
		assertEquals(11, XpMultiplier.apply(10, 1.1));
		assertEquals(1, XpMultiplier.apply(1, 0.1));
		assertEquals(0, XpMultiplier.apply(0, 5.0));
	}
}
