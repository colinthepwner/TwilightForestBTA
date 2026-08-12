package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntityMobSpawner;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFWitchHut extends TFWorldFeature {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		return this.generateTinyHut(world, rand, x, y, z);
	}

	public boolean generateTinyHut(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		if (!this.isAreaClear(world, rand, x, y, z, 5, 7, 6)) {
			return false;
		}

		int brick = Blocks.BRICK_CLAY.id();
		int plank = Blocks.PLANKS_OAK.id();
		int slab = Blocks.SLAB_PLANKS_OAK.id();

		this.putBlock(x + 1, y, z + 1, this.randStone(rand, 1), true);
		this.putBlock(x + 2, y, z + 1, this.randStone(rand, 1), true);
		this.putBlock(x + 3, y, z + 1, this.randStone(rand, 1), true);
		this.putBlock(x + 5, y, z + 1, this.randStone(rand, 1), true);
		this.putBlock(x, y, z + 2, brick, true);
		this.putBlock(x + 1, y, z + 2, brick, true);
		this.putBlock(x + 5, y, z + 2, this.randStone(rand, 1), true);
		this.putBlock(x, y, z + 3, brick, true);
		this.putBlock(x + 5, y, z + 3, this.randStone(rand, 1), true);
		this.putBlock(x, y, z + 4, brick, true);
		this.putBlock(x + 1, y, z + 4, brick, true);
		this.putBlock(x + 5, y, z + 4, this.randStone(rand, 1), true);
		this.putBlock(x + 1, y, z + 5, this.randStone(rand, 1), true);
		this.putBlock(x + 2, y, z + 5, this.randStone(rand, 1), true);
		this.putBlock(x + 3, y, z + 5, this.randStone(rand, 1), true);
		this.putBlock(x + 5, y, z + 5, this.randStone(rand, 1), true);

		this.putBlock(x + 1, y + 1, z + 1, this.randStone(rand, 2), true);
		this.putBlock(x + 3, y + 1, z + 1, this.randStone(rand, 2), true);
		this.putBlock(x + 5, y + 1, z + 1, this.randStone(rand, 2), true);
		this.putBlock(x, y + 1, z + 2, brick, true);
		this.putBlock(x + 1, y + 1, z + 2, brick, true);
		this.putBlock(x + 5, y + 1, z + 2, this.randStone(rand, 2), true);
		this.putBlock(x, y + 1, z + 3, brick, true);
		this.putBlock(x, y + 1, z + 4, brick, true);
		this.putBlock(x + 1, y + 1, z + 4, brick, true);
		this.putBlock(x + 5, y + 1, z + 4, this.randStone(rand, 2), true);
		this.putBlock(x + 1, y + 1, z + 5, this.randStone(rand, 2), true);
		this.putBlock(x + 3, y + 1, z + 5, this.randStone(rand, 2), true);
		this.putBlock(x + 5, y + 1, z + 5, this.randStone(rand, 2), true);

		this.putBlock(x + 1, y + 2, z + 1, this.randStone(rand, 3), true);
		this.putBlock(x + 2, y + 2, z + 1, this.randStone(rand, 3), true);
		this.putBlock(x + 3, y + 2, z + 1, this.randStone(rand, 3), true);
		this.putBlock(x + 4, y + 2, z + 1, this.randStone(rand, 3), true);
		this.putBlock(x + 5, y + 2, z + 1, this.randStone(rand, 3), true);
		this.putBlock(x, y + 2, z + 2, brick, true);
		this.putBlock(x + 1, y + 2, z + 2, brick, true);
		this.putBlock(x + 5, y + 2, z + 2, this.randStone(rand, 3), true);
		this.putBlock(x, y + 2, z + 3, brick, true);
		this.putBlock(x + 5, y + 2, z + 3, this.randStone(rand, 3), true);
		this.putBlock(x, y + 2, z + 4, brick, true);
		this.putBlock(x + 1, y + 2, z + 4, brick, true);

		this.putBlock(x + 5, y + 2, z + 4, this.randStone(rand, 1), true);
		this.putBlock(x + 1, y + 2, z + 5, this.randStone(rand, 3), true);
		this.putBlock(x + 2, y + 2, z + 5, this.randStone(rand, 3), true);
		this.putBlock(x + 3, y + 2, z + 5, this.randStone(rand, 3), true);
		this.putBlock(x + 4, y + 2, z + 5, this.randStone(rand, 3), true);
		this.putBlock(x + 5, y + 2, z + 5, this.randStone(rand, 3), true);

		this.putBlock(x, y + 3, z + 2, brick, true);
		this.putBlock(x, y + 3, z + 3, brick, true);
		this.putBlock(x, y + 3, z + 4, brick, true);
		this.putBlock(x + 2, y + 3, z + 1, this.randStone(rand, 4), true);
		this.putBlock(x + 3, y + 3, z + 1, this.randStone(rand, 4), true);
		this.putBlock(x + 4, y + 3, z + 1, this.randStone(rand, 4), true);
		this.putBlock(x + 2, y + 3, z + 5, this.randStone(rand, 4), true);
		this.putBlock(x + 3, y + 3, z + 5, this.randStone(rand, 4), true);
		this.putBlock(x + 4, y + 3, z + 5, this.randStone(rand, 4), true);

		this.putBlock(x, y + 4, z + 3, brick, true);
		this.putBlock(x + 3, y + 4, z + 1, this.randStone(rand, 5), true);
		this.putBlock(x + 3, y + 4, z + 5, this.randStone(rand, 5), true);
		this.putBlock(x, y + 5, z + 3, brick, true);
		this.putBlock(x, y + 6, z + 3, brick, true);

		int[][] roofPlanks = {
			{0, 2, 0}, {0, 2, 1}, {0, 2, 5}, {0, 2, 6},
			{6, 2, 0}, {6, 2, 1}, {6, 2, 2}, {6, 2, 3}, {6, 2, 4}, {6, 2, 5}, {6, 2, 6},
			{1, 3, 0}, {1, 3, 1}, {1, 3, 2}, {1, 3, 4}, {1, 3, 5}, {1, 3, 6},
			{5, 3, 0}, {5, 3, 1}, {5, 3, 2}, {5, 3, 3}, {5, 3, 4}, {5, 3, 5}, {5, 3, 6},
			{2, 4, 0}, {2, 4, 1}, {2, 4, 2}, {2, 4, 3}, {2, 4, 4}, {2, 4, 5}, {2, 4, 6},
			{4, 4, 0}, {4, 4, 1}, {4, 4, 2}, {4, 4, 3}, {4, 4, 4}, {4, 4, 5}, {4, 4, 6},
			{3, 5, 0}, {3, 5, 1}, {3, 5, 2}, {3, 5, 3}, {3, 5, 4}, {3, 5, 5}, {3, 5, 6},
			{3, 6, 0}, {3, 6, 1}, {3, 6, 5}, {3, 6, 6},
		};
		for (int[] p : roofPlanks) {
			this.putBlock(x + p[0], y + p[1], z + p[2], plank, true);
		}

		int[][] roofSlabs = {
			{1, 4, 0}, {1, 4, 6}, {5, 4, 0}, {5, 4, 6},
			{2, 5, 0}, {2, 5, 1}, {4, 5, 0}, {4, 5, 1},
			{2, 5, 5}, {2, 5, 6}, {4, 5, 5}, {4, 5, 6},
			{3, 6, 2}, {3, 6, 4},
			{3, 7, 0}, {3, 7, 6},
		};
		for (int[] p : roofSlabs) {
			this.putBlock(x + p[0], y + p[1], z + p[2], slab, true);
		}

		this.putBlock(x + 1, y - 1, z + 3, Blocks.NETHERRACK.id(), true);

		this.worldObj.setBlockWithNotify(x + 3, y + 1, z + 3, Blocks.MOBSPAWNER.id());
		if (world.getTileEntity(x + 3, y + 1, z + 3) instanceof TileEntityMobSpawner spawner) {
			spawner.setMobId("Skeleton");
		}

		return true;
	}
}
