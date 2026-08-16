package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;

import java.util.Random;

public class StructureTFTowerWoods extends StructureComponentTF.BlockSelector {

	@Override
	public void select(Random rand, int x, int y, int z, boolean shell) {
		if (!shell) {
			this.blockId = 0;
			this.meta = 0;
			return;
		}

		this.meta = 0;

		float f = rand.nextFloat();
		if (f < 0.1F) {
			this.blockId = TFBlocks.TOWER_WOOD_CRACKED.id();
		} else if (f < 0.2F) {
			this.blockId = TFBlocks.TOWER_WOOD_MOSSY.id();
		} else if (f < 0.225F) {
			this.blockId = TFBlocks.TOWER_WOOD_INFESTED.id();
		} else {
			this.blockId = TFBlocks.TOWER_WOOD.id();
		}
	}
}
