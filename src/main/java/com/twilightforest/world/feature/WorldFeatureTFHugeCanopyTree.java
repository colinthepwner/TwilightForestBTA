package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFHugeCanopyTree extends TFWorldFeature {

	private final int treeBlock = TFBlocks.LOG_CANOPY.id();
	private final int leafBlock = TFBlocks.LEAVES_CANOPY.id();
	private final int rootBlock = TFBlocks.ROOTS.id();

	private static final int META = 0;

	private static final int BASE_HEIGHT = 35;

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {

		int treeHeight = BASE_HEIGHT;
		if (random.nextInt(3) == 0) {
			treeHeight += random.nextInt(10);
			if (random.nextInt(8) == 0) {
				treeHeight += random.nextInt(10);
			}
		}

		int maxY = world.getWorldType().getMaxY(world);
		int below = getBlockId(world, x, y - 1, z);
		if ((below != Blocks.GRASS.id() && below != Blocks.DIRT.id())
			|| y >= maxY + 1 - treeHeight) {
			return false;
		}

		if (getBlockId(world, x, y, z) == Blocks.FLUID_WATER_STILL.id()
			|| getBlockId(world, x, y, z) == Blocks.FLUID_WATER_FLOWING.id()) {
			return false;
		}

		this.buildTrunk(world, x, y, z, treeHeight);

		int numBranches = 5 + random.nextInt(3);
		double offset = random.nextDouble();
		for (int b = 0; b < numBranches; b++) {
			this.buildBranch(x, y, z, treeHeight - 23 + (int) (b * 1.5), 17.0, 0.3 * b + offset, 0.25);
		}

		int numRoots = 4 + random.nextInt(3);
		offset = random.nextDouble();
		for (int b = 0; b < numRoots; b++) {
			this.buildRoot(world, x, y, z, offset, b);
		}
		return true;
	}

	private void buildTrunk(World world, int x, int y, int z, int treeHeight) {
		int[] src = {x, y, z};
		int[] dest = this.translate(src[0], src[1], src[2], treeHeight, 0.0, 0.0);

		for (int dy = -6; dy < 0; dy++) {
			this.drawRootBlock(world, x, y + dy, z);
			this.drawRootBlock(world, x + 1, y + dy, z);
			this.drawRootBlock(world, x, y + dy, z + 1);
			this.drawRootBlock(world, x + 1, y + dy, z + 1);
		}

		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2],
			this.treeBlock, META, true);
		this.drawBresehnam(src[0] + 1, src[1], src[2], dest[0] + 1, dest[1], dest[2],
			this.treeBlock, META, true);
		this.drawBresehnam(src[0] + 1, src[1], src[2] + 1, dest[0] + 1, dest[1], dest[2] + 1,
			this.treeBlock, META, true);
		this.drawBresehnam(src[0], src[1], src[2] + 1, dest[0], dest[1], dest[2] + 1,
			this.treeBlock, META, true);

		this.makeLeafNode(dest);
	}

	private void buildBranch(int x, int y, int z, int height, double length, double angle, double tilt) {
		int[] src = {x, y + height, z};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);

		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2],
			this.treeBlock, META, true);
		this.drawBresehnam(src[0], src[1] - 1, src[2], dest[0], dest[1] - 1, dest[2],
			this.treeBlock, META, true);

		this.makeLeafNode(dest);
	}

	private void makeLeafNode(int[] dest) {
		this.drawBresehnam(dest[0], dest[1], dest[2], dest[0] + 4, dest[1], dest[2],
			this.treeBlock, META, true);
		this.drawBresehnam(dest[0], dest[1], dest[2], dest[0] - 4, dest[1], dest[2],
			this.treeBlock, META, true);
		this.drawBresehnam(dest[0], dest[1], dest[2], dest[0], dest[1], dest[2] + 4,
			this.treeBlock, META, true);
		this.drawBresehnam(dest[0], dest[1], dest[2], dest[0], dest[1], dest[2] - 4,
			this.treeBlock, META, true);

		this.putBlockAndMetadata(dest[0] + 5, dest[1], dest[2] + 1, this.treeBlock, META, true);
		this.putBlockAndMetadata(dest[0] + 5, dest[1], dest[2] - 1, this.treeBlock, META, true);
		this.putBlockAndMetadata(dest[0] - 5, dest[1], dest[2] + 1, this.treeBlock, META, true);
		this.putBlockAndMetadata(dest[0] - 5, dest[1], dest[2] - 1, this.treeBlock, META, true);
		this.putBlockAndMetadata(dest[0] + 1, dest[1], dest[2] + 5, this.treeBlock, META, true);
		this.putBlockAndMetadata(dest[0] - 1, dest[1], dest[2] + 5, this.treeBlock, META, true);
		this.putBlockAndMetadata(dest[0] + 1, dest[1], dest[2] - 5, this.treeBlock, META, true);
		this.putBlockAndMetadata(dest[0] - 1, dest[1], dest[2] - 5, this.treeBlock, META, true);

		this.drawBresehnam(dest[0] + 1, dest[1], dest[2], dest[0] + 4, dest[1], dest[2] + 3,
			this.treeBlock, META, true);
		this.drawBresehnam(dest[0] - 1, dest[1], dest[2], dest[0] - 4, dest[1], dest[2] - 3,
			this.treeBlock, META, true);
		this.drawBresehnam(dest[0], dest[1], dest[2] + 1, dest[0] - 3, dest[1], dest[2] + 4,
			this.treeBlock, META, true);
		this.drawBresehnam(dest[0], dest[1], dest[2] - 1, dest[0] + 3, dest[1], dest[2] - 4,
			this.treeBlock, META, true);

		this.drawCircle(dest[0], dest[1] - 2, dest[2], 4, this.leafBlock, META, false);
		this.drawCircle(dest[0], dest[1] - 1, dest[2], 7, this.leafBlock, META, false);
		this.drawCircle(dest[0], dest[1], dest[2], 8, this.leafBlock, META, false);
		this.drawCircle(dest[0], dest[1] + 1, dest[2], 6, this.leafBlock, META, false);
		this.drawCircle(dest[0], dest[1] + 2, dest[2], 3, this.leafBlock, META, false);
	}

	private void drawRootBlock(World world, int dx, int dy, int dz) {
		if (this.hasAirAround(world, dx, dy, dz)) {
			this.putBlockAndMetadata(dx, dy, dz, this.treeBlock, META, true);
		} else {
			this.putBlockAndMetadata(dx, dy, dz, this.rootBlock, META, true);
		}
	}

	private void buildRoot(World world, int x, int y, int z, double offset, int b) {
		int startY = y - b - 2;
		int[] dest = this.translate(x, startY, z, 8.0, 0.278 * b + offset, 0.8);

		if (getBlockMaterial(world, dest[0], dest[1], dest[2]).isSolid()) {
			this.drawBresehnam(x, startY, z, dest[0], dest[1], dest[2], this.rootBlock, META, true);
			return;
		}

		int[] line = getBresehnamArray(x, startY, z, dest[0], dest[1], dest[2]);
		for (int i = 0; i < line.length; i += 3) {
			int px = line[i];
			int py = line[i + 1];
			int pz = line[i + 2];
			if (getBlockId(world, px, py, pz) > 0 || this.isNearSolid(world, px, py, pz)) {
				this.putBlockAndMetadata(px, py, pz, this.rootBlock, META, true);
			}
		}
	}
}
