package com.twilightforest.world.biome;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeTaigaBushy;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeTaigaTall;

import java.util.Random;

public class TFBiomeSnow extends TFBiome {
	public TFBiomeSnow(String key) {
		super(key);
	}

	@Override
	public WorldFeature getTreeFeature(Random random) {
		return random.nextInt(3) == 0
			? new WorldFeatureTreeTaigaTall(Blocks.LEAVES_PINE.id(), Blocks.LOG_PINE.id())
			: new WorldFeatureTreeTaigaBushy(Blocks.LEAVES_PINE.id(), Blocks.LOG_PINE.id());
	}
}
