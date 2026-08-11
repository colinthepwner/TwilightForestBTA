package com.twilightforest.world.layer;

public class GenLayerReinsertFeatures extends GenLayer {

	private final GenLayer withoutFeatures;
	private final GenLayer withFeatures;

	public GenLayerReinsertFeatures(long baseSeed, GenLayer withoutFeatures, GenLayer withFeatures) {
		super(baseSeed);
		this.withoutFeatures = withoutFeatures;
		this.withFeatures = withFeatures;
	}

	@Override
	public void initWorldGenSeed(long seed) {
		this.withoutFeatures.initWorldGenSeed(seed);
		this.withFeatures.initWorldGenSeed(seed);
		super.initWorldGenSeed(seed);
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int[] without = this.withoutFeatures.getInts(x, z, width, height);
		int[] with = this.withFeatures.getInts(x, z, width, height);
		int[] out = IntCache.get(width * height);

		for (int i = 0; i < width * height; i++) {
			out[i] = with[i] == TFBiomeIds.LARGE_FEATURE ? with[i] : without[i];
		}

		return out;
	}
}
