package com.twilightforest.world.type;

import com.twilightforest.TwilightForest;
import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.chunk.BiomeProviderTF;
import com.twilightforest.world.chunk.ChunkGeneratorTF;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.provider.BiomeProvider;
import net.minecraft.core.world.generate.chunk.ChunkGenerator;
import net.minecraft.core.world.type.WorldType;
import net.minecraft.core.world.type.WorldTypes;
import net.minecraft.core.world.type.overworld.WorldTypeOverworld;
import net.minecraft.core.world.config.season.SeasonConfig;
import net.minecraft.core.world.season.Seasons;
import net.minecraft.core.world.weather.Weathers;
import org.jetbrains.annotations.NotNull;

public class WorldTypeTwilightForest extends WorldTypeOverworld {

	@SuppressWarnings({"java:S1104", "java:S1444", "java:S3008"})
	public static WorldType TWILIGHT_FOREST;

	public WorldTypeTwilightForest(WorldType.Properties properties) {
		super(properties);
	}

	public static void register() {
		TWILIGHT_FOREST = WorldTypes.register(
			TwilightForest.MOD_ID + ":twilightforest.default",
			new WorldTypeTwilightForest(
				WorldTypeOverworld.defaultProperties("worldType.twilightforest.default")
					.fillerBlock(Blocks.STONE)
					.defaultWeather(Weathers.OVERWORLD_CLEAR)

					.seasonConfig(SeasonConfig.builder().withSingleSeason(Seasons.NULL).build())
					.bounds(0, 127, 0)
					.portalBounds(0, 127)));

		TwilightForest.LOGGER.info("Registered world type '{}:twilightforest.default'.",
			TwilightForest.MOD_ID);
	}

	@Override
	public float getCelestialAngle(World world, long tick, float partialTick) {
		return 0.25F;
	}

	@Override
	public float getTimeOfDay(World world, long tick, float partialTick) {
		return 0.25F;
	}

	@Override
	public int getOceanY() {
		return 0;
	}

	@Override
	public int[] getOceanBlockIds() {
		return new int[]{Blocks.FLUID_WATER_STILL.id(), Blocks.FLUID_WATER_FLOWING.id()};
	}

	@NotNull
	@Override
	public BiomeProvider createBiomeProvider(World world) {
		return new BiomeProviderTF(world);
	}

	@Override
	public ChunkGenerator createChunkGenerator(World world) {
		TwilightForest.LOGGER.info("Creating Twilight Forest chunk generator for dimension {}.",
			world.dimension);
		return new ChunkGeneratorTF(world);
	}

	@NotNull
	@Override
	public Biome[] allBiomes() {
		return TFBiomes.all();
	}

	@Override
	public boolean isValidSpawn(World world, int x, int y, int z) {
		return world.getBlockId(x, y, z) == Blocks.GRASS.id();
	}
}
