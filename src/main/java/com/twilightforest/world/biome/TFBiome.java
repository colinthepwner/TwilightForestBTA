package com.twilightforest.world.biome;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.entity.MobTFBighorn;
import com.twilightforest.entity.MobTFBoar;
import com.twilightforest.entity.MobTFBunny;
import com.twilightforest.entity.MobTFDeer;
import com.twilightforest.entity.MobTFMobileFirefly;
import com.twilightforest.entity.MobTFRaven;
import com.twilightforest.entity.MobTFSquirrel;
import com.twilightforest.entity.MobTFTinyBird;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.SpawnListEntry;
import net.minecraft.core.entity.animal.MobButterfly;
import net.minecraft.core.entity.animal.MobChicken;
import net.minecraft.core.entity.animal.MobFireflyCluster;
import net.minecraft.core.entity.animal.MobWolf;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.SurfaceProperties;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeFancy;

import java.util.Random;

public class TFBiome extends Biome {

	public TFBiome(String key) {
		super(key);

		this.withSurfaceProperties(new SurfaceProperties.Builder()
			.withTopBlock(Blocks.GRASS)
			.withFillerBlock(Blocks.DIRT)
			.build());

		this.spawnableCreatureList.clear();
		this.spawnableMonsterList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableAmbientCreatureList.clear();

		this.spawnableCreatureList.add(new SpawnListEntry(MobTFBighorn.class, 12));
		this.spawnableCreatureList.add(new SpawnListEntry(MobTFBoar.class, 10));
		this.spawnableCreatureList.add(new SpawnListEntry(MobChicken.class, 10));
		this.spawnableCreatureList.add(new SpawnListEntry(MobTFDeer.class, 15));
		this.spawnableCreatureList.add(new SpawnListEntry(MobWolf.class, 5));

		this.spawnableCreatureList.add(new SpawnListEntry(MobTFTinyBird.class, 15));
		this.spawnableCreatureList.add(new SpawnListEntry(MobTFSquirrel.class, 10));
		this.spawnableCreatureList.add(new SpawnListEntry(MobTFBunny.class, 10));
		this.spawnableCreatureList.add(new SpawnListEntry(MobTFRaven.class, 10));

		this.spawnableAmbientCreatureList.add(new SpawnListEntry(MobTFMobileFirefly.class, 40));

		this.spawnableAmbientCreatureList.add(new SpawnListEntry(MobFireflyCluster.class, 60));
		this.spawnableAmbientCreatureList.add(new SpawnListEntry(MobButterfly.class, 65));
	}

	@Override
	public int getSkyColor(float temperature) {
		return SKY_COLOR;
	}

	private static final int SKY_COLOR = 0x2B2E63;

	@Override
	public WorldFeature getTreeFeature(Random random) {
		if (random.nextInt(5) == 0) {
			return new WorldFeatureTree(Blocks.LEAVES_BIRCH.id(), Blocks.LOG_BIRCH.id(), 5);
		}
		if (random.nextInt(10) == 0) {
			return new WorldFeatureTreeFancy(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
				TFBlocks.LOG_TWILIGHT_OAK.id());
		}
		return new WorldFeatureTree(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
			TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
	}
}
