package com.twilightforest.world.layer;

public class GenLayerTFBiomes extends GenLayer {

	private static final int[] COMMON = {
		TFBiomeIds.TWILIGHT_FOREST,
		TFBiomeIds.TWILIGHT_FOREST_VARIANT,
		TFBiomeIds.HIGHLANDS,
		TFBiomeIds.DEEP_MUSHROOMS,
		TFBiomeIds.SWAMP,
		TFBiomeIds.CLEARING,

		TFBiomeIds.DARK_FOREST,
	};

	private static final int[] RARE = {
		TFBiomeIds.LAKE,
		TFBiomeIds.GLACIER,

		TFBiomeIds.ENCHANTED_FOREST,
	};

	public GenLayerTFBiomes(long baseSeed) {
		super(baseSeed);
	}

	public GenLayerTFBiomes(long baseSeed, GenLayer parent) {
		super(baseSeed);
		this.parent = parent;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int[] dest = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				this.initChunkSeed(dx + x, dz + z);
				dest[dx + dz * width] = this.nextInt(15) == 0
					? RARE[this.nextInt(RARE.length)]
					: COMMON[this.nextInt(COMMON.length)];
			}
		}

		return dest;
	}
}
