package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFMyceliumBlob extends TFWorldFeature {

	private final int numberOfBlocks;

	public WorldFeatureTFMyceliumBlob(int numberOfBlocks) {
		this.numberOfBlocks = numberOfBlocks;
	}

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		this.worldObj = world;

		int radius = random.nextInt(this.numberOfBlocks - 2) + 2;
		int vertical = 1;

		int mycelium = Blocks.MUD.id();
		int grass = Blocks.GRASS.id();
		int dirt = Blocks.DIRT.id();

		for (int bx = x - radius; bx <= x + radius; bx++) {
			for (int bz = z - radius; bz <= z + radius; bz++) {
				int dx = bx - x;
				int dz = bz - z;

				if (dx * dx + dz * dz > radius * radius) {
					continue;
				}
				for (int by = y - vertical; by <= y + vertical; by++) {
					int there = getBlockId(world, bx, by, bz);
					if (there == grass || there == dirt) {

						this.putBlock(bx, by, bz, mycelium, true);
					}
				}
			}
		}

		return true;
	}
}
