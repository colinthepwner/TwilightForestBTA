package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import com.twilightforest.world.chunk.TFWorldConstants;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFDarkCanopyTree extends TFWorldFeature {

	private final int treeBlock = TFBlocks.LOG_DARKWOOD.id();
	private final int leafBlock = TFBlocks.LEAVES_DARKWOOD.id();
	private final int rootBlock = TFBlocks.ROOTS.id();

	private static final int META = 0;

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {

		boolean foundDirt = false;
		for (int dy = y; dy >= TFWorldConstants.SEA_LEVEL; dy--) {
			Material under = getBlockMaterial(world, x, dy - 1, z);
			if (under == Materials.GRASS || under == Materials.DIRT) {
				foundDirt = true;
				y = dy;
				break;
			}

			if (under == Materials.STONE || under == Materials.WATER) {
				break;
			}
		}
		if (!foundDirt) {
			return false;
		}

		if (getBlockMaterial(world, x + 1, y, z) == Materials.WATER
			|| getBlockMaterial(world, x - 1, y, z) == Materials.WATER
			|| getBlockMaterial(world, x, y, z + 1) == Materials.WATER
			|| getBlockMaterial(world, x, y, z - 1) == Materials.WATER) {
			return false;
		}

		int treeHeight = 4 + random.nextInt(3);
		this.drawBresehnam(x, y, z, x, y + treeHeight, z, this.treeBlock, META, true);

		final int numBranches = 4;
		double offset = random.nextFloat();
		for (int b = 0; b < numBranches; b++) {
			this.buildBranch(world, x, y, z, treeHeight - numBranches + b / 2,
				8.0, 0.23 * b + offset, 0.23, random);
		}

		if (this.hasAirAround(world, x, y - 1, z)) {
			this.putBlockAndMetadata(x, y - 1, z, this.treeBlock, META, true);
		} else {
			this.putBlockAndMetadata(x, y - 1, z, this.rootBlock, META, true);
		}

		int numRoots = 3 + random.nextInt(2);
		offset = random.nextDouble();
		for (int b = 0; b < numRoots; b++) {
			this.buildRoot(world, x, y, z, offset, b);
		}
		return true;
	}

	private void buildBranch(World world, int x, int y, int z, int height, double length,
	                         double angle, double tilt, Random random) {
		int[] src = {x, y + height, z};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);

		this.drawBresehnam(src[0], src[1], src[2], dest[0], dest[1], dest[2],
			this.treeBlock, META, true);

		int leafSize = 4;
		if (random.nextInt(3) == 0) {
			leafSize += random.nextInt(3) - 1;
		}

		if (isAirBlock(world, dest[0], dest[1] - 1, dest[2])
			|| isAirBlock(world, dest[0], dest[1] + 1, dest[2])
			|| isAirBlock(world, dest[0] + 4, dest[1], dest[2])
			|| isAirBlock(world, dest[0] - 4, dest[1], dest[2])
			|| isAirBlock(world, dest[0], dest[1], dest[2] + 4)
			|| isAirBlock(world, dest[0], dest[1], dest[2] - 4)) {
			this.drawCircle(dest[0], dest[1] - 1, dest[2], leafSize, this.leafBlock, META, false);
			this.drawCircle(dest[0], dest[1], dest[2], leafSize + 1, this.leafBlock, META, false);
			this.drawCircle(dest[0], dest[1] + 1, dest[2], leafSize, this.leafBlock, META, false);
			this.drawCircle(dest[0], dest[1] + 2, dest[2], leafSize - 2, this.leafBlock, META, false);
		}
	}

	private void buildRoot(World world, int x, int y, int z, double offset, int b) {
		int startY = y - b - 2;
		int[] dest = this.translate(x, startY, z, 5.0, 0.3 * b + offset, 0.8);

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
