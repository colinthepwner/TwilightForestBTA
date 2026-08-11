package com.twilightforest.world.layer;

public class GenLayerTFBiomeBorders extends GenLayer {

	public GenLayerTFBiomeBorders(long baseSeed, GenLayer parent) {
		super(baseSeed);
		this.parent = parent;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int srcX = x - 1;
		int srcZ = z - 1;
		int srcWidth = width + 2;
		int srcHeight = height + 2;

		int[] src = this.parent.getInts(srcX, srcZ, srcWidth, srcHeight);
		int[] out = IntCache.get(width * height);

		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				int right = src[dx + (dz + 1) * srcWidth];
				int left = src[dx + 2 + (dz + 1) * srcWidth];
				int up = src[dx + 1 + dz * srcWidth];
				int down = src[dx + 1 + (dz + 2) * srcWidth];
				int centre = src[dx + 1 + (dz + 1) * srcWidth];

				int result;
				if (onBorder(TFBiomeIds.LAKE, centre, right, left, up, down)) {
					result = TFBiomeIds.LAKE_BORDER;
				} else if (onBorder(TFBiomeIds.CLEARING, centre, right, left, up, down)) {
					result = TFBiomeIds.CLEARING_BORDER;
				} else if (onBorder(TFBiomeIds.DEEP_MUSHROOMS, centre, right, left, up, down)) {
					result = TFBiomeIds.MUSHROOMS;
				} else if (onBorder(TFBiomeIds.GLACIER, centre, right, left, up, down)) {
					result = TFBiomeIds.SNOW;
				} else {
					result = centre;
				}

				out[dx + dz * width] = result;
			}
		}

		return out;
	}

	boolean onBorder(int biome, int centre, int right, int left, int up, int down) {
		if (centre != biome) {
			return false;
		}
		if (right != biome && right != TFBiomeIds.LARGE_FEATURE) return true;
		if (left != biome && left != TFBiomeIds.LARGE_FEATURE) return true;
		if (up != biome && up != TFBiomeIds.LARGE_FEATURE) return true;
		return down != biome && down != TFBiomeIds.LARGE_FEATURE;
	}
}
