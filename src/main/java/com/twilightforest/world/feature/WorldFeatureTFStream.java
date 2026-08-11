package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFStream extends TFWorldFeature {

	@Override
	@SuppressWarnings("unused")
	public boolean generate(World world, Random random, int x, int y, int z) {
		this.worldObj = world;
		int streamLength = 80 + random.nextInt(20);
		double streamAngle = random.nextDouble();
		return false;
	}
}
