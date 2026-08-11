package com.twilightforest.world.biome;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeFancy;

import java.util.Random;

public class TFBiomeHighlands extends TFBiome {
	public TFBiomeHighlands(String key) {
		super(key);
	}

	@Override
	public WorldFeature getTreeFeature(Random random) {
		if (random.nextInt(10) == 0) {
			return new WorldFeatureTree(Blocks.LEAVES_BIRCH.id(), Blocks.LOG_BIRCH.id(), 5);
		}
		if (random.nextInt(7) == 0) {
			return new WorldFeatureTreeFancy(Blocks.LEAVES_OAK.id(), Blocks.LOG_OAK.id());
		}
		return new WorldFeatureTree(Blocks.LEAVES_OAK.id(), Blocks.LOG_OAK.id(), 4);
	}
}
