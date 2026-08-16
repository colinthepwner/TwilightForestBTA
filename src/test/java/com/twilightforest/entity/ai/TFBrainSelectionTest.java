package com.twilightforest.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.twilightforest.entity.ai.harness.FakeWorld;
import com.twilightforest.entity.ai.harness.ScriptedTask;
import com.twilightforest.entity.ai.harness.TestHost;
import org.junit.jupiter.api.Test;

import java.util.List;

class TFBrainSelectionTest {

	private TestHost host() {
		FakeWorld w = new FakeWorld();
		return new TestHost(w.place(w.mob(), 0.5, 4.0, 0.5));
	}

	@Test
	void higherPriorityTakesTheMutexBit() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask high = new ScriptedTask("high", TFTask.MUTEX_MOVE);
		ScriptedTask low = new ScriptedTask("low", TFTask.MUTEX_MOVE);
		brain.add(1, high);
		brain.add(5, low);

		brain.tick(host.vanillaAI());

		assertTrue(brain.isRunning(high));
		assertFalse(brain.isRunning(low), "two MUTEX_MOVE tasks must not run together");
		assertEquals(List.of("start", "update"), high.events());
		assertEquals(List.of(), low.events());
	}

	@Test
	void disjointMutexBitsCoexist() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask move = new ScriptedTask("move", TFTask.MUTEX_MOVE);
		ScriptedTask look = new ScriptedTask("look", TFTask.MUTEX_LOOK);
		brain.add(1, move);
		brain.add(5, look);

		brain.tick(host.vanillaAI());

		assertTrue(brain.isRunning(move));
		assertTrue(brain.isRunning(look));
	}

	@Test
	void uninterruptibleTaskBlocksEvenDisjointHigherPriority() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask stubborn = new ScriptedTask("stubborn", TFTask.MUTEX_MOVE);
		stubborn.interruptible = false;
		ScriptedTask look = new ScriptedTask("look", TFTask.MUTEX_LOOK);
		look.should = false;

		brain.add(5, stubborn);
		brain.add(1, look);

		brain.tick(host.vanillaAI());
		assertTrue(brain.isRunning(stubborn));

		look.should = true;
		look.clear();
		for (int i = 0; i < TFBrain.SELECT_INTERVAL; i++) {
			brain.tick(host.vanillaAI());
		}
		assertFalse(brain.isRunning(look));
	}

	@Test
	void selectionIsStaggeredButContinuationIsNot() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask task = new ScriptedTask("t", TFTask.MUTEX_MOVE);
		task.should = false;
		brain.add(1, task);

		brain.tick(host.vanillaAI());
		brain.tick(host.vanillaAI());
		brain.tick(host.vanillaAI());
		assertEquals(1, task.log.stream().filter(s -> s.startsWith("should=")).count());

		task.should = true;
		task.clear();
		brain.tick(host.vanillaAI());
		assertTrue(brain.isRunning(task));
		task.clear();
		brain.tick(host.vanillaAI());
		brain.tick(host.vanillaAI());
		assertEquals(2, task.log.stream().filter(s -> s.startsWith("continue=")).count());
	}

	@Test
	void continuationFailureStopsImmediatelyAndResetsExactlyOnce() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask task = new ScriptedTask("t", TFTask.MUTEX_MOVE);
		brain.add(1, task);

		brain.tick(host.vanillaAI());
		assertTrue(brain.isRunning(task));

		task.canContinue = false;
		task.clear();
		brain.tick(host.vanillaAI());
		assertFalse(brain.isRunning(task));
		assertEquals(List.of("reset"), task.events());

		task.clear();
		brain.tick(host.vanillaAI());
		brain.tick(host.vanillaAI());
		assertFalse(task.events().contains("reset"), "reset must not repeat on a stopped task");
	}

	@Test
	void speedIsClaimedByPriorityAndRestoredOnStop() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.45F);
		ScriptedTask fast = new ScriptedTask("fast", TFTask.MUTEX_LOOK);
		fast.speed = 1.2F;
		ScriptedTask slow = new ScriptedTask("slow", TFTask.MUTEX_JUMP);
		slow.speed = 0.1F;
		brain.add(1, fast);
		brain.add(5, slow);

		brain.tick(host.vanillaAI());
		assertEquals(1.2F, host.speed, 1.0E-6F, "the highest-priority claimant wins");

		fast.canContinue = false;
		brain.tick(host.vanillaAI());
		assertEquals(0.1F, host.speed, 1.0E-6F, "the survivor's claim takes over");

		slow.canContinue = false;
		brain.tick(host.vanillaAI());
		assertEquals(0.45F, host.speed, 1.0E-6F, "base speed comes back with nothing running");
	}

	@Test
	void taskWithoutSpeedDoesNotDisturbTheBase() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.45F);
		brain.add(1, new ScriptedTask("silent", TFTask.MUTEX_MOVE));

		brain.tick(host.vanillaAI());

		assertEquals(0.45F, host.speed, 1.0E-6F);
	}

	@Test
	void fullLetsTheInheritedLoopRunWithRoamingIntact() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		brain.add(1, new ScriptedTask("idle", 0));

		brain.tick(host.vanillaAI());

		assertEquals(1, host.vanillaRuns);
		assertTrue(host.randomWalk);
		assertFalse(host.driven, "FULL must not hand-steer");
	}

	@Test
	void noRoamRunsTheInheritedLoopWithRandomWalkCleared() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask t = new ScriptedTask("flee", TFTask.MUTEX_MOVE);
		t.vanilla = TFTask.VanillaAI.NO_ROAM;
		brain.add(1, t);

		brain.tick(host.vanillaAI());

		assertEquals(1, host.vanillaRuns);
		assertFalse(host.randomWalk);
	}

	@Test
	void noneSuppressesTheInheritedLoopEntirely() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask t = new ScriptedTask("panic", TFTask.MUTEX_MOVE);
		t.vanilla = TFTask.VanillaAI.NONE;
		brain.add(1, t);

		brain.tick(host.vanillaAI());

		assertEquals(0, host.vanillaRuns);
	}

	@Test
	void strongestSuppressionWinsRegardlessOfPriority() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask polite = new ScriptedTask("polite", TFTask.MUTEX_LOOK);
		polite.vanilla = TFTask.VanillaAI.FULL;
		ScriptedTask rude = new ScriptedTask("rude", TFTask.MUTEX_MOVE);
		rude.vanilla = TFTask.VanillaAI.NONE;
		brain.add(1, polite);
		brain.add(9, rude);

		brain.tick(host.vanillaAI());

		assertTrue(brain.isRunning(polite));
		assertTrue(brain.isRunning(rude));
		assertEquals(0, host.vanillaRuns);
	}

	@Test
	void noneWithNoRouteDrivesTheMobToAStop() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask t = new ScriptedTask("charge", TFTask.MUTEX_MOVE);
		t.vanilla = TFTask.VanillaAI.NONE;
		brain.add(1, t);

		host.tfDrive(17.0F, 1.67F, true);

		brain.tick(host.vanillaAI());

		assertTrue(brain.isRunning(t));
		assertTrue(brain.noPath(), "the task deliberately has no route");
		assertEquals(0.0F, host.drivenForward, 1.0E-6F, "a routeless NONE task means motionless");
		assertFalse(host.drivenJumping, "and not jumping either");
	}

	@Test
	void stoppingPreservesWhateverHeadingTheTaskAimed() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.5F);
		ScriptedTask t = new ScriptedTask("stare", TFTask.MUTEX_MOVE | TFTask.MUTEX_LOOK);
		t.vanilla = TFTask.VanillaAI.NONE;
		brain.add(1, t);

		host.mob.yRot = 143.5F;

		brain.tick(host.vanillaAI());

		assertEquals(143.5F, host.drivenYRot, 1.0E-6F, "the aim survives the stop");
	}

	@Test
	void anIdleBrainIsInvisible() {
		TestHost host = host();
		TFBrain brain = new TFBrain(host, 0.45F);
		ScriptedTask t = new ScriptedTask("t", TFTask.MUTEX_MOVE);
		t.should = false;
		brain.add(1, t);

		brain.tick(host.vanillaAI());

		assertEquals(1, host.vanillaRuns);
		assertTrue(host.randomWalk);
		assertEquals(0.45F, host.speed, 1.0E-6F);
		assertTrue(brain.noPath());
	}
}
