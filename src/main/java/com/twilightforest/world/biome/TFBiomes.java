package com.twilightforest.world.biome;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.entity.MobTFPenguin;
import com.twilightforest.world.layer.TFBiomeIds;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.entity.SpawnListEntry;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.biome.Biomes;
import net.minecraft.core.world.biome.SurfaceProperties;
import net.minecraft.core.world.generate.feature.WorldFeature;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTree;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeFancy;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeShrub;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeTaigaBushy;
import net.minecraft.core.world.generate.feature.tree.WorldFeatureTreeTaigaTall;

import java.util.Random;

public final class TFBiomes {
	private TFBiomes() {}

	@SuppressWarnings({"java:S1104", "java:S1444", "java:S3008"})
	public static Biome LAKE;
	public static Biome TWILIGHT_FOREST;
	public static Biome TWILIGHT_FOREST_VARIANT;
	public static Biome HIGHLANDS;
	public static Biome MUSHROOMS;
	public static Biome SWAMP;
	public static Biome STREAM;
	public static Biome SNOW;
	public static Biome GLACIER;
	public static Biome CLEARING;
	public static Biome CLEARING_BORDER;
	public static Biome LAKE_BORDER;
	public static Biome DEEP_MUSHROOMS;
	public static Biome LARGE_FEATURE;

	private static boolean hasInit = false;

	public static void init() {
		if (hasInit) {
			return;
		}
		hasInit = true;

		LAKE = reg("lake", new Ocean("twilight.lake"), 0x0000FF);
		TWILIGHT_FOREST = reg("forest", new TFBiome("twilight.forest"), 0x007700);
		TWILIGHT_FOREST_VARIANT = reg("forest_variant", new Variant("twilight.forest_variant"), 0x11753E);
		HIGHLANDS = reg("highlands", new Highlands("twilight.highlands"), 0x667766);
		MUSHROOMS = reg("mushrooms", new TFBiome("twilight.mushrooms"), 0x996633);
		SWAMP = reg("swamp", new Swamp("twilight.swamp"), 0x999933);
		STREAM = reg("stream", new TFBiome("twilight.stream"), 0x00A8FF);
		SNOW = reg("snow", new Conifer("twilight.snow"), 0xCCFFFF);
		GLACIER = reg("glacier", new Glacier("twilight.glacier"), 0xEEEEEE);
		CLEARING = reg("clearing", new Clearing("twilight.clearing"), 0xA5F0A5);

		CLEARING_BORDER = reg("clearing_border", new TFBiome("twilight.clearing_border"), 0x349134);
		LAKE_BORDER = reg("lake_border", new TFBiome("twilight.lake_border"), 0x1175A4);
		DEEP_MUSHROOMS = reg("deep_mushrooms", new TFBiome("twilight.deep_mushrooms"), 0x59268F);
		LARGE_FEATURE = reg("large_feature", new Center("twilight.large_feature"), 0xFFF000);
	}

	private static Biome reg(String name, Biome biome, int debugColor) {
		return Biomes.register("twilightforest:" + name, biome.withDebugColor(debugColor));
	}

	public static Biome[] all() {
		return new Biome[]{
			LAKE, TWILIGHT_FOREST, TWILIGHT_FOREST_VARIANT, HIGHLANDS, MUSHROOMS, SWAMP, STREAM,
			SNOW, GLACIER, CLEARING, CLEARING_BORDER, LAKE_BORDER, DEEP_MUSHROOMS, LARGE_FEATURE,
		};
	}

