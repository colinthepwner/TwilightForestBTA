package com.twilightforest.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.twilightforest.client.render.TFTowerRenderers;
import com.twilightforest.world.chunk.TFWorldConstants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class TowerMobLogicTest {

	@Test
	void deadOnStareAlwaysAggros() {
		for (double distance : new double[]{1.0, 4.0, 8.0, 16.0, 64.0}) {
			assertTrue(MobTFMiniGhast.isLookingAt(1.0, distance),
				"a dead-on look should aggro at " + distance + " blocks");
		}
	}

	@Test
	void theConeTightensWithDistance() {

		double dot = 0.9938;
		assertTrue(MobTFMiniGhast.isLookingAt(dot, 4.0), "should aggro a ghast four blocks away");
		assertFalse(MobTFMiniGhast.isLookingAt(dot, 32.0),
			"the same glance must NOT aggro one thirty-two blocks away");
	}

	@Test
	void lookingAwayNeverAggros() {
		assertFalse(MobTFMiniGhast.isLookingAt(0.0, 4.0), "ninety degrees off");
		assertFalse(MobTFMiniGhast.isLookingAt(-1.0, 4.0), "facing directly away");
	}

	@Test
	void theSpiralWalksOutwardsAlternating() {
		int[] expected = {0, 1, -1, 2, -2, 3, -3, 4, -4, 5};
		int v = 0;
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], v, "step " + i);
			v = MobTFTowerTermite.nextOffset(v);
		}
	}

	@Test
	void theSpiralCoversTheRangeOnceAndStops() {
		List<Integer> visited = new ArrayList<>();
		int v = 0;
		int guard = 0;
		while (v <= 10 && v >= -10) {
			visited.add(v);
			v = MobTFTowerTermite.nextOffset(v);
			if (++guard > 1000) {
				throw new AssertionError("the spiral never left ±10 — the loop cannot terminate");
			}
		}
		assertEquals(21, visited.size(), "±10 inclusive is twenty-one cells");
		assertEquals(21, visited.stream().distinct().count(), "no cell should be visited twice");
		assertEquals(-10, visited.stream().mapToInt(Integer::intValue).min().orElseThrow());
		assertEquals(10, visited.stream().mapToInt(Integer::intValue).max().orElseThrow());
	}

	@Test
	void aCalmGhastIsState0() {
		assertEquals(MobTFTowerGhast.STATE_CALM, MobTFTowerGhast.aggroStateFor(0, false));
		assertEquals(MobTFTowerGhast.STATE_CALM, MobTFTowerGhast.aggroStateFor(-40, false));
	}

	@Test
	void staringIsState1() {
		assertEquals(MobTFTowerGhast.STATE_STARING, MobTFTowerGhast.aggroStateFor(0, true));
		assertEquals(MobTFTowerGhast.STATE_STARING, MobTFTowerGhast.aggroStateFor(10, true));
	}

	@Test
	void aChargingGhastIsState2FromEleven() {
		assertEquals(MobTFTowerGhast.STATE_STARING, MobTFTowerGhast.aggroStateFor(10, true));
		assertEquals(MobTFTowerGhast.STATE_FIRING, MobTFTowerGhast.aggroStateFor(11, true));
		assertEquals(MobTFTowerGhast.STATE_FIRING, MobTFTowerGhast.aggroStateFor(20, true));
		assertEquals(MobTFTowerGhast.STATE_FIRING, MobTFTowerGhast.aggroStateFor(20, false),
			"the charge counter alone decides the mouth, stare or no stare");
	}

	private static final int TOWER_X = 1000;
	private static final int TOWER_Z = -2000;
	private static final float RADIUS = 64.0F;

	@Test
	void directlyOverTheTowerIsHome() {
		assertTrue(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X, TFWorldConstants.WORLD_HEIGHT - 8, TOWER_Z));
	}

	@Test
	void theRadiusIsHorizontalOnly() {
		int low = TFWorldConstants.SEA_LEVEL * 2 + 1;
		int high = TFWorldConstants.WORLD_HEIGHT - 1;

		assertTrue(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X + 63, low, TOWER_Z), "63 out and low");
		assertTrue(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X + 63, high, TOWER_Z), "63 out and high");
	}

	@Test
	void outsideTheRadiusIsNotHome() {
		assertFalse(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X + 65, 100, TOWER_Z));
		assertFalse(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X, 100, TOWER_Z - 65));
	}

	@Test
	void belowTheHeightBandIsNotHome() {
		assertFalse(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X, TFWorldConstants.SEA_LEVEL * 2, TOWER_Z), "exactly on the floor is out");
		assertFalse(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X, TFWorldConstants.SEA_LEVEL, TOWER_Z), "sea level is well out");
		assertFalse(MobTFTowerGhast.withinHomeCylinder(TOWER_X, TOWER_Z, RADIUS,
			TOWER_X, TFWorldConstants.WORLD_HEIGHT, TOWER_Z), "the ceiling itself is out");
	}

	@Test
	void aGhastWithNoHomeIsNeverOutOfBounds() {
		assertTrue(MobTFTowerGhast.withinHomeCylinder(0, 0, -1.0F, 99999, 3, -99999));
	}

	@Test
	void theWaveSpansMinusOneToOne() {
		float period = 13.0F;
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		for (int i = 0; i <= 1300; i++) {
			float v = TFTowerRenderers.Golem.triangleWave(i * 0.01F, period);
			min = Math.min(min, v);
			max = Math.max(max, v);
		}
		assertEquals(-1.0F, min, 1.0E-4F);
		assertEquals(1.0F, max, 1.0E-4F);
	}

	@Test
	void theWaveIsPeriodic() {
		float period = 10.0F;
		for (float t = 0.0F; t < period; t += 0.37F) {
			assertEquals(TFTowerRenderers.Golem.triangleWave(t, period),
				TFTowerRenderers.Golem.triangleWave(t + period, period), 1.0E-4F);
		}
	}

	@Test
	void theWaveIsPiecewiseLinearAndNotASine() {
		float period = 13.0F;

		float a = TFTowerRenderers.Golem.triangleWave(3.0F, period);
		float b = TFTowerRenderers.Golem.triangleWave(4.0F, period);
		float c = TFTowerRenderers.Golem.triangleWave(5.0F, period);
		assertEquals(0.0F, (c - b) - (b - a), 1.0E-5F, "a straight run must have zero curvature");

		double sa = Math.sin(3.0 / period * 2 * Math.PI);
		double sb = Math.sin(4.0 / period * 2 * Math.PI);
		double sc = Math.sin(5.0 / period * 2 * Math.PI);
		assertTrue(Math.abs((sc - sb) - (sb - sa)) > 0.05,
			"sanity: a sine over the same span really does curve");
	}
}
