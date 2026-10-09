package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class XpRewardsTest {
	@Test
	void mobXpIsHalfMaxHealth() {
		assertEquals(10, XpRewards.forMob(20.0F));
		assertEquals(150, XpRewards.forMob(300.0F));
	}

	@Test
	void tinyMobsGiveAtLeastOne() {
		assertEquals(1, XpRewards.forMob(0.5F));
		assertEquals(2, XpRewards.forMob(3.0F));
	}
}
