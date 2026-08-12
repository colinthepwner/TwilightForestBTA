package com.twilightforest.world.feature;

import com.twilightforest.block.BlockLogicTFGiantMushroom;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFBigMushroom extends TFWorldFeature {

	private final int mushroomType;

	public WorldFeatureTFBigMushroom() {
		this(-1);
	}

	public WorldFeatureTFBigMushroom(int mushroomType) {
		this.mushroomType = mushroomType;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		int type = rand.nextInt(2);
		if (this.mushroomType >= 0) {
			type = this.mushroomType;
		}

		int trunk = rand.nextInt(3) + 4;
		int height = world.getWorldType().getMaxY(world) + 1;

		if (y < 1 || y + trunk + 1 >= height) {
			return false;
		}

		for (int cy = y; cy <= y + 1 + trunk; cy++) {
			int radius = cy <= y + 3 ? 0 : 3;
			for (int cx = x - radius; cx <= x + radius; cx++) {
				for (int cz = z - radius; cz <= z + radius; cz++) {
					if (cy < 0 || cy >= height) {
						return false;
					}
					int here = getBlockId(world, cx, cy, cz);
					if (here != 0 && !isLeafBlock(here)) {
						return false;
					}
				}
			}
		}

		int below = getBlockId(world, x, y - 1, z);
		if (!isMushroomSoil(below)) {
			return false;
		}

		int cap = type == 0 ? TFBlocks.MUSHROOM_GIANT_BROWN.id() : TFBlocks.MUSHROOM_GIANT_RED.id();
		int top = y + trunk;

		int firstCapLayer = type == 1 ? y + trunk - 3 : y + trunk;

		for (int layer = firstCapLayer; layer <= top; layer++) {
			int radius = layer < top ? 2 : 1;
			if (type == 0) {
				radius = 3;
			}

			for (int cx = x - radius; cx <= x + radius; cx++) {
				for (int cz = z - radius; cz <= z + radius; cz++) {
					int data = 5;

					if (cx == x - radius) data--;
					if (cx == x + radius) data++;
					if (cz == z - radius) data -= 3;
					if (cz == z + radius) data += 3;

					if (type == 0 || layer < top) {
						boolean corner = (cx == x - radius || cx == x + radius)
							&& (cz == z - radius || cz == z + radius);
						if (corner) {
							continue;
						}

						if (cx == x - (radius - 1) && cz == z - radius) data = 1;
						if (cx == x - radius && cz == z - (radius - 1)) data = 1;
						if (cx == x + (radius - 1) && cz == z - radius) data = 3;
						if (cx == x + radius && cz == z - (radius - 1)) data = 3;
						if (cx == x - (radius - 1) && cz == z + radius) data = 7;
						if (cx == x - radius && cz == z + (radius - 1)) data = 7;
						if (cx == x + (radius - 1) && cz == z + radius) data = 9;
						if (cx == x + radius && cz == z + (radius - 1)) data = 9;
					}

					if (data == 5 && layer < top) {
						data = BlockLogicTFGiantMushroom.PORES;
					}

					if (data != BlockLogicTFGiantMushroom.PORES && canReplace(world, cx, layer, cz)) {
						putBlockAndMetadata(cx, layer, cz, cap, data, true);
					}
				}
			}
		}

		for (int cy = 0; cy < trunk; cy++) {
			if (canReplace(world, x, y + cy, z)) {
				putBlockAndMetadata(x, y + cy, z,
					TFBlocks.MUSHROOM_GIANT_BROWN.id(), BlockLogicTFGiantMushroom.STEM, true);
			}
		}

		return true;
	}

	private static boolean isMushroomSoil(int blockId) {
		return blockId == Blocks.GRASS.id()
			|| blockId == Blocks.DIRT.id()
			|| blockId == Blocks.MUD.id();
	}

	private static boolean canReplace(World world, int x, int y, int z) {
		int id = getBlockId(world, x, y, z);
		if (id == 0) {
			return true;
		}
		return !Blocks.getBlock(id).isSolidRender();
	}

	private static boolean isLeafBlock(int blockId) {
		return blockId == Blocks.LEAVES_OAK.id()
			|| blockId == Blocks.LEAVES_PINE.id()
			|| blockId == Blocks.LEAVES_BIRCH.id()
			|| blockId == TFBlocks.LEAVES_TWILIGHT_OAK.id()
			|| blockId == TFBlocks.LEAVES_CANOPY.id()
			|| blockId == TFBlocks.LEAVES_MANGROVE.id();
	}
}
