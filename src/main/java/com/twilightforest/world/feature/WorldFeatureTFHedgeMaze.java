package com.twilightforest.world.feature;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import com.twilightforest.world.treasure.TFTreasure;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFHedgeMaze extends TFWorldFeature {

	private static final int MAZE_CELLS = 16;

	private static final int ROOM_SPACING = 3;

	private static final int ROOM_SPREAD = 8;

	private final int size;
	private TFMaze maze;
	private Random rand;

	public WorldFeatureTFHedgeMaze(int size) {
		this.size = size;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;
		this.rand = rand;
		int sx = x - 7 - this.size * 16;
		int sz = z - 7 - this.size * 16;

		this.maze = new TFMaze(MAZE_CELLS, MAZE_CELLS);
		this.maze.setSeed(rand.nextLong());
		this.maze.oddBias = 2;
		this.maze.torchBlockId = TFBlocks.FIREFLY.id();
		this.maze.wallBlockId = TFBlocks.HEDGE.id();
		this.maze.type = 4;
		this.maze.tall = 3;
		this.maze.roots = 3;

		fill(sx, y - 1, sz, MAZE_CELLS * 3, 1, MAZE_CELLS * 3, Blocks.GRASS.id());

		lantern(world, sx - 1, y, sz + 23, 1);
		lantern(world, sx - 1, y, sz + 28, 1);
		lantern(world, sx + 49, y, sz + 23, 3);
		lantern(world, sx + 49, y, sz + 28, 3);
		lantern(world, sx + 23, y, sz - 1, 2);
		lantern(world, sx + 28, y, sz - 1, 2);
		lantern(world, sx + 23, y, sz + 49, 0);
		lantern(world, sx + 28, y, sz + 49, 0);

		int roomCount = MAZE_CELLS / 3;
		int[] rooms = new int[roomCount * 2];
		for (int i = 0; i < roomCount; i++) {
			int rx;
			int rz;
			do {
				rx = rand.nextInt(MAZE_CELLS - 2) + 1;
				rz = rand.nextInt(MAZE_CELLS - 2) + 1;
			} while (isNearRoom(rx, rz, rooms));

			this.maze.carveRoom1(rx, rz);
			rooms[i * 2] = rx;
			rooms[i * 2 + 1] = rz;
		}

		this.maze.generateRecursiveBacktracker(0, 0);
		this.maze.add4Exits();
		this.maze.copyToWorld(world, sx, y, sz);

		for (int i = 0; i < roomCount; i++) {
			decorateRoom(world, rooms[i * 2], rooms[i * 2 + 1]);
		}
		return true;
	}

	private boolean isNearRoom(int dx, int dz, int[] rooms) {
		for (int i = 0; i < rooms.length / 2; i++) {
			int rx = rooms[i * 2];
			int rz = rooms[i * 2 + 1];
			if ((rx != 0 || rz != 0)
				&& Math.abs(dx - rx) < ROOM_SPACING && Math.abs(dz - rz) < ROOM_SPACING) {
				return true;
			}
		}
		return false;
	}

	private void decorateRoom(World world, int cellX, int cellZ) {
		int dx = this.maze.getWorldX(cellX) + 1;
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(cellZ) + 1;

		roomSpawner(world, dx, dy, dz);

		if (!roomTreasure(world, dx, dy, dz)) {
			roomTreasure(world, dx, dy, dz);
		}
		if (!roomJackO(world, dx, dy, dz) || this.rand.nextInt(4) == 0) {
			roomJackO(world, dx, dy, dz);
		}
	}

	private void roomSpawner(World world, int dx, int dy, int dz) {
		int rx = this.rand.nextInt(ROOM_SPREAD) + dx - ROOM_SPREAD / 2;
		int rz = this.rand.nextInt(ROOM_SPREAD) + dz - ROOM_SPREAD / 2;

		String mob;
		switch (this.rand.nextInt(3)) {
			case 1 -> mob = TwilightForest.MOD_ID + ":swarmspider";
			case 2 -> mob = TwilightForest.MOD_ID + ":hostilewolf";
			default -> mob = TwilightForest.MOD_ID + ":hedgespider";
		}

		world.setBlockWithNotify(rx, dy, rz, Blocks.MOBSPAWNER.id());
		if (world.getTileEntity(rx, dy, rz) instanceof TileEntityMobSpawner spawner) {
			spawner.setMobId(mob);
		}
	}

	private boolean roomTreasure(World world, int dx, int dy, int dz) {
		int rx = this.rand.nextInt(ROOM_SPREAD) + dx - ROOM_SPREAD / 2;
		int rz = this.rand.nextInt(ROOM_SPREAD) + dz - ROOM_SPREAD / 2;
		return TFTreasure.place(world, this.rand, rx, dy, rz, TFTreasure.HEDGE_MAZE);
	}

	private boolean roomJackO(World world, int dx, int dy, int dz) {
		int rx = this.rand.nextInt(ROOM_SPREAD) + dx - ROOM_SPREAD / 2;
		int rz = this.rand.nextInt(ROOM_SPREAD) + dz - ROOM_SPREAD / 2;
		if (world.getBlockId(rx, dy, rz) != 0) {
			return false;
		}
		world.setBlockAndMetadataWithNotify(rx, dy, rz,
			Blocks.PUMPKIN_CARVED_ACTIVE.id(), this.rand.nextInt(4));
		return true;
	}

	private void lantern(World world, int x, int y, int z, int facing) {
		world.setBlockAndMetadataWithNotify(x, y, z, Blocks.PUMPKIN_CARVED_ACTIVE.id(), facing);
	}
}
