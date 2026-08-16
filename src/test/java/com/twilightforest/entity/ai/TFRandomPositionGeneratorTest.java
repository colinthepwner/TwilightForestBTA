package com.twilightforest.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.twilightforest.entity.ai.harness.FakeWorld;
import com.twilightforest.entity.ai.harness.TestHost;
import net.minecraft.core.entity.MobPathfinder;
import org.junit.jupiter.api.Test;
import org.joml.Vector3d;

import java.util.Random;

class TFRandomPositionGeneratorTest {

	private static final int TRIALS = 500;

	private TestHost host(long seed, double x, double y, double z) {
		FakeWorld w = new FakeWorld(new Random(seed));
		MobPathfinder mob = w.place(w.mob(), x, y, z);
		return new TestHost(mob);
	}

	@Test
	void alwaysFindsSomethingWhenUndirected() {
		TestHost host = host(1L, 0.5, 64.0, 0.5);
		for (int i = 0; i < TRIALS; i++) {
			assertNotNull(TFRandomPositionGenerator.findRandomTarget(host, 5, 4));
		}
	}

	@Test
	void staysInsideUpstreamsBox() {
		TestHost host = host(2L, 10.5, 64.0, -3.5);
		for (int i = 0; i < TRIALS; i++) {
			Vector3d v = TFRandomPositionGenerator.findRandomTarget(host, 5, 4);
			assertNotNull(v);
			assertTrue(Math.abs(v.x - 10.5) <= 5.0 + 0.5, "x out of box: " + v.x);
			assertTrue(Math.abs(v.z - (-3.5)) <= 5.0 + 0.5, "z out of box: " + v.z);
			assertTrue(Math.abs(v.y - 64.0) <= 4.0, "y out of box: " + v.y);
		}
	}

	@Test
	void takesTheBestWeightedCandidateNotARandomOne() {
		TestHost host = host(3L, 0.5, 64.0, 0.5);
		host.weight = pos -> pos.y();

		for (int i = 0; i < 200; i++) {
			Vector3d v = TFRandomPositionGenerator.findRandomTarget(host, 5, 4);
			assertNotNull(v);

			assertTrue(v.y >= 63.0, "a height-loving weight picked low: " + v.y);
		}
	}

	@Test
	void awayFromNeverReturnsSomewhereNearerTheThreat() {
		TestHost host = host(4L, 0.5, 64.0, 0.5);
		Vector3d threat = new Vector3d(2.0, 64.0, 0.0);
		double currentSq = threat.distanceSquared(0.5, 64.0, 0.5);

		int found = 0;
		for (int i = 0; i < TRIALS; i++) {
			Vector3d v = TFRandomPositionGenerator.findRandomTargetAwayFrom(host, 16, 7, threat);
			if (v == null) {
				continue;
			}
			found++;
			assertTrue(threat.distanceSquared(v.x, v.y, v.z) >= currentSq,
				"returned a point nearer the threat: " + v);
		}
		assertTrue(found > TRIALS / 2, "the filter discarded almost everything (" + found + ")");
	}

	@Test
	void theUndirectedVariantSometimesRunsStraightAtTheThreat() {
		TestHost host = host(5L, 0.5, 64.0, 0.5);
		Vector3d threat = new Vector3d(4.0, 64.0, 0.0);
		double currentSq = threat.distanceSquared(0.5, 64.0, 0.5);

		int towards = 0;
		for (int i = 0; i < TRIALS; i++) {
			Vector3d v = TFRandomPositionGenerator.findRandomTarget(host, 5, 4);
			assertNotNull(v);
			if (threat.distanceSquared(v.x, v.y, v.z) < currentSq) {
				towards++;
			}
		}
		assertTrue(towards > 0, "the panic scatter has grown a direction it is not supposed to have");
	}

	@Test
	void returnsNullWhenNothingLeadsAway() {
		TestHost host = host(6L, 0.9, 64.0, 0.5);
		Vector3d threat = new Vector3d(-1000.0, 64.0, 0.5);

		int nulls = 0;
		for (int i = 0; i < 4000; i++) {
			if (TFRandomPositionGenerator.findRandomTargetAwayFrom(host, 1, 0, threat) == null) {
				nulls++;
			}
		}
		assertTrue(nulls > 0, "a cornered mob must sometimes get no answer at all");
	}

	@Test
	void returnsTileCentres() {
		TestHost host = host(7L, 0.5, 64.0, 0.5);
		Vector3d v = TFRandomPositionGenerator.findRandomTarget(host, 5, 4);
		assertNotNull(v);
		assertEquals(0.5, v.x - Math.floor(v.x), 1.0E-9);
		assertEquals(0.5, v.z - Math.floor(v.z), 1.0E-9);
		assertEquals(0.0, v.y - Math.floor(v.y), 1.0E-9, "y is not offset -- it is the tile you stand on");
	}

	@Test
	void theUndirectedVariantNeverReturnsNull() {
		TestHost host = host(8L, 0.9, 64.0, 0.5);
		for (int i = 0; i < TRIALS; i++) {
			assertNotNull(TFRandomPositionGenerator.findRandomTarget(host, 16, 7));
		}
		assertNull(TFRandomPositionGenerator.findRandomTargetAwayFrom(host, 0, 0,
			new Vector3d(-500.0, 64.0, 0.5)), "a zero-sized box away from a distant threat has no answer");
	}
}
