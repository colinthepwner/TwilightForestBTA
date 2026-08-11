package com.twilightforest.world.feature;

import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFFoundation extends TFWorldFeature {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		this.worldObj = world;

		int sx = 5 + rand.nextInt(5);
		int sz = 5 + rand.nextInt(5);

		if (!this.isAreaClear(world, rand, x, y, z, sx, 4, sz)) {
			return false;
		}

		int planks = Blocks.PLANKS_OAK.id();

		for (int cx = 0; cx <= sx; cx++) {
			for (int cz = 0; cz <= sz; cz++) {
				boolean edge = cx == 0 || cx == sx || cz == 0 || cz == sz;

				if (!edge) {

					if (rand.nextInt(3) != 0) {
						this.putBlock(x + cx, y - 1, z + cz, planks, true);
					}
					continue;
				}

				int ht = rand.nextInt(4) + 1;
				for (int cy = 0; cy <= ht; cy++) {
					this.putBlock(x + cx, y + cy - 1, z + cz, this.randStone(rand, cy + 1), true);
				}
			}
		}

		return true;
	}
}
