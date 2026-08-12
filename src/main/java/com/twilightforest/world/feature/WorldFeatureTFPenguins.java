package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import com.twilightforest.entity.MobTFPenguin;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFPenguins extends TFWorldFeature {

	private static final int COLONY = 10;

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		this.worldObj = world;

		for (int i = 0; i < COLONY; i++) {
			int px = x + random.nextInt(8) - random.nextInt(8);
			int pz = z + random.nextInt(8) - random.nextInt(8);
			int py = world.getHeightValue(px, pz);

			MobTFPenguin penguin = new MobTFPenguin(world);

			penguin.moveTo(px, py, pz, random.nextFloat() * 360.0F, 0.0F);
			world.entityJoinedWorld(penguin);
		}

		return true;
	}
}
