package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFStoneCircle extends TFWorldFeature {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		for (int cx = -3; cx <= 3; cx++) {
			for (int cz = -3; cz <= 3; cz++) {
				for (int cy = 0; cy <= 4; cy++) {
					if (!getBlockMaterial(world, x + cx, y - 1, z + cz).isSolid()) {
						return false;
					}
					if (!world.isAirBlock(x + cx, y + cy, z + cz)) {
						return false;
					}
				}
			}
		}

		int stone = Blocks.COBBLE_STONE_MOSSY.id();

		for (int cy = 0; cy <= 2; cy++) {
			this.putBlock(x - 3, y + cy, z, stone, true);
			this.putBlock(x + 3, y + cy, z, stone, true);
			this.putBlock(x, y + cy, z - 3, stone, true);
			this.putBlock(x, y + cy, z + 3, stone, true);
			this.putBlock(x - 2, y + cy, z - 2, stone, true);
			this.putBlock(x + 2, y + cy, z - 2, stone, true);
			this.putBlock(x - 2, y + cy, z + 2, stone, true);
			this.putBlock(x + 2, y + cy, z + 2, stone, true);
		}

		return true;
	}
}
