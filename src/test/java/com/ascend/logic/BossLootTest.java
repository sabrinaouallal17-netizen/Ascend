package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BossLootTest {
	@Test
	void guaranteedDropAlwaysDrops() {
		List<BossLoot.Drop> drops = BossLoot.roll(BossLoot.SHADOW_ASSASSIN, () -> 0.999);
		assertEquals(1, drops.size());
		assertEquals("legendary_exp_bottle", drops.get(0).item());
	}

	@Test
	void luckyRollDropsEverything() {
		assertEquals(BossLoot.SHADOW_ASSASSIN.size(), BossLoot.roll(BossLoot.SHADOW_ASSASSIN, () -> 0.0).size());
	}

	@Test
	void ratesMatchOverManyRolls() {
		Random random = new Random(1);
		int daggers = 0;
		for (int i = 0; i < 10_000; i++) {
			if (BossLoot.roll(BossLoot.SHADOW_ASSASSIN, random::nextDouble).stream()
					.anyMatch(drop -> drop.item().equals("shadow_dagger"))) {
				daggers++;
			}
		}
		assertTrue(daggers > 2_300 && daggers < 2_700, "daggers=" + daggers);
	}

	@Test
	void percentText() {
		assertEquals("25%", BossLoot.percent(BossLoot.SHADOW_ASSASSIN.get(0)));
	}
}
