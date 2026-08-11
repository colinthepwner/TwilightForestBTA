package com.twilightforest.world.biome;

import com.twilightforest.world.feature.WorldFeatureTFMangroveTree;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;

import java.util.Random;

public class TFBiomeSwamp extends TFBiome {
	public TFBiomeSwamp(String key) {
		super(key);
	}

	@Override
	public WorldFeature getTreeFeature(Random random) {
		return random.nextInt(10) == 0
			? new WorldFeatureTree(Blocks.LEAVES_OAK.id(), Blocks.LOG_OAK.id(), 4)
			: new WorldFeatureTFMangroveTree();
	}
}
