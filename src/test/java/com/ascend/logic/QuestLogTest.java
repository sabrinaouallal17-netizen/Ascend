package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class QuestLogTest {
	@Test
	void firstKillUnlocksFirstBlood() {
		QuestLog log = new QuestLog();
		assertEquals(List.of(Achievement.FIRST_BLOOD), log.add(Counter.MOBS_KILLED, 1));
		assertTrue(log.add(Counter.MOBS_KILLED, 1).isEmpty());
		assertEquals(2, log.count(Counter.MOBS_KILLED));
	}

	@Test
	void setAtLeastNeverLowers() {
		QuestLog log = new QuestLog();
		log.setAtLeast(Counter.BIOMES, 12);
		log.setAtLeast(Counter.BIOMES, 5);
		assertEquals(12, log.count(Counter.BIOMES));
		assertTrue(log.isUnlocked(Achievement.EXPLORER));
	}

	@Test
	void achievementsAreCounted() {
		QuestLog log = new QuestLog();
		log.add(Counter.MOBS_KILLED, 1);
		log.add(Counter.DIAMONDS, 1);
		assertEquals(2, log.count(Counter.ACHIEVEMENTS));
	}

	@Test
	void collectorUnlocksAfterTenAchievements() {
		QuestLog log = new QuestLog();
		log.add(Counter.MOBS_KILLED, 500);
		log.add(Counter.DIAMONDS, 1);
		log.add(Counter.ORES, 250);
		log.setAtLeast(Counter.BIOMES, 30);
		log.add(Counter.FISH, 25);
		log.add(Counter.TRADES, 50);
		log.add(Counter.ELITE_KILLS, 1);
		List<Achievement> last = log.add(Counter.SKILLS_LEARNED, 1);
		assertTrue(last.contains(Achievement.COLLECTOR));
		assertEquals(11, log.count(Counter.ACHIEVEMENTS));
	}
}
