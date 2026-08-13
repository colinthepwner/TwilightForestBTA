package com.twilightforest.world.biome;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.entity.MobTFKobold;
import net.minecraft.core.entity.SpawnListEntry;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeShrub;

import net.minecraft.core.enums.MobCategory;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TFBiomeDarkForest extends TFBiome {

	private static final int MONSTER_SPAWN_RATE = 20;

	private final Random monsterGate = new Random();

	public TFBiomeDarkForest(String key) {
		super(key);

		this.spawnableMonsterList.add(new SpawnListEntry(MobTFKobold.class, 10));

		this.withPlacementDefaults(0.7F, 0.8F, 0.5F);
	}

	@Override
	public List<SpawnListEntry> getSpawnableList(MobCategory category) {
		if (category == MobCategory.MONSTER) {
			return this.monsterGate.nextInt(MONSTER_SPAWN_RATE) == 0
				? super.getSpawnableList(category)
				: Collections.emptyList();
		}
		return super.getSpawnableList(category);
	}

	@Override
	public WorldFeature getTreeFeature(Random random) {
		if (random.nextInt(5) == 0) {
			return new WorldFeatureTreeShrub(TFBlocks.LEAVES_DARKWOOD.id(), TFBlocks.LOG_DARKWOOD.id());
		}
		if (random.nextInt(8) == 0) {
			return new WorldFeatureTree(Blocks.LEAVES_OAK.id(), Blocks.LOG_OAK.id(), 4);
		}
		return new WorldFeatureTree(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
			TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
	}
}
