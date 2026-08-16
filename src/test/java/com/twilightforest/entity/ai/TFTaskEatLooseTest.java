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
import net.minecraft.core.world.pos.TilePosc;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TFTaskEatLooseTest {

	private static final int FLOOR_Y = 4;
	private static final int STAND_Y = 5;

	private static final int WOOL = Blocks.WOOL.id();

	private static final int WHITE = 0;
	private static final int BLUE = 11;

	private FakeWorld plain(long seed) {
		FakeWorld w = new FakeWorld(new Random(seed));
		w.fill(-40, FLOOR_Y, -40, 40, FLOOR_Y, 40, Blocks.STONE);
		return w;
	}

	private static ItemStack wool(int color) {
		return new ItemStack(Blocks.WOOL, 1, color);
	}

	private record Fixture(FakeWorld world, MobPathfinder mob, RecordingRam ram, TFBrain brain,
	                       TFTaskEatLoose eat) {}

	private Fixture arena(long seed) {
		FakeWorld w = plain(seed);
		MobPathfinder mob = w.place(w.mob(), 0.5, STAND_Y, 0.5);
		TestHost host = new TestHost(mob);
		TFBrain brain = new TFBrain(host, 0.5F);
		RecordingRam ram = new RecordingRam(host);
		TFTaskEatLoose eat = new TFTaskEatLoose(ram, brain, WOOL);
		brain.add(3, eat);
		return new Fixture(w, mob, ram, brain, eat);
	}

	@Test
	void woolWithinTwoBlocksIsNoticed() {
		Fixture f = arena(1L);
		EntityItem stack = f.world.item(wool(WHITE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());
		assertSame(stack, f.eat.getTemptingItem());
	}

	@Test
	void woolAcrossTheGroveIsNotNoticed() {
		Fixture f = arena(2L);
		f.world.item(wool(WHITE), 9.5, STAND_Y, 0.5);

		assertFalse(f.eat.shouldExecute());
		assertNull(f.eat.getTemptingItem());
	}

	@Test
	void aColourItAlreadyHasIsRefused() {
		Fixture f = arena(3L);
		f.ram.setColorPresent(WHITE);
		f.world.item(wool(WHITE), 1.4, STAND_Y, 0.5);

		assertFalse(f.eat.shouldExecute());
	}

	@Test
	void aColourItLacksIsAcceptedEvenWhenItHasOthers() {
		Fixture f = arena(4L);
		f.ram.setColorPresent(WHITE);
		EntityItem blue = f.world.item(wool(BLUE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());
		assertSame(blue, f.eat.getTemptingItem());
	}

	@Test
	void eatingConsumesTheStackAndRecordsTheColour() {
		Fixture f = arena(5L);
		EntityItem stack = f.world.item(wool(BLUE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());
		f.eat.updateTask();

		assertFalse(stack.isAlive(), "the stack is gone");
		assertTrue(f.ram.isColorPresent(BLUE), "and the colour is recorded");
	}

	@Test
	void eatingOffTheFloorBleatsTwiceAndShowersFiftyMotes() {
		Fixture f = arena(6L);
		f.world.item(wool(BLUE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());
		f.eat.updateTask();

		assertEquals(2, f.ram.bleats, "the explicit call plus the one on the end of animateAddColor");
		assertEquals(List.of("color=" + BLUE + " x50"), f.ram.animations);
	}

	@Test
	void woolOutOfReachOnTheUpdateTickIsNotEaten() {
		Fixture f = arena(7L);
		EntityItem stack = f.world.item(wool(BLUE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());

		f.world.place(f.mob, -6.5, STAND_Y, 0.5);
		f.eat.updateTask();

		assertTrue(stack.isAlive(), "still on the floor");
		assertFalse(f.ram.isColorPresent(BLUE));
	}

	@Test
	void aColourAcquiredBetweenTicksIsNotEatenAgain() {
		Fixture f = arena(8L);
		EntityItem stack = f.world.item(wool(BLUE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());
		f.ram.setColorPresent(BLUE);
		f.eat.updateTask();

		assertTrue(stack.isAlive(), "the stack is not destroyed for a colour it now has");
		assertEquals(0, f.ram.bleats);
	}

	@Test
	void itStopsOnceTheColourIsIn() {
		Fixture f = arena(9L);
		f.world.item(wool(BLUE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());
		f.eat.updateTask();

		assertFalse(f.eat.continueExecuting(), "nothing left within reach that it wants");
	}

	@Test
	void itClaimsNothingAtAll() {
		Fixture f = arena(10L);

		assertEquals(0, f.eat.getMutexBits());
		assertTrue(Float.isNaN(f.eat.moveSpeed()), "it does not move the ram, so it names no speed");
	}

	@Test
	void stoppingStartsAHundredCallCooldown() {
		Fixture f = arena(11L);
		f.world.item(wool(BLUE), 1.4, STAND_Y, 0.5);

		assertTrue(f.eat.shouldExecute());
		f.eat.resetTask();

		for (int i = 0; i < 100; i++) {
			assertFalse(f.eat.shouldExecute(), "still cooling down after " + i + " calls");
		}
		assertTrue(f.eat.shouldExecute());
	}

	private static final class RecordingRam implements TFTaskEatLoose.WoolEater {
		private final TestHost host;
		private int flags;

		int bleats;
		final List<String> animations = new ArrayList<>();

		RecordingRam(TestHost host) {
			this.host = host;
		}

		@Override
		public boolean isColorPresent(int color) {
			return (this.flags & (1 << color)) != 0;
		}

		@Override
		public void setColorPresent(int color) {
			this.flags |= 1 << color;
		}

		@Override
		public void animateAddColor(int color, int iterations) {
			this.animations.add("color=" + color + " x" + iterations);
			this.bleat();
		}

		@Override
		public void bleat() {
			this.bleats++;
		}

		@Override public MobPathfinder asMob() { return this.host.asMob(); }
		@Override public float tfBlockPathWeight(TilePosc pos) { return 0.0F; }
		@Override public void tfSetSpeed(float speed) { this.host.tfSetSpeed(speed); }
		@Override public void tfSetRandomWalk(boolean enabled) { this.host.tfSetRandomWalk(enabled); }
		@Override public void tfDrive(float yRot, float moveForward, boolean jumping) {
			this.host.tfDrive(yRot, moveForward, jumping);
		}
	}
}
