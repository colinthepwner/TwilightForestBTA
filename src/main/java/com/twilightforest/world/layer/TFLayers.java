package com.twilightforest.world.layer;

public final class TFLayers {
	private TFLayers() {}

	public static final int COARSE = 0;

	public static final int PER_BLOCK = 1;

	public static final int FEATURES = 2;

	public static GenLayer[] makeTheWorld(long seed) {
		int zoomFactor = 4;

		GenLayer biomes = new GenLayerTFBiomes(1L);
		biomes = new GenLayerTFFourZoom(1000L, biomes);
		biomes = new GenLayerTFRegionZoom(1001L, biomes, true);
		biomes = new GenLayerTFBiomeBorders(500L, biomes);

		for (int i = 0; i < zoomFactor; i++) {

			biomes = new GenLayerTFRegionZoom(1002 + i, biomes, i == 1);
		}

		GenLayer streams = new GenLayerTFStream(1L, biomes);
		streams = new GenLayerSmooth(7000L, streams);
		biomes = new GenLayerRiverMix(100L, biomes, streams);

		GenLayer features = new GenLayerTFRegionZoom(10L, biomes, true);
		features = new GenLayerTFRegionZoom(11L, features, false);

		biomes = new GenLayerTFRemoveFeatures(700L, biomes);

		GenLayer perBlock = new GenLayerVoronoiZoom(10L, biomes);
		perBlock = new GenLayerReinsertFeatures(100L, perBlock, features);

		biomes.initWorldGenSeed(seed);
		perBlock.initWorldGenSeed(seed);
		features.initWorldGenSeed(seed);

		return new GenLayer[]{biomes, perBlock, features};
	}
}
