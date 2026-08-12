package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFMangroveTree extends TFWorldFeature {
	private Random treeRNG;
	private int x;
	private int y;
	private int z;

	private final int height = 0;

	private int treeBlock;
	private int treeMeta;
	private int leafBlock;
	private int leafMeta;

	@Override
	public boolean generate(World world, Random random, int treeX, int treeY, int treeZ) {
		this.worldObj = world;
		this.treeRNG = random;
		this.x = treeX;
		this.y = treeY;
		this.z = treeZ;

		this.treeBlock = TFBlocks.LOG_MANGROVE.id();
		this.treeMeta = 0;
		this.leafBlock = TFBlocks.LEAVES_MANGROVE.id();
		this.leafMeta = 0;

		int below = getBlockId(world, this.x, this.y - 1, this.z);
		if (below != Blocks.FLUID_WATER_STILL.id() || this.y >= 128 - this.height - 1) {
			return false;
		}

		this.buildBranch(5, 6 + this.treeRNG.nextInt(3), 0.0, 0.0);

		int numBranches = this.treeRNG.nextInt(3);
		double offset = this.treeRNG.nextDouble();
		for (int b = 0; b < numBranches; b++) {
			this.buildBranch(7 + b, 6 + this.treeRNG.nextInt(2), 0.3 * b + offset, 0.25);
		}

		int numRoots = 3 + this.treeRNG.nextInt(2);
		offset = this.treeRNG.nextDouble();
		for (int i = 0; i < numRoots; i++) {

			double rTilt = 0.75 + this.treeRNG.nextDouble() * 0.1;
			this.buildRoot(5, 8.0, 0.4 * i + offset, rTilt);
		}

		this.addFirefly(5 + this.treeRNG.nextInt(5), this.treeRNG.nextDouble());
		return true;
	}

	void buildBranch(int branchHeight, double length, double angle, double tilt) {
		int[] src = new int[]{this.x, this.y + branchHeight, this.z};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);

		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2],
			this.treeBlock, this.treeMeta, true);

		this.putBlockAndMetadata(dest[0] + 1, dest[1], dest[2], this.treeBlock, this.treeMeta, true);
		this.putBlockAndMetadata(dest[0] - 1, dest[1], dest[2], this.treeBlock, this.treeMeta, true);
		this.putBlockAndMetadata(dest[0], dest[1], dest[2] + 1, this.treeBlock, this.treeMeta, true);
		this.putBlockAndMetadata(dest[0], dest[1], dest[2] - 1, this.treeBlock, this.treeMeta, true);

		int bSize = 2 + this.treeRNG.nextInt(3);
		this.drawCircle(dest[0], dest[1] - 1, dest[2], bSize - 1, this.leafBlock, this.leafMeta, false);
		this.drawCircle(dest[0], dest[1], dest[2], bSize, this.leafBlock, this.leafMeta, false);
		this.drawCircle(dest[0], dest[1] + 1, dest[2], bSize - 2, this.leafBlock, this.leafMeta, false);
	}

	void buildRoot(int rootHeight, double length, double angle, double tilt) {
		int[] src = new int[]{this.x, this.y + rootHeight, this.z};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);
		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2],
			this.treeBlock, this.treeMeta, true);
	}

	private void addFirefly(int fireflyHeight, double angle) {
		int iAngle = (int) (angle * 4.0);

		int torch = TFBlocks.FIREFLY.id();
		switch (iAngle) {
			case 0 -> this.putBlockAndMetadata(this.x + 1, this.y + fireflyHeight, this.z, torch, 1, false);
			case 1 -> this.putBlockAndMetadata(this.x - 1, this.y + fireflyHeight, this.z, torch, 2, false);
			case 2 -> this.putBlockAndMetadata(this.x, this.y + fireflyHeight, this.z + 1, torch, 3, false);
			case 3 -> this.putBlockAndMetadata(this.x, this.y + fireflyHeight, this.z - 1, torch, 4, false);
			default -> {  }
		}
	}
}
