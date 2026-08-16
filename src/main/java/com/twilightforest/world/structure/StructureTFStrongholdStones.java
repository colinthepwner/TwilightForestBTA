package com.twilightforest.world.structure;

import net.minecraft.core.block.Blocks;

import java.util.Random;

public class StructureTFStrongholdStones extends StructureComponentTF.BlockSelector {

	private static final int STONE_BRICK = Blocks.BRICK_STONE_POLISHED.id();
	private static final int STONE_BRICK_MOSSY = Blocks.BRICK_STONE_POLISHED_MOSSY.id();

	@Override
	public void select(Random rand, int x, int y, int z, boolean shell) {
		if (!shell) {
			this.blockId = 0;
			this.meta = 0;
			return;
		}

		this.meta = 0;
		float f = rand.nextFloat();
		if (f < 0.2F) {
			this.blockId = STONE_BRICK;
		} else if (f < 0.5F) {
			this.blockId = STONE_BRICK_MOSSY;
		} else if (f < 0.55F) {
			this.blockId = STONE_BRICK;
		} else {
			this.blockId = STONE_BRICK;
		}
	}
}
