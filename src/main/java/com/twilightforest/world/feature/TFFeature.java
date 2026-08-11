package com.twilightforest.world.feature;

import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.chunk.BiomeProviderTF;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.provider.BiomeProvider;

import java.util.Random;

public final class TFFeature {
	private TFFeature() {}

	public static final int NOTHING = -1;

	public static final int SMALL_HILL = 1;
	public static final int MEDIUM_HILL = 2;
	public static final int LARGE_HILL = 3;
	public static final int HEDGE_MAZE = 4;
	public static final int NAGA_COURTYARD = 5;
	public static final int LICH_TOWER = 6;
	public static final int GLACIER_FEATURE = 7;

	private static final int MAX_RADIUS = 3;

	public static boolean isInFeatureChunk(World world, int mapX, int mapZ) {
		BiomeProvider provider = world.getBiomeProvider();
		if (!(provider instanceof BiomeProviderTF)) {
			return false;
		}

		Biome[] biomes = provider.getBiomes(null, mapX, 64, mapZ, 16, 1, 16);
		for (Biome biome : biomes) {
			if (biome == TFBiomes.LARGE_FEATURE) {
				return true;
			}
		}
		return false;
	}

	public static int featureType(World world, int cx, int cz) {
		if (!isInFeatureChunk(world, cx * 16, cz * 16)) {
			return NOTHING;
		}

		Biome biomeAt = world.getBiomeProvider().getBiome(cx * 16 + 8, 64, cz * 16 + 8);
		Random hillRNG = new Random(world.getRandomSeed() + cx * 25117L + cz * 151121L);
		int roll = hillRNG.nextInt(16);

		if (biomeAt == TFBiomes.GLACIER && roll % 2 == 0) {
			return GLACIER_FEATURE;
		}

		switch (roll) {
			case 1: case 2: case 3: case 4: case 5: case 6:
				return SMALL_HILL;
			case 7: case 8: case 9:
				return MEDIUM_HILL;
			case 10:
				return LARGE_HILL;
			case 11: case 12:
				return biomeAt != TFBiomes.GLACIER ? HEDGE_MAZE : NOTHING;
			case 13:
				return biomeAt != TFBiomes.GLACIER && biomeAt != TFBiomes.LAKE
					? NAGA_COURTYARD : NOTHING;
			case 14: case 15:
				return LICH_TOWER;
			default:

				return NOTHING;
		}
	}

	public static int featureSize(World world, int cx, int cz) {
		int type = featureType(world, cx, cz);
		switch (type) {
			case HEDGE_MAZE: return 2;
			case NAGA_COURTYARD: return 3;
			case LICH_TOWER: return 1;
			case GLACIER_FEATURE: return 1;
			case 8: return 2;
			case 9: return 3;
			default:
				return type == SMALL_HILL || type == MEDIUM_HILL || type == LARGE_HILL ? type : -1;
		}
	}

	public static boolean isHollowHill(World world, int cx, int cz) {
		int type = featureType(world, cx, cz);
		return type > 0 && type < 4;
	}

	public static boolean nearChunkFeature(World world, int cx, int cz) {
		for (int rad = 1; rad <= MAX_RADIUS; rad++) {
			for (int x = -rad; x <= rad; x++) {
				for (int z = -rad; z <= rad; z++) {
					if (featureSize(world, x + cx, z + cz) == rad) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static int[] nearestFeatureCenter(World world, int cx, int cz) {
		for (int rad = 1; rad <= MAX_RADIUS; rad++) {
			for (int x = -rad; x <= rad; x++) {
				for (int z = -rad; z <= rad; z++) {
					if (featureSize(world, x + cx, z + cz) == rad) {
						return new int[]{x * 16 + 8, z * 16 + 8};
					}
				}
			}
		}
		return new int[]{0, 0};
	}

	public static int nearestFeatureSize(World world, int cx, int cz) {
		for (int rad = 1; rad <= MAX_RADIUS; rad++) {
			for (int x = -rad; x <= rad; x++) {
				for (int z = -rad; z <= rad; z++) {
					if (featureSize(world, x + cx, z + cz) == rad) {
						return rad;
					}
				}
			}
		}
		return -1;
	}

	public static int nearestFeatureType(World world, int cx, int cz) {
		for (int rad = 1; rad <= MAX_RADIUS; rad++) {
			for (int x = -rad; x <= rad; x++) {
				for (int z = -rad; z <= rad; z++) {
					if (featureSize(world, x + cx, z + cz) == rad) {
						return featureType(world, x + cx, z + cz);
					}
				}
			}
		}
		return -1;
	}
}
