package com.twilightforest.world.feature;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFOutsideStalagmite extends WorldFeatureTFCaveStalactite {

	public WorldFeatureTFOutsideStalagmite() {
		super(Blocks.STONE.id(), 1.0, false);
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;
		int length = rand.nextInt(10) + 5;

		if (!this.isAreaClear(world, rand, x, y, z, 1, length, 1)) {
			return false;
		}

		return this.makeSpike(rand, x, y - 1, z, length);
	}
}
