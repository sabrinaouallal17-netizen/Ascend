package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatTest {
	@Test
	void maxBonuses() {
		assertEquals(5.0, Stat.STRENGTH.bonus(Stat.CAP), 1e-9);
		assertEquals(20.0, Stat.VITALITY.bonus(Stat.CAP), 1e-9);
		assertEquals(0.2, Stat.AGILITY.bonus(Stat.CAP), 1e-9);
		assertEquals(0.5, Stat.MINING.bonus(Stat.CAP), 1e-9);
	}

	@Test
	void bonusNeverExceedsCap() {
		assertEquals(Stat.VITALITY.bonus(Stat.CAP), Stat.VITALITY.bonus(Stat.CAP + 10), 1e-9);
	}
}
