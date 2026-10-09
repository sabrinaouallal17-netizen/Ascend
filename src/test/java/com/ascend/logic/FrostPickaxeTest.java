package com.ascend.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FrostPickaxeTest {
	@Test
	void levels() {
		assertEquals(0, FrostPickaxe.levelFor(0));
		assertEquals(0, FrostPickaxe.levelFor(4_999));
		assertEquals(1, FrostPickaxe.levelFor(5_000));
		assertEquals(2, FrostPickaxe.levelFor(25_000));
		assertEquals(3, FrostPickaxe.levelFor(100_000));
		assertEquals(3, FrostPickaxe.levelFor(Long.MAX_VALUE));
	}

	@Test
	void areas() {
		assertEquals(8, FrostPickaxe.areaFor(0));
		assertEquals(10, FrostPickaxe.areaFor(1));
		assertEquals(12, FrostPickaxe.areaFor(2));
		assertEquals(16, FrostPickaxe.areaFor(3));
	}

	@Test
	void offsetsCoverTheArea() {
		int[] eight = FrostPickaxe.offsets(8);
		assertArrayEquals(new int[] {-3, 4}, eight);
		assertEquals(8, eight[1] - eight[0] + 1);
		int[] sixteen = FrostPickaxe.offsets(16);
		assertEquals(16, sixteen[1] - sixteen[0] + 1);
	}

	@Test
	void nextThresholds() {
		assertEquals(5_000, FrostPickaxe.nextThreshold(0));
		assertEquals(-1, FrostPickaxe.nextThreshold(3));
	}
}
