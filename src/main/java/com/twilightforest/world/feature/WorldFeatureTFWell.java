package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFWell extends TFWorldFeature {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		return rand.nextInt(4) == 0
			? this.generate4x4Well(world, rand, x, y, z)
			: this.generate3x3Well(world, rand, x, y, z);
	}

	public boolean generate3x3Well(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		if (!this.isAreaClear(world, rand, x, y, z, 3, 4, 3)) {
			return false;
		}

		int stone = Blocks.COBBLE_STONE_MOSSY.id();
		int water = Blocks.FLUID_WATER_STILL.id();
		int fence = Blocks.FENCE_PLANKS_OAK.id();
		int slab = Blocks.SLAB_PLANKS_OAK.id();
		int planks = Blocks.PLANKS_OAK.id();

		for (int dx = 0; dx <= 2; dx++) {
			for (int dz = 0; dz <= 2; dz++) {
				if (dx == 1 && dz == 1) {
					continue;
				}
				this.putBlock(x + dx, y, z + dz, stone, true);
			}
		}
		this.putBlock(x + 1, y, z + 1, water, true);

		for (int dy = 1; dy <= 2; dy++) {
			this.putBlock(x, y + dy, z, fence, true);
			this.putBlock(x + 2, y + dy, z, fence, true);
			this.putBlock(x, y + dy, z + 2, fence, true);
			this.putBlock(x + 2, y + dy, z + 2, fence, true);
		}

		for (int dx = 0; dx <= 2; dx++) {
			for (int dz = 0; dz <= 2; dz++) {
				if (dx == 1 && dz == 1) {
					this.putBlock(x + 1, y + 3, z + 1, planks, true);
				} else {
					this.putBlock(x + dx, y + 3, z + dz, slab, true);
				}
			}
		}

		this.sinkShaft(world, x + 1, y, z + 1, water);
		return true;
	}

	public boolean generate4x4Well(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		if (!this.isAreaClear(world, rand, x, y, z, 4, 4, 4)) {
			return false;
		}

		int stone = Blocks.COBBLE_STONE_MOSSY.id();
		int water = Blocks.FLUID_WATER_STILL.id();
		int fence = Blocks.FENCE_PLANKS_OAK.id();
		int slab = Blocks.SLAB_PLANKS_OAK.id();
		int planks = Blocks.PLANKS_OAK.id();

		for (int dx = 0; dx <= 3; dx++) {
			for (int dz = 0; dz <= 3; dz++) {
				boolean centre = dx >= 1 && dx <= 2 && dz >= 1 && dz <= 2;
				this.putBlock(x + dx, y, z + dz, centre ? water : stone, true);
			}
		}

		for (int dy = 1; dy <= 2; dy++) {
			this.putBlock(x, y + dy, z, fence, true);
			this.putBlock(x + 3, y + dy, z, fence, true);
			this.putBlock(x, y + dy, z + 3, fence, true);
			this.putBlock(x + 3, y + dy, z + 3, fence, true);
		}

		for (int dx = 0; dx <= 3; dx++) {
			for (int dz = 0; dz <= 3; dz++) {
				boolean centre = dx >= 1 && dx <= 2 && dz >= 1 && dz <= 2;
				this.putBlock(x + dx, y + 3, z + dz, centre ? planks : slab, true);
			}
		}

		for (int dx = 1; dx <= 2; dx++) {
			for (int dz = 1; dz <= 2; dz++) {
				this.sinkShaft(world, x + dx, y, z + dz, water);
			}
		}

		return true;
	}

	private void sinkShaft(World world, int x, int y, int z, int water) {
		int dirt = Blocks.DIRT.id();
		int grass = Blocks.GRASS.id();
		int gravel = Blocks.GRAVEL.id();
		int stone = Blocks.STONE.id();

		for (int dy = -1; dy >= -20; dy--) {
			int here = getBlockId(world, x, y + dy, z);
			boolean natural = here == dirt || here == grass || here == gravel || here == stone;
			if (!natural || !getBlockMaterial(world, x, y + dy - 1, z).isSolid()) {
				break;
			}
			this.putBlock(x, y + dy, z, water, true);
		}
	}
}
