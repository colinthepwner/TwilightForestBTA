package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFNagaTemple extends TFWorldFeature {

	private static final int RADIUS = 46;

	private static final int PILLARS = 20;

	private static final int PILLAR_HEIGHT = 8;

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		fillFloor(x - RADIUS, y - 1, z - RADIUS, RADIUS * 2 + 1, RADIUS * 2 + 1, Blocks.GRASS.id());

		ruinFloor(world, rand, x, y, z);
		buildWalls(world, rand, x, y, z);

		world.setBlockWithNotify(x, y + 2, z, TFBlocks.BOSS_SPAWNER.id());

		for (int i = 0; i < PILLARS; i++) {
			int rx = x - RADIUS + 2 + rand.nextInt(2 * RADIUS - 4);
			int rz = z - RADIUS + 2 + rand.nextInt(2 * RADIUS - 4);
			makePillar(world, rand, rx, y, rz);
		}
		return true;
	}

	private void ruinFloor(World world, Random rand, int x, int y, int z) {
		for (int fx = -RADIUS; fx <= RADIUS; fx++) {
			for (int fz = -RADIUS; fz <= RADIUS; fz++) {
				if (rand.nextInt(3) != 0) {
					continue;
				}
				world.setBlockWithNotify(x + fx, y, z + fz, 0);
				world.setBlockWithNotify(x + fx, y - 1, z + fz, Blocks.STONE_POLISHED.id());
				if (rand.nextInt(20) == 0) {
					world.setBlockWithNotify(x + fx, y, z + fz, Blocks.SLAB_STONE_POLISHED.id());
				}
			}
		}
	}

	private void buildWalls(World world, Random rand, int x, int y, int z) {
		for (int fx = -RADIUS; fx <= RADIUS; fx++) {
			randStone(world, rand, x + fx, y, z + RADIUS);
			randStone(world, rand, x + fx, y, z - RADIUS);
			randStone(world, rand, x + fx, y + 1, z + RADIUS);
			randStone(world, rand, x + fx, y + 1, z - RADIUS);

			if (fx % 2 == 0) {
				slab(world, x + fx, y + 2, z + RADIUS);
				randStone(world, rand, x + fx, y + 2, z - RADIUS);
			} else {
				slab(world, x + fx, y + 2, z - RADIUS);
				randStone(world, rand, x + fx, y + 2, z + RADIUS);
			}
		}

		for (int fz = -RADIUS; fz <= RADIUS; fz++) {
			randStone(world, rand, x + RADIUS, y, z + fz);
			randStone(world, rand, x - RADIUS, y, z + fz);
			randStone(world, rand, x + RADIUS, y + 1, z + fz);
			randStone(world, rand, x - RADIUS, y + 1, z + fz);

			if (fz % 2 == 0) {
				slab(world, x + RADIUS, y + 2, z + fz);
				randStone(world, rand, x - RADIUS, y + 2, z + fz);
			} else {
				slab(world, x - RADIUS, y + 2, z + fz);
				randStone(world, rand, x + RADIUS, y + 2, z + fz);
			}
		}
	}

	private boolean makePillar(World world, Random rand, int x, int y, int z) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx == 0 && dz == 0) continue;
				slab(world, x + dx, y, z + dz);
			}
		}

		for (int i = 0; i < PILLAR_HEIGHT; i++) {
			randStone(world, rand, x, y + i, z);

			if (i > 0 && rand.nextInt(2) == 0) {
				rand.nextInt(4);
			} else if (i > 0 && rand.nextInt(4) == 0) {
				int face = rand.nextInt(4);
				int fx = face == 0 ? -1 : face == 1 ? 1 : 0;
				int fz = face == 2 ? 1 : face == 3 ? -1 : 0;
				world.setBlockWithNotify(x + fx, y + i, z + fz, TFBlocks.FIREFLY.id());
			}
		}

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx == 0 && dz == 0) continue;
				slab(world, x + dx, y + PILLAR_HEIGHT, z + dz);
			}
		}
		slab(world, x, y + PILLAR_HEIGHT, z);
		return true;
	}

	private void randStone(World world, Random rand, int x, int y, int z) {
		int variant = rand.nextInt(3);
		int block = variant == 1 ? Blocks.BRICK_STONE_POLISHED_MOSSY.id() : Blocks.BRICK_STONE_POLISHED.id();
		world.setBlockWithNotify(x, y, z, block);
	}

	private void slab(World world, int x, int y, int z) {
		world.setBlockWithNotify(x, y, z, Blocks.SLAB_STONE_POLISHED.id());
	}
}
