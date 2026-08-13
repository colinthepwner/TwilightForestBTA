package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFMinersTree extends TFWorldFeature {

	private final int treeBlock = TFBlocks.LOG_MINEWOOD.id();
	private final int leafBlock = TFBlocks.LEAVES_MINERS.id();
	private final int rootBlock = TFBlocks.ROOTS.id();

	private static final int META = 0;

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		this.worldObj = world;

		Material under = getBlockMaterial(world, x, y - 1, z);
		int maxY = world.getWorldType().getMaxY(world);
		if ((under != Materials.GRASS && under != Materials.DIRT) || y >= maxY + 1 - 12) {
			return false;
		}

		for (int dy = 0; dy < 10; dy++) {
			this.putBlockAndMetadata(x, y + dy, z, this.treeBlock, META, true);
		}

		this.putBranchWithLeaves(x, y + 9, z + 1, true);
		this.putBranchWithLeaves(x, y + 9, z + 2, false);
		this.putBranchWithLeaves(x, y + 8, z + 3, false);
		this.putBranchWithLeaves(x, y + 7, z + 4, false);
		this.putBranchWithLeaves(x, y + 6, z + 5, false);
		this.putBranchWithLeaves(x, y + 9, z - 1, true);
		this.putBranchWithLeaves(x, y + 9, z - 2, false);
		this.putBranchWithLeaves(x, y + 8, z - 3, false);
		this.putBranchWithLeaves(x, y + 7, z - 4, false);
		this.putBranchWithLeaves(x, y + 6, z - 5, false);

		this.putBlockAndMetadata(x, y + 1, z, this.treeBlock, META, true);

		if (this.hasAirAround(world, x, y - 1, z)) {
			this.putBlockAndMetadata(x, y - 1, z, this.treeBlock, META, true);
		} else {
			this.putBlockAndMetadata(x, y - 1, z, this.rootBlock, META, true);
		}

		int numRoots = 3 + random.nextInt(2);
		double offset = random.nextDouble();
		for (int b = 0; b < numRoots; b++) {
			this.buildRoot(world, x, y, z, offset, b);
		}
		return true;
	}

	private void putBranchWithLeaves(int bx, int by, int bz, boolean bushy) {
		this.putBlockAndMetadata(bx, by, bz, this.treeBlock, META, true);

		for (int lx = -1; lx <= 1; lx++) {
			for (int ly = -1; ly <= 1; ly++) {
				for (int lz = -1; lz <= 1; lz++) {
					if (!bushy && Math.abs(ly) > 0 && Math.abs(lx) > 0) {
						continue;
					}
					this.putBlockAndMetadata(bx + lx, by + ly, bz + lz, this.leafBlock, META, false);
				}
			}
		}
	}

	private void buildRoot(World world, int x, int y, int z, double offset, int b) {
		int startY = y - b - 2;
		int[] dest = this.translate(x, startY, z, 6.0, 0.3 * b + offset, 0.8);
		this.drawRoot(world, x, startY, z, dest[0], dest[1], dest[2]);
	}

	private void drawRoot(World world, int sx, int sy, int sz, int dx, int dy, int dz) {
		if (getBlockMaterial(world, dx, dy, dz).isSolid()) {
			this.drawBresehnam(sx, sy, sz, dx, dy, dz, this.rootBlock, META, true);
			return;
		}

		int[] line = getBresehnamArray(sx, sy, sz, dx, dy, dz);
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
