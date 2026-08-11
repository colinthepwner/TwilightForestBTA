package com.twilightforest.world.layer;

public class GenLayerTFRegionZoom extends GenLayer {

	public final boolean shiftCenter;

	public GenLayerTFRegionZoom(long baseSeed, GenLayer parent, boolean shiftCenter) {
		super(baseSeed);
		this.parent = parent;
		this.shiftCenter = shiftCenter;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int sourceX = x >> 1;
		int sourceZ = z >> 1;
		int sourceWidth = (width >> 1) + 3;
		int sourceHeight = (height >> 1) + 3;

		int[] src = this.parent.getInts(sourceX, sourceZ, sourceWidth, sourceHeight);
		int zoomedWidth = sourceWidth << 1;
		int[] zoomed = IntCache.get(zoomedWidth * (sourceHeight << 1));

		for (int dz = 0; dz < sourceHeight - 1; dz++) {
			int index = (dz << 1) * zoomedWidth;
			int topLeft = src[(dz) * sourceWidth];
			int bottomLeft = src[(dz + 1) * sourceWidth];

			for (int dx = 0; dx < sourceWidth - 1; dx++) {
				this.initChunkSeed(dx + sourceX << 1, dz + sourceZ << 1);

				int topRight = src[dx + 1 + dz * sourceWidth];
				int bottomRight = src[dx + 1 + (dz + 1) * sourceWidth];

				if (this.shiftCenter) {
					zoomed[index] = topLeft == TFBiomeIds.LARGE_FEATURE ? bottomRight : topLeft;
				} else {
					zoomed[index] = topLeft;
				}

				zoomed[index++ + zoomedWidth] = this.chooseRandom(topLeft, bottomLeft);
				zoomed[index] = this.chooseRandom(topLeft, topRight);
				zoomed[index++ + zoomedWidth] =
					this.chooseWeighted(topLeft, topRight, bottomLeft, bottomRight);

				topLeft = topRight;
				bottomLeft = bottomRight;
			}
		}

		int[] out = IntCache.get(width * height);
		for (int copyZ = 0; copyZ < height; copyZ++) {
			System.arraycopy(zoomed, (copyZ + (z & 1)) * zoomedWidth + (x & 1),
				out, copyZ * width, width);
		}
		return out;
	}

	protected int chooseRandom(int a, int b) {
		if (a == TFBiomeIds.LARGE_FEATURE) {
			return b;
		}
		if (b == TFBiomeIds.LARGE_FEATURE) {
			return a;
		}
		return this.nextInt(2) != 0 ? b : a;
	}

	protected int chooseRandom(int a, int b, int c) {
		return this.nextInt(3) != 0 ? a : (this.nextInt(2) != 0 ? b : c);
	}

	protected int chooseWeighted(int topLeft, int topRight, int bottomLeft, int bottomRight) {
		if (this.shiftCenter && topLeft == TFBiomeIds.LARGE_FEATURE) {
			return topLeft;
		}
		if (topRight == bottomLeft && bottomLeft == bottomRight) return topRight;
		if (topLeft == topRight && topLeft == bottomLeft) return topLeft;
		if (topLeft == topRight && topLeft == bottomRight) return topLeft;
		if (topLeft == bottomLeft && topLeft == bottomRight) return topLeft;
		if (topLeft == topRight && bottomLeft != bottomRight) return topLeft;
		if (topLeft == bottomLeft && topRight != bottomRight) return topLeft;
		if (topLeft == bottomRight && topRight != bottomLeft) return topLeft;
		if (topRight == topLeft && bottomLeft != bottomRight) return topRight;
		if (topRight == bottomLeft && topLeft != bottomRight) return topRight;
		if (topRight == bottomRight && topLeft != bottomLeft) return topRight;
		if (bottomLeft == topLeft && topRight != bottomRight) return bottomLeft;
		if (bottomLeft == topRight && topLeft != bottomRight) return bottomLeft;
		if (bottomLeft == bottomRight && topLeft != topRight) return bottomLeft;
		if (bottomRight == topLeft && topRight != bottomLeft) return bottomLeft;
		if (bottomRight == topRight && topLeft != bottomLeft) return bottomLeft;
		if (bottomRight == bottomLeft && topLeft != topRight) return bottomLeft;

		if (topLeft == TFBiomeIds.LARGE_FEATURE) {
			return this.chooseRandom(topRight, bottomLeft, bottomRight);
		}
		if (topRight == TFBiomeIds.LARGE_FEATURE) {
			return this.chooseRandom(topLeft, bottomLeft, bottomRight);
		}
		if (bottomLeft == TFBiomeIds.LARGE_FEATURE) {
			return this.chooseRandom(topLeft, topRight, bottomRight);
		}
		if (bottomRight == TFBiomeIds.LARGE_FEATURE) {
			return this.chooseRandom(topLeft, topRight, bottomLeft);
		}

		int roll = this.nextInt(4);
		if (roll == 0) return topLeft;
		if (roll == 1) return topRight;
		return roll == 2 ? bottomLeft : bottomRight;
	}

	public static GenLayer multipleZoom(long baseSeed, GenLayer parent, int count) {
		GenLayer layer = parent;
		for (int i = 0; i < count; i++) {
			layer = new GenLayerTFRegionZoom(baseSeed + i, layer, false);
		}
		return layer;
	}
}
