package com.twilightforest.world.feature;

import com.twilightforest.world.chunk.TFWorldConstants;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import net.minecraft.core.world.generate.LargeFeature;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;

import java.util.Random;

public class TFRavineLargeFeature extends LargeFeature {

	private final float[] widthNoise = new float[TFWorldConstants.WORLD_HEIGHT];

	private int chunkRadius() {
		return this.getRadiusChunk();
	}

	@Override
	protected void doGeneration(World world, Random random, int sourceChunkX, int sourceChunkZ,
	                            int targetChunkX, int targetChunkZ, ChunkGeneratorResult result) {

		if (random.nextInt(127) != 0) {
			return;
		}

		double startX = sourceChunkX * 16 + random.nextInt(16);

		double startY = random.nextInt(random.nextInt(40) + 8) + 20;
		double startZ = sourceChunkZ * 16 + random.nextInt(16);

		int tunnels = 1;
		for (int i = 0; i < tunnels; i++) {
			float heading = random.nextFloat() * (float) Math.PI * 2.0F;
			float pitch = (random.nextFloat() - 0.5F) * 2.0F / 8.0F;
			float radius = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
			this.carve(world, random.nextLong(), targetChunkX, targetChunkZ, result,
				startX, startY, startZ, radius, heading, pitch, 0, 0, 3.0);
		}
	}

	private void carve(World world, long seed, int chunkX, int chunkZ, ChunkGeneratorResult result,
	                   double x, double y, double z, float radius, float heading, float pitch,
	                   int step, int length, double heightMul) {
		Random random = new Random(seed);
		double centreX = chunkX * 16 + 8;
		double centreZ = chunkZ * 16 + 8;
		float headingDrift = 0.0F;
		float pitchDrift = 0.0F;

		if (length <= 0) {
			int reach = this.chunkRadius() * 16 - 16;
			length = reach - random.nextInt(reach / 4);
		}

		boolean fromMiddle = false;
		if (step == -1) {
			step = length / 2;
			fromMiddle = true;
		}

		float width = 1.0F;
		for (int cy = 0; cy < TFWorldConstants.WORLD_HEIGHT; cy++) {
			if (cy == 0 || random.nextInt(3) == 0) {
				width = 1.0F + random.nextFloat() * random.nextFloat();
			}
			this.widthNoise[cy] = width * width;
		}

		for (; step < length; step++) {

			double horizontal = 1.5 + MathHelper.sin(step * (float) Math.PI / length) * radius;
			double vertical = horizontal * heightMul;
			horizontal *= random.nextFloat() * 0.25 + 0.75;
			vertical *= random.nextFloat() * 0.25 + 0.75;

			float pitchCos = MathHelper.cos(pitch);
			float pitchSin = MathHelper.sin(pitch);
			x += MathHelper.cos(heading) * pitchCos;
			y += pitchSin;
			z += MathHelper.sin(heading) * pitchCos;

			pitch *= 0.7F;
			pitch += pitchDrift * 0.05F;
			heading += headingDrift * 0.05F;
			pitchDrift *= 0.8F;
			headingDrift *= 0.5F;
			pitchDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			headingDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

			if (!fromMiddle && random.nextInt(4) == 0) {
				continue;
			}

			double dx = x - centreX;
			double dz = z - centreZ;
			double remaining = length - step;
			double reach = radius + 2.0F + 16.0F;
			if (dx * dx + dz * dz - remaining * remaining > reach * reach) {
				return;
			}

			if (x < centreX - 16.0 - horizontal * 2.0 || z < centreZ - 16.0 - horizontal * 2.0
				|| x > centreX + 16.0 + horizontal * 2.0 || z > centreZ + 16.0 + horizontal * 2.0) {
				continue;
			}

			int minX = MathHelper.floor(x - horizontal) - chunkX * 16 - 1;
			int maxX = MathHelper.floor(x + horizontal) - chunkX * 16 + 1;
			int minY = MathHelper.floor(y - vertical) - 1;
			int maxY = MathHelper.floor(y + vertical) + 1;
			int minZ = MathHelper.floor(z - horizontal) - chunkZ * 16 - 1;
			int maxZ = MathHelper.floor(z + horizontal) - chunkZ * 16 + 1;

			if (minX < 0) minX = 0;
			if (maxX > 16) maxX = 16;
			if (minY < 1) minY = 1;
			if (maxY > TFWorldConstants.WORLD_HEIGHT - 8) maxY = TFWorldConstants.WORLD_HEIGHT - 8;
			if (minZ < 0) minZ = 0;
			if (maxZ > 16) maxZ = 16;

			if (this.touchesWater(result, minX, maxX, minY, maxY, minZ, maxZ)) {
				continue;
			}

			this.cut(world, result, chunkX, chunkZ, x, y, z, horizontal, vertical,
				minX, maxX, minY, maxY, minZ, maxZ);

			if (fromMiddle) {
				break;
			}
		}
	}

	private boolean touchesWater(ChunkGeneratorResult result, int minX, int maxX, int minY, int maxY,
	                             int minZ, int maxZ) {
		int flowing = Blocks.FLUID_WATER_FLOWING.id();
		int still = Blocks.FLUID_WATER_STILL.id();

		for (int cx = minX; cx < maxX; cx++) {
			for (int cz = minZ; cz < maxZ; cz++) {
				for (int cy = maxY + 1; cy >= minY - 1; cy--) {
					if (cy < 0 || cy >= TFWorldConstants.WORLD_HEIGHT) {
						continue;
					}
					int block = result.getBlock(cx, cy, cz);
					if (block == flowing || block == still) {
						return true;
					}
					if (cy != minY - 1 && cx != minX && cx != maxX - 1 && cz != minZ && cz != maxZ - 1) {
						cy = minY;
					}
				}
			}
		}
		return false;
	}

	private void cut(World world, ChunkGeneratorResult result, int chunkX, int chunkZ,
	                 double x, double y, double z, double horizontal, double vertical,
	                 int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
		int air = 0;
		int stone = Blocks.STONE.id();
		int grass = Blocks.GRASS.id();
		int dirt = Blocks.DIRT.id();

		for (int cx = minX; cx < maxX; cx++) {
			double nx = (cx + chunkX * 16 + 0.5 - x) / horizontal;

			for (int cz = minZ; cz < maxZ; cz++) {
				double nz = (cz + chunkZ * 16 + 0.5 - z) / horizontal;
				if (nx * nx + nz * nz >= 1.0) {
					continue;
				}

				boolean underGrass = false;

				for (int cy = maxY - 1; cy >= minY; cy--) {
					double ny = (cy + 0.5 - y) / vertical;

					if ((nx * nx + nz * nz) * this.widthNoise[cy] + ny * ny / 6.0 >= 1.0) {
						continue;
					}

					int block = result.getBlock(cx, cy, cz);
					if (block == grass) {
						underGrass = true;
					}

					if (block == stone || block == dirt || block == grass) {
						result.setBlock(cx, cy, cz, air);
						if (underGrass && cy > 0 && result.getBlock(cx, cy - 1, cz) == dirt) {
							result.setBlock(cx, cy - 1, cz, this.topBlockAt(world, cx + chunkX * 16,
								cz + chunkZ * 16, grass));
						}
					}
				}
			}
		}
	}

	private int topBlockAt(World world, int x, int z, int fallback) {
		var biome = world.getBiomeProvider().getBiome(x, 64, z);
		if (biome == null || biome.getSurfaceProperties() == null
			|| biome.getSurfaceProperties().getTopBlock() == null) {
			return fallback;
		}
		return biome.getSurfaceProperties().getTopBlock().id();
	}
}
