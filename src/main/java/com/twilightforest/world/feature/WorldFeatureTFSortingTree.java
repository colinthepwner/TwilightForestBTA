package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFSortingTree extends TFWorldFeature {

	private final int treeBlock = TFBlocks.LOG_SORTINGWOOD.id();
	private final int leafBlock = TFBlocks.LEAVES_SORTING.id();

	private static final int META = 0;

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		this.worldObj = world;

		Material under = getBlockMaterial(world, x, y - 1, z);
		int maxY = world.getWorldType().getMaxY(world);
		if ((under != Materials.GRASS && under != Materials.DIRT) || y >= maxY + 1 - 12) {
			return false;
		}

		for (int dy = 0; dy < 4; dy++) {
			this.putBlockAndMetadata(x, y + dy, z, this.treeBlock, META, true);
		}

		this.putLeaves(x, y + 2, z, false);
		this.putLeaves(x, y + 3, z, false);

		this.putBlockAndMetadata(x, y + 1, z, this.treeBlock, META, true);
		return true;
	}

	private void putLeaves(int bx, int by, int bz, boolean bushy) {
		for (int lx = -1; lx <= 1; lx++) {
			for (int ly = -1; ly <= 1; ly++) {
				for (int lz = -1; lz <= 1; lz++) {
					if (!bushy && Math.abs(ly) > 0 && Math.abs(lx) + Math.abs(lz) > 1) {
						continue;
					}
					this.putBlockAndMetadata(bx + lx, by + ly, bz + lz, this.leafBlock, META, false);
				}
			}
		}
	}
}
