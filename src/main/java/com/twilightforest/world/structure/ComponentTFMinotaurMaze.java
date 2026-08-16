package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.world.feature.TFFeature;
import com.twilightforest.world.feature.TFMaze;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.List;
import java.util.Random;

public class ComponentTFMinotaurMaze extends StructureComponentTF {

	private static final int FLOOR_LEVEL = 1;

	private static final int ROOM_COUNT = 7;

	public static final int MAZESTONE_PLAIN = 0;

	public static final int MAZESTONE_BRICK = 1;

	public static final int MAZESTONE_PILLAR = 2;

	public static final int MAZESTONE_DECO = 3;

	public static final int MAZESTONE_CRACKED = 4;

	public static final int MAZESTONE_MOSSY = 5;

	public static final int MAZESTONE_MOSAIC = 6;

	public static final int MAZESTONE_BORDER = 7;

	public static int mazestone(int variant) {
		return switch (variant) {
			case MAZESTONE_PILLAR, MAZESTONE_CRACKED, MAZESTONE_BORDER -> TFBlocks.MAZESTONE_COBBLE.id();
			case MAZESTONE_DECO, MAZESTONE_MOSSY -> TFBlocks.MAZESTONE_MOSSY.id();
			default -> TFBlocks.MAZESTONE.id();
		};
	}

	private final TFMaze maze;

	private final int[] rcoords;

	private final int level;

	public ComponentTFMinotaurMaze(int componentType, int x, int y, int z,
	                               int entranceX, int entranceZ, int level) {
		super(componentType);
		this.coordBaseMode = 0;
		this.level = level;
		this.boundingBox = componentBox(x, y, z,
			-getRadius(), 0, -getRadius(), getRadius() * 2, 5, getRadius() * 2, 0);

		this.maze = new TFMaze(getMazeSize(), getMazeSize());

		this.maze.setSeed((this.boundingBox.minX * 90342903 + this.boundingBox.minY * 90342903)
			^ this.boundingBox.minZ);

		this.rcoords = new int[ROOM_COUNT * 2];
		this.rcoords[0] = entranceX;
		this.rcoords[1] = entranceZ;
		this.maze.carveRoom1(entranceX, entranceZ);

		for (int i = 1; i < ROOM_COUNT; i++) {
			int rx;
			int rz;

			do {
				rx = this.maze.rand.nextInt(getMazeSize() - 2) + 1;
				rz = this.maze.rand.nextInt(getMazeSize() - 2) + 1;
			} while (isNearRoom(rx, rz, this.rcoords, i == 1 ? 7 : 4));

			this.maze.carveRoom1(rx, rz);
			this.rcoords[i * 2] = rx;
			this.rcoords[i * 2 + 1] = rz;
		}

		this.maze.generateRecursiveBacktracker(0, 0);
	}

	public ComponentTFMinotaurMaze(int componentType, int x, int y, int z, int level) {
		this(componentType, x, y, z, 11, 11, level);
	}

	@Override
	public int featureType() {
		return TFFeature.LABYRINTH;
	}

	@Override
	public void buildComponent(StructureComponentTF parent, List<StructureComponentTF> pieces,
	                           Random rand) {
		super.buildComponent(parent, pieces, rand);

		if (this.level == 1) {
			int centerX = this.boundingBox.minX + (this.boundingBox.maxX - this.boundingBox.minX) / 2;
			int centerZ = this.boundingBox.minZ + (this.boundingBox.maxZ - this.boundingBox.minZ) / 2;
			ComponentTFMinotaurMaze lower = new ComponentTFMinotaurMaze(1,
				centerX, this.boundingBox.minY - 10, centerZ,
				this.rcoords[2], this.rcoords[3], 2);
			pieces.add(lower);
			lower.buildComponent(this, pieces, rand);
		}

		for (int i = 0; i < this.rcoords.length / 2; i++) {
			ComponentTFMazeRoom room = makeRoom(rand, i, this.rcoords[i * 2], this.rcoords[i * 2 + 1]);
			pieces.add(room);
			room.buildComponent(this, pieces, rand);
		}

		decorateDeadEndsCorridors(rand, pieces);
	}

