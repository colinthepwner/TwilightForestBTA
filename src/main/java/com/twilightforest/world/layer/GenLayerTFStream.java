package com.twilightforest.world.layer;

public class GenLayerTFStream extends GenLayer {

	public GenLayerTFStream(long baseSeed, GenLayer parent) {
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
				int left = src[dx + (dz + 1) * srcWidth];
				int right = src[dx + 2 + (dz + 1) * srcWidth];
				int down = src[dx + 1 + dz * srcWidth];
				int up = src[dx + 1 + (dz + 2) * srcWidth];
				int mid = src[dx + 1 + (dz + 1) * srcWidth];

				out[dx + dz * width] = shouldStream(mid, left, down, right, up)
					? TFBiomeIds.STREAM
					: -1;
			}
		}

		return out;
	}

	boolean shouldStream(int mid, int left, int down, int right, int up) {
		if (shouldStream(mid, left)) return true;
		if (shouldStream(mid, right)) return true;
		if (shouldStream(mid, down)) return true;
		return shouldStream(mid, up);
	}

	boolean shouldStream(int a, int b) {
		if (a == b) return false;
		if (a == -b) return false;
		if (a == TFBiomeIds.GLACIER && b == TFBiomeIds.SNOW) return false;
		if (a == TFBiomeIds.SNOW && b == TFBiomeIds.GLACIER) return false;
		if (a == TFBiomeIds.DEEP_MUSHROOMS && b == TFBiomeIds.MUSHROOMS) return false;
		if (a == TFBiomeIds.MUSHROOMS && b == TFBiomeIds.DEEP_MUSHROOMS) return false;
		if (a == TFBiomeIds.LAKE || b == TFBiomeIds.LAKE) return false;
		if (a == TFBiomeIds.CLEARING || b == TFBiomeIds.CLEARING) return false;
		return a != TFBiomeIds.LARGE_FEATURE && b != TFBiomeIds.LARGE_FEATURE;
	}
}
