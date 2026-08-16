package com.twilightforest.entity.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.twilightforest.entity.ai.harness.FakeWorld;
import com.twilightforest.entity.ai.harness.TestHost;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.pathfinder.Node;
import net.minecraft.core.world.pathfinder.Path;
import org.junit.jupiter.api.Test;

import java.util.Random;

class TFTaskAvoidEntityTest {

	private static final int FLOOR_Y = 4;
	private static final int STAND_Y = 5;
	private static final double TRIGGER = 2.0;
	private static final float FAR = 0.41F;
	private static final float NEAR = 0.72F;

	private FakeWorld plain(long seed) {
		FakeWorld w = new FakeWorld(new Random(seed));
		w.fill(-20, FLOOR_Y, -20, 20, FLOOR_Y, 20, Blocks.STONE);
		return w;
	}

	private TestHost hostOn(FakeWorld w, MobPathfinder mob) {
		TestHost host = new TestHost(mob);
		host.weight = pos -> w.world.getBlockId(pos.x(), pos.y() - 1, pos.z()) != 0
			&& w.world.getBlockId(pos.x(), pos.y(), pos.z()) == 0 ? 10.0 : -10.0;
		return host;
	}

	private TFTaskAvoidEntity task(TestHost host, TFBrain brain) {
		return new TFTaskAvoidEntity(host, brain, Player.class, TRIGGER, FAR, NEAR);
	}

	@Test
	void doesNothingWithNobodyAround() {
		FakeWorld w = plain(1L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);

		assertFalse(task(host, brain).shouldExecute());
	}

	@Test
	void ignoresAPlayerBeyondTheTriggerRange() {
		FakeWorld w = plain(2L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		w.place(w.player(), 3.5, STAND_Y, 0.5);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);

		assertFalse(task(host, brain).shouldExecute());
	}

	@Test
	void ignoresAPeaceablePlayerInsideTheTriggerRange() {
		FakeWorld w = plain(3L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		w.place(w.player(false), 1.2, STAND_Y, 0.5);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);

		assertFalse(task(host, brain).shouldExecute());
	}

	@Test
	void standsDownWhileTempted() {
		FakeWorld w = plain(9L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		Player player = w.place(w.player(), 1.2, STAND_Y, 0.5);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskAvoidEntity flee = task(host, brain);

		when(bird.getTarget()).thenReturn(player);

		assertFalse(flee.shouldExecute(), "a tempted bird must not start fleeing");
		assertFalse(flee.continueExecuting(),
			"and one tempted mid-flight must stop, since BTA's tempt cannot take the mutex itself");
	}

	@Test
	void ignoresAPlayerItCannotSee() {
		FakeWorld w = plain(4L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		w.place(w.player(), 1.2, STAND_Y, 0.5);
		when(bird.canEntityBeSeen(any())).thenReturn(false);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);

		assertFalse(task(host, brain).shouldExecute());
	}

	@Test
	void everyAcceptedRouteEndsFurtherFromThePlayer() {
		int fired = 0;
		int trials = 8;

		for (long seed = 0; seed < trials; seed++) {
			FakeWorld w = plain(seed);
			MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
			Player player = w.place(w.player(), 1.4, STAND_Y, 0.5);
			TestHost host = hostOn(w, bird);
			TFBrain brain = new TFBrain(host, 0.45F);
			TFTaskAvoidEntity flee = task(host, brain);

			if (!flee.shouldExecute()) {
				continue;
			}
			fired++;
			flee.startExecuting();

			Path route = brain.getPath();
			assertTrue(route != null && !route.isDone(), "a started flee must own a live route");
			Node last = route.last();
			double startSq = sq(player.x - bird.x) + sq(player.z - bird.z);
			double endSq = sq(player.x - (last.x + 0.5)) + sq(player.z - (last.z + 0.5));
			assertTrue(endSq > startSq,
				"seed " + seed + ": route ended nearer the player (" + endSq + " vs " + startSq + ")");
		}

		assertTrue(fired >= 3,
			"the flee fired only " + fired + " times in " + trials
				+ " -- a bird that almost never runs is as wrong as one that never does");
	}

	@Test
	void speedSwapsToTheSprintInsideSevenBlocks() {
		FakeWorld w = plain(7L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		Player player = w.place(w.player(), 1.4, STAND_Y, 0.5);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskAvoidEntity flee = task(host, brain);

		assertTrue(flee.moveSpeed() == FAR);

		assertTrue(flee.shouldExecute(), "seed 7 is chosen because the flee fires on it");
		assertTrue(flee.moveSpeed() == NEAR, "1.4 blocks away is well inside the sprint range");

		w.place(player, 9.0, STAND_Y, 0.5);
		assertTrue(flee.moveSpeed() == FAR);
	}

	@Test
	void suppressesRoamingButNotTheRestOfTheInheritedLoop() {
		FakeWorld w = plain(8L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);

		assertTrue(task(host, brain).vanillaAI() == TFTask.VanillaAI.NO_ROAM);
	}

	@Test
	void resettingReleasesTheRoute() {
		FakeWorld w = plain(7L);
		MobPathfinder bird = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		w.place(w.player(), 1.4, STAND_Y, 0.5);
		TestHost host = hostOn(w, bird);
		TFBrain brain = new TFBrain(host, 0.45F);
		TFTaskAvoidEntity flee = task(host, brain);

		assertTrue(flee.shouldExecute());
		flee.startExecuting();
		assertFalse(brain.noPath());

		flee.resetTask();
		assertTrue(brain.noPath());
		assertTrue(flee.getThreat() == null);
	}

	private static double sq(double d) {
		return d * d;
	}
}
