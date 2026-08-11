package com.twilightforest.world.layer;

public class GenLayerTFRemoveFeatures extends GenLayer {

	public GenLayerTFRemoveFeatures(long baseSeed, GenLayer parent) {
		super(baseSeed);
		this.parent = parent;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int srcWidth = width + 1;
		int srcHeight = height + 1;

		int[] src = this.parent.getInts(x, z, srcWidth, srcHeight);
		int[] out = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				int value = src[dx + dz * srcWidth];
				out[dx + dz * width] = value != TFBiomeIds.LARGE_FEATURE
					? value
					: src[dx + 1 + (dz + 1) * srcWidth];
			}
		}

		return out;
	}
}
