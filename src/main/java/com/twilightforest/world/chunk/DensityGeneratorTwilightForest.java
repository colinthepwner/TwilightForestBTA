package com.twilightforest.world.chunk;

import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.perlin.DensityGenerator;
import net.minecraft.core.world.noise.FractalNoise3D;
import net.minecraft.core.world.noise.ImprovedPerlinNoise;
import org.jetbrains.annotations.NotNull;

public class DensityGeneratorTwilightForest implements DensityGenerator {

	private final World world;

	private final FractalNoise3D<ImprovedPerlinNoise> minLimitNoise;

	private final FractalNoise3D<ImprovedPerlinNoise> maxLimitNoise;

	private final FractalNoise3D<ImprovedPerlinNoise> mainNoise;

	private final FractalNoise3D<ImprovedPerlinNoise> scaleNoise;

	private final FractalNoise3D<ImprovedPerlinNoise> depthNoise;

	private double[] scaleRegion;
	private double[] depthRegion;
	private double[] mainRegion;
	private double[] minLimitRegion;
	private double[] maxLimitRegion;

	public DensityGeneratorTwilightForest(@NotNull World world) {
		this.world = world;
		long seed = world.getRandomSeed();

		this.minLimitNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 16, 0));
		this.maxLimitNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 16, 16));
		this.mainNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 8, 32));
		this.scaleNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 10, 40));
		this.depthNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 16, 50));
	}

	@NotNull
	@Override
	public double[] generateDensityMap(@NotNull Chunk chunk) {
		int minY = this.world.getWorldType().getMinY(this.world);
		int maxY = this.world.getWorldType().getMaxY(this.world);
		int terrainHeight = maxY + 1 - minY;

		int width = 5;
		int height = terrainHeight / 8 + 1;
		int length = 5;

		int chunkX = chunk.pos.x * 4;
		int chunkY = 0;
		int chunkZ = chunk.pos.z * 4;

		double[] storage = new double[width * height * length];

		double[] temperature = chunk.temperature;
		double[] humidity = chunk.humidity;

		double coordScale = 684.412;
		double heightScale = 684.412;

		this.scaleRegion = this.scaleNoise.getRegion(this.scaleRegion,
			chunkX, 10.0, chunkZ, width, 1, length, 1.121, 1.0, 1.121);
		this.depthRegion = this.depthNoise.getRegion(this.depthRegion,
			chunkX, 10.0, chunkZ, width, 1, length, 200.0, 1.0, 200.0);

		this.mainRegion = this.mainNoise.getRegion(this.mainRegion,
			chunkX, chunkY, chunkZ, width, height, length,
			coordScale / 80.0, heightScale / 160.0, coordScale / 80.0);
		this.minLimitRegion = this.minLimitNoise.getRegion(this.minLimitRegion,
			chunkX, chunkY, chunkZ, width, height, length, coordScale, heightScale, coordScale);
		this.maxLimitRegion = this.maxLimitNoise.getRegion(this.maxLimitRegion,
			chunkX, chunkY, chunkZ, width, height, length, coordScale, heightScale, coordScale);

		int densityIndex = 0;
		int columnIndex = 0;

		int blocksPerSample = 16 / width;

		for (int sx = 0; sx < width; sx++) {
			int sampleX = sx * blocksPerSample + blocksPerSample / 2;

			for (int sz = 0; sz < length; sz++) {
				int sampleZ = sz * blocksPerSample + blocksPerSample / 2;

				double localTemperature = temperature[sampleX * 16 + sampleZ];

				double localHumidity = humidity[sampleX * 16 + sampleZ] * localTemperature;

				double inverseHumidity = 1.0 - localHumidity;
				inverseHumidity *= inverseHumidity;
				inverseHumidity *= inverseHumidity;
				inverseHumidity = 1.0 - inverseHumidity;

				double scale = (this.scaleRegion[columnIndex] + 256.0) / 512.0;
				scale *= inverseHumidity;
				if (scale > 1.0) {
					scale = 1.0;
				}

				double depth = this.depthRegion[columnIndex] / 8000.0;
				if (depth < 0.0) {
					depth = -depth * 0.3;
				}

				depth = depth * 3.0 - 2.0;
				if (depth < 0.0) {
					depth /= 2.0;
					if (depth < -1.0) {
						depth = -1.0;
					}
					depth /= 1.4;
					depth /= 2.0;
					scale = 0.0;
				} else {
					if (depth > 1.0) {
						depth = 1.0;
					}
					depth /= 8.0;
				}

				if (scale < 0.0) {
					scale = 0.0;
				}

				scale += 0.5;
				depth = depth * height / 16.0;
				double centreY = height / 2.0 + depth * 4.0;
				columnIndex++;

				for (int sy = 0; sy < height; sy++) {
					double distanceFromCentre = (sy - centreY) * 12.0 / scale;
					if (distanceFromCentre < 0.0) {
						distanceFromCentre *= 4.0;
					}

					double minDensity = this.minLimitRegion[densityIndex] / 512.0;
					double maxDensity = this.maxLimitRegion[densityIndex] / 512.0;
					double selector = (this.mainRegion[densityIndex] / 10.0 + 1.0) / 2.0;

					double result;
					if (selector < 0.0) {
						result = minDensity;
					} else if (selector > 1.0) {
						result = maxDensity;
					} else {
						result = minDensity + (maxDensity - minDensity) * selector;
					}

					result -= distanceFromCentre;

					if (sy > height - 4) {
						double taper = (sy - (height - 4)) / 3.0F;
						result = result * (1.0 - taper) + -10.0 * taper;
					}

					storage[densityIndex] = result;
					densityIndex++;
				}
			}
		}

		return storage;
	}
}
