package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFSlab extends TFWorldFeature {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		int sx = 3 + rand.nextInt(7);
		int sz = 3 + rand.nextInt(7);

		int sy = (int) (rand.nextInt(2) * rand.nextDouble());

		if (!this.isAreaClear(world, rand, x, y, z, sx, sy, sz)) {
			return false;
		}

		for (int cx = 0; cx <= sx; cx++) {
			for (int cz = 0; cz <= sz; cz++) {
				for (int cy = 0; cy <= sy; cy++) {
					this.putBlock(x + cx, y + cy - 1, z + cz, this.randStone(rand, cy + 2), true);
				}
			}
		}

		return true;
	}
}
