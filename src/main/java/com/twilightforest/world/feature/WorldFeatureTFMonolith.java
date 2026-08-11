package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFMonolith extends TFWorldFeature {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		int ht = rand.nextInt(10) + 10;
		int dir = rand.nextInt(4);

		if (!this.isAreaClear(world, rand, x, y, z, 2, ht, 2)) {
			return false;
		}

		int h0;
		int h1;
		int h2;
		int h3;
		switch (dir) {
			case 0 -> {
				h0 = ht;
				h1 = (int) (ht * 0.75);
				h2 = (int) (ht * 0.75);
				h3 = (int) (ht * 0.5);
			}
			case 1 -> {
				h0 = (int) (ht * 0.5);
				h1 = ht;
				h2 = (int) (ht * 0.75);
				h3 = (int) (ht * 0.75);
			}
			case 2 -> {
				h0 = (int) (ht * 0.75);
				h1 = (int) (ht * 0.5);
				h2 = ht;
				h3 = (int) (ht * 0.75);
			}
			default -> {
				h0 = (int) (ht * 0.75);
				h1 = (int) (ht * 0.75);
				h2 = (int) (ht * 0.5);
				h3 = ht;
			}
		}

		buildColumn(x, y, z, h0, ht);
		buildColumn(x + 1, y, z, h1, ht);
		buildColumn(x, y, z + 1, h2, ht);
		buildColumn(x + 1, y, z + 1, h3, ht);

		return true;
	}

	private void buildColumn(int x, int y, int z, int height, int fullHeight) {
		int obsidian = Blocks.OBSIDIAN.id();
		int lapis = Blocks.BLOCK_LAPIS.id();

		for (int cy = 0; cy <= height; cy++) {
			this.putBlock(x, y + cy - 1, z, cy == fullHeight ? lapis : obsidian, true);
		}
	}
}
