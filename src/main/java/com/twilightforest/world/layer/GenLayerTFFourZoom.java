package com.twilightforest.world.layer;

public class GenLayerTFFourZoom extends GenLayer {

	private final int zoomFactor = 2;

	public GenLayerTFFourZoom(long baseSeed, GenLayer parent) {
		super(baseSeed);
		this.parent = parent;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int sourceX = x / this.zoomFactor - (x < 0 && x % this.zoomFactor != 0 ? 1 : 0);
		int sourceZ = z / this.zoomFactor - (z < 0 && z % this.zoomFactor != 0 ? 1 : 0);
		int sourceWidth = width / this.zoomFactor + 3;
		int sourceHeight = height / this.zoomFactor + 3;

		int[] src = this.parent.getInts(sourceX, sourceZ, sourceWidth, sourceHeight);
		int zoomedWidth = sourceWidth * this.zoomFactor;
		int[] zoomed = IntCache.get(zoomedWidth * sourceHeight * this.zoomFactor);

		for (int sz = 0; sz < sourceHeight - 1; sz++) {
			for (int sx = 0; sx < sourceWidth - 1; sx++) {
				this.initChunkSeed(sx + sourceX * this.zoomFactor, sz + sourceZ * this.zoomFactor);

				int index = sx * this.zoomFactor + sz * this.zoomFactor * zoomedWidth;
				int source = src[sx + sz * sourceWidth];

				int reservedX = 0;
				int reservedZ = 0;

				for (int dz = 0; dz < this.zoomFactor; dz++) {
					for (int dx = 0; dx < this.zoomFactor; dx++) {
						zoomed[index + dx] = dx == reservedX && dz == reservedZ
							? TFBiomeIds.LARGE_FEATURE
							: source;
					}
					index += zoomedWidth;
				}
			}
		}

		int[] out = IntCache.get(width * height);
		int offsetX = x - sourceX * this.zoomFactor;
		int offsetZ = z - sourceZ * this.zoomFactor;
		for (int copyZ = 0; copyZ < height; copyZ++) {
			System.arraycopy(zoomed, (copyZ + offsetZ) * zoomedWidth + offsetX,
				out, copyZ * width, width);
		}
		return out;
	}
}
