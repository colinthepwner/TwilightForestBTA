package com.twilightforest.world.layer;

public class GenLayerSmooth extends GenLayer {

	public GenLayerSmooth(long baseSeed, GenLayer parent) {
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
				int up = src[dx + 1 + dz * srcWidth];
				int down = src[dx + 1 + (dz + 2) * srcWidth];
				int centre = src[dx + 1 + (dz + 1) * srcWidth];

				if (left == right && up == down) {
					this.initChunkSeed(dx + x, dz + z);
					centre = this.nextInt(2) == 0 ? left : up;
				} else {
					if (left == right) {
						centre = left;
					}
					if (up == down) {
						centre = up;
					}
				}

				out[dx + dz * width] = centre;
			}
		}

		return out;
	}
}
