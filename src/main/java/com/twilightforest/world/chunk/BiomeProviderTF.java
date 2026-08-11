package com.twilightforest.world.chunk;

import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.layer.GenLayer;
import com.twilightforest.world.layer.TFBiomeIds;
import com.twilightforest.world.layer.TFLayers;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.provider.BiomeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BiomeProviderTF extends BiomeProvider {

	private final GenLayer coarseLayer;
	private final GenLayer perBlockLayer;
	private final GenLayer featureLayer;

	public BiomeProviderTF(@NotNull World world) {
		super(world);
		GenLayer[] layers = TFLayers.makeTheWorld(world.getRandomSeed());
		this.coarseLayer = layers[TFLayers.COARSE];
		this.perBlockLayer = layers[TFLayers.PER_BLOCK];
		this.featureLayer = layers[TFLayers.FEATURES];
	}

	public GenLayer getFeatureLayer() {
		return this.featureLayer;
	}

	public GenLayer getCoarseLayer() {
		return this.coarseLayer;
	}

	@Override
	public Biome[] getBiomes(Biome[] biomes, double[] temperatures, double[] humidities,
	                         double[] varieties, int x, int y, int z,
	                         int xSize, int ySize, int zSize) {
		if (biomes == null || biomes.length < xSize * ySize * zSize) {
			biomes = new Biome[xSize * ySize * zSize];
		}
		if (temperatures == null || temperatures.length < xSize * zSize) {
			temperatures = new double[xSize * zSize];
		}
		if (humidities == null || humidities.length < xSize * zSize) {
			humidities = new double[xSize * zSize];
		}
		if (varieties == null || varieties.length < xSize * zSize) {
			varieties = new double[xSize * zSize];
		}
		Arrays.fill(varieties, 0.0);

		int[] ids = this.perBlockLayer.getInts(x, z, xSize, zSize);

		for (int xx = 0; xx < xSize; xx++) {
			for (int zz = 0; zz < zSize; zz++) {

				int layerIndex = xx + zz * xSize;
				int flat = xx * zSize + zz;

				Biome biome = TFBiomes.byLayerId(ids[layerIndex]);
				temperatures[flat] = temperatureOf(ids[layerIndex]);
				humidities[flat] = humidityOf(ids[layerIndex]);

				for (int yy = 0; yy < ySize; yy++) {
					biomes[(xx * zSize + zz) * ySize + yy] = biome;
				}
			}
		}

		return biomes;
	}

	@Override
	public double[] getTemperatures(double[] temperatures, int x, int z, int xSize, int zSize) {
		if (temperatures == null || temperatures.length < xSize * zSize) {
			temperatures = new double[xSize * zSize];
		}
		int[] ids = this.perBlockLayer.getInts(x, z, xSize, zSize);
		for (int xx = 0; xx < xSize; xx++) {
			for (int zz = 0; zz < zSize; zz++) {
				temperatures[xx * zSize + zz] = temperatureOf(ids[xx + zz * xSize]);
			}
		}
		return temperatures;
	}

	@Override
	public double[] getHumidities(double[] humidities, int x, int z, int xSize, int zSize) {
		if (humidities == null || humidities.length < xSize * zSize) {
			humidities = new double[xSize * zSize];
		}
		int[] ids = this.perBlockLayer.getInts(x, z, xSize, zSize);
		for (int xx = 0; xx < xSize; xx++) {
			for (int zz = 0; zz < zSize; zz++) {
				humidities[xx * zSize + zz] = humidityOf(ids[xx + zz * xSize]);
			}
		}
		return humidities;
	}

	@Override
	public double[] getVarieties(double[] varieties, int x, int z, int xSize, int zSize) {
		if (varieties == null || varieties.length < xSize * zSize) {
			varieties = new double[xSize * zSize];
		}
		Arrays.fill(varieties, 0.0);
		return varieties;
	}

	@Override
	public double[] getBiomenesses(double[] biomenesses, int x, int y, int z,
	                               int xSize, int ySize, int zSize) {
		if (biomenesses == null || biomenesses.length < xSize * ySize * zSize) {
			biomenesses = new double[xSize * ySize * zSize];
		}
		Arrays.fill(biomenesses, 1.0);
		return biomenesses;
	}

	@Override
	public Biome lookupBiome(double temperature, double humidity, double altitude, double variety) {
		return TFBiomes.TWILIGHT_FOREST;
	}

	private static double temperatureOf(int id) {
		switch (id) {
			case TFBiomeIds.GLACIER: return 0.0;
			case TFBiomeIds.SNOW: return 0.125;
			case TFBiomeIds.HIGHLANDS: return 0.5;
			case TFBiomeIds.STREAM: return 0.5;
			case TFBiomeIds.LAKE: return 0.66;
			case TFBiomeIds.TWILIGHT_FOREST_VARIANT: return 0.7;
			case TFBiomeIds.SWAMP:
			case TFBiomeIds.CLEARING:
			case TFBiomeIds.DEEP_MUSHROOMS: return 0.8;
			default: return 0.5;
		}
	}

	private static double humidityOf(int id) {
		switch (id) {
			case TFBiomeIds.GLACIER: return 0.1;
			case TFBiomeIds.HIGHLANDS: return 0.3;
			case TFBiomeIds.CLEARING: return 0.4;
			case TFBiomeIds.MUSHROOMS:
			case TFBiomeIds.TWILIGHT_FOREST_VARIANT: return 0.8;
			case TFBiomeIds.SWAMP:
			case TFBiomeIds.SNOW: return 0.9;
			case TFBiomeIds.STREAM:
			case TFBiomeIds.LAKE:
			case TFBiomeIds.DEEP_MUSHROOMS: return 1.0;
			default: return 0.5;
		}
	}
}
