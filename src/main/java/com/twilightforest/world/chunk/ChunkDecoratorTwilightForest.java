package com.twilightforest.world.chunk;

import com.twilightforest.compat.TFWorldFeature;
import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.feature.WorldFeatureTFFoundation;
import com.twilightforest.world.feature.WorldFeatureTFHollowHill;
import com.twilightforest.world.feature.WorldFeatureTFHollowTree;
import com.twilightforest.world.feature.WorldFeatureTFMonolith;
import com.twilightforest.world.feature.WorldFeatureTFOutsideStalagmite;
import com.twilightforest.world.feature.WorldFeatureTFStoneCircle;
import com.twilightforest.world.feature.WorldFeatureTFWell;
import com.twilightforest.world.feature.WorldFeatureTFWitchHut;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.generate.chunk.ChunkDecorator;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.WorldFeatureFlowers;
import net.minecraft.core.world.generate.feature.WorldFeatureLake;
import net.minecraft.core.world.generate.feature.WorldFeatureLiquid;
import net.minecraft.core.world.generate.feature.WorldFeatureOre;
import net.minecraft.core.world.generate.feature.WorldFeaturePumpkin;
import net.minecraft.core.world.generate.feature.WorldFeatureSugarCane;
import net.minecraft.core.world.generate.feature.WorldFeatureTallGrass;
import net.minecraft.core.world.noise.FractalNoise3D;
import net.minecraft.core.world.noise.ImprovedPerlinNoise;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ChunkDecoratorTwilightForest implements ChunkDecorator {

	private final World world;
	private final Random rand = new Random();

	private final FractalNoise3D<ImprovedPerlinNoise> treeDensityNoise;

	public ChunkDecoratorTwilightForest(@NotNull World world) {
		this.world = world;

		this.treeDensityNoise = new FractalNoise3D<>(
			ImprovedPerlinNoise.genOctaves(world.getRandomSeed(), 8, 66));
	}

	private static boolean loggedFirstChunk = false;

	@Override
	public void decorate(@NotNull Chunk chunk) {
		int chunkX = chunk.pos.x;
		int chunkZ = chunk.pos.z;
		int mapX = chunkX * 16;
		int mapZ = chunkZ * 16;

		int maxY = this.world.getWorldType().getMaxY(this.world);
		int height = maxY + 1;

		Biome biome = this.world.getBiomeProvider().getBiome(mapX + 16, 64, mapZ + 16);

		if (!loggedFirstChunk) {
			loggedFirstChunk = true;
			int surface = this.world.getHeightValue(mapX + 8, mapZ + 8);
			com.twilightforest.TwilightForest.LOGGER.info(
				"Decorating the first Twilight Forest chunk: ({}, {}) as biome '{}', surface y={}, "
					+ "hollow hill size {}.",
				chunkX, chunkZ,
				net.minecraft.core.data.registry.Registries.BIOMES.getKey(biome),
				surface,
				TFHollowHills.nearestHillSize(chunkX, chunkZ, 0L));
		}

		this.rand.setSeed(this.world.getRandomSeed());
		long l1 = this.rand.nextLong() / 2L * 2L + 1L;
		long l2 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed(chunkX * l1 + chunkZ * l2 ^ this.world.getRandomSeed());

		if (this.rand.nextInt(4) == 0) {
			int lx = mapX + this.rand.nextInt(16) + 8;
			int ly = this.rand.nextInt(height);
			int lz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureLake(Blocks.FLUID_WATER_STILL.id()).place(this.world, this.rand, lx, ly, lz);
		}

		if (biome == TFBiomes.SWAMP) {
			for (int i = 0; i < 6; i++) {
				int lx = mapX + this.rand.nextInt(16) + 8;
				int lz = mapZ + this.rand.nextInt(16) + 8;
				int ly = this.world.getHeightValue(lx, lz);
				new WorldFeatureLake(Blocks.FLUID_WATER_STILL.id()).place(this.world, this.rand, lx, ly, lz);
			}
		}

		if (this.rand.nextInt(64) == 0) {
			int lx = mapX + this.rand.nextInt(16) + 8;
			int rawY = this.rand.nextInt(this.rand.nextInt(height - 8) + 8);
			int lz = mapZ + this.rand.nextInt(16) + 8;

			if (rawY < 64 || this.rand.nextInt(10) == 0) {

				int ly = undergroundY(rawY, lx + 4, lz + 4, 8, 4, 3);
				if (ly > 0) {
					new WorldFeatureLake(Blocks.FLUID_LAVA_STILL.id())
						.place(this.world, this.rand, lx, ly, lz);
				}
			}
		}

		if (this.rand.nextInt(4) == 0) {
			int fx = mapX + this.rand.nextInt(16) + 8;
			int fz = mapZ + this.rand.nextInt(16) + 8;
			int fy = this.world.getHeightValue(fx, fz);
			randomFeature(this.rand).place(this.world, this.rand, fx, fy, fz);
		}

		if (this.rand.nextInt(4) == 0) {
			int treeX = mapX + this.rand.nextInt(16) + 8;
			int treeZ = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureTFHollowTree()
				.place(this.world, this.rand, treeX, this.world.getHeightValue(treeX, treeZ), treeZ);
		}

		scatterOre(mapX, mapZ, 20, Blocks.DIRT.id(), 32, height);
		scatterOre(mapX, mapZ, 10, Blocks.GRAVEL.id(), 32, height);
		scatterOre(mapX, mapZ, 20, Blocks.ORE_COAL_STONE.id(), 16, height);
		scatterOre(mapX, mapZ, 20, Blocks.ORE_IRON_STONE.id(), 8, 64);
		scatterOre(mapX, mapZ, 1, Blocks.ORE_LAPIS_STONE.id(), 6, 32);

		int noisy = (int) ((this.treeDensityNoise.getValue(mapX * 0.5, mapZ * 0.5) / 8.0
			+ this.rand.nextDouble() * 4.0 + 4.0) / 3.0);
		int treeFreq = noisy + 20;

		if (biome == TFBiomes.SWAMP) {
			treeFreq -= 18;
		}
		if (biome == TFBiomes.SNOW) {
			treeFreq -= 10;
		}
		if (biome == TFBiomes.HIGHLANDS) {
			treeFreq -= 10;
		}
		if (biome == TFBiomes.GLACIER) {
			treeFreq -= 10;
		}

		if (biome == TFBiomes.CLEARING) {
			treeFreq = 0;
		}

		for (int i = 0; i < treeFreq; i++) {
			int tx = mapX + this.rand.nextInt(16) + 8;
			int tz = mapZ + this.rand.nextInt(16) + 8;
			WorldFeature tree = biome.getTreeFeature(this.rand);
			tree.init(1.0, 1.0, 1.0);
			tree.place(this.world, this.rand, tx, this.world.getHeightValue(tx, tz), tz);
		}

		int yellowChance = 2;
		if (biome == TFBiomes.TWILIGHT_FOREST) {
			yellowChance = 2;
		}
		scatterPlant(mapX, mapZ, yellowChance, Blocks.FLOWER_YELLOW.id(), height);

		int grassFreq = 7;
		if (biome == TFBiomes.CLEARING) {
			grassFreq = 3;
		}
		if (biome == TFBiomes.SWAMP) {
			grassFreq = 1;
		}

		for (int i = 0; i < grassFreq; i++) {

			int grassBlock = Blocks.TALLGRASS.id();
			if ((biome == TFBiomes.TWILIGHT_FOREST
				|| biome == TFBiomes.MUSHROOMS
				|| biome == TFBiomes.SWAMP)
				&& this.rand.nextInt(3) != 0) {
				grassBlock = Blocks.TALLGRASS_FERN.id();
			}

			int gx = mapX + this.rand.nextInt(16) + 8;
			int gy = this.rand.nextInt(height);
			int gz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureTallGrass(grassBlock).place(this.world, this.rand, gx, gy, gz);
		}

		if (this.rand.nextInt(2) == 0) {
			scatterPlant(mapX, mapZ, 1, Blocks.FLOWER_RED.id(), height);
		}
		if (this.rand.nextInt(3) == 0) {
			scatterPlant(mapX, mapZ, 1, Blocks.MUSHROOM_BROWN.id(), 64);
		}
		if (this.rand.nextInt(6) == 0) {
			scatterPlant(mapX, mapZ, 1, Blocks.MUSHROOM_RED.id(), 64);
		}

		if (biome == TFBiomes.MUSHROOMS) {
			scatterPlant(mapX, mapZ, 48, Blocks.MUSHROOM_BROWN.id(), 64);
			scatterPlant(mapX, mapZ, 24, Blocks.MUSHROOM_RED.id(), 64);
		}

		for (int i = 0; i < 3; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			int ry = this.world.getHeightValue(rx, rz);
			new WorldFeatureSugarCane().place(this.world, this.rand, rx, ry, rz);
		}

		if (this.rand.nextInt(32) == 0) {
			int px = mapX + this.rand.nextInt(16) + 8;
			int py = this.rand.nextInt(height);
			int pz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeaturePumpkin().place(this.world, this.rand, px, py, pz);
		}

		for (int i = 0; i < 50; i++) {
			int sx = mapX + this.rand.nextInt(16) + 8;
			int sy = this.rand.nextInt(this.rand.nextInt(height - 8) + 8);
			int sz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureLiquid(Blocks.FLUID_WATER_FLOWING.id()).place(this.world, this.rand, sx, sy, sz);
		}

		for (int i = 0; i < 10; i++) {
			int sx = mapX + this.rand.nextInt(16) + 8;
			int rawY = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(height - 16) + 8) + 8);
			int sz = mapZ + this.rand.nextInt(16) + 8;

			int sy = undergroundY(rawY, sx, sz, 1, 0, 0);
			if (sy > 0) {
				new WorldFeatureLiquid(Blocks.FLUID_LAVA_FLOWING.id())
					.place(this.world, this.rand, sx, sy, sz);
			}
		}

		double[] temperatures =
			this.world.getBiomeProvider().getTemperatures(null, mapX + 8, mapZ + 8, 16, 16);

		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int wx = mapX + 8 + dx;
				int wz = mapZ + 8 + dz;
				int top = this.world.getHeightValue(wx, wz);
				double t = temperatures[dx * 16 + dz] - (top - 64) / 64.0 * 0.3;

				if (t < 0.5
					&& top > 0
					&& top <= maxY
					&& this.world.isAirBlock(wx, top, wz)
					&& this.world.getBlockMaterial(new net.minecraft.core.world.pos.TilePos(wx, top - 1, wz)).isSolid()
					&& this.world.getBlockId(wx, top - 1, wz) != Blocks.ICE.id()) {
					this.world.setBlockWithNotify(wx, top, wz, Blocks.LAYER_SNOW.id());
				}
			}
		}

		int hsize = TFHollowHills.nearestHillSize(chunkX, chunkZ, 0L);
		if (TFHollowHills.isHollowHill(chunkX, chunkZ, 0L) && hsize > 0) {
			new WorldFeatureTFHollowHill(hsize).place(this.world, this.rand, mapX + 8, 17, mapZ + 8);
		}
	}

	private static final int LAVA_FLOOR = 9;

	private static final int LAVA_SURFACE_MARGIN = 4;

	private int undergroundY(int rawY, int x, int z, int footprint, int below, int above) {
		int surface = Integer.MAX_VALUE;
		for (int dx = 0; dx < footprint; dx++) {
			for (int dz = 0; dz < footprint; dz++) {
				surface = Math.min(surface, this.world.getHeightValue(x + dx, z + dz));
			}
		}

		int lowest = LAVA_FLOOR + below;
		int highest = surface - LAVA_SURFACE_MARGIN - above;
		if (highest < lowest) {
			return -1;
		}

		return lowest + (rawY * (highest - lowest + 1)) / 128;
	}

	private TFWorldFeature randomFeature(Random rand) {
		return switch (rand.nextInt(6)) {
			case 1 -> new WorldFeatureTFWell();
			case 2 -> new WorldFeatureTFWitchHut();
			case 3 -> new WorldFeatureTFOutsideStalagmite();
			case 4 -> new WorldFeatureTFFoundation();
			case 5 -> new WorldFeatureTFMonolith();
			default -> new WorldFeatureTFStoneCircle();
		};
	}

	private void scatterOre(int mapX, int mapZ, int count, int blockId, int blobSize, int maxHeight) {
		for (int i = 0; i < count; i++) {
			int ox = mapX + this.rand.nextInt(16);
			int oy = this.rand.nextInt(maxHeight);
			int oz = mapZ + this.rand.nextInt(16);
			new WorldFeatureOre(blockId, blobSize)
				.place(this.world, this.rand, new TilePos(ox, oy, oz));
		}
	}

	private void scatterPlant(int mapX, int mapZ, int count, int blockId, int maxHeight) {
		for (int i = 0; i < count; i++) {
			int px = mapX + this.rand.nextInt(16) + 8;
			int py = this.rand.nextInt(maxHeight);
			int pz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureFlowers(blockId, 64, false).place(this.world, this.rand, px, py, pz);
		}
	}
}
