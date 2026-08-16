package com.twilightforest.world.structure;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;

public class StructureDecoratorDarkTower extends StructureTFDecorator {

	public StructureDecoratorDarkTower() {

		this.blockID = TFBlocks.TOWER_WOOD.id();
		this.blockMeta = 0;

		this.accentID = TFBlocks.TOWER_WOOD_ENCASED.id();
		this.accentMeta = 0;

		this.fenceID = Blocks.FENCE_PLANKS_OAK.id();
		this.fenceMeta = 0;

		this.stairID = Blocks.STAIRS_PLANKS_OAK.id();
		this.stairMeta = 0;

		this.pillarID = TFBlocks.TOWER_WOOD_ENCASED.id();
		this.pillarMeta = 0;

		this.platformID = TFBlocks.TOWER_WOOD_ENCASED.id();
		this.platformMeta = 0;
	}
}
