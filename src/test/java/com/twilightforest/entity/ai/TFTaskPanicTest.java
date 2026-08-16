package com.twilightforest.entity.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.twilightforest.entity.ai.harness.FakeWorld;
import com.twilightforest.entity.ai.harness.TestHost;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Random;

class TFTaskPanicTest {

	private static final int FLOOR_Y = 4;
	private static final int STAND_Y = 5;
	private static final float PANIC_SPEED = 0.68F;

	private FakeWorld plain(long seed) {
		FakeWorld w = new FakeWorld(new Random(seed));
		w.fill(-40, FLOOR_Y, -40, 40, FLOOR_Y, 40, Blocks.STONE);
		return w;
	}

	private TestHost hostOn(FakeWorld w, MobPathfinder mob) {
		TestHost host = new TestHost(mob);
		host.weight = pos -> w.world.getBlockId(pos.x(), pos.y() - 1, pos.z()) != 0
			&& w.world.getBlockId(pos.x(), pos.y(), pos.z()) == 0 ? 10.0 : -10.0;
		return host;
	}

	@Test
	void anUnhurtMobDoesNotPanic() {
		FakeWorld w = plain(1L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);

		assertFalse(new TFTaskPanic(host, brain, PANIC_SPEED).shouldExecute());
	}

	@Test
	void beingHurtStartsIt() {
		FakeWorld w = plain(2L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		Player attacker = w.place(w.player(), 2.0, STAND_Y, 0.5);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskPanic panic = new TFTaskPanic(host, brain, PANIC_SPEED);

		panic.alarm(attacker);

		assertTrue(panic.shouldExecute());
		assertSame(attacker, panic.getAttacker());
	}

	@Test
	void environmentalDamagePanicsWithNoAttacker() {
		FakeWorld w = plain(3L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskPanic panic = new TFTaskPanic(host, brain, PANIC_SPEED);

		panic.alarm(null);

		assertTrue(panic.shouldExecute());
		assertNull(panic.getAttacker());
	}

	@Test
	void burningPanicsWithoutAnyAlarm() {
		FakeWorld w = plain(4L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		when(mob.isOnFire()).thenReturn(true);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);

		assertTrue(new TFTaskPanic(host, brain, PANIC_SPEED).shouldExecute());
	}

	@Test
	void theAlarmFadesAfterAHundredTicks() {
		FakeWorld w = plain(5L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		Player attacker = w.place(w.player(), 2.0, STAND_Y, 0.5);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskPanic panic = new TFTaskPanic(host, brain, PANIC_SPEED);

		panic.alarm(attacker);
		for (int i = 0; i < 99; i++) {
			panic.updateTask();
		}
		assertTrue(panic.shouldExecute(), "99 ticks in, it is still frightened");
		assertSame(attacker, panic.getAttacker());

		panic.updateTask();
		assertFalse(panic.shouldExecute(), "100 ticks and the memory is gone");
		assertNull(panic.getAttacker());
	}

	@Test
	void startingLaysARouteAndStoppingReleasesIt() {
		FakeWorld w = plain(2L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		Player attacker = w.place(w.player(), 3.0, STAND_Y, 0.5);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskPanic panic = new TFTaskPanic(host, brain, PANIC_SPEED);

		panic.alarm(attacker);
		assertTrue(panic.shouldExecute());
		panic.startExecuting();
		assertFalse(brain.noPath(), "seed 2 is chosen because its scatter tile is reachable");
		assertTrue(panic.continueExecuting());

		panic.resetTask();
		assertTrue(brain.noPath());
		assertFalse(panic.continueExecuting(), "no route left means the panic is over");
	}

	@Test
	void itClaimsThePanicSpeedAndTheMoveMutex() {
		FakeWorld w = plain(6L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskPanic panic = new TFTaskPanic(host, brain, PANIC_SPEED);

		assertTrue(panic.moveSpeed() == PANIC_SPEED);
		assertTrue((panic.getMutexBits() & TFTask.MUTEX_MOVE) != 0);
		assertTrue((panic.getMutexBits() & TFTask.MUTEX_LOOK) == 0,
			"upstream deliberately leaves the look bit free, so a fleeing animal still looks around");
	}

	@Test
	void panicOutranksTheFleeForTheMovementBit() {
		FakeWorld w = plain(7L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		Player player = w.place(w.player(), 1.4, STAND_Y, 0.5);
		TestHost host = hostOn(w, mob);
		TFBrain brain = new TFBrain(host, 0.45F);

		TFTaskPanic panic = new TFTaskPanic(host, brain, PANIC_SPEED);
		TFTaskAvoidEntity flee = new TFTaskAvoidEntity(host, brain, Player.class, 2.0, 0.41F, 0.72F);
		brain.add(1, panic);
		brain.add(3, flee);

		panic.alarm(player);
		brain.tick(host.vanillaAI());

		assertTrue(brain.isRunning(panic));
		assertFalse(brain.isRunning(flee), "both hold MUTEX_MOVE; only the higher priority may run");
		assertTrue(host.speed == PANIC_SPEED);
	}

	private static double sq(double d) {
		return d * d;
	}
}
