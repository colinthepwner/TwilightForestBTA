package com.twilightforest.entity.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.twilightforest.entity.ai.harness.FakeWorld;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.world.pathfinder.Node;
import net.minecraft.core.world.pathfinder.Path;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class TFPathfinderAvoidTest {

	private static final int WALL_X = 5;
	private static final int NEAR_DOOR_Z = 0;
	private static final int FAR_DOOR_Z = 4;
	private static final int FLOOR_Y = 4;
	private static final int STAND_Y = 5;

	private FakeWorld build() {
		FakeWorld w = new FakeWorld();

		w.fill(-2, FLOOR_Y, -8, 14, FLOOR_Y, 8, Blocks.STONE);
		w.fill(-2, STAND_Y + 2, -8, 14, STAND_Y + 2, 8, Blocks.STONE);

		w.fill(-2, STAND_Y, -8, 14, STAND_Y + 1, -8, Blocks.STONE);
		w.fill(-2, STAND_Y, 8, 14, STAND_Y + 1, 8, Blocks.STONE);
		w.fill(-2, STAND_Y, -8, -2, STAND_Y + 1, 8, Blocks.STONE);
		w.fill(14, STAND_Y, -8, 14, STAND_Y + 1, 8, Blocks.STONE);

		w.fill(WALL_X, STAND_Y, -8, WALL_X, STAND_Y + 1, 8, Blocks.STONE);
		w.fill(WALL_X, STAND_Y, NEAR_DOOR_Z, WALL_X, STAND_Y + 1, NEAR_DOOR_Z, null);
		w.fill(WALL_X, STAND_Y, FAR_DOOR_Z, WALL_X, STAND_Y + 1, FAR_DOOR_Z, null);
		return w;
	}

	private MobPathfinder walker(FakeWorld w) {
		return w.place(w.mob(), 0.5, STAND_Y, 0.5);
	}

	@Test
	void withoutAnAvoidSetItTakesTheNearDoorway() {
		FakeWorld w = build();
		Path path = TFPathfinder.findPath(w.world, walker(w), 10, STAND_Y, 0, 1.0, null);

		assertNotNull(path, "there is a route; the planner must find one");
		assertTrue(maxZ(path) < FAR_DOOR_Z - 1,
			"unobstructed, the route should stay on the near doorway's line, got " + describe(path));
	}

	@Test
	void anAvoidedDoorwayIsRoutedAround() {
		FakeWorld w = build();
		Set<Long> avoid = new HashSet<>();
		for (int y = STAND_Y; y <= STAND_Y + 1; y++) {
			avoid.add(TFPathfinder.packKey(WALL_X, y, NEAR_DOOR_Z));
		}

		Path path = TFPathfinder.findPath(w.world, walker(w), 10, STAND_Y, 0, 1.0, avoid);

		assertNotNull(path, "the far doorway is still open; a route exists");
		assertTrue(maxZ(path) >= FAR_DOOR_Z - 1,
			"the route should have detoured to the far doorway, got " + describe(path));
	}

	@Test
	void aCorneredRouteStillCrossesAnAvoidedCell() {
		FakeWorld w = build();

		w.fill(WALL_X, STAND_Y, FAR_DOOR_Z, WALL_X, STAND_Y + 1, FAR_DOOR_Z, Blocks.STONE);

		Set<Long> avoid = new HashSet<>();
		for (int y = STAND_Y; y <= STAND_Y + 1; y++) {
			avoid.add(TFPathfinder.packKey(WALL_X, y, NEAR_DOOR_Z));
		}

		Path path = TFPathfinder.findPath(w.world, walker(w), 10, STAND_Y, 0, 1.0, avoid);

		assertNotNull(path, "an expensive route is still a route");
		Node last = path.last();
		assertTrue(last.x > WALL_X,
			"the route must reach the far side of the wall, ended at " + describe(path));
	}

	private static int maxZ(Path path) {
		int max = Integer.MIN_VALUE;
		for (int[] n : nodes(path)) {
			max = Math.max(max, n[2]);
		}
		return max;
	}

	private static String describe(Path path) {
		StringBuilder sb = new StringBuilder("[");
		for (int[] n : nodes(path)) {
			sb.append('(').append(n[0]).append(',').append(n[1]).append(',').append(n[2]).append(')');
		}
		return sb.append(']').toString();
	}

	private static java.util.List<int[]> nodes(Path path) {
		try {
			java.lang.reflect.Field f = Path.class.getDeclaredField("nodes");
			f.setAccessible(true);
			Node[] raw = (Node[]) f.get(path);
			java.util.List<int[]> out = new java.util.ArrayList<>(raw.length);
			for (Node n : raw) {
				out.add(new int[]{n.x, n.y, n.z});
			}
			return out;
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Path.nodes moved; this test needs updating", e);
		}
	}
}
