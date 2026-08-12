package com.twilightforest.world.biome;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeFancy;

import java.util.Random;

public class TFBiomeEnchantedForest extends TFBiome {

	public TFBiomeEnchantedForest(String key) {
		super(key);
	}

	@Override
	public WorldFeature getTreeFeature(Random random) {
		if (random.nextInt(15) == 0) {
			return new WorldFeatureTree(TFBlocks.LEAVES_RAINBOW.id(),
				TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
		}
		if (random.nextInt(50) == 0) {
			return new WorldFeatureTreeFancy(TFBlocks.LEAVES_RAINBOW.id(),
				TFBlocks.LOG_TWILIGHT_OAK.id());
		}
		if (random.nextInt(5) == 0) {
			return new WorldFeatureTree(Blocks.LEAVES_BIRCH.id(), Blocks.LOG_BIRCH.id(), 5);
		}
		if (random.nextInt(10) == 0) {
			return new WorldFeatureTreeFancy(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
				TFBlocks.LOG_TWILIGHT_OAK.id());
		}
		return new WorldFeatureTree(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
			TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
	}
}
