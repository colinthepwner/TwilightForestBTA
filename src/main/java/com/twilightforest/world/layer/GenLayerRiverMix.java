package com.twilightforest.world.layer;

public class GenLayerRiverMix extends GenLayer {

	private static final int VANILLA_OCEAN = 0;
	private static final int VANILLA_ICE_PLAINS = 12;
	private static final int VANILLA_FROZEN_RIVER = 11;
	private static final int VANILLA_MUSHROOM_ISLAND = 14;
	private static final int VANILLA_MUSHROOM_ISLAND_SHORE = 15;

	private final GenLayer biomes;
	private final GenLayer rivers;

	public GenLayerRiverMix(long baseSeed, GenLayer biomes, GenLayer rivers) {
		super(baseSeed);
		this.biomes = biomes;
		this.rivers = rivers;
	}

	@Override
	public void initWorldGenSeed(long seed) {
		this.biomes.initWorldGenSeed(seed);
		this.rivers.initWorldGenSeed(seed);
		super.initWorldGenSeed(seed);
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int[] biome = this.biomes.getInts(x, z, width, height);
		int[] river = this.rivers.getInts(x, z, width, height);
		int[] out = IntCache.get(width * height);

		for (int i = 0; i < width * height; i++) {
			if (biome[i] == VANILLA_OCEAN) {
				out[i] = biome[i];
			} else if (river[i] >= 0) {
				if (biome[i] == VANILLA_ICE_PLAINS) {
					out[i] = VANILLA_FROZEN_RIVER;
				} else if (biome[i] != VANILLA_MUSHROOM_ISLAND
					&& biome[i] != VANILLA_MUSHROOM_ISLAND_SHORE) {
					out[i] = river[i];
				} else {
					out[i] = VANILLA_MUSHROOM_ISLAND_SHORE;
				}
			} else {
				out[i] = biome[i];
			}
		}

		return out;
	}
}
