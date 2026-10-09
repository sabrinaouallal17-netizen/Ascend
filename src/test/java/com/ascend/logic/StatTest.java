package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatTest {
	@Test
	void maxBonuses() {
		assertEquals(75, Stat.CAP);
		assertEquals(7.5, Stat.STRENGTH.bonus(Stat.CAP), 1e-9);
		assertEquals(30.0, Stat.VITALITY.bonus(Stat.CAP), 1e-9);
		assertEquals(0.3, Stat.AGILITY.bonus(Stat.CAP), 1e-9);
		assertEquals(0.75, Stat.MINING.bonus(Stat.CAP), 1e-9);
	}

	@Test
	void bonusNeverExceedsCap() {
		assertEquals(Stat.VITALITY.bonus(Stat.CAP), Stat.VITALITY.bonus(Stat.CAP + 10), 1e-9);
	}
}
