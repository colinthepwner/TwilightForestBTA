package com.twilightforest.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.twilightforest.entity.ai.harness.FakeWorld;
import com.twilightforest.entity.ai.harness.TestHost;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.HitResult;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePosc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TFTaskChargeAttackTest {

	private static final int FLOOR_Y = 4;
	private static final int STAND_Y = 5;

	private static final float CHARGE_SPEED = 1.67F;
	private static final float WALK_SPEED = 0.76F;

	private static final int WINDUP_MIN = 15;

	private static final class RecordingCharger implements TFTaskChargeAttack.Charger {

		final TestHost host;

		boolean charging;
		int chargingRaised;
		int chargingCleared;

		final List<Entity> hits = new ArrayList<>();

		boolean attackConnects = true;

		RecordingCharger(TestHost host) {
			this.host = host;
		}

		@Override
		public void setCharging(boolean charging) {
			if (charging && !this.charging) {
				this.chargingRaised++;
			}
			if (!charging && this.charging) {
				this.chargingCleared++;
			}
			this.charging = charging;
		}

		@Override
		public boolean chargeAttack(Entity victim) {
			this.hits.add(victim);
			return this.attackConnects;
		}

		@Override
		public MobPathfinder asMob() {
			return this.host.asMob();
		}

		@Override
		public float tfBlockPathWeight(TilePosc pos) {
			return this.host.tfBlockPathWeight(pos);
		}

		@Override
		public void tfSetSpeed(float speed) {
			this.host.tfSetSpeed(speed);
		}

		@Override
		public void tfSetRandomWalk(boolean enabled) {
			this.host.tfSetRandomWalk(enabled);
		}

		@Override
		public void tfDrive(float yRot, float moveForward, boolean jumping) {
			this.host.tfDrive(yRot, moveForward, jumping);
		}
	}

	private static final class Fixture {
		FakeWorld w;
		MobPathfinder mob;
		Player target;
		TestHost host;
		RecordingCharger charger;
		TFBrain brain;
		TFTaskChargeAttack charge;
	}

	private Fixture arena(long seed, double targetX) {
		Fixture f = new Fixture();
		f.w = new FakeWorld(new Random(seed));
		f.w.fill(-40, FLOOR_Y, -40, 40, FLOOR_Y, 40, Blocks.STONE);

		f.mob = f.w.place(f.w.mob(), 0.5, STAND_Y, 0.5);
		f.mob.onGround = true;

		f.target = f.w.place(f.w.player(), targetX, STAND_Y, 0.5);
		when(f.mob.getTarget()).thenReturn(f.target);

		f.host = new TestHost(f.mob);
		f.charger = new RecordingCharger(f.host);
		f.brain = new TFBrain(f.charger, WALK_SPEED);
		f.charge = new TFTaskChargeAttack(f.charger, f.brain, CHARGE_SPEED);
		f.brain.add(2, f.charge);
		return f;
	}

	private Fixture arena(long seed) {
		return arena(seed, 6.5);
	}

	@Test
	void noTargetMeansNoCharge() {
		Fixture f = arena(1L);
		when(f.mob.getTarget()).thenReturn(null);

		assertFalse(f.charge.shouldExecute());
	}

	@Test
	void aTargetInsideFourBlocksIsTooCloseToChargeAt() {
		Fixture f = arena(2L, 3.0);

		assertFalse(f.charge.shouldExecute(), "2.5 blocks away, squared distance 6.25 < 16");
	}

	@Test
	void aTargetBeyondEightBlocksIsTooFarToChargeAt() {
		Fixture f = arena(3L, 12.5);

		assertFalse(f.charge.shouldExecute(), "12 blocks away, squared distance 144 > 64");
	}

	@Test
	void theChargeBandIsFourToEightBlocks() {
		for (double distance = 1.0; distance <= 12.0; distance += 0.5) {
			Fixture f = arena(4L, 0.5 + distance);
			double distanceSq = distance * distance;
			boolean expected = distanceSq >= 16.0 && distanceSq <= 64.0;

			assertEquals(expected, f.charge.shouldExecute(),
				"at " + distance + " blocks (squared " + distanceSq + ")");
		}
	}

	@Test
	void anAirborneMinotaurDoesNotStartACharge() {
		Fixture f = arena(5L);
		f.mob.onGround = false;

		assertFalse(f.charge.shouldExecute());
	}

	@Test
	void aTargetWithItsBackToAWallIsNotChargeable() {
		Fixture f = arena(6L);

		HitResult blocked = mock(HitResult.class, withSettings().stubOnly().lenient());
		when(f.w.world.checkBlockCollisionBetweenPoints(any3d(), any3d())).thenReturn(blocked);

		assertFalse(f.charge.shouldExecute());
	}

	private static Vector3dc any3d() {
		return org.mockito.ArgumentMatchers.any(Vector3dc.class);
	}

	@Test
	void theChargePointOvershootsTheTargetByTwoAndABitBlocks() {
		Fixture f = arena(7L);

		Vector3d point = TFTaskChargeAttack.findChargePoint(f.mob, f.target, TFTaskChargeAttack.OVERSHOOT);

		assertEquals(6.5 + 2.1, point.x, 1.0e-6);
		assertEquals(0.5, point.z, 1.0e-6);
		assertEquals(f.target.y, point.y, 1.0e-6,
			"the charge is flat: it takes the target's own height, not the mob's");
	}

	@Test
	void theOvershootIsBlocksAndNotAProportion() {
		Fixture f = arena(8L);
		f.w.place(f.target, 4.5, STAND_Y, 4.5);

		Vector3d point = TFTaskChargeAttack.findChargePoint(f.mob, f.target, TFTaskChargeAttack.OVERSHOOT);

		double mobToTarget = Math.hypot(f.target.x - f.mob.x, f.target.z - f.mob.z);
		double mobToPoint = Math.hypot(point.x - f.mob.x, point.z - f.mob.z);

		assertEquals(TFTaskChargeAttack.OVERSHOOT, mobToPoint - mobToTarget, 1.0e-6);
	}

	@Test
	void theWindupHoldsTheMobStillEvenIfItWasAlreadyWalking() {
		Fixture f = arena(9L);
		f.host.tfDrive(0.0F, WALK_SPEED, false);

		f.brain.tick(f.host.vanillaAI());

		assertTrue(f.brain.isRunning(f.charge));
		assertTrue(f.charge.getWindup() > 0, "still winding up on the first tick");
		assertTrue(f.brain.noPath(), "and deliberately has no route yet");
		assertEquals(0.0F, f.host.drivenForward, 1.0e-6F,
			"a winding-up minotaur is motionless, whatever it was doing a tick ago");
	}

	@Test
	void theWindupLastsBetweenFifteenAndFortyFourTicks() {
		for (long seed = 1L; seed <= 12L; seed++) {
			Fixture f = arena(seed);
			assertTrue(f.charge.shouldExecute());
			f.charge.startExecuting();

			int windup = f.charge.getWindup();
			assertTrue(windup >= WINDUP_MIN && windup < WINDUP_MIN + 30,
				"seed " + seed + " gave a windup of " + windup);
		}
	}

	@Test
	void whenTheWindupEndsTheRouteIsLaidAndTheMobRuns() {
		Fixture f = arena(10L);

		int guard = 0;
		while (f.brain.noPath() && guard++ < 200) {
			f.brain.tick(f.host.vanillaAI());
		}

		assertTrue(guard <= 200, "the charge never released");
		assertEquals(0, f.charge.getWindup(), "the windup is spent");
		assertFalse(f.brain.noPath(), "and there is a route to the charge point");
		assertEquals(CHARGE_SPEED, f.host.speed, 1.0e-6F);
		assertTrue(f.host.drivenForward > 0.0F, "and it is finally moving");
	}

	@Test
	void theWindupChurnsTheLegs() {
		Fixture f = arena(11L);
		f.mob.walkAnimSpeed = 0.0F;

		f.brain.tick(f.host.vanillaAI());
		float afterOne = f.mob.walkAnimSpeed;
		f.brain.tick(f.host.vanillaAI());

		assertTrue(afterOne > 0.0F, "the first windup tick already drives the animation");
		assertTrue(f.mob.walkAnimSpeed > afterOne, "and it keeps building");
	}

	@Test
	void theChargingFlagIsRaisedBeforeTheMobMoves() {
		Fixture f = arena(12L);

		f.brain.tick(f.host.vanillaAI());

		assertTrue(f.charger.charging);
		assertTrue(f.brain.noPath(), "still stationary at the moment the flag went up");
	}

	@Test
	void resettingLowersTheFlagAndForgetsTheTarget() {
		Fixture f = arena(13L);

		f.brain.tick(f.host.vanillaAI());
		assertTrue(f.charger.charging);
		assertNotNull(f.charge.getChargeTarget());

		f.charge.resetTask();

		assertFalse(f.charger.charging);
		assertEquals(1, f.charger.chargingCleared);
		assertNull(f.charge.getChargeTarget());
		assertEquals(0, f.charge.getWindup());
		assertTrue(f.brain.noPath());
	}

	@Test
	void itGoresOnceAndOnlyOncePerCharge() {
		Fixture f = arena(14L);

		assertTrue(f.charge.shouldExecute());
		f.charge.startExecuting();

		f.w.place(f.target, f.mob.x, STAND_Y, f.mob.z);
		for (int i = 0; i < 5; i++) {
			f.charge.updateTask();
		}

		assertEquals(1, f.charger.hits.size(), "five ticks of contact, one gore");
		assertSame(f.target, f.charger.hits.get(0));
	}

	@Test
	void aSecondChargeCanGoreAgain() {
		Fixture f = arena(15L);

		assertTrue(f.charge.shouldExecute());
		f.charge.startExecuting();
		f.w.place(f.target, f.mob.x, STAND_Y, f.mob.z);
		f.charge.updateTask();
		f.charge.resetTask();

		f.w.place(f.target, 6.5, STAND_Y, 0.5);
		assertTrue(f.charge.shouldExecute());
		f.charge.startExecuting();
		f.w.place(f.target, f.mob.x, STAND_Y, f.mob.z);
		f.charge.updateTask();

		assertEquals(2, f.charger.hits.size());
	}

	@Test
	void steppingAsideAvoidsTheGore() {
		Fixture f = arena(16L);

		assertTrue(f.charge.shouldExecute());
		f.charge.startExecuting();

		f.w.place(f.target, f.mob.x, STAND_Y, f.mob.z + 2.0);
		for (int i = 0; i < 5; i++) {
			f.charge.updateTask();
		}

		assertTrue(f.charger.hits.isEmpty(), "two blocks off the line is a clean miss");
	}

	@Test
	void theInheritedAiIsSuppressedForTheWholeCharge() {
		Fixture f = arena(17L);

		for (int i = 0; i < 40; i++) {
			f.brain.tick(f.host.vanillaAI());
		}

		assertTrue(f.brain.isRunning(f.charge));
		assertEquals(0, f.host.vanillaRuns,
			"a single run of BTA's own updateAI would repath at the player and cancel the charge");
	}

	@Test
	void itClaimsBothTheMoveAndTheLookBits() {
		Fixture f = arena(18L);

		assertTrue((f.charge.getMutexBits() & TFTask.MUTEX_MOVE) != 0);
		assertTrue((f.charge.getMutexBits() & TFTask.MUTEX_LOOK) != 0);
		assertEquals(CHARGE_SPEED, f.charge.moveSpeed(), 1.0e-6F);
	}

	@Test
	void aChargeSurvivesTheTargetDisappearing() {
		Fixture f = arena(19L);

		f.brain.tick(f.host.vanillaAI());
		assertTrue(f.brain.isRunning(f.charge));

		when(f.mob.getTarget()).thenReturn(null);
		f.w.remove(f.target);

		assertTrue(f.charge.continueExecuting(), "mid-windup, and still going");
	}

	@Test
	void theChargeRouteNeverPassesThroughSolidRock() {
		FakeWorld w = new FakeWorld(new Random(20L));
		w.fill(-40, FLOOR_Y, -40, 40, FLOOR_Y, 40, Blocks.STONE);

		w.fill(3, STAND_Y, -1, 4, STAND_Y + 2, 1, Blocks.STONE);

		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		mob.onGround = true;
		Player target = w.place(w.player(), 6.5, STAND_Y, 0.5);
		when(mob.getTarget()).thenReturn(target);

		TestHost host = new TestHost(mob);
		RecordingCharger charger = new RecordingCharger(host);
		TFBrain brain = new TFBrain(charger, WALK_SPEED);
		TFTaskChargeAttack charge = new TFTaskChargeAttack(charger, brain, CHARGE_SPEED);
		brain.add(2, charge);

		int guard = 0;
		while (brain.noPath() && guard++ < 200) {
			brain.tick(host.vanillaAI());
		}

		Path route = brain.getPath();
		if (route == null) {

			return;
		}

		int steps = 0;
		while (!route.isDone() && steps++ < 200) {
			Vector3dc at = route.getPos(mob);
			int nx = MathHelper.floor(at.x());
			int ny = MathHelper.floor(at.y());
			int nz = MathHelper.floor(at.z());

			assertEquals(0, w.world.getBlockId(nx, ny, nz),
				"route step " + steps + " at " + nx + "," + ny + "," + nz + " is inside a block");
			assertTrue(w.world.getBlockId(nx, ny - 1, nz) != 0,
				"route step " + steps + " at " + nx + "," + ny + "," + nz + " has nothing under it");

			route.next();
		}

		assertTrue(steps < 200, "the route did not terminate");
	}
}
