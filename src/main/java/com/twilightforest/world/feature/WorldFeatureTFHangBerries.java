package com.twilightforest.world.feature;

import com.twilightforest.block.BlockLogicTFTorchberries;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.compat.TFWorldFeature;
import net.minecraft.core.world.World;

import java.util.Random;

public class WorldFeatureTFHangBerries extends TFWorldFeature {

	private static final int FLOOR = 5;

	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		int copyX = x;
		int copyZ = z;

		while (y > FLOOR) {

			if (isAirBlock(world, x, y, z)
				&& BlockLogicTFTorchberries.canPlaceRootBelow(world, x, y + 1, z)
				&& random.nextInt(6) > 0) {
				this.putBlockAndMetadata(x, y, z, TFBlocks.TORCHBERRIES.id(), 0, true);
				break;
			}

			x = copyX + random.nextInt(4) - random.nextInt(4);
			z = copyZ + random.nextInt(4) - random.nextInt(4);
			y--;
		}

		return true;
	}
}