	protected ComponentTFMazeRoom makeRoom(Random random, int i, int dx, int dz) {
		int worldX = this.boundingBox.minX + dx * 5 - 4;
		int worldY = this.boundingBox.minY;
		int worldZ = this.boundingBox.minZ + dz * 5 - 4;

		if (i == 0) {
			return new ComponentTFMazeRoom(3 + i, random, worldX, worldY, worldZ);
		}
		if (i == 1) {
			return this.level == 1
				? new ComponentTFMazeRoomExit(3 + i, random, worldX, worldY, worldZ)
				: new ComponentTFMazeRoomBoss(3 + i, random, worldX, worldY, worldZ);
		}
		if (i == 2 || i == 3) {
			return this.level == 1
				? new ComponentTFMazeRoomCollapse(3 + i, random, worldX, worldY, worldZ)
				: new ComponentTFMazeMushRoom(3 + i, random, worldX, worldY, worldZ);
		}
		if (i == 4) {
			return this.level == 1
				? new ComponentTFMazeRoomFountain(3 + i, random, worldX, worldY, worldZ)
				: new ComponentTFMazeRoomVault(3 + i, random, worldX, worldY, worldZ);
		}
		return new ComponentTFMazeRoomSpawnerChests(3 + i, random, worldX, worldY, worldZ);
	}

	protected void decorateDeadEndsCorridors(Random random, List<StructureComponentTF> pieces) {
		for (int x = 0; x < this.maze.width; x++) {
			for (int z = 0; z < this.maze.depth; z++) {
				StructureComponentTF component = null;

				boolean west = this.maze.isWall(x, z, x - 1, z);
				boolean east = this.maze.isWall(x, z, x + 1, z);
				boolean north = this.maze.isWall(x, z, x, z - 1);
				boolean south = this.maze.isWall(x, z, x, z + 1);

				if (!west && east && north && south) {
					component = makeDeadEnd(random, x, z, 3);
				}
				if (west && !east && north && south) {
					component = makeDeadEnd(random, x, z, 1);
				}
				if (west && east && !north && south) {
					component = makeDeadEnd(random, x, z, 0);
				}
				if (west && east && north && !south) {
					component = makeDeadEnd(random, x, z, 2);
				}

				if (!west && !east && north && south
					&& this.maze.isWall(x - 1, z, x - 1, z - 1) && this.maze.isWall(x - 1, z, x - 1, z + 1)
					&& this.maze.isWall(x + 1, z, x + 1, z - 1) && this.maze.isWall(x + 1, z, x + 1, z + 1)) {
					component = makeCorridor(random, x, z, 1);
				}

				if (!north && !south && west && east
					&& this.maze.isWall(x, z - 1, x - 1, z - 1) && this.maze.isWall(x, z - 1, x + 1, z - 1)
					&& this.maze.isWall(x, z + 1, x - 1, z + 1) && this.maze.isWall(x, z + 1, x + 1, z + 1)) {
					component = makeCorridor(random, x, z, 0);
				}

				if (component == null) {
					continue;
				}
				pieces.add(component);
				component.buildComponent(this, pieces, random);
			}
		}
	}

	protected ComponentTFMazeDeadEnd makeDeadEnd(Random random, int dx, int dz, int rotation) {
		int worldX = this.boundingBox.minX + dx * 5 + 1;
		int worldY = this.boundingBox.minY;
		int worldZ = this.boundingBox.minZ + dz * 5 + 1;

		return switch (random.nextInt(8)) {
			case 1 -> new ComponentTFMazeDeadEndChest(4, worldX, worldY, worldZ, rotation);
			case 2 -> new ComponentTFMazeDeadEndTrappedChest(4, worldX, worldY, worldZ, rotation);
			case 3 -> new ComponentTFMazeDeadEndTorches(4, worldX, worldY, worldZ, rotation);
			case 4 -> new ComponentTFMazeDeadEndFountain(4, worldX, worldY, worldZ, rotation);
			case 5 -> new ComponentTFMazeDeadEndFountainLava(4, worldX, worldY, worldZ, rotation);
			case 6 -> new ComponentTFMazeDeadEndPainting(4, worldX, worldY, worldZ, rotation);
			case 7 -> this.level == 1
				? new ComponentTFMazeDeadEndRoots(4, worldX, worldY, worldZ, rotation)
				: new ComponentTFMazeDeadEndShrooms(4, worldX, worldY, worldZ, rotation);
			default -> new ComponentTFMazeDeadEnd(4, worldX, worldY, worldZ, rotation);
		};
	}

