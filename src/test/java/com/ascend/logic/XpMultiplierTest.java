package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class XpMultiplierTest {
	@Test
	void noMilestonesUsesTheSetting() {
		assertEquals(1.0, XpMultiplier.multiplier(Set.of(), true, 1.0), 1e-9);
		assertEquals(2.5, XpMultiplier.multiplier(Set.of(), false, 2.5), 1e-9);
	}

	@Test
	void milestonesStack() {
		Set<Milestone> unlocked = EnumSet.of(Milestone.FAST_LEARNER, Milestone.NIGHT_OWL);
		assertEquals(1.1, XpMultiplier.multiplier(unlocked, false, 1.0), 1e-9);
		assertEquals(2.2, XpMultiplier.multiplier(unlocked, true, 1.0), 1e-9);
		unlocked.add(Milestone.VETERAN);
		assertEquals(1.21, XpMultiplier.multiplier(unlocked, false, 1.0), 1e-9);
	}

	@Test
	void applyRoundsAndKeepsAtLeastOne() {
		assertEquals(11, XpMultiplier.apply(10, 1.1));
		assertEquals(1, XpMultiplier.apply(1, 0.1));
		assertEquals(0, XpMultiplier.apply(0, 5.0));
	}
}