	public static Biome byLayerId(int id) {
		switch (id) {
			case TFBiomeIds.LAKE: return LAKE;
			case TFBiomeIds.TWILIGHT_FOREST: return TWILIGHT_FOREST;
			case TFBiomeIds.TWILIGHT_FOREST_VARIANT: return TWILIGHT_FOREST_VARIANT;
			case TFBiomeIds.HIGHLANDS: return HIGHLANDS;
			case TFBiomeIds.MUSHROOMS: return MUSHROOMS;
			case TFBiomeIds.SWAMP: return SWAMP;
			case TFBiomeIds.STREAM: return STREAM;
			case TFBiomeIds.SNOW: return SNOW;
			case TFBiomeIds.GLACIER: return GLACIER;
			case TFBiomeIds.CLEARING: return CLEARING;
			case TFBiomeIds.CLEARING_BORDER: return CLEARING_BORDER;
			case TFBiomeIds.LAKE_BORDER: return LAKE_BORDER;
			case TFBiomeIds.DEEP_MUSHROOMS: return DEEP_MUSHROOMS;
			case TFBiomeIds.LARGE_FEATURE: return LARGE_FEATURE;
			default: return TWILIGHT_FOREST;
		}
	}

	static class Variant extends TFBiome {
		Variant(String key) { super(key); }

		@Override
		public WorldFeature getTreeFeature(Random random) {
			if (random.nextInt(5) == 0) {
				return new WorldFeatureTreeShrub(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
					TFBlocks.LOG_TWILIGHT_OAK.id());
			}
			if (random.nextInt(10) == 0) {
				return new WorldFeatureTreeFancy(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
					TFBlocks.LOG_TWILIGHT_OAK.id());
			}
			return new WorldFeatureTree(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
				TFBlocks.LOG_TWILIGHT_OAK.id(), 4);
		}
	}

	static class Highlands extends TFBiome {
		Highlands(String key) { super(key); }

		@Override
		public WorldFeature getTreeFeature(Random random) {
			if (random.nextInt(4) == 0) {
				return new WorldFeatureTreeFancy(TFBlocks.LEAVES_TWILIGHT_OAK.id(),
					TFBlocks.LOG_TWILIGHT_OAK.id());
			}
			if (random.nextInt(10) == 0) {
				return new WorldFeatureTreeTaigaBushy(Blocks.LEAVES_PINE.id(), Blocks.LOG_PINE.id());
			}
			return new WorldFeatureTree(Blocks.LEAVES_BIRCH.id(), Blocks.LOG_BIRCH.id(), 5);
		}
	}

	static class Conifer extends TFBiome {
		Conifer(String key) { super(key); }

		@Override
		public WorldFeature getTreeFeature(Random random) {
			return random.nextInt(3) == 0
				? new WorldFeatureTreeTaigaTall(Blocks.LEAVES_PINE.id(), Blocks.LOG_PINE.id())
				: new WorldFeatureTreeTaigaBushy(Blocks.LEAVES_PINE.id(), Blocks.LOG_PINE.id());
		}
	}

	static class Glacier extends Conifer {
		Glacier(String key) {
			super(key);

			this.spawnableCreatureList.add(new SpawnListEntry(MobTFPenguin.class, 10));
		}
	}

	static class Clearing extends TFBiome {
		Clearing(String key) { super(key); }

		@Override
		public WorldFeature getTreeFeature(Random random) {

			return super.getTreeFeature(random);
		}
	}

	static class Swamp extends TFBiome {
		Swamp(String key) { super(key); }

		@Override
		public WorldFeature getTreeFeature(Random random) {
			return random.nextInt(3) == 0
				? new WorldFeatureTreeShrub(TFBlocks.LEAVES_MANGROVE.id(),
					TFBlocks.LOG_MANGROVE.id())
				: new WorldFeatureTree(TFBlocks.LEAVES_MANGROVE.id(),
					TFBlocks.LOG_MANGROVE.id(), 4);
		}
	}

	static class Ocean extends TFBiome {
		Ocean(String key) {
			super(key);
			this.spawnableCreatureList.clear();
		}
	}

	static class Center extends TFBiome {
		Center(String key) {
			super(key);
			this.withSurfaceProperties(new SurfaceProperties.Builder()
				.withTopBlock(Blocks.STONE)
				.withFillerBlock(Blocks.STONE)
				.build());
		}
	}
}
