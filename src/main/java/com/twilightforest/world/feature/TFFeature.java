package com.twilightforest.world.feature;

import com.twilightforest.entity.MobTFMiniGhast;
import com.twilightforest.entity.MobTFPinchBeetle;
import com.twilightforest.entity.MobTFTowerBroodling;
import com.twilightforest.entity.MobTFTowerGhast;
import com.twilightforest.entity.MobTFTowerGolem;
import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.chunk.BiomeProviderTF;
import net.minecraft.core.entity.Mob;
import net.minecraft.core.entity.SpawnListEntry;
import net.minecraft.core.entity.animal.MobSquid;
import net.minecraft.core.entity.monster.MobCreeper;
import net.minecraft.core.entity.monster.MobSkeleton;
import net.minecraft.core.entity.monster.MobZombieArmored;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.provider.BiomeProvider;

import java.util.List;
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
	public static final int QUEST_ISLAND = 8;
	public static final int QUEST_GROVE = 9;
	public static final int DRUID_GROVE = 10;
	public static final int FLOATING_RUINS = 11;
	public static final int HYDRA_LAIR = 12;
	public static final int LABYRINTH = 13;
	public static final int DARK_TOWER = 14;
	public static final int KNIGHT_STRONGHOLD = 15;

	public static final int UNDERGROUND = 255;

	private static final int MAX_ID = KNIGHT_STRONGHOLD;

	private static final int MAX_RADIUS = 3;

	private static final int[] SIZE = {
		0,
		1,
		2,
		3,
		2,
		3,
		1,
		2,
		1,
		1,
		1,
		3,
		2,
		3,
		1,
		3,
	};

	private static final boolean[] STRUCTURE_ENABLED = {
		false,
		true,
		true,
		true,
		true,
		true,
		true,
		false,
		false,
		true,
		false,
		false,
		true,
		true,
		true,
		false,
	};

	private static final boolean[] CHUNK_DECORATIONS_ENABLED = {
		true,
		true,
		true,
		true,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		false,
		true,
		false,
		false,
	};

	private static final String[] NAME = {
		"No Feature", "Small Hollow Hill", "Medium Hollow Hill", "Large Hollow Hill",
		"Hedge Maze", "Naga Courtyard", "Wizard Tower", "Glacier Maze",
		"Quest Island", "Quest Grove", "Druid Grove", "Floating Ruins",
		"Hydra Lair", "Labyrinth", "Dark Tower", "Knight Stronghold",
	};

	private static final Class<? extends Mob> ENDERMAN_STANDIN = MobZombieArmored.class;

	private static final List<SpawnListEntry> DARK_TOWER_MONSTERS = List.of(
		new SpawnListEntry(MobTFTowerGolem.class, 10),
		new SpawnListEntry(MobSkeleton.class, 10),
		new SpawnListEntry(MobCreeper.class, 10),
		new SpawnListEntry(ENDERMAN_STANDIN, 2),
		new SpawnListEntry(MobTFMiniGhast.class, 10),
		new SpawnListEntry(MobTFTowerBroodling.class, 10),
		new SpawnListEntry(MobTFPinchBeetle.class, 10)
	);

	private static final List<SpawnListEntry> DARK_TOWER_ROOF_MONSTERS = List.of(
		new SpawnListEntry(MobTFTowerGhast.class, 10)
	);

	private static final List<SpawnListEntry> DARK_TOWER_WATER = List.of(
		new SpawnListEntry(MobSquid.class, 10)
	);

	public static List<SpawnListEntry> spawnableMonsters(int type, int listIndex) {
		if (type != DARK_TOWER) {
			return List.of();
		}
		return switch (listIndex) {
			case 0 -> DARK_TOWER_MONSTERS;
			case 1 -> DARK_TOWER_ROOF_MONSTERS;
			default -> List.of();
		};
	}

	public static List<SpawnListEntry> spawnableWaterCreatures(int type) {
		return type == DARK_TOWER ? DARK_TOWER_WATER : List.of();
	}

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

		if (biomeAt == TFBiomes.GLACIER) {
			return GLACIER_FEATURE;
		}
		if (biomeAt == TFBiomes.LAKE) {
			return QUEST_ISLAND;
		}
		if (biomeAt == TFBiomes.ENCHANTED_FOREST) {
			return QUEST_GROVE;
		}
		if (biomeAt == TFBiomes.CLEARING || biomeAt == TFBiomes.CLEARING_BORDER) {
			return LABYRINTH;
		}

		if (biomeAt == TFBiomes.DARK_FOREST && roll % 3 == 1) {
			return DARK_TOWER;
		}

		switch (roll) {
			case 7: case 8: case 9:
				return MEDIUM_HILL;
			case 10:
				return LARGE_HILL;
			case 11: case 12:
				return HEDGE_MAZE;
			case 13:
				return biomeAt != TFBiomes.SWAMP ? NAGA_COURTYARD : HYDRA_LAIR;
			case 14: case 15:
				return LICH_TOWER;
			default:

				return SMALL_HILL;
		}
	}

	public static int featureSize(World world, int cx, int cz) {
		return sizeOf(featureType(world, cx, cz));
	}

	public static int sizeOf(int type) {
		if (type < 0 || type > MAX_ID) {
			return -1;
		}
		return SIZE[type];
	}

	public static boolean isStructureEnabled(int type) {
		return type >= 0 && type <= MAX_ID && STRUCTURE_ENABLED[type];
	}

	public static boolean areChunkDecorationsEnabled(int type) {
		return type < 0 || type > MAX_ID || CHUNK_DECORATIONS_ENABLED[type];
	}

	public static String nameOf(int type) {
		return type >= 0 && type <= MAX_ID ? NAME[type] : NAME[0];
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
