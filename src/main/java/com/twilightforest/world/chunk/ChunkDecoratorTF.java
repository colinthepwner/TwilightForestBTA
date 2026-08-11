package com.twilightforest.world.chunk;

import com.twilightforest.compat.TFWorldFeature;
import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.feature.TFFeature;
import com.twilightforest.world.feature.WorldFeatureTFCanopyTree;
import com.twilightforest.world.feature.WorldFeatureTFFoundation;
import com.twilightforest.world.feature.WorldFeatureTFHollowTree;
import com.twilightforest.world.feature.WorldFeatureTFMangroveTree;
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
import net.minecraft.core.world.generate.feature.WorldFeatureSugarCane;
import net.minecraft.core.world.generate.feature.WorldFeatureTallGrass;
import net.minecraft.core.world.pos.TilePos;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ChunkDecoratorTF implements ChunkDecorator {

	private final World world;
	private final Random rand = new Random();

	private static boolean loggedFirstChunk = false;

	public ChunkDecoratorTF(@NotNull World world) {
		this.world = world;
	}

	private static final class Counts {
		static final int CANOPY_OFF = -999;

		final int canopy;
		final float canopyMushroomChance;
		final int mycelium;
		final int mangroves;
		final int lakes;

		Counts(int canopy, float canopyMushroomChance, int mycelium, int mangroves, int lakes) {
			this.canopy = canopy;
			this.canopyMushroomChance = canopyMushroomChance;
			this.mycelium = mycelium;
			this.mangroves = mangroves;
			this.lakes = lakes;
		}

		static Counts of(Biome biome) {
			if (biome == TFBiomes.HIGHLANDS || biome == TFBiomes.SNOW
				|| biome == TFBiomes.GLACIER || biome == TFBiomes.CLEARING) {
				return new Counts(CANOPY_OFF, 0.0F, 0, 0, 0);
			}
			if (biome == TFBiomes.SWAMP) {

				return new Counts(CANOPY_OFF, 0.0F, 0, 3, 2);
			}
			if (biome == TFBiomes.DEEP_MUSHROOMS) {

				return new Counts(1, 0.9F, 3, 0, 0);
			}
			if (biome == TFBiomes.MUSHROOMS) {
				return new Counts(1, 0.2F, 0, 0, 0);
			}
			if (biome == TFBiomes.TWILIGHT_FOREST_VARIANT) {

				return new Counts(3, 0.0F, 0, 0, 0);
			}
			return new Counts(1, 0.0F, 0, 0, 0);
		}
	}

	@Override
	public void decorate(@NotNull Chunk chunk) {
		int chunkX = chunk.pos.x;
		int chunkZ = chunk.pos.z;
		int mapX = chunkX * 16;
		int mapZ = chunkZ * 16;

		Biome biome = this.world.getBiomeProvider().getBiome(mapX + 16, 32, mapZ + 16);

		this.rand.setSeed(this.world.getRandomSeed());
		long l1 = this.rand.nextLong() / 2L * 2L + 1L;
		long l2 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed(chunkX * l1 + chunkZ * l2 ^ this.world.getRandomSeed());

		int nearType = TFFeature.nearestFeatureType(this.world, chunkX, chunkZ);

		if (!loggedFirstChunk) {
			loggedFirstChunk = true;
			com.twilightforest.TwilightForest.LOGGER.info(
				"Decorating the first Twilight Forest 1.7.1 chunk: ({}, {}) as biome '{}', "
					+ "surface y={}, nearest feature type {}.",
				chunkX, chunkZ,
				net.minecraft.core.data.registry.Registries.BIOMES.getKey(biome),
				this.world.getHeightValue(mapX + 8, mapZ + 8), nearType);
		}

		boolean insideStructure = nearType > 3;

		if (!insideStructure && this.rand.nextInt(4) == 0) {
			int lx = mapX + this.rand.nextInt(16) + 8;
			int ly = this.rand.nextInt(TFWorldConstants.WORLD_HEIGHT);
			int lz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureLake(Blocks.FLUID_WATER_STILL.id())
				.place(this.world, this.rand, lx, ly, lz);
		}

		if (!insideStructure && this.rand.nextInt(32) == 0) {
			int lx = mapX + this.rand.nextInt(16) + 8;
			int rawY = this.rand.nextInt(
				this.rand.nextInt(TFWorldConstants.WORLD_HEIGHT - 8) + 8);
			int lz = mapZ + this.rand.nextInt(16) + 8;

			if (rawY < TFWorldConstants.SEA_LEVEL || this.rand.nextInt(10) == 0) {
				new WorldFeatureLake(Blocks.FLUID_LAVA_STILL.id())
					.place(this.world, this.rand, lx, rawY, lz);
			}
		}

		if (nearType == TFFeature.HEDGE_MAZE || nearType == TFFeature.NAGA_COURTYARD
			|| nearType == TFFeature.LICH_TOWER || nearType == 9) {
			return;
		}

		Counts counts = Counts.of(biome);

		if (this.rand.nextInt(24) == 0) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureTFHollowTree()
				.place(this.world, this.rand, rx, this.world.getHeightValue(rx, rz), rz);
		}

		if (this.rand.nextInt(6) == 0) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			randomFeature(this.rand)
				.place(this.world, this.rand, rx, this.world.getHeightValue(rx, rz), rz);
		}

		int canopyCount = counts.canopy + this.rand.nextInt(2);
		for (int i = 0; i < canopyCount; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			int ry = this.world.getHeightValue(rx, rz);

			boolean wantsMushroom = counts.canopyMushroomChance > 0.0F
				&& this.rand.nextFloat() <= counts.canopyMushroomChance;

			new WorldFeatureTFCanopyTree().place(this.world, this.rand, rx, ry, rz);
		}

		for (int i = 0; i < counts.lakes; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureLake(Blocks.FLUID_WATER_STILL.id())
				.place(this.world, this.rand, rx, this.world.getHeightValue(rx, rz), rz);
		}

		for (int i = 0; i < counts.mycelium; i++) {
			this.rand.nextInt(16);
			this.rand.nextInt(16);
		}

		for (int i = 0; i < counts.mangroves; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureTFMangroveTree()
				.place(this.world, this.rand, rx, this.world.getHeightValue(rx, rz), rz);
		}

		com.twilightforest.world.structure.TFStructures.generate(this.world, this.rand, chunkX, chunkZ);

		vanillaPass(biome, mapX, mapZ);
	}

	private void vanillaPass(Biome biome, int mapX, int mapZ) {
		VanillaCounts v = VanillaCounts.of(biome);
		int height = TFWorldConstants.WORLD_HEIGHT;

		scatterOre(mapX, mapZ, 20, Blocks.DIRT.id(), 32, height);
		scatterOre(mapX, mapZ, 10, Blocks.GRAVEL.id(), 32, height);
		scatterOre(mapX, mapZ, 20, Blocks.ORE_COAL_STONE.id(), 16, height);
		scatterOre(mapX, mapZ, 20, Blocks.ORE_IRON_STONE.id(), 8, 64);
		scatterOre(mapX, mapZ, 2, Blocks.ORE_GOLD_STONE.id(), 8, 32);
		scatterOre(mapX, mapZ, 8, Blocks.ORE_REDSTONE_STONE.id(), 7, 16);
		scatterOre(mapX, mapZ, 1, Blocks.ORE_DIAMOND_STONE.id(), 7, 16);
		scatterOre(mapX, mapZ, 1, Blocks.ORE_LAPIS_STONE.id(), 6, 32);

		for (int i = 0; i < v.trees; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			WorldFeature tree = biome.getTreeFeature(this.rand);
			tree.init(1.0, 1.0, 1.0);
			tree.place(this.world, this.rand, rx, this.world.getHeightValue(rx, rz), rz);
		}

		for (int i = 0; i < v.flowers; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int ry = this.rand.nextInt(height);
			int rz = mapZ + this.rand.nextInt(16) + 8;
			int flower = this.rand.nextInt(3) == 0 ? Blocks.FLOWER_RED.id() : Blocks.FLOWER_YELLOW.id();
			new WorldFeatureFlowers(flower, 64, false).place(this.world, this.rand, rx, ry, rz);
		}

		for (int i = 0; i < v.grass; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int ry = this.rand.nextInt(height);
			int rz = mapZ + this.rand.nextInt(16) + 8;
			int grass = this.rand.nextInt(4) == 0
				? Blocks.TALLGRASS_FERN.id() : Blocks.TALLGRASS.id();
			new WorldFeatureTallGrass(grass).place(this.world, this.rand, rx, ry, rz);
		}

		for (int i = 0; i < v.deadBush; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int ry = this.rand.nextInt(height);
			int rz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureFlowers(Blocks.DEADBUSH.id(), 64, false)
				.place(this.world, this.rand, rx, ry, rz);
		}

		for (int i = 0; i < v.mushrooms; i++) {
			if (this.rand.nextInt(4) == 0) {
				int rx = mapX + this.rand.nextInt(16) + 8;
				int rz = mapZ + this.rand.nextInt(16) + 8;
				new WorldFeatureFlowers(Blocks.MUSHROOM_BROWN.id(), 64, false)
					.place(this.world, this.rand, rx, this.world.getHeightValue(rx, rz), rz);
			}
			if (this.rand.nextInt(8) == 0) {
				int rx = mapX + this.rand.nextInt(16) + 8;
				int ry = this.rand.nextInt(height);
				int rz = mapZ + this.rand.nextInt(16) + 8;
				new WorldFeatureFlowers(Blocks.MUSHROOM_RED.id(), 64, false)
					.place(this.world, this.rand, rx, ry, rz);
			}
		}

		for (int i = 0; i < v.reeds; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int rz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureSugarCane()
				.place(this.world, this.rand, rx, this.rand.nextInt(height), rz);
		}

		for (int i = 0; i < 50; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int ry = this.rand.nextInt(this.rand.nextInt(height - 8) + 8);
			int rz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureLiquid(Blocks.FLUID_WATER_FLOWING.id())
				.place(this.world, this.rand, rx, ry, rz);
		}
		for (int i = 0; i < 20; i++) {
			int rx = mapX + this.rand.nextInt(16) + 8;
			int ry = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(height - 16) + 8) + 8);
			int rz = mapZ + this.rand.nextInt(16) + 8;
			new WorldFeatureLiquid(Blocks.FLUID_LAVA_FLOWING.id())
				.place(this.world, this.rand, rx, ry, rz);
		}
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

	private static final class VanillaCounts {
		final int trees;
		final int flowers;
		final int grass;
		final int deadBush;
		final int mushrooms;
		final int reeds;

		VanillaCounts(int trees, int flowers, int grass, int deadBush, int mushrooms, int reeds) {
			this.trees = trees;
			this.flowers = flowers;
			this.grass = grass;
			this.deadBush = deadBush;
			this.mushrooms = mushrooms;
			this.reeds = reeds;
		}

		static VanillaCounts of(Biome biome) {

			if (biome == TFBiomes.TWILIGHT_FOREST_VARIANT) return new VanillaCounts(25, 8, 15, 0, 0, 0);
			if (biome == TFBiomes.CLEARING) return new VanillaCounts(Counts.CANOPY_OFF, 4, 10, 0, 0, 0);
			if (biome == TFBiomes.SNOW) return new VanillaCounts(7, 2, 1, 0, 0, 0);
			if (biome == TFBiomes.GLACIER) return new VanillaCounts(1, 2, 0, 0, 0, 0);
			if (biome == TFBiomes.MUSHROOMS) return new VanillaCounts(8, 2, 2, 0, 8, 0);
			if (biome == TFBiomes.DEEP_MUSHROOMS) return new VanillaCounts(1, 2, 2, 0, 12, 0);
			if (biome == TFBiomes.SWAMP) return new VanillaCounts(2, 2, 2, 1, 8, 10);

			return new VanillaCounts(10, 2, 2, 0, 0, 0);
		}
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
}
