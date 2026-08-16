package com.twilightforest.mixin;

import com.twilightforest.world.feature.TFFeature;
import net.minecraft.core.entity.SpawnListEntry;
import net.minecraft.core.enums.MobCategory;
import net.minecraft.core.world.World;
import net.minecraft.core.world.biome.Biome;

import java.lang.ref.WeakReference;
import java.util.List;

final class StructureSpawnLists {

	private StructureSpawnLists() {}

	static final int INTERIOR_LIST_INDEX = 0;

	private static final int SLOTS = 4096;

	private static final long[] KEYS = new long[SLOTS];
	private static final int[] VALUES = new int[SLOTS];
	private static final boolean[] FILLED = new boolean[SLOTS];

	private static WeakReference<World> cachedWorld = new WeakReference<>(null);

	static Biome biomeFor(World world, Biome actual, int blockX, int blockZ) {
		if (actual == null) {

			return null;
		}

		int type = coveringFeature(world, blockX >> 4, blockZ >> 4);
		if (type == TFFeature.NOTHING) {
			return actual;
		}

		boolean hasMonsters = !TFFeature.spawnableMonsters(type, INTERIOR_LIST_INDEX).isEmpty();
		boolean hasWater = !TFFeature.spawnableWaterCreatures(type).isEmpty();
		if (!hasMonsters && !hasWater) {
			return actual;
		}

		return new StructureBiome(type, actual);
	}

	private static int coveringFeature(World world, int chunkX, int chunkZ) {
		if (cachedWorld.get() != world) {
			java.util.Arrays.fill(FILLED, false);
			cachedWorld = new WeakReference<>(world);
		}

		long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
		int slot = slotFor(key);

		if (FILLED[slot] && KEYS[slot] == key) {
			return VALUES[slot];
		}

		int type = TFFeature.nearestFeatureType(world, chunkX, chunkZ);
		KEYS[slot] = key;
		VALUES[slot] = type;
		FILLED[slot] = true;
		return type;
	}

	private static int slotFor(long key) {
		long h = key * 0x9E3779B97F4A7C15L;
		return (int) ((h >>> 48) & (SLOTS - 1));
	}

	private static final class StructureBiome extends Biome {

		private final int featureType;
		private final Biome actual;

		StructureBiome(int featureType, Biome actual) {
			super("twilightforest.structure");
			this.featureType = featureType;
			this.actual = actual;
		}

		@Override
		public List<SpawnListEntry> getSpawnableList(MobCategory category) {
			if (category == MobCategory.MONSTER) {
				List<SpawnListEntry> monsters =
					TFFeature.spawnableMonsters(this.featureType, INTERIOR_LIST_INDEX);
				if (!monsters.isEmpty()) {
					return monsters;
				}
			} else if (category == MobCategory.WATER_CREATURE) {
				List<SpawnListEntry> water = TFFeature.spawnableWaterCreatures(this.featureType);
				if (!water.isEmpty()) {
					return water;
				}
			}
			return this.actual.getSpawnableList(category);
		}
	}
}
