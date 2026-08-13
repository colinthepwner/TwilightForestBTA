package com.twilightforest.world.feature;

import com.twilightforest.block.TFBlocks;

public class WorldFeatureTFTreeOfTransformation extends WorldFeatureTFCanopyTree {

	public WorldFeatureTFTreeOfTransformation() {

		this.treeBlock = TFBlocks.LOG_TRANSWOOD.id();
		this.leafBlock = TFBlocks.LEAVES_TRANSFORMATION.id();
		this.minHeight = 11;
	}
}
