package com.twilightforest.world.chunk;

import com.twilightforest.world.biome.TFBiomes;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;
import net.minecraft.core.world.generate.chunk.perlin.SurfaceGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SurfaceGeneratorTwilightForest implements SurfaceGenerator {

	private final World world;
	private final Random rand = new Random();

	public SurfaceGeneratorTwilightForest(@NotNull World world) {
		this.world = world;
	}

	@Override
	public void generateSurface(@NotNull Chunk chunk, @NotNull ChunkGeneratorResult result) {
		int chunkX = chunk.pos.x;
		int chunkZ = chunk.pos.z;

		this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

		Biome[] biomes = biomeGrid(chunk);
		double[] temperature = chunk.temperature;
		double[] humidity = chunk.humidity;

		terraform(result, biomes, temperature, humidity);
		addGlaciers(result, biomes, temperature);
		raiseHills(result, chunkX, chunkZ);
		replaceBlocksForBiome(result, biomes, chunkX, chunkZ);
	}

	private Biome[] biomeGrid(Chunk chunk) {
		Biome[] biomes = new Biome[256];
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Biome biome = chunk.getBlockBiome(x, 64, z);
				if (biome == null) {
					biome = this.world.getBiomeProvider()
						.getBiome(chunk.pos.x * 16 + x, 64, chunk.pos.z * 16 + z);
				}
				biomes[x * 16 + z] = biome;
			}
		}
		return biomes;
	}

	private void terraform(ChunkGeneratorResult result, Biome[] biomes,
	                       double[] temperature, double[] humidity) {
		int minY = this.world.getWorldType().getMinY(this.world);
		int maxY = this.world.getWorldType().getMaxY(this.world);

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Biome biome = biomes[x * 16 + z];
				double t = temperature[x * 16 + z];
				double h = humidity[x * 16 + z];

				double squish = 0.25;

				if (biome == TFBiomes.HIGHLANDS) {
					double f = (1.0 - h * 4.0) * (1.0 - (1.0 - t) * 4.0);
					squish = 0.35 + 0.25 * f;
				}

				if (biome == TFBiomes.SWAMP) {
					double f = (1.0 - (1.0 - h) * 4.0) * (1.0 - (1.0 - t) * 4.0);
					squish = 0.24 - 0.04 * f;
				}

				int newGround = -1;

				for (int y = maxY; y >= minY; y--) {
					if (result.getBlock(x, y, z) == 0) {
						continue;
					}
					if (newGround == -1) {
						newGround = (int) (y * squish);
					}
					if (y >= newGround) {
						result.setBlock(x, y, z, 0);
					}
				}
			}
		}
	}

	private void addGlaciers(ChunkGeneratorResult result, Biome[] biomes, double[] temperature) {
		int minY = this.world.getWorldType().getMinY(this.world);
		int maxY = this.world.getWorldType().getMaxY(this.world);
		int filler = this.world.getWorldType().getFillerBlockId();
		int ice = Blocks.ICE.id();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				if (biomes[x * 16 + z] != TFBiomes.GLACIER) {
					continue;
				}

				int topLevel = -1;
				for (int y = maxY; y >= minY; y--) {
					if (result.getBlock(x, y, z) == filler) {
						topLevel = y;
						break;
					}
				}

				double t = Math.min(temperature[x * 16 + z], 0.1);
				int gHeight = 10 + (int) ((0.1 - t) * 10.0);
				int gTop = topLevel + gHeight + 1;

				for (int y = topLevel + 1; y <= gTop && y <= maxY; y++) {
					result.setBlock(x, y, z, ice);
				}
			}
		}
	}

	private void raiseHills(ChunkGeneratorResult result, int chunkX, int chunkZ) {
		if (!TFHollowHills.nearHollowHill(chunkX, chunkZ, 0L)) {
			return;
		}

		int minY = this.world.getWorldType().getMinY(this.world);
		int maxY = this.world.getWorldType().getMaxY(this.world);
		int filler = this.world.getWorldType().getFillerBlockId();
		int ice = Blocks.ICE.id();

		int[] centre = TFHollowHills.nearestHillCenter(chunkX, chunkZ, 0L);
		int hsize = TFHollowHills.nearestHillSize(chunkX, chunkZ, 0L);
		double hdiam = (hsize * 2 + 1) * 16.0;
		int hx = centre[0];
		int hz = centre[1];

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int dx = x - hx;
				int dz = z - hz;
				int dist = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
				int hheight = (int) (Math.cos(dist / hdiam * Math.PI) * (hdiam / 3.0));

				int newGround = -1;
				for (int y = minY; y <= maxY; y++) {
					int here = result.getBlock(x, y, z);
					if (here != 0 && here != ice) {
						continue;
					}
					if (newGround == -1) {
						newGround = y + hheight;
					}
					if (y <= newGround) {
						result.setBlock(x, y, z, filler);
					}
				}

				int hollow = Math.max(hheight - 4, 0);

				for (int y = minY; y <= maxY; y++) {
					if (y > 16 && y < 16 + hollow) {
						result.setBlock(x, y, z, 0);
					}
				}
			}
		}
	}

	private void replaceBlocksForBiome(ChunkGeneratorResult result, Biome[] biomes,
	                                   int chunkX, int chunkZ) {
		int minY = this.world.getWorldType().getMinY(this.world);
		int maxY = this.world.getWorldType().getMaxY(this.world);
		int filler = this.world.getWorldType().getFillerBlockId();

		boolean isHill = TFHollowHills.nearHollowHill(chunkX, chunkZ, 0L);

		int bedrock = Blocks.BEDROCK.id();
		int obsidian = Blocks.OBSIDIAN.id();
		int glowstone = Blocks.GLOWSTONE.id();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Biome biome = biomes[x + z * 16];
				int top = biome.getSurfaceProperties().getTopBlock().id();
				int fill = biome.getSurfaceProperties().getFillerBlock().id();

				int fillLevel = 1 + (int) (this.rand.nextDouble() * this.rand.nextDouble() * 3.0 + 0.65);
				int topLevel = -1;

				for (int y = maxY; y >= minY; y--) {
					int bx = z;
					int bz = x;

					if (y <= 8) {
						int mb = bedrock;
						if (isHill) {
							mb = switch (y) {
								case 1, 2 -> obsidian;
								case 3 -> glowstone;
								case 4, 5, 6 -> 0;
								case 7, 8 -> glowstone;
								default -> bedrock;
							};
						}
						result.setBlock(bx, y, bz, mb);
						continue;
					}

					if (result.getBlock(bx, y, bz) != filler) {
						continue;
					}

					if (topLevel == -1) {
						topLevel = y;
						result.setBlock(bx, y, bz, top);
					} else if (y < topLevel && y >= topLevel - fillLevel) {
						result.setBlock(bx, y, bz, fill);
					}
				}
			}
		}
	}
}
