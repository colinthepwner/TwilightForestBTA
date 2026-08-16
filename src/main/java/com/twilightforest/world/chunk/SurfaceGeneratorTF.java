package com.twilightforest.world.chunk;

import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.ChunkGeneratorResult;
import net.minecraft.core.world.generate.chunk.perlin.SurfaceGenerator;
import net.minecraft.core.world.noise.FractalNoise3D;
import net.minecraft.core.world.noise.ImprovedPerlinNoise;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class SurfaceGeneratorTF implements SurfaceGenerator {

	private final World world;
	private final Random rand = new Random();

	private final FractalNoise3D<ImprovedPerlinNoise> soilNoise;

	private double[] stoneNoise;

	public SurfaceGeneratorTF(@NotNull World world) {
		this.world = world;
		this.soilNoise = new FractalNoise3D<>(
			ImprovedPerlinNoise.genOctaves(world.getRandomSeed(), 4, 40));
	}

	private static final boolean TRACE_PLATEAU = false;
	private static final int TRACE_Z = -175;
	private static final int TRACE_X0 = -404;
	private static final int TRACE_X1 = -395;

	private static boolean tracedChunk(int chunkX, int chunkZ) {
		return TRACE_PLATEAU && chunkZ == (TRACE_Z >> 4)
			&& chunkX >= (TRACE_X0 >> 4) && chunkX <= (TRACE_X1 >> 4);
	}

	private static int topSolid(ChunkGeneratorResult result, int x, int z, int maxY) {
		for (int y = maxY; y >= 0; y--) {
			if (result.getBlock(x, y, z) != 0) {
				return y;
			}
		}
		return -1;
	}

	private void tracePass(String pass, ChunkGeneratorResult result, int chunkX, int chunkZ, int maxY) {
		if (!tracedChunk(chunkX, chunkZ)) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (int wx = TRACE_X0; wx <= TRACE_X1; wx++) {
			if ((wx >> 4) != chunkX) {
				continue;
			}
			int lx = wx - (chunkX << 4);
			int lz = TRACE_Z - (chunkZ << 4);
			sb.append(" x=").append(wx).append(':')
				.append(topSolid(result, lx, lz, maxY))
				.append('/').append(result.getBlock(lx, 32, lz));
		}
		if (sb.length() > 0) {
			com.twilightforest.TwilightForest.LOGGER.info(
				"[plateau] after {} chunk({},{}) topSolid/blockAt32:{}", pass, chunkX, chunkZ, sb);
		}
	}

	@Override
	public void generateSurface(@NotNull Chunk chunk, @NotNull ChunkGeneratorResult result) {
		int chunkX = chunk.pos.x;
		int chunkZ = chunk.pos.z;

		this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

		Biome[] biomes = biomeGrid(chunk);
		double[] temperature = chunk.temperature;

		int maxY = this.world.getWorldType().getMaxY(this.world);
		tracePass("terrain", result, chunkX, chunkZ, maxY);
		addGlaciers(result, biomes, temperature);
		tracePass("glaciers", result, chunkX, chunkZ, maxY);
		raiseHills(result, chunkX, chunkZ);
		tracePass("raiseHills", result, chunkX, chunkZ, maxY);
		replaceBlocksForBiome(result, biomes, temperature, chunkX, chunkZ);
		tracePass("replaceBlocks", result, chunkX, chunkZ, maxY);
	}

	private Biome[] biomeGrid(Chunk chunk) {
		Biome[] biomes = new Biome[256];
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Biome biome = chunk.getBlockBiome(x, 32, z);
				if (biome == null) {
					biome = this.world.getBiomeProvider()
						.getBiome(chunk.pos.x * 16 + x, 32, chunk.pos.z * 16 + z);
				}
				biomes[x * 16 + z] = biome;
			}
		}
		return biomes;
	}

	private void addGlaciers(ChunkGeneratorResult result, Biome[] biomes, double[] temperature) {
		int maxY = this.world.getWorldType().getMaxY(this.world);
		int stone = Blocks.STONE.id();
		int gravel = Blocks.GRAVEL.id();
		int ice = Blocks.ICE.id();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				if (biomes[x * 16 + z] != TFBiomes.GLACIER) {
					continue;
				}

				int topLevel = -1;
				for (int y = maxY; y >= 0; y--) {
					if (result.getBlock(x, y, z) == stone) {
						topLevel = y;
						result.setBlock(x, y, z, gravel);
						break;
					}
				}

				double t = Math.min(temperature[x * 16 + z], 0.1);
				int capHeight = 16 + (int) ((0.1 - t) * 16.0);
				int capTop = topLevel + capHeight + 1;

				for (int y = topLevel + 1; y <= capTop && y <= maxY; y++) {
					result.setBlock(x, y, z, ice);
				}
			}
		}
	}

	private void raiseHills(ChunkGeneratorResult result, int chunkX, int chunkZ) {
		if (tracedChunk(chunkX, chunkZ)) {
			com.twilightforest.TwilightForest.LOGGER.info(
				"[plateau] raiseHills chunk({},{}) nearChunkFeature={} htype={} hsize={} centre={},{}",
				chunkX, chunkZ,
				TFFeature.nearChunkFeature(this.world, chunkX, chunkZ),
				TFFeature.nearestFeatureType(this.world, chunkX, chunkZ),
				TFFeature.nearestFeatureSize(this.world, chunkX, chunkZ),
				TFFeature.nearestFeatureCenter(this.world, chunkX, chunkZ)[0],
				TFFeature.nearestFeatureCenter(this.world, chunkX, chunkZ)[1]);
		}
		if (!TFFeature.nearChunkFeature(this.world, chunkX, chunkZ)) {
			return;
		}

		int maxY = this.world.getWorldType().getMaxY(this.world);
		int stone = Blocks.STONE.id();
		int ice = Blocks.ICE.id();
		int water = Blocks.FLUID_WATER_STILL.id();

		int[] centre = TFFeature.nearestFeatureCenter(this.world, chunkX, chunkZ);
		int hsize = TFFeature.nearestFeatureSize(this.world, chunkX, chunkZ);
		int htype = TFFeature.nearestFeatureType(this.world, chunkX, chunkZ);
		double hdiam = (hsize * 2 + 1) * 16.0;
		int hx = centre[0];
		int hz = centre[1];

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int dx = x - hx;
				int dz = z - hz;
				int dist = (int) Math.sqrt((double) dx * dx + (double) dz * dz);
				int hheight = (int) (Math.cos(dist / hdiam * Math.PI) * (hdiam / 3.0));

				if (htype == TFFeature.SMALL_HILL || htype == TFFeature.MEDIUM_HILL
					|| htype == TFFeature.LARGE_HILL || htype == TFFeature.HYDRA_LAIR) {
					int newGround = -1;
					for (int y = 0; y <= maxY; y++) {
						int here = result.getBlock(x, y, z);
						if (here != 0 && here != ice) {
							continue;
						}
						if (newGround == -1) {
							newGround = y + hheight;
						}
						if (y <= newGround) {
							result.setBlock(x, y, z, stone);
						}
					}

					int hollow = hheight - 4 - hsize;

					if (htype == TFFeature.HYDRA_LAIR) {
						int mx = dx + 16;
						int mz = dz + 16;
						int mdist = (int) Math.sqrt((double) mx * mx + (double) mz * mz);
						int mheight = (int) (Math.cos(mdist / (hdiam / 1.5) * Math.PI) * (hdiam / 1.5));
						hollow = Math.max(mheight - 4, hollow);
					}

					if (hollow < 0) {
						hollow = 0;
					}

					int hollowFloor = htype == TFFeature.HYDRA_LAIR
						? TFWorldConstants.SEA_LEVEL
						: TFWorldConstants.SEA_LEVEL - 3 - hollow / 8;

					for (int y = 0; y <= maxY; y++) {

						if (hheight > 0 && y < TFWorldConstants.SEA_LEVEL
							&& result.getBlock(x, y, z) != stone) {
							result.setBlock(x, y, z, stone);
						}
						if (y > hollowFloor && y < hollowFloor + hollow) {
							result.setBlock(x, y, z, 0);
						}
					}
				}

				if (htype == TFFeature.HEDGE_MAZE || htype == TFFeature.NAGA_COURTYARD
					|| htype == TFFeature.QUEST_GROVE) {
					float squish = 0.0F;
					int mazeHeight = TFWorldConstants.SEA_LEVEL + 1;
					int boundary = (hsize * 2 + 1) * 8 - 8;

					if (dx <= -boundary) {
						squish = (-dx - boundary) / 8.0F;
					}
					if (dx >= boundary) {
						squish = (dx - boundary) / 8.0F;
					}
					if (dz <= -boundary) {
						squish = Math.max(squish, (-dz - boundary) / 8.0F);
					}
					if (dz >= boundary) {
						squish = Math.max(squish, (dz - boundary) / 8.0F);
					}

					if (squish > 0.0F) {
						int newGround = -1;
						for (int y = 0; y <= maxY; y++) {
							if (result.getBlock(x, y, z) != stone && newGround == -1) {
								mazeHeight = (int) (mazeHeight + (y - mazeHeight) * squish);
								newGround = y;
							}
						}
					}

					int worldX = (chunkX << 4) + x;
					int worldZ = (chunkZ << 4) + z;
					boolean trace = tracedChunk(chunkX, chunkZ)
						&& worldZ == TRACE_Z && worldX >= TRACE_X0 && worldX <= TRACE_X1;
					if (trace) {
						com.twilightforest.TwilightForest.LOGGER.info(
							"[plateau]   col x={} z={} dx={} dz={} boundary={} squish={} mazeHeight={} "
								+ "topSolidBefore={}",
							worldX, worldZ, dx, dz, boundary, squish, mazeHeight,
							topSolid(result, x, z, maxY));
					}

					for (int y = 0; y <= maxY; y++) {
						int here = result.getBlock(x, y, z);
						if (y < mazeHeight && (here == 0 || here == water)) {
							result.setBlock(x, y, z, stone);
						}
						if (y >= mazeHeight && here != water) {
							result.setBlock(x, y, z, 0);
						}
					}

					if (trace) {
						com.twilightforest.TwilightForest.LOGGER.info(
							"[plateau]   col x={} z={} -> topSolidAfter={}",
							worldX, worldZ, topSolid(result, x, z, maxY));
					}
				}
			}
		}
	}

	private void replaceBlocksForBiome(ChunkGeneratorResult result, Biome[] biomes,
	                                   double[] temperature, int chunkX, int chunkZ) {
		int maxY = this.world.getWorldType().getMaxY(this.world);
		int seaLevel = TFWorldConstants.SEA_LEVEL;

		double scale = 0.03125;
		this.stoneNoise = this.soilNoise.getRegion(this.stoneNoise,
			chunkX * 16, 0, chunkZ * 16, 16, 1, 16, scale * 2.0, scale * 2.0, scale * 2.0);

		int stone = Blocks.STONE.id();
		int bedrock = Blocks.BEDROCK.id();
		int ice = Blocks.ICE.id();
		int water = Blocks.FLUID_WATER_STILL.id();
		int sand = Blocks.SAND.id();
		int sandstone = Blocks.SANDSTONE.id();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				Biome biome = biomes[z + x * 16];

				double columnTemperature = temperature[z + x * 16];

				int soilDepth = (int) (this.stoneNoise[x + z * 16] / 3.0 + 3.0
					+ this.rand.nextDouble() * 0.25);
				int remaining = -1;

				int topBlock = biome.getSurfaceProperties().getTopBlock().id();
				int fillerBlock = biome.getSurfaceProperties().getFillerBlock().id();

				for (int y = maxY; y >= 0; y--) {

					if (y <= this.rand.nextInt(5)) {
						result.setBlock(z, y, x, bedrock);
						continue;
					}

					if (result.getBlock(z, y, x) != stone) {
						continue;
					}

					if (remaining == -1) {
						if (soilDepth <= 0) {
							topBlock = 0;
							fillerBlock = stone;
						} else if (y >= seaLevel - 4 && y <= seaLevel + 1) {
							topBlock = biome.getSurfaceProperties().getTopBlock().id();
							fillerBlock = biome.getSurfaceProperties().getFillerBlock().id();
						}

						if (y < seaLevel && topBlock == 0) {
							topBlock = columnTemperature < 0.15 ? ice : water;
						}

						remaining = soilDepth;
						result.setBlock(z, y, x, y >= seaLevel - 1 ? topBlock : fillerBlock);
					} else if (remaining > 0) {
						remaining--;
						result.setBlock(z, y, x, fillerBlock);

						if (remaining == 0 && fillerBlock == sand) {
							remaining = this.rand.nextInt(4);
							fillerBlock = sandstone;
						}
					}
				}
			}
		}
	}
}
