package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import com.twilightforest.world.treasure.TFTreasure;
import com.twilightforest.world.treasure.TFTreasureTable;
import com.twilightforest.world.treasure.TFTreasure;
import com.twilightforest.world.treasure.TFTreasureTable;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.entity.EntityPainting;
import net.minecraft.core.enums.ArtType;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFHillMaze extends TFWorldFeature {

	private final int hsize;

	private TFMaze maze;
	private Random rand;

	public WorldFeatureTFHillMaze(int size) {
		this.hsize = size;
	}

	private int cellCount() {
		switch (this.hsize) {
			case 2: return 19;
			case 3: return 27;
			default: return 11;
		}
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;
		this.rand = rand;

		int sx = x - 7 - this.hsize * 16;
		int sz = z - 7 - this.hsize * 16;
		int msize = cellCount();

		fill(sx, y - 1, sz, msize * 4, 1, msize * 4, TFBlocks.MAZESTONE.id());
		fill(sx, y, sz, msize * 4, 3, msize * 4, 0);
		fill(sx, y + 3, sz, msize * 4, 1, msize * 4, TFBlocks.MAZESTONE.id());

		this.maze = new TFMaze(msize, msize);
		this.maze.setSeed(rand.nextLong());

		int roomCount = msize / 3;
		int[] rooms = new int[roomCount * 2];
		for (int i = 0; i < roomCount; i++) {
			int rx;
			int rz;
			do {
				rx = rand.nextInt(msize - 2) + 1;
				rz = rand.nextInt(msize - 2) + 1;
			} while (isNearRoom(rx, rz, rooms));

			this.maze.carveRoom1(rx, rz);
			rooms[i * 2] = rx;
			rooms[i * 2 + 1] = rz;
		}

		this.maze.generateRecursiveBacktracker(0, 0);
		this.maze.copyToWorld(world, sx, y, sz);

		decorateDeadEnds();
		for (int i = 0; i < roomCount; i++) {
			decorate3x3Room(rooms[i * 2], rooms[i * 2 + 1]);
		}
		return true;
	}

	private boolean isNearRoom(int dx, int dz, int[] rooms) {
		for (int i = 0; i < rooms.length / 2; i++) {
			int rx = rooms[i * 2];
			int rz = rooms[i * 2 + 1];
			if ((rx != 0 || rz != 0) && Math.abs(dx - rx) < 3 && Math.abs(dz - rz) < 3) {
				return true;
			}
		}
		return false;
	}

	private void decorateDeadEnds() {
		for (int x = 0; x < cellCount(); x++) {
			for (int z = 0; z < cellCount(); z++) {
				boolean west = this.maze.isWall(x, z, x - 1, z);
				boolean east = this.maze.isWall(x, z, x + 1, z);
				boolean north = this.maze.isWall(x, z, x, z - 1);
				boolean south = this.maze.isWall(x, z, x, z + 1);

				if (!west && east && north && south) decorateDeadEnd(x, z, 3);
				if (west && !east && north && south) decorateDeadEnd(x, z, 1);
				if (west && east && !north && south) decorateDeadEnd(x, z, 0);
				if (west && east && north && !south) decorateDeadEnd(x, z, 2);
			}
		}
	}

	private void decorateDeadEnd(int x, int z, int f) {
		switch (this.rand.nextInt(17)) {
			case 0 -> deadEndSpiderSpawner(x, z, f);
			case 1 -> deadEndWebs(x, z, f);
			case 2 -> deadEndTreasure(x, z, f);
			case 3 -> deadEndSpawner(x, z, f);
			case 4 -> deadEndPainting(x, z, f);
			case 5 -> deadEndTrap(x, z, f);
			case 6 -> deadEndTrappedChest(x, z, f);
			case 7 -> deadEndTorch(x, z, f);
			case 8 -> deadEndTorchRedstone(x, z, f);
			case 9 -> deadEndFountain(x, z, f);
			case 10 -> deadEndLavaFountain(x, z, f);
			case 11 -> deadEndDoorway(x, z, f);
			case 12 -> deadEndDoor(x, z, f);
			case 13 -> deadEndDoorSteel(x, z, f);
			case 14 -> deadEndDoorTreasure(x, z, f);
			default -> {  }
		}
	}

	private int[] endSpot(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		return switch (f) {
			case 1 -> new int[]{dx, dy, dz + 1};
			case 2 -> new int[]{dx + 1, dy, dz};
			case 3 -> new int[]{dx + 2, dy, dz + 1};
			default -> new int[]{dx + 1, dy, dz + 2};
		};
	}

	private void deadEndSpiderSpawner(int x, int z, int f) {
		deadEndWebs(x, z, f);
		int[] s = endSpot(x, z, f);
		placeMobSpawner(s[0], s[1], s[2], "minecraft:spider");
	}

	private void deadEndWebs(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int web = Blocks.COBWEB.id();

		switch (f) {
			case 0 -> {
				web(dx + 1, dy, dz + 1, web); web(dx, dy + 1, dz + 1, web);
				web(dx + 2, dy + 1, dz + 1, web); web(dx + 1, dy + 2, dz + 1, web);
				web(dx, dy, dz + 2, web); web(dx + 2, dy, dz + 2, web);
				web(dx + 1, dy + 1, dz + 2, web);
			}
			case 1 -> {
				web(dx + 1, dy, dz + 1, web); web(dx + 1, dy + 1, dz, web);
				web(dx + 1, dy + 1, dz + 2, web); web(dx + 1, dy + 2, dz + 1, web);
				web(dx, dy, dz, web); web(dx, dy, dz + 2, web);
				web(dx, dy + 1, dz + 1, web);
			}
			case 2 -> {
				web(dx + 1, dy, dz + 1, web); web(dx, dy + 1, dz + 1, web);
				web(dx + 2, dy + 1, dz + 1, web); web(dx + 1, dy + 2, dz + 1, web);
				web(dx, dy, dz, web); web(dx + 2, dy, dz, web);
				web(dx + 1, dy + 1, dz, web);
			}
			case 3 -> {
				web(dx + 1, dy, dz + 1, web); web(dx + 1, dy + 1, dz, web);
				web(dx + 1, dy + 1, dz + 2, web); web(dx + 1, dy + 2, dz + 1, web);
				web(dx + 2, dy, dz, web); web(dx + 2, dy, dz + 2, web);
				web(dx + 2, dy + 1, dz + 1, web);
			}
			default -> { }
		}
	}

	private void web(int x, int y, int z, int blockId) {
		this.worldObj.setBlockAndMetadataRaw(x, y, z, blockId, 0);
	}

	private void deadEndTreasure(int x, int z, int f) {
		int[] s = endSpot(x, z, f);
		placeTreasureChest(s[0], s[1], s[2], TFTreasure.UNDERHILL_DEADEND);
	}

	private void deadEndSpawner(int x, int z, int f) {
		String mobId = this.rand.nextInt(3) == 0 ? "minecraft:skeleton" : "minecraft:zombie";
		int[] s = endSpot(x, z, f);
		placeMobSpawner(s[0], s[1], s[2], mobId);
	}

	private void deadEndPainting(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int torch = Blocks.TORCH_COAL.id();

		String art = ArtType.values.get(this.rand.nextInt(7)).key;
		EntityPainting painting;

		switch (f) {
			case 1 -> {
				web(dx, dy + 1, dz, torch);
				painting = new EntityPainting(this.worldObj, dx - 1, dy + 1, dz + 1, 3, art);
				web(dx, dy + 1, dz + 2, torch);
			}
			case 2 -> {
				web(dx, dy + 1, dz, torch);
				painting = new EntityPainting(this.worldObj, dx + 1, dy + 1, dz - 1, 2, art);
				web(dx + 2, dy + 1, dz, torch);
			}
			case 3 -> {
				web(dx + 2, dy + 1, dz, torch);
				painting = new EntityPainting(this.worldObj, dx + 3, dy + 1, dz + 1, 1, art);
				web(dx + 2, dy + 1, dz + 2, torch);
			}
			default -> {
				web(dx, dy + 1, dz + 2, torch);
				painting = new EntityPainting(this.worldObj, dx + 1, dy + 1, dz + 3, 0, art);
				web(dx + 2, dy + 1, dz + 2, torch);
			}
		}

		if (painting.canStay() && !this.worldObj.isClientSide) {
			this.worldObj.entityJoinedWorld(painting);
		}
	}

	private void deadEndTrap(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		web(dx + 1, dy, dz + 1, Blocks.PRESSURE_PLATE_STONE.id());

		int[] s = endSpot(x, z, f);
		web(s[0], s[1] - 1, s[2], Blocks.TNT.id());
	}

	private void deadEndTrappedChest(int x, int z, int f) {
		deadEndTrap(x, z, f);
		deadEndTreasure(x, z, f);
	}

	private void deadEndTorch(int x, int z, int f) {
		int[] s = endSpot(x, z, f);
		web(s[0], s[1] + 1, s[2], Blocks.TORCH_COAL.id());
	}

	private void deadEndTorchRedstone(int x, int z, int f) {
		int[] s = endSpot(x, z, f);
		web(s[0], s[1] + 1, s[2], Blocks.TORCH_REDSTONE_ACTIVE.id());
	}

	private void deadEndNook(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int stone = Blocks.STONE.id();

		for (int a = 0; a <= 2; a++) {
			for (int b = 0; b <= 2; b++) {
				if (a == 1 && b == 1) {
					continue;
				}
				switch (f) {
					case 0 -> web(dx + a, dy + b, dz + 2, stone);
					case 1 -> web(dx, dy + b, dz + a, stone);
					case 2 -> web(dx + a, dy + b, dz, stone);
					case 3 -> web(dx + 2, dy + b, dz + a, stone);
					default -> { }
				}
			}
		}
	}

	private void deadEndFountain(int x, int z, int f) {
		fountain(x, z, f, Blocks.FLUID_WATER_FLOWING.id());
	}

	private void deadEndLavaFountain(int x, int z, int f) {
		fountain(x, z, f, Blocks.FLUID_LAVA_FLOWING.id());
	}

	private void fountain(int x, int z, int f, int fluidId) {
		deadEndNook(x, z, f);
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		web(dx + 1, dy - 1, dz + 1, 0);

		int[] s = endSpot(x, z, f);
		web(s[0], s[1] + 1, s[2], fluidId);
	}

	private void deadEndDoorway(int x, int z, int f) {
		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);
		int wall = this.maze.wallBlockId;

		switch (f) {
			case 0 -> {
				web(dx, dy, dz, wall); web(dx + 2, dy, dz, wall);
				web(dx, dy + 1, dz, wall); web(dx + 2, dy + 1, dz, wall);
				web(dx, dy + 2, dz, wall); web(dx + 1, dy + 2, dz, wall);
				web(dx + 2, dy + 2, dz, wall);
			}
			case 1 -> {
				web(dx + 2, dy, dz, wall); web(dx + 2, dy, dz + 2, wall);
				web(dx + 2, dy + 1, dz, wall); web(dx + 2, dy + 1, dz + 2, wall);
				web(dx + 2, dy + 2, dz, wall); web(dx + 2, dy + 2, dz + 1, wall);
				web(dx + 2, dy + 2, dz + 2, wall);
			}
			case 2 -> {
				web(dx, dy, dz + 2, wall); web(dx + 2, dy, dz + 2, wall);
				web(dx, dy + 1, dz + 2, wall); web(dx + 2, dy + 1, dz + 2, wall);
				web(dx, dy + 2, dz + 2, wall); web(dx + 1, dy + 2, dz + 2, wall);
				web(dx + 2, dy + 2, dz + 2, wall);
			}
			case 3 -> {
				web(dx, dy, dz, wall); web(dx, dy, dz + 2, wall);
				web(dx, dy + 1, dz, wall); web(dx, dy + 1, dz + 2, wall);
				web(dx, dy + 2, dz, wall); web(dx, dy + 2, dz + 1, wall);
				web(dx, dy + 2, dz + 2, wall);
			}
			default -> { }
		}
	}

	private void deadEndDoor(int x, int z, int f) {
		placeDoor(x, z, f, Blocks.DOOR_PLANKS_OAK_BOTTOM.id(), Blocks.DOOR_PLANKS_OAK_TOP.id());
	}

	private void deadEndDoorSteel(int x, int z, int f) {
		placeDoor(x, z, f, Blocks.DOOR_IRON_BOTTOM.id(), Blocks.DOOR_IRON_TOP.id());
	}

	private void placeDoor(int x, int z, int f, int bottomId, int topId) {
		deadEndDoorway(x, z, f);

		int dx = this.maze.getWorldX(x);
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(z);

		int doorX;
		int doorZ;
		int rotation;
		switch (f) {
			case 1 -> { doorX = dx + 2; doorZ = dz + 1; rotation = 2; }
			case 2 -> { doorX = dx + 1; doorZ = dz + 2; rotation = 3; }
			case 3 -> { doorX = dx; doorZ = dz + 1; rotation = 0; }
			default -> { doorX = dx + 1; doorZ = dz; rotation = 1; }
		}

		this.worldObj.setBlockAndMetadataRaw(doorX, dy, doorZ, bottomId, rotation);
		this.worldObj.setBlockAndMetadataRaw(doorX, dy + 1, doorZ, topId, rotation);
	}

	private void deadEndDoorTreasure(int x, int z, int f) {
		deadEndDoor(x, z, f);
		deadEndTreasure(x, z, f);
	}

	private void decorate3x3Room(int cellX, int cellZ) {
		int dx = this.maze.getWorldX(cellX) + 1;
		int dy = this.maze.worldY;
		int dz = this.maze.getWorldZ(cellZ) + 1;

		roomSpawner(dx, dy, dz, 11);
		if (!roomTreasure(dx, dy, dz, 11) || this.rand.nextInt(2) == 0) {
			roomTreasure(dx, dy, dz, 11);
		}
		roomSpiderwebs(dx, dy, dz);
	}

	private void roomSpawner(int dx, int dy, int dz, int diameter) {
		int rx = this.rand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.rand.nextInt(diameter) + dz - diameter / 2;
		placeMobSpawner(rx, dy, rz, "minecraft:skeleton");
	}

	private boolean roomTreasure(int dx, int dy, int dz, int diameter) {
		int rx = this.rand.nextInt(diameter) + dx - diameter / 2;
		int rz = this.rand.nextInt(diameter) + dz - diameter / 2;
		if (this.worldObj.getBlockId(rx, dy, rz) != 0) {
			return false;
		}
		placeTreasureChest(rx, dy, rz, TFTreasure.UNDERHILL_ROOM);
		return true;
	}

	private void roomSpiderwebs(int dx, int dy, int dz) {
		int rx = dx;
		int rz = dz;
		switch (this.rand.nextInt(4)) {
			case 0 -> { rx = dx - 5; rz = dz - 5; }
			case 1 -> { rx = dx + 5; rz = dz - 5; }
			case 2 -> { rx = dx - 5; rz = dz + 5; }
			case 3 -> { rx = dx + 5; rz = dz + 5; }
			default -> { }
		}

		roomSpiderweb(rx, dy, rz);
		roomSpiderweb(rx, dy, rz);
		roomSpiderweb(rx, dy, rz);
	}

	private void roomSpiderweb(int dx, int dy, int dz) {
		int rx = this.rand.nextInt(3) + dx - 1;
		int rz = this.rand.nextInt(3) + dz - 1;
		if (this.worldObj.getBlockId(rx, dy + 2, rz) == 0) {
			web(rx, dy + 2, rz, Blocks.COBWEB.id());
		}
	}

	private void placeMobSpawner(int dx, int dy, int dz, String mobId) {
		this.worldObj.setBlockWithNotify(dx, dy, dz, Blocks.MOBSPAWNER.id());
		if (this.worldObj.getTileEntity(dx, dy, dz) instanceof TileEntityMobSpawner spawner) {
			spawner.setMobId(mobId);
		}
	}

	private void placeTreasureChest(int dx, int dy, int dz, TFTreasureTable table) {
		TFTreasure.place(this.worldObj, this.rand, dx, dy, dz, table);
	}
}
