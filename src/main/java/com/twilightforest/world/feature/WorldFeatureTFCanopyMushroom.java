package com.twilightforest.world.feature;

import com.twilightforest.block.BlockLogicTFGiantMushroom;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFCanopyMushroom extends TFWorldFeature {

	private Random treeRNG;
	private int x;
	private int y;
	private int z;
	private int treeHeight;
	private int treeBlock;
	private int treeMeta;
	private int leafBlock;
	private int leafMeta;

	@Override
	public boolean generate(World world, Random random, int treeX, int treeY, int treeZ) {
		this.worldObj = world;
		this.treeRNG = random;

		int blockUnder = getBlockId(world, treeX, treeY - 1, treeZ);

		boolean footing = blockUnder == Blocks.GRASS.id()
			|| blockUnder == Blocks.DIRT.id()
			|| blockUnder == Blocks.MUD.id();

		int atBase = getBlockId(world, this.x, this.y, this.z);
		if (atBase == Blocks.FLUID_WATER_STILL.id() || atBase == Blocks.FLUID_WATER_FLOWING.id()) {
			return false;
		}

		if (!footing || this.y >= 128 - this.treeHeight - 1) {
			return false;
		}

		this.x = treeX;
		this.y = treeY;
		this.z = treeZ;

		boolean red = this.treeRNG.nextInt(3) == 0;

		this.treeBlock = TFBlocks.MUSHROOM_GIANT_BROWN.id();
		this.treeMeta = BlockLogicTFGiantMushroom.STEM;
		this.leafBlock = (red ? TFBlocks.MUSHROOM_GIANT_RED : TFBlocks.MUSHROOM_GIANT_BROWN).id();

		this.leafMeta = BlockLogicTFGiantMushroom.CAP_DOMED;

		this.treeHeight = 12;
		if (this.treeRNG.nextInt(3) == 0) {
			this.treeHeight += this.treeRNG.nextInt(5);
			if (this.treeRNG.nextInt(8) == 0) {
				this.treeHeight += this.treeRNG.nextInt(5);
			}
		}

		this.buildBranch(0, this.treeHeight, 0.0, 0.0, true);

		int numBranches = 3 + this.treeRNG.nextInt(2);
		double offset = this.treeRNG.nextDouble();
		for (int b = 0; b < numBranches; b++) {
			this.buildBranch(this.treeHeight - 5 + b, 9.0, 0.3 * b + offset, 0.2, false);
		}

		return true;
	}

	void buildBranch(int height, double length, double angle, double tilt, boolean firefly) {
		int[] src = new int[]{this.x, this.y + height, this.z};
		int[] dest = this.translate(src[0], src[1], src[2], length, angle, tilt);

		this.drawBresehnam(src[0], src[1], src[2], dest[0], src[1], dest[2],
			this.treeBlock, this.treeMeta, true);
		this.drawBresehnam(dest[0], src[1], dest[2], dest[0], dest[1] - 1, dest[2],
			this.treeBlock, this.treeMeta, true);

		if (firefly) {
			this.addFirefly(3 + this.treeRNG.nextInt(7), this.treeRNG.nextDouble());
		}

		this.drawMushroomCircle(dest[0], dest[1], dest[2], 4, this.leafBlock, true);
	}

	public void drawMushroomCircle(int sx, int sy, int sz, int rad, int blockValue, boolean priority) {
		for (int dx = 0; dx <= rad; dx++) {
			for (int dz = 0; dz <= rad; dz++) {
				int dist = (int) (Math.max(dx, dz) + Math.min(dx, dz) * 0.5);
				if (dx == 3 && dz == 3) {
					dist = 6;
				}

				if (dx == 0) {
					this.putBlockAndMetadata(sx, sy, sz + dz, blockValue, this.leafMeta, priority);
					this.putBlockAndMetadata(sx, sy, sz - dz, blockValue, this.leafMeta, priority);
				} else if (dz == 0) {
					this.putBlockAndMetadata(sx + dx, sy, sz, blockValue, this.leafMeta, priority);
					this.putBlockAndMetadata(sx - dx, sy, sz, blockValue, this.leafMeta, priority);
				} else if (dist <= rad) {
					this.putBlockAndMetadata(sx + dx, sy, sz + dz, blockValue, this.leafMeta, priority);
					this.putBlockAndMetadata(sx + dx, sy, sz - dz, blockValue, this.leafMeta, priority);
					this.putBlockAndMetadata(sx - dx, sy, sz + dz, blockValue, this.leafMeta, priority);
					this.putBlockAndMetadata(sx - dx, sy, sz - dz, blockValue, this.leafMeta, priority);
				}
			}
		}
	}

	private void addFirefly(int height, double angle) {
		int iAngle = (int) (angle * 4.0);
		int firefly = TFBlocks.FIREFLY.id();
		switch (iAngle) {
			case 0 -> this.putBlockAndMetadata(this.x + 1, this.y + height, this.z, firefly, 1, false);
			case 1 -> this.putBlockAndMetadata(this.x - 1, this.y + height, this.z, firefly, 2, false);
			case 2 -> this.putBlockAndMetadata(this.x, this.y + height, this.z + 1, firefly, 3, false);
			case 3 -> this.putBlockAndMetadata(this.x, this.y + height, this.z - 1, firefly, 4, false);
			default -> {  }
		}
	}
}
