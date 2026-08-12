package com.twilightforest.world.biome;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeShrub;

import java.util.Random;

public class TFBiomeDarkForest extends TFBiome {

	public TFBiomeDarkForest(String key) {
		super(key);

		this.withPlacementDefaults(0.7f, 0.8f, 0.0f);
	}

	@Override
	public WorldFeature getTreeFeature(Random random) {
		if (random.nextInt(5) == 0) {
			return new WorldFeatureTreeShrub(TFBlocks.LEAVES_DARKWOOD.id(), TFBlocks.LOG_DARKWOOD.id());
		}
		if (random.nextInt(8) == 0) {
			return new WorldFeatureTree(Blocks.LEAVES_OAK.id(), Blocks.LOG_OAK.id(), 4);
		}
		return new WorldFeatureTree(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
			TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
	}
}