	protected ComponentTFMazeCorridor makeCorridor(Random random, int dx, int dz, int rotation) {
		int worldX = this.boundingBox.minX + dx * 5 + 1;
		int worldY = this.boundingBox.minY;
		int worldZ = this.boundingBox.minZ + dz * 5 + 1;

		return switch (random.nextInt(5)) {
			case 1 -> new ComponentTFMazeCorridor(4, worldX, worldY, worldZ, rotation);
			case 2 -> new ComponentTFMazeCorridorIronFence(4, worldX, worldY, worldZ, rotation);
			case 4 -> this.level == 1
				? new ComponentTFMazeCorridorRoots(4, worldX, worldY, worldZ, rotation)
				: new ComponentTFMazeCorridorShrooms(4, worldX, worldY, worldZ, rotation);
			default -> null;
		};
	}

	@Override
	public boolean addComponentParts(World world, Random rand, BoundingBox clip) {

		if (this.level == 2) {
			fillWithBlocks(world, clip, 0, -1, 0,
				getDiameter() + 2, 6, getDiameter() + 2, Blocks.BEDROCK.id(), 0, false);
		}

		fillWithBlocks(world, clip, 1, 1, 1, getDiameter(), 4, getDiameter(), 0, 0, false);

		fillWithBlocks(world, clip, 1, 5, 1, getDiameter(), 5, getDiameter(),
			mazestone(MAZESTONE_PLAIN), 0, Blocks.STONE.id(), 0, this.level == 1);
		fillWithBlocks(world, clip, 1, 0, 1, getDiameter(), 0, getDiameter(),
			mazestone(MAZESTONE_MOSAIC), 0, Blocks.STONE.id(), 0, false);

		this.maze.headBlockId = mazestone(MAZESTONE_DECO);
		this.maze.headBlockMeta = 0;
		this.maze.wallBlockId = mazestone(MAZESTONE_BRICK);
		this.maze.wallBlockMeta = 0;
		this.maze.rootBlockId = mazestone(MAZESTONE_DECO);
		this.maze.rootBlockMeta = 0;
		this.maze.pillarBlockId = mazestone(MAZESTONE_PILLAR);
		this.maze.pillarBlockMeta = 0;
		this.maze.wallVar0Id = mazestone(MAZESTONE_CRACKED);
		this.maze.wallVar0Meta = 0;
		this.maze.wallVarRarity = 0.2f;

		this.maze.torchRarity = 0.05f;

		this.maze.tall = 2;
		this.maze.head = 1;
		this.maze.roots = 1;
		this.maze.oddBias = 4;

		this.maze.copyToStructure(world, 1, FLOOR_LEVEL + 1, 1, this, clip);
		return true;
	}

	public int getMazeSize() {
		return 22;
	}

	public int getRadius() {
		return (int) ((double) getMazeSize() * 2.5);
	}

	public int getDiameter() {
		return getMazeSize() * 5;
	}

	protected boolean isNearRoom(int dx, int dz, int[] rcoords, int range) {
		if (dx == 1 && dz == 1) {
			return true;
		}
		for (int i = 0; i < rcoords.length / 2; i++) {
			int rx = rcoords[i * 2];
			int rz = rcoords[i * 2 + 1];
			if (rx == 0 && rz == 0) {
				continue;
			}
			if (Math.abs(dx - rx) < range && Math.abs(dz - rz) < range) {
				return true;
			}
		}
		return false;
	}
}
