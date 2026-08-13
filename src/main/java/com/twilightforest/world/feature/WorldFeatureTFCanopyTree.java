package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFCanopyTree extends TFWorldFeature {
	private Random treeRNG;
	private int x;
	private int y;
	private int z;

	private final int height = 0;

	protected int minHeight = 20;

	protected int treeBlock = TFBlocks.LOG_CANOPY.id();
	protected int treeMeta = 0;
	protected int leafBlock = TFBlocks.LEAVES_CANOPY.id();
	protected int leafMeta = 0;

	@Override
	public boolean generate(World world, Random random, int treeX, int treeY, int treeZ) {
		this.worldObj = world;
		this.treeRNG = random;
		this.x = treeX;
		this.y = treeY;
		this.z = treeZ;

		int below = getBlockId(world, this.x, this.y - 1, this.z);
		if ((below != Blocks.GRASS.id() && below != Blocks.DIRT.id())
			|| this.y >= 128 - this.height - 1) {
			return false;
		}

		if (getBlockId(world, this.x, this.y, this.z) == Blocks.FLUID_WATER_STILL.id()
			|| getBlockId(world, this.x, this.y, this.z) == Blocks.FLUID_WATER_FLOWING.id()) {
			return false;
		}

		this.buildBranch(0, this.minHeight, 0.0, 0.0);

		int numBranches = 3 + this.treeRNG.nextInt(2);
		double offset = this.treeRNG.nextDouble();
		for (int b = 0; b < numBranches; b++) {
			this.buildBranch(this.minHeight - 10 + b, 9.0, 0.3 * b + offset, 0.2);
		}

		this.addFirefly(3 + this.treeRNG.nextInt(7), this.treeRNG.nextDouble());
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

		this.drawCircle(dest[0], dest[1] - 1, dest[2], 3, this.leafBlock, this.leafMeta, false);
		this.drawCircle(dest[0], dest[1], dest[2], 4, this.leafBlock, this.leafMeta, false);
		this.drawCircle(dest[0], dest[1] + 1, dest[2], 2, this.leafBlock, this.leafMeta, false);
	}

	private static final double CICADA_SHARE = 0.25;

	private void addFirefly(int fireflyHeight, double angle) {

		double quarters = angle * 4.0;
		int iAngle = (int) quarters;

		int critter = (quarters - iAngle) < CICADA_SHARE ? TFBlocks.CICADA.id() : TFBlocks.FIREFLY.id();

		switch (iAngle) {

			case 0 -> this.putBlockAndMetadata(this.x + 1, this.y + fireflyHeight, this.z, critter, 1, false);

			case 1 -> this.putBlockAndMetadata(this.x - 1, this.y + fireflyHeight, this.z, critter, 2, false);

			case 2 -> this.putBlockAndMetadata(this.x, this.y + fireflyHeight, this.z + 1, critter, 3, false);

			case 3 -> this.putBlockAndMetadata(this.x, this.y + fireflyHeight, this.z - 1, critter, 4, false);
			default -> {  }
		}
	}
}
