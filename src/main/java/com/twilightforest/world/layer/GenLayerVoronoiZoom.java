package com.twilightforest.world.layer;

public class GenLayerVoronoiZoom extends GenLayer {

	public GenLayerVoronoiZoom(long baseSeed, GenLayer parent) {
		super(baseSeed);
		this.parent = parent;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		x -= 2;
		z -= 2;

		int shift = 2;
		int scale = 1 << shift;
		int srcX = x >> shift;
		int srcZ = z >> shift;
		int srcWidth = (width >> shift) + 3;
		int srcHeight = (height >> shift) + 3;

		int[] src = this.parent.getInts(srcX, srcZ, srcWidth, srcHeight);
		int zoomedWidth = srcWidth << shift;
		int zoomedHeight = srcHeight << shift;
		int[] zoomed = IntCache.get(zoomedWidth * zoomedHeight);

		for (int dz = 0; dz < srcHeight - 1; dz++) {
			int topLeft = src[dz * srcWidth];
			int bottomLeft = src[(dz + 1) * srcWidth];

			for (int dx = 0; dx < srcWidth - 1; dx++) {
				double jitter = scale * 0.9;

				this.initChunkSeed(dx + srcX << shift, dz + srcZ << shift);
				double tlZ = (this.nextInt(1024) / 1024.0 - 0.5) * jitter;
				double tlX = (this.nextInt(1024) / 1024.0 - 0.5) * jitter;

				this.initChunkSeed(dx + srcX + 1 << shift, dz + srcZ << shift);
				double trZ = (this.nextInt(1024) / 1024.0 - 0.5) * jitter + scale;
				double trX = (this.nextInt(1024) / 1024.0 - 0.5) * jitter;

				this.initChunkSeed(dx + srcX << shift, dz + srcZ + 1 << shift);
				double blZ = (this.nextInt(1024) / 1024.0 - 0.5) * jitter;
				double blX = (this.nextInt(1024) / 1024.0 - 0.5) * jitter + scale;

				this.initChunkSeed(dx + srcX + 1 << shift, dz + srcZ + 1 << shift);
				double brZ = (this.nextInt(1024) / 1024.0 - 0.5) * jitter + scale;
				double brX = (this.nextInt(1024) / 1024.0 - 0.5) * jitter + scale;

				int topRight = src[dx + 1 + dz * srcWidth];
				int bottomRight = src[dx + 1 + (dz + 1) * srcWidth];

				for (int cellX = 0; cellX < scale; cellX++) {
					int index = ((dz << shift) + cellX) * zoomedWidth + (dx << shift);

					for (int cellZ = 0; cellZ < scale; cellZ++) {
						double dTL = (cellX - tlX) * (cellX - tlX) + (cellZ - tlZ) * (cellZ - tlZ);
						double dTR = (cellX - trX) * (cellX - trX) + (cellZ - trZ) * (cellZ - trZ);
						double dBL = (cellX - blX) * (cellX - blX) + (cellZ - blZ) * (cellZ - blZ);
						double dBR = (cellX - brX) * (cellX - brX) + (cellZ - brZ) * (cellZ - brZ);

						if (dTL < dTR && dTL < dBL && dTL < dBR) {
							zoomed[index++] = topLeft;
						} else if (dTR < dTL && dTR < dBL && dTR < dBR) {
							zoomed[index++] = topRight;
						} else if (dBL < dTL && dBL < dTR && dBL < dBR) {
							zoomed[index++] = bottomLeft;
						} else {
							zoomed[index++] = bottomRight;
						}
					}
				}

				topLeft = topRight;
				bottomLeft = bottomRight;
			}
		}

		int[] out = IntCache.get(width * height);
		for (int copyZ = 0; copyZ < height; copyZ++) {
			System.arraycopy(zoomed, (copyZ + (z & scale - 1)) * zoomedWidth + (x & scale - 1),
				out, copyZ * width, width);
		}
		return out;
	}
}
