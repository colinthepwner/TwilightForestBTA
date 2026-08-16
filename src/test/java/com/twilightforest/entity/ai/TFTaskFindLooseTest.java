package com.twilightforest.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.twilightforest.entity.ai.harness.FakeWorld;
import com.twilightforest.entity.ai.harness.TestHost;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.EntityItem;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Random;

class TFTaskFindLooseTest {

	private static final int FLOOR_Y = 4;
	private static final int STAND_Y = 5;
	private static final float PURSUE_SPEED = 0.70F;

	private static final int WOOL = Blocks.WOOL.id();

	private static final int WHITE = 0;

	private FakeWorld plain(long seed) {
		FakeWorld w = new FakeWorld(new Random(seed));
		w.fill(-40, FLOOR_Y, -40, 40, FLOOR_Y, 40, Blocks.STONE);
		return w;
	}

	private static ItemStack wool(int color) {
		return new ItemStack(Blocks.WOOL, 1, color);
	}

	private record Fixture(FakeWorld world, MobPathfinder mob, TestHost host, TFBrain brain,
	                       TFTaskFindLoose find) {}

	private Fixture arena(long seed) {
		FakeWorld w = plain(seed);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = new TestHost(mob);
		host.weight = pos -> w.world.getBlockId(pos.x(), pos.y() - 1, pos.z()) != 0
			&& w.world.getBlockId(pos.x(), pos.y(), pos.z()) == 0 ? 10.0 : -10.0;
		TFBrain brain = new TFBrain(host, 0.5F);
		TFTaskFindLoose find = new TFTaskFindLoose(host, brain, PURSUE_SPEED, WOOL);
		brain.add(4, find);
		return new Fixture(w, mob, host, brain, find);
	}

	@Test
	void anEmptyGroveIsNotInteresting() {
		Fixture f = arena(1L);

		assertFalse(f.find.shouldExecute());
		assertNull(f.find.getTemptingItem());
	}

	@Test
	void woolWithinSixteenBlocksIsNoticed() {
		Fixture f = arena(2L);
		EntityItem stack = f.world.item(wool(WHITE), 12.5, STAND_Y, 0.5);

		assertTrue(f.find.shouldExecute());
		assertSame(stack, f.find.getTemptingItem());
	}

	@Test
	void woolBeyondSixteenBlocksIsNot() {
		Fixture f = arena(3L);
		f.world.item(wool(WHITE), 30.5, STAND_Y, 0.5);

		assertFalse(f.find.shouldExecute());
	}

	@Test
	void anythingThatIsNotWoolIsIgnored() {
		Fixture f = arena(4L);
		f.world.item(new ItemStack(Blocks.COBBLE_STONE, 1), 4.5, STAND_Y, 0.5);

		assertFalse(f.find.shouldExecute());
	}

	@Test
	void itWalksToColoursItAlreadyHas() {
		Fixture f = arena(5L);
		EntityItem stack = f.world.item(wool(WHITE), 5.5, STAND_Y, 0.5);

		assertTrue(f.find.shouldExecute());
		assertSame(stack, f.find.getTemptingItem());
	}

	@Test
	void aRemovedStackIsNotChased() {
		Fixture f = arena(6L);
		EntityItem stack = f.world.item(wool(WHITE), 5.5, STAND_Y, 0.5);
		stack.remove();

		assertFalse(f.find.shouldExecute());
	}

	@Test
	void startingToWalkLaysARealRoute() {
		Fixture f = arena(7L);
		f.world.item(wool(WHITE), 6.5, STAND_Y, 0.5);

		assertTrue(f.find.shouldExecute());
		f.find.updateTask();

		assertFalse(f.brain.noPath(), "a ram that has noticed wool six blocks away has a route to it");
	}

	@Test
	void arrivingDropsTheRoute() {
		Fixture f = arena(8L);
		f.world.item(wool(WHITE), 6.5, STAND_Y, 0.5);

		assertTrue(f.find.shouldExecute());
		f.find.updateTask();
		assertFalse(f.brain.noPath());

		f.world.place(f.mob, 4.5, STAND_Y, 0.5);
		f.find.updateTask();

		assertTrue(f.brain.noPath(), "close enough to eat means stop pushing");
	}

	@Test
	void itAsksForNoRoamSoTheInheritedLoopKeepsSteering() {
		Fixture f = arena(9L);

		assertEquals(TFTask.VanillaAI.NO_ROAM, f.find.vanillaAI());
	}

	@Test
	void stoppingStartsAHundredCallCooldown() {
		Fixture f = arena(10L);
		f.world.item(wool(WHITE), 5.5, STAND_Y, 0.5);

		assertTrue(f.find.shouldExecute());
		f.find.resetTask();
		assertTrue(f.brain.noPath(), "reset releases the route");

		for (int i = 0; i < 100; i++) {
			assertFalse(f.find.shouldExecute(), "still cooling down after " + i + " calls");
		}
		assertTrue(f.find.shouldExecute(), "and on the hundred-and-first it is interested again");
	}

	@Test
	void itClaimsMovementAndLook() {
		Fixture f = arena(11L);

		assertTrue((f.find.getMutexBits() & TFTask.MUTEX_MOVE) != 0);
		assertTrue((f.find.getMutexBits() & TFTask.MUTEX_LOOK) != 0);
		assertEquals(PURSUE_SPEED, f.find.moveSpeed(), 1.0E-6F);
	}

	@Test
	void theEatingTaskCanRunAlongsideIt() {
		FakeWorld w = plain(12L);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = new TestHost(mob);
		TFBrain brain = new TFBrain(host, 0.5F);

		TFTaskFindLoose find = new TFTaskFindLoose(host, brain, PURSUE_SPEED, WOOL);
		RecordingRam ram = new RecordingRam(host);
		TFTaskEatLoose eat = new TFTaskEatLoose(ram, brain, WOOL);
		brain.add(3, eat);
		brain.add(4, find);

		w.item(wool(WHITE), 1.4, STAND_Y, 0.5);
		brain.tick(host.vanillaAI());

		assertTrue(brain.isRunning(eat));
		assertTrue(brain.isRunning(find), "no shared mutex bit means the walk survives the eat");
	}

	private static final class RecordingRam implements TFTaskEatLoose.WoolEater {
		private final TestHost host;
		private int flags;

		RecordingRam(TestHost host) {
			this.host = host;
		}

		@Override public boolean isColorPresent(int color) { return (this.flags & (1 << color)) != 0; }
		@Override public void setColorPresent(int color) { this.flags |= 1 << color; }
		@Override public void animateAddColor(int color, int iterations) { }
		@Override public void bleat() { }

		@Override public MobPathfinder asMob() { return this.host.asMob(); }
		@Override public float tfBlockPathWeight(net.minecraft.core.world.pos.TilePosc pos) { return 0.0F; }
		@Override public void tfSetSpeed(float speed) { this.host.tfSetSpeed(speed); }
		@Override public void tfSetRandomWalk(boolean enabled) { this.host.tfSetRandomWalk(enabled); }
		@Override public void tfDrive(float yRot, float moveForward, boolean jumping) {
			this.host.tfDrive(yRot, moveForward, jumping);
		}
	}
}
