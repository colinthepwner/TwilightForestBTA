package com.twilightforest.world.structure;

import com.twilightforest.world.feature.TFMaze;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.world.World;

import java.util.Random;

public class ComponentTFHillMaze extends StructureComponentTF {

	public static final int DEPTH_BELOW_CHAMBER = 20;

	private static final int ROOM_SPREAD = 8;

	private final int hsize;

	public ComponentTFHillMaze(int componentType, int x, int y, int z, int hsize) {
		super(componentType);
		this.coordBaseMode = 0;
		this.hsize = hsize;

		this.boundingBox = componentBox(x, y, z,
			-radius(), 0, -radius(), radius() * 2, 5, radius() * 2, 0);
	}

	private static int cellCount(int hsize) {
		return switch (hsize) {
			case 1 -> 11;
			case 2 -> 19;
			default -> 27;
		};
	}

	private int radius() {
		return cellCount(this.hsize) * 2;
	}

	private int diameter() {
		return cellCount(this.hsize) * 4;
	}

	public static int mazeY() {
		return ComponentTFHollowHill.chamberY() - DEPTH_BELOW_CHAMBER;
	}

	@Override
	public int featureType() {
		return this.hsize;
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {
		int dia = diameter();

		fillWithBlocks(world, clip, 0, 1, 0, dia, 3, dia, 0, 0, 0, 0, false);
		fillWithBlocks(world, clip, 0, 0, 0, dia, 0, dia,
			ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_PLAIN), 0, 0, 0, false);

		fillWithBlocks(world, clip, 0, 4, 0, dia, 4, dia,
			ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_PLAIN), 0, 0, 0, true);

		int cells = cellCount(this.hsize);
		TFMaze maze = new TFMaze(cells, cells);

		maze.wallBlockId = ComponentTFMinotaurMaze.mazestone(ComponentTFMinotaurMaze.MAZESTONE_DECO);
		maze.wallBlockMeta = 0;

		maze.torchRarity = 0.05f;

		maze.setSeed(world.getRandomSeed()
			+ (long) this.boundingBox.minX * this.boundingBox.minZ);

		int nrooms = cells / 3;
		int[] rcoords = new int[nrooms * 2];
		for (int i = 0; i < nrooms; i++) {
			int rx;
			int rz;
			do {
				rx = maze.rand.nextInt(cells - 2) + 1;
				rz = maze.rand.nextInt(cells - 2) + 1;
			} while (isNearRoom(rx, rz, rcoords));
			maze.carveRoom1(rx, rz);
			rcoords[i * 2] = rx;
			rcoords[i * 2 + 1] = rz;
		}

		maze.generateRecursiveBacktracker(0, 0);
		maze.add4Exits();
		maze.copyToStructure(world, 0, 1, 0, this, clip);

		decorate3x3Rooms(world, rcoords, clip);
		return true;
	}

	private boolean isNearRoom(int dx, int dz, int[] rcoords) {
		if (dx == 1 && dz == 1) {
			return true;
		}
		for (int i = 0; i < rcoords.length / 2; i++) {
			int rx = rcoords[i * 2];
			int rz = rcoords[i * 2 + 1];
			if (rx == 0 && rz == 0) {
				continue;
			}
			if (Math.abs(dx - rx) < 3 && Math.abs(dz - rz) < 3) {
				return true;
			}
		}
		return false;
	}

	private void decorate3x3Rooms(World world, int[] rcoords, BoundingBox clip) {
		for (int i = 0; i < rcoords.length / 2; i++) {
			int dx = rcoords[i * 2] * 4 + 2;
			int dz = rcoords[i * 2 + 1] * 4 + 2;
			decorate3x3Room(world, dx, dz, clip);
		}
	}

	private void decorate3x3Room(World world, int x, int z, BoundingBox clip) {
		Random roomRand = new Random(world.getRandomSeed() ^ (x + z));
		roomSpawner(world, roomRand, x, z, ROOM_SPREAD, clip);
		roomTreasure(world, roomRand, x, z, ROOM_SPREAD, clip);
		if (roomRand.nextInt(4) == 0) {
			roomTreasure(world, roomRand, x, z, ROOM_SPREAD, clip);
		}
	}

	private void roomSpawner(World world, Random rand, int x, int z, int spread, BoundingBox clip) {
		int rx = x + rand.nextInt(spread) - spread / 2;
		int rz = z + rand.nextInt(spread) - spread / 2;
		String mobId = switch (rand.nextInt(3)) {
			case 1 -> "minecraft:skeleton";
			case 2 -> "minecraft:zombie";
			default -> "minecraft:spider";
		};
		placeSpawner(world, rand, rx, 1, rz, mobId, clip);
	}

	private void roomTreasure(World world, Random rand, int x, int z, int spread, BoundingBox clip) {
		int rx = x + rand.nextInt(spread) - spread / 2;
		int rz = z + rand.nextInt(spread) - spread / 2;
		placeTreasure(world, rand, rx, 1, rz, TFTreasure.LABYRINTH_ROOM, clip);
	}
}
