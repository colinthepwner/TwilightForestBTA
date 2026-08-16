package com.twilightforest.world.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.twilightforest.entity.MobTFTowerGhast;
import net.minecraft.core.entity.SpawnListEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

class TFFeatureSpawnListTest {

	@Test
	void theGroundTableIsTheOrdinaryTowerMobs() {
		List<SpawnListEntry> ground = TFFeature.spawnableMonsters(TFFeature.DARK_TOWER, 0);

		assertEquals(7, ground.size(), "upstream's darkTower list at index 0 has seven entries");
		assertFalse(containsClass(ground, MobTFTowerGhast.class),
			"the tower ghast is index 1 only -- it circles the roofs, it does not spawn in the rooms");
	}

	@Test
	void theRoofTableIsTowerGhastsAndOnlyTowerGhasts() {
		List<SpawnListEntry> roof = TFFeature.spawnableMonsters(TFFeature.DARK_TOWER, 1);

		assertEquals(1, roof.size(), "upstream adds exactly one entry at index 1");
		assertTrue(containsClass(roof, MobTFTowerGhast.class));
	}

	@Test
	void theTreasureRoomIndexResolvesToNothing() {
		assertTrue(TFFeature.spawnableMonsters(TFFeature.DARK_TOWER, Integer.MAX_VALUE).isEmpty());
	}

	@Test
	void theNoSpawningIndexResolvesToNothing() {
		assertTrue(TFFeature.spawnableMonsters(TFFeature.DARK_TOWER, -1).isEmpty());
	}

	@Test
	void otherFeaturesHaveNoStructureSpawnList() {
		int[] others = {
			TFFeature.NOTHING, TFFeature.SMALL_HILL, TFFeature.LARGE_HILL, TFFeature.LICH_TOWER,
			TFFeature.LABYRINTH, TFFeature.HYDRA_LAIR, TFFeature.UNDERGROUND,
		};
		for (int type : others) {
			assertTrue(TFFeature.spawnableMonsters(type, 0).isEmpty(),
				"feature " + type + " should have no structure spawn list yet");
			assertTrue(TFFeature.spawnableWaterCreatures(type).isEmpty(),
				"feature " + type + " should have no water list -- only the dark tower has one");
		}
	}

	@Test
	void onlyTheDarkTowerStocksItsWater() {
		assertEquals(1, TFFeature.spawnableWaterCreatures(TFFeature.DARK_TOWER).size());
	}

	private static boolean containsClass(List<SpawnListEntry> list, Class<?> type) {
		return list.stream().anyMatch(entry -> entry.entityClass == type);
	}
}
