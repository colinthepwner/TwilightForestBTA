package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFTreeOfTime extends WorldFeatureTFHollowTree {

	private static final int TREE_HEIGHT = 8;
	private static final int TREE_DIAMETER = 1;

	@Override
	public boolean generate(World world, Random random, int treeX, int treeY, int treeZ) {
		this.worldObj = world;
		this.treeRNG = random;
		this.x = treeX;
		this.y = treeY;
		this.z = treeZ;
		this.height = TREE_HEIGHT;
		this.diameter = TREE_DIAMETER;

		this.treeBlock = TFBlocks.LOG_TIMEWOOD.id();
		this.leafBlock = TFBlocks.LEAVES_TIMEWOOD.id();

		this.rootBlock = TFBlocks.ROOTS.id();

		int maxY = world.getWorldType().getMaxY(world);
		if (this.y < 1 || this.y + this.height + this.diameter > maxY + 1) {
			return false;
		}

		int below = getBlockId(world, this.x, this.y - 1, this.z);
		if (below != Blocks.GRASS.id() && below != Blocks.DIRT.id()) {
			return false;
		}

		this.buildTrunk();
		this.buildTinyCrown();

		this.buildBranchRing(1, 0, 12, 0, 0.75, 0.0, 3, 5, 3, false);
		this.buildBranchRing(1, 2, 18, 0, 0.9, 0.0, 3, 5, 3, false);

		this.putBlock(this.x - 1, this.y + 2, this.z, this.treeBlock, true);
		return true;
	}

	private void buildTinyCrown() {
		int crownRadius = 4;
		int bvar = 1;
		this.buildBranchRing(this.height - crownRadius, 0, crownRadius, 0, 0.35, 0.0, bvar, bvar + 2, 1, true);
		this.buildBranchRing(this.height - crownRadius / 2, 0, crownRadius, 0, 0.28, 0.0, bvar, bvar + 2, 1, true);
		this.buildBranchRing(this.height, 0, crownRadius, 0, 0.15, 0.0, 2, 4, 0, true);
		this.buildBranchRing(this.height, 0, crownRadius / 2, 0, 0.05, 0.0, bvar, bvar + 2, 0, true);
	}
}
