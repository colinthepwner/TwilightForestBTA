package com.twilightforest.entity;

import net.minecraft.core.block.material.Material;
import net.minecraft.core.block.material.Materials;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePos;
import net.minecraft.core.world.pos.TilePosc;

public final class TFAmbientPathing {
	private TFAmbientPathing() {}

	private static final float ON_LEAVES = 200.0F;
	private static final float ON_WOOD = 15.0F;
	private static final float ON_GRASS = 9.0F;

	public static float weigh(World world, TilePosc pos) {

		if (!world.isBlockLoaded(pos)) {
			return 0.0F;
		}

		Material below = world.getBlockMaterial(pos.down(new TilePos()));
		if (below == Materials.LEAVES) {
			return ON_LEAVES;
		}
		if (below == Materials.WOOD) {
			return ON_WOOD;
		}
		if (below == Materials.GRASS) {
			return ON_GRASS;
		}
		return world.getLightBrightness(pos) - 0.5F;
	}
}
