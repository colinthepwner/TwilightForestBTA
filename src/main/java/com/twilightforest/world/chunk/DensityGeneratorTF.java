package com.twilightforest.world.chunk;

import com.twilightforest.world.layer.GenLayer;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.perlin.DensityGenerator;
import net.minecraft.core.world.noise.FractalNoise3D;
import net.minecraft.core.world.noise.ImprovedPerlinNoise;
import net.minecraft.core.util.helper.MathHelper;
import org.jetbrains.annotations.NotNull;

public class DensityGeneratorTF implements DensityGenerator {

	private final World world;

	private final FractalNoise3D<ImprovedPerlinNoise> minLimitNoise;
	private final FractalNoise3D<ImprovedPerlinNoise> maxLimitNoise;
	private final FractalNoise3D<ImprovedPerlinNoise> mainNoise;
	private final FractalNoise3D<ImprovedPerlinNoise> scaleNoise;
	private final FractalNoise3D<ImprovedPerlinNoise> depthNoise;

	private final float[] squareTable = new float[25];

	private double[] scaleRegion;
	private double[] depthRegion;
	private double[] mainRegion;
	private double[] minLimitRegion;
	private double[] maxLimitRegion;

	public DensityGeneratorTF(@NotNull World world) {
		this.world = world;
		long seed = world.getRandomSeed();

		this.minLimitNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 16, 0));
		this.maxLimitNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 16, 16));
		this.mainNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 8, 32));
		this.scaleNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 10, 44));
		this.depthNoise = new FractalNoise3D<>(ImprovedPerlinNoise.genOctaves(seed, 16, 54));

		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				this.squareTable[dx + 2 + (dz + 2) * 5] =
					10.0F / MathHelper.sqrt_float(dx * dx + dz * dz + 0.2F);
			}
		}
	}

	@NotNull
	@Override
	public double[] generateDensityMap(@NotNull Chunk chunk) {
		int width = 5;
		int height = 9;
		int length = 5;

		int x = chunk.pos.x * 4;
		int z = chunk.pos.z * 4;

		double[] density = new double[width * height * length];

		int[] biomeIds = sampleBiomeIds(chunk.pos.x * 4 - 2, chunk.pos.z * 4 - 2, width + 5, length + 5);

		double coordScale = 684.412;
		double heightScale = 684.412;

		this.scaleRegion = this.scaleNoise.getRegion(this.scaleRegion,
			x, 10.0, z, width, 1, length, 1.121, 1.0, 1.121);
		this.depthRegion = this.depthNoise.getRegion(this.depthRegion,
			x, 10.0, z, width, 1, length, 200.0, 1.0, 200.0);
		this.mainRegion = this.mainNoise.getRegion(this.mainRegion,
			x, 0, z, width, height, length,
			coordScale / 80.0, heightScale / 160.0, coordScale / 80.0);
		this.minLimitRegion = this.minLimitNoise.getRegion(this.minLimitRegion,
			x, 0, z, width, height, length, coordScale, heightScale, coordScale);
		this.maxLimitRegion = this.maxLimitNoise.getRegion(this.maxLimitRegion,
			x, 0, z, width, height, length, coordScale, heightScale, coordScale);

		int densityIndex = 0;
		int columnIndex = 0;

		for (int sx = 0; sx < width; sx++) {
			for (int sz = 0; sz < length; sz++) {

				float scaleAcc = 0.0F;
				float offsetAcc = 0.0F;
				float weightAcc = 0.0F;

				int centreId = biomeIds[sx + 2 + (sz + 2) * (width + 5)];
				float centreMin = TFBiomeHeights.minHeight(centreId);

				for (int dx = -2; dx <= 2; dx++) {
					for (int dz = -2; dz <= 2; dz++) {
						int id = biomeIds[sx + dx + 2 + (sz + dz + 2) * (width + 5)];
						float neighbourMin = TFBiomeHeights.minHeight(id);

						float weight = this.squareTable[dx + 2 + (dz + 2) * 5] / (neighbourMin + 2.0F);

						if (neighbourMin > centreMin) {
							weight /= 2.0F;
						}

						scaleAcc += TFBiomeHeights.maxHeight(id) * weight;
						offsetAcc += neighbourMin * weight;
						weightAcc += weight;
					}
				}

				float scale = scaleAcc / weightAcc;
				float offset = offsetAcc / weightAcc;
				scale = scale * 0.9F + 0.1F;
				offset = (offset * 4.0F - 1.0F) / 8.0F;

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
				} else {
					if (depth > 1.0) {
						depth = 1.0;
					}
					depth /= 8.0;
				}
				columnIndex++;

				for (int sy = 0; sy < height; sy++) {
					double columnOffset = offset;
					columnOffset += depth * 0.2;
					columnOffset = columnOffset * height / 16.0;

					double centreY = height / 2.0 + columnOffset * 4.0;

					double distanceFromCentre =
						(sy - centreY) * 12.0 * 128.0 / TFWorldConstants.WORLD_HEIGHT / scale;
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

					density[densityIndex] = result;
					densityIndex++;
				}
			}
		}

		return density;
	}

	private int[] sampleBiomeIds(int x, int z, int width, int length) {
		if (this.world.getBiomeProvider() instanceof BiomeProviderTF tf) {
			GenLayer layer = tf.getCoarseLayer();
			return layer.getInts(x, z, width, length);
		}

		return new int[width * length];
	}
}
