package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFCanopyTree extends TFWorldFeature {
	private Random treeRNG;
	private int x;
	private int y;
	private int z;

	protected int minHeight = 20;

	protected int chanceAddFirstFive = 3;
	protected int chanceAddSecondFive = 8;

	protected int treeBlock = TFBlocks.LOG_CANOPY.id();
	protected int treeMeta = 0;
	protected int leafBlock = TFBlocks.LEAVES_CANOPY.id();
	protected int leafMeta = 0;

	protected int rootBlock = TFBlocks.ROOTS.id();

	@Override
	public boolean generate(World world, Random random, int treeX, int treeY, int treeZ) {
		this.worldObj = world;
		this.treeRNG = random;
		this.x = treeX;
		this.y = treeY;
		this.z = treeZ;

		Material under = getBlockMaterial(world, this.x, this.y - 1, this.z);
		int maxY = world.getWorldType().getMaxY(world);
		if ((under != Materials.GRASS && under != Materials.DIRT) || this.y >= maxY + 1 - 12) {
			return false;
		}

		if (getBlockId(world, this.x, this.y, this.z) == Blocks.FLUID_WATER_STILL.id()
			|| getBlockId(world, this.x, this.y, this.z) == Blocks.FLUID_WATER_FLOWING.id()) {
			return false;
		}

		int treeHeight = this.minHeight;
		if (this.treeRNG.nextInt(this.chanceAddFirstFive) == 0) {
			treeHeight += this.treeRNG.nextInt(5);
			if (this.treeRNG.nextInt(this.chanceAddSecondFive) == 0) {
				treeHeight += this.treeRNG.nextInt(5);
			}
		}

		this.buildBranch(0, treeHeight, 0.0, 0.0, true);

		int numBranches = 3 + this.treeRNG.nextInt(2);
		double offset = this.treeRNG.nextDouble();
		for (int b = 0; b < numBranches; b++) {
			this.buildBranch(treeHeight - 10 + b, 9.0, 0.3 * b + offset, 0.2, false);
		}

		this.putBlock(this.x, this.y - 1, this.z,
			this.hasAirAround(world, this.x, this.y - 1, this.z) ? this.treeBlock : this.rootBlock,
			true);

		int numRoots = 3 + this.treeRNG.nextInt(2);
		offset = this.treeRNG.nextDouble();
		for (int b = 0; b < numRoots; b++) {
			this.buildRoot(world, offset, b);
		}

		return true;
	}

	void buildBranch(int branchHeight, double length, double angle, double tilt, boolean trunk) {
		int[] src = new int[]{this.x, this.y + branchHeight, this.z};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);

		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2],
			this.treeBlock, this.treeMeta, true);

		if (trunk) {
			this.addFirefly(3 + this.treeRNG.nextInt(7), this.treeRNG.nextDouble());
		}

		this.putBlockAndMetadata(dest[0] + 1, dest[1], dest[2], this.treeBlock, this.treeMeta, true);
		this.putBlockAndMetadata(dest[0] - 1, dest[1], dest[2], this.treeBlock, this.treeMeta, true);
		this.putBlockAndMetadata(dest[0], dest[1], dest[2] + 1, this.treeBlock, this.treeMeta, true);
		this.putBlockAndMetadata(dest[0], dest[1], dest[2] - 1, this.treeBlock, this.treeMeta, true);

		this.drawCircle(dest[0], dest[1] - 1, dest[2], 3, this.leafBlock, this.leafMeta, false);
		this.drawCircle(dest[0], dest[1], dest[2], 4, this.leafBlock, this.leafMeta, false);
		this.drawCircle(dest[0], dest[1] + 1, dest[2], 2, this.leafBlock, this.leafMeta, false);
	}

	private void buildRoot(World world, double offset, int b) {
		int startY = this.y - b - 2;
		int[] dest = this.translate(this.x, startY, this.z, 6.0, 0.3 * b + offset, 0.8);
		this.drawRoot(world, this.x, startY, this.z, dest[0], dest[1], dest[2]);
	}

	protected void drawRoot(World world, int sx, int sy, int sz, int dx, int dy, int dz) {
		if (getBlockMaterial(world, dx, dy, dz).isSolid()) {
			this.drawBresehnam(sx, sy, sz, dx, dy, dz, this.rootBlock, true);
			return;
		}

		int[] line = getBresehnamArray(sx, sy, sz, dx, dy, dz);
		for (int i = 0; i < line.length; i += 3) {
			int px = line[i];
			int py = line[i + 1];
			int pz = line[i + 2];
			if (getBlockId(world, px, py, pz) > 0 || this.isNearSolid(world, px, py, pz)) {
				this.putBlock(px, py, pz, this.rootBlock, true);
			}
		}
	}

	private static final double CICADA_SHARE = 0.05;

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
