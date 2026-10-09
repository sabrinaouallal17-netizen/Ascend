package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerProgressTest {
	@Test
	void startsAtLevelOne() {
		PlayerProgress progress = new PlayerProgress();
		assertEquals(1, progress.level());
		assertEquals(0, progress.xp());
		assertEquals(0, progress.points());
	}

	@Test
	void levelUpGivesAPoint() {
		PlayerProgress progress = new PlayerProgress();
		assertEquals(0, progress.addXp(Leveling.xpToNextLevel(1) - 1));
		assertEquals(1, progress.addXp(1));
		assertEquals(2, progress.level());
		assertEquals(0, progress.xp());
		assertEquals(1, progress.points());
	}

	@Test
	void bigXpGainsSeveralLevels() {
		PlayerProgress progress = new PlayerProgress();
		int needed = Leveling.xpToNextLevel(1) + Leveling.xpToNextLevel(2) + Leveling.xpToNextLevel(3);
		assertEquals(3, progress.addXp(needed + 5));
		assertEquals(4, progress.level());
		assertEquals(5, progress.xp());
		assertEquals(3, progress.points());
	}

	@Test
	void stopsAtMaxLevel() {
		PlayerProgress progress = new PlayerProgress();
		progress.addXp(Integer.MAX_VALUE / 2);
		assertEquals(Leveling.MAX_LEVEL, progress.level());
		assertEquals(Leveling.MAX_LEVEL - 1, progress.points());
		assertEquals(0, progress.xp());
		assertEquals(0, progress.addXp(1_000));
	}

	@Test
	void spendingNeedsPoints() {
		PlayerProgress progress = new PlayerProgress();
		assertFalse(progress.spend(Stat.STRENGTH));
		progress.addXp(Leveling.xpToNextLevel(1));
		assertTrue(progress.spend(Stat.STRENGTH));
		assertEquals(1, progress.stat(Stat.STRENGTH));
		assertEquals(0, progress.points());
	}

	@Test
	void statsAreCapped() {
		int[] stats = new int[Stat.values().length];
		stats[Stat.VITALITY.ordinal()] = Stat.CAP;
		PlayerProgress progress = new PlayerProgress(60, 0, 5, stats);
		assertFalse(progress.spend(Stat.VITALITY));
		assertTrue(progress.spend(Stat.AGILITY));
		assertEquals(4, progress.points());
	}

	@Test
	void savedValuesAreSanitised() {
		PlayerProgress progress = new PlayerProgress(999, -5, -1, new int[] {1, 2});
		assertEquals(Leveling.MAX_LEVEL, progress.level());
		assertEquals(0, progress.xp());
		assertEquals(0, progress.points());
		assertEquals(4, progress.stats().length);
	}

	@Test
	void setLevelGivesOnePointPerLevel() {
		PlayerProgress progress = new PlayerProgress();
		progress.setLevel(50);
		assertEquals(50, progress.level());
		assertEquals(0, progress.xp());
		assertEquals(49, progress.points());
	}

	@Test
	void setLevelKeepsSpentPointsWhenPossible() {
		PlayerProgress progress = new PlayerProgress();
		progress.setLevel(11);
		progress.spend(Stat.STRENGTH);
		progress.spend(Stat.STRENGTH);
		progress.setLevel(21);
		assertEquals(2, progress.stat(Stat.STRENGTH));
		assertEquals(18, progress.points());
	}

	@Test
	void setLevelLowerRefundsStats() {
		PlayerProgress progress = new PlayerProgress();
		progress.setLevel(11);
		for (int i = 0; i < 10; i++) {
			progress.spend(Stat.AGILITY);
		}
		progress.setLevel(5);
		assertEquals(0, progress.stat(Stat.AGILITY));
		assertEquals(4, progress.points());
	}

	@Test
	void setLevelIsClamped() {
		PlayerProgress progress = new PlayerProgress();
		progress.setLevel(500);
		assertEquals(Leveling.MAX_LEVEL, progress.level());
		progress.setLevel(-3);
		assertEquals(1, progress.level());
		assertEquals(0, progress.points());
	}

	@Test
	void respecRefundsEverything() {
		PlayerProgress progress = new PlayerProgress();
		progress.setLevel(21);
		for (int i = 0; i < 5; i++) {
			progress.spend(Stat.MINING);
		}
		progress.spend(Stat.VITALITY);
		assertEquals(6, progress.spentPoints());
		progress.respec();
		assertEquals(0, progress.spentPoints());
		assertEquals(20, progress.points());
	}

	@Test
	void spendManyAtOnce() {
		PlayerProgress progress = new PlayerProgress();
		progress.setLevel(11);
		assertEquals(10, progress.spend(Stat.AGILITY, 25));
		assertEquals(10, progress.stat(Stat.AGILITY));
		assertEquals(0, progress.points());
	}
}
