package com.twilightforest.world.chunk;

import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;
import net.minecraft.core.world.generate.chunk.perlin.DensityGenerator;
import net.minecraft.core.world.generate.chunk.perlin.TerrainGeneratorLerp;
import org.jetbrains.annotations.NotNull;

public class TerrainGeneratorTF extends TerrainGeneratorLerp {

	private final DensityGenerator densityGenerator;

	public TerrainGeneratorTF(@NotNull World world) {
		super(world);
		this.densityGenerator = new DensityGeneratorTF(world);
	}

	@NotNull
	@Override
	public DensityGenerator getDensityGenerator() {
		return this.densityGenerator;
	}

	@Override
	protected int getBlockAt(@NotNull Chunk chunk, int x, int y, int z, double density) {
		if (density > 0.0) {
			return Blocks.STONE.id();
		}
		if (y < TFWorldConstants.SEA_LEVEL) {
			return Blocks.FLUID_WATER_STILL.id();
		}
		return 0;
	}

	@NotNull
	@Override
	public ChunkGeneratorResult generateTerrain(@NotNull Chunk chunk, @NotNull double[] densityMap) {
		ChunkGeneratorResult result = new ChunkGeneratorResult();

		int xzBlobs = 4;
		int yBlobs = 8;
		int densityStride = 9;
		int densityWidth = xzBlobs + 1;

		for (int blobX = 0; blobX < xzBlobs; blobX++) {
			for (int blobZ = 0; blobZ < xzBlobs; blobZ++) {
				for (int blobY = 0; blobY < yBlobs; blobY++) {
					double eighth = 0.125;

					int base = (blobX * densityWidth + blobZ) * densityStride + blobY;
					double d1 = densityMap[base];
					double d2 = densityMap[base + densityStride];
					double d3 = densityMap[base + densityWidth * densityStride];
					double d4 = densityMap[base + (densityWidth + 1) * densityStride];

					double d5 = (densityMap[base + 1] - d1) * eighth;
					double d6 = (densityMap[base + densityStride + 1] - d2) * eighth;
					double d7 = (densityMap[base + densityWidth * densityStride + 1] - d3) * eighth;
					double d8 = (densityMap[base + (densityWidth + 1) * densityStride + 1] - d4) * eighth;

					for (int yInBlob = 0; yInBlob < 8; yInBlob++) {
						double quarter = 0.25;
						double lerpXMinZ = d1;
						double lerpXMaxZ = d2;
						double stepXMinZ = (d3 - d1) * quarter;
						double stepXMaxZ = (d4 - d2) * quarter;

						int y = blobY * 8 + yInBlob;

						for (int xInBlob = 0; xInBlob < 4; xInBlob++) {
							double stepZ = (lerpXMaxZ - lerpXMinZ) * quarter;
							double density = lerpXMinZ;

							for (int zInBlob = 0; zInBlob < 4; zInBlob++) {
								int x = xInBlob + blobX * 4;
								int z = zInBlob + blobZ * 4;
								result.setBlock(x, y, z, this.getBlockAt(chunk, x, y, z, density));
								density += stepZ;
							}

							lerpXMinZ += stepXMinZ;
							lerpXMaxZ += stepXMaxZ;
						}

						d1 += d5;
						d2 += d6;
						d3 += d7;
						d4 += d8;
					}
				}
			}
		}

		return result;
	}
}
