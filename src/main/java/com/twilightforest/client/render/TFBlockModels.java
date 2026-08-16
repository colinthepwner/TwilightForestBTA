package com.twilightforest.client.render;

import com.twilightforest.TwilightForest;
import com.twilightforest.asset.TFBlockTextureBridge;
import com.twilightforest.block.TFBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.color.BlockColorCustom;
import net.minecraft.client.render.block.color.BlockColorDispatcher;
import net.minecraft.client.render.block.model.BlockModelAxisAligned;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.block.model.BlockModelTransparent;
import net.minecraft.client.render.block.model.generic.BlockModelGeneric;
import net.minecraft.client.render.block.model.generic.BlockModelGenericLeaves;
import net.minecraft.client.render.colorizer.Colorizers;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class TFBlockModels {
	private TFBlockModels() {}

	private static final String LOG_TOP = "twilightforest:block/log_top";
	private static final String LOG_OAK_SIDE = "twilightforest:block/log_twilight_oak_side";
	private static final String LOG_CANOPY_SIDE = "twilightforest:block/log_canopy_side";
	private static final String LOG_MANGROVE_SIDE = "twilightforest:block/log_mangrove_side";
	private static final String FIREFLY = "twilightforest:block/firefly";
	private static final String CICADA = "twilightforest:block/cicada";
	private static final String HEDGE = "twilightforest:block/hedge";

	private static final String LOG_DARKWOOD_SIDE = "twilightforest:block/log_darkwood_side";
	private static final String LEAVES_DARKWOOD = "twilightforest:block/leaves_darkwood";
	private static final String ROOTS = "twilightforest:block/roots";
	private static final String TORCHBERRIES = "twilightforest:block/torchberries";
	private static final String ROOT_STRANDS = "twilightforest:block/root_strands";

	private static final String LOG_TIMEWOOD_SIDE = "twilightforest:block/log_timewood_side";
	private static final String LOG_TIMEWOOD_TOP = "twilightforest:block/log_timewood_top";
	private static final String LOG_TRANSWOOD_SIDE = "twilightforest:block/log_transwood_side";
	private static final String LOG_TRANSWOOD_TOP = "twilightforest:block/log_transwood_top";
	private static final String LOG_MINEWOOD_SIDE = "twilightforest:block/log_minewood_side";
	private static final String LOG_MINEWOOD_TOP = "twilightforest:block/log_minewood_top";
	private static final String LOG_SORTINGWOOD_SIDE = "twilightforest:block/log_sortingwood_side";
	private static final String LOG_SORTINGWOOD_TOP = "twilightforest:block/log_sortingwood_top";

	private static final String LEAVES_TIMEWOOD = "twilightforest:block/leaves_timewood";
	private static final String LEAVES_TRANSFORMATION = "twilightforest:block/leaves_transformation";
	private static final String LEAVES_SORTING = "twilightforest:block/leaves_sorting";

	private static final String SAPLING_TIMEWOOD = "twilightforest:block/sapling_timewood";
	private static final String SAPLING_TRANSFORMATION = "twilightforest:block/sapling_transformation";
	private static final String SAPLING_MINERS = "twilightforest:block/sapling_miners";
	private static final String SAPLING_SORTING = "twilightforest:block/sapling_sorting";

	private static final String M_SAPLING_TIMEWOOD = "twilightforest:block/sapling/timewood";
	private static final String M_SAPLING_TRANSFORMATION = "twilightforest:block/sapling/transformation";
	private static final String M_SAPLING_MINERS = "twilightforest:block/sapling/miners";
	private static final String M_SAPLING_SORTING = "twilightforest:block/sapling/sorting";

	private static final String M_TORCHBERRIES = "twilightforest:block/plant/torchberries";

	private static final String M_ROOT_STRANDS = "twilightforest:block/plant/root_strands";

	private static final String M_SAPLING_OAK = "minecraft:block/sapling/oak";

	private static final String V_LOG_TOP = "minecraft:block/log/oak_top";
	private static final String V_LOG_SIDE = "minecraft:block/log/oak_side";
	private static final String V_LEAVES_OAK = "minecraft:block/leaves/oak";

	private static final String M_LEAVES_OAK = "minecraft:block/leaves/oak";
	private static final String M_LEAVES_PINE = "minecraft:block/leaves/pine";
	private static final String M_LEAVES_BIRCH = "minecraft:block/leaves/birch";

	private static final String M_LEAVES_TIMEWOOD = "twilightforest:block/leaves/timewood";
	private static final String M_LEAVES_TRANSFORMATION = "twilightforest:block/leaves/transformation";
	private static final String M_LEAVES_MINERS = "twilightforest:block/leaves/miners";
	private static final String M_LEAVES_SORTING = "twilightforest:block/leaves/sorting";

	private static final String TOWER_WOOD = "twilightforest:block/tower_wood";
	private static final String TOWER_WOOD_ENCASED = "twilightforest:block/tower_wood_encased";
	private static final String TOWER_WOOD_CRACKED = "twilightforest:block/tower_wood_cracked";
	private static final String TOWER_WOOD_MOSSY = "twilightforest:block/tower_wood_mossy";
	private static final String TOWER_WOOD_INFESTED = "twilightforest:block/tower_wood_infested";

	private static final String V_PLANKS_OAK = "minecraft:block/planks/oak";
	private static final String V_OBSIDIAN = "minecraft:block/obsidian";
	private static final String V_GLASS = "minecraft:block/glass";

	private static final String V_STONE = "minecraft:block/stone";
	private static final String V_COBBLE = "minecraft:block/cobbled_stone";
	private static final String V_COBBLE_MOSSY = "minecraft:block/cobbled_stone_mossy";
	private static final String V_MOBSPAWNER = "minecraft:block/mobspawner";
	private static final String V_TORCH = "minecraft:block/torch_coal";

	private static final String V_PORTAL = "minecraft:block/portal_nether/green";

	public static void registerBlockModels(BlockModelDispatcher dispatcher) {

		dispatcher.addDispatch(log(TFBlocks.LOG_TWILIGHT_OAK, LOG_OAK_SIDE));
		dispatcher.addDispatch(log(TFBlocks.LOG_CANOPY, LOG_CANOPY_SIDE));
		dispatcher.addDispatch(log(TFBlocks.LOG_MANGROVE, LOG_MANGROVE_SIDE));
		dispatcher.addDispatch(log(TFBlocks.LOG_DARKWOOD, LOG_DARKWOOD_SIDE));

		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_TWILIGHT_OAK, M_LEAVES_OAK));
		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_CANOPY, M_LEAVES_PINE));
		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_MANGROVE, M_LEAVES_BIRCH));

		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_RAINBOW, M_LEAVES_OAK));

		dispatcher.addDispatch(magicLog(TFBlocks.LOG_TIMEWOOD, LOG_TIMEWOOD_SIDE, LOG_TIMEWOOD_TOP));
		dispatcher.addDispatch(magicLog(TFBlocks.LOG_TRANSWOOD, LOG_TRANSWOOD_SIDE, LOG_TRANSWOOD_TOP));
		dispatcher.addDispatch(magicLog(TFBlocks.LOG_MINEWOOD, LOG_MINEWOOD_SIDE, LOG_MINEWOOD_TOP));
		dispatcher.addDispatch(magicLog(TFBlocks.LOG_SORTINGWOOD, LOG_SORTINGWOOD_SIDE, LOG_SORTINGWOOD_TOP));

		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_TIMEWOOD,
			bridgedModel(LEAVES_TIMEWOOD, M_LEAVES_TIMEWOOD)));
		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_TRANSFORMATION,
			bridgedModel(LEAVES_TRANSFORMATION, M_LEAVES_TRANSFORMATION)));
		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_MINERS,
			bridgedModel(LEAVES_TIMEWOOD, M_LEAVES_MINERS)));
		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_SORTING,
			bridgedModel(LEAVES_SORTING, M_LEAVES_SORTING)));

		dispatcher.addDispatch(cross(TFBlocks.SAPLING_TIMEWOOD, SAPLING_TIMEWOOD, M_SAPLING_TIMEWOOD));
		dispatcher.addDispatch(cross(TFBlocks.SAPLING_TRANSFORMATION, SAPLING_TRANSFORMATION,
			M_SAPLING_TRANSFORMATION));
		dispatcher.addDispatch(cross(TFBlocks.SAPLING_MINERS, SAPLING_MINERS, M_SAPLING_MINERS));
		dispatcher.addDispatch(cross(TFBlocks.SAPLING_SORTING, SAPLING_SORTING, M_SAPLING_SORTING));

		dispatcher.addDispatch(cross(TFBlocks.TORCHBERRIES, TORCHBERRIES, M_TORCHBERRIES));

		dispatcher.addDispatch(cross(TFBlocks.ROOT_STRANDS, ROOT_STRANDS, M_ROOT_STRANDS));

		dispatcher.addDispatch(new BlockModelTFGiantMushroom<>(TFBlocks.MUSHROOM_GIANT_BROWN,
			"twilightforest:block/mushroom_skin_brown"));
		dispatcher.addDispatch(new BlockModelTFGiantMushroom<>(TFBlocks.MUSHROOM_GIANT_RED,
			"twilightforest:block/mushroom_skin_red"));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE).withTextures(V_STONE));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE_COBBLE).withTextures(V_COBBLE));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE_MOSSY).withTextures(V_COBBLE_MOSSY));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.HEDGE)
			.withTextures(bridged(HEDGE, V_LEAVES_OAK)));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.LEAVES_DARKWOOD)
			.withTextures(bridged(LEAVES_DARKWOOD, V_LEAVES_OAK)));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.ROOTS)
			.withTextures(bridged(ROOTS, V_LOG_SIDE)));

		dispatcher.addDispatch(new BlockModelTFCritter<>(TFBlocks.FIREFLY)
			.withTextures(bridged(FIREFLY, V_TORCH)));
		dispatcher.addDispatch(new BlockModelTFCritter<>(TFBlocks.CICADA)
			.withTextures(bridged(CICADA, V_TORCH)));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.BOSS_SPAWNER).withTextures(V_MOBSPAWNER));

		dispatcher.addDispatch(new BlockModelTransparent<>(TFBlocks.PORTAL_TWILIGHT, true)
			.withTextures(V_PORTAL));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.TOWER_WOOD)
			.withTextures(bridged(TOWER_WOOD, V_PLANKS_OAK)));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.TOWER_WOOD_ENCASED)
			.withTextures(bridged(TOWER_WOOD_ENCASED, V_PLANKS_OAK)));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.TOWER_WOOD_CRACKED)
			.withTextures(bridged(TOWER_WOOD_CRACKED, V_PLANKS_OAK)));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.TOWER_WOOD_MOSSY)
			.withTextures(bridged(TOWER_WOOD_MOSSY, V_PLANKS_OAK)));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.TOWER_WOOD_INFESTED)
			.withTextures(bridged(TOWER_WOOD_INFESTED, V_PLANKS_OAK)));

		dispatcher.addDispatch(new BlockModelTFTowerDevice<>(TFBlocks.TOWER_DEVICE, V_OBSIDIAN));
		dispatcher.addDispatch(
			new BlockModelTFTowerTranslucent<>(TFBlocks.TOWER_TRANSLUCENT, V_GLASS));

		int available = 0;
		for (String id : BRIDGED_TEXTURES) {
			if (hasTexture(id)) available++;
		}
		TwilightForest.LOGGER.info(
			"Registered block models for 40 Twilight Forest blocks; {} of {} bridged tiles were available.",
			available, BRIDGED_TEXTURES.length);
	}

	public static void registerBlockColors(BlockColorDispatcher dispatcher) {
		dispatcher.addDispatch(TFBlocks.LEAVES_TWILIGHT_OAK, new BlockColorCustom(Colorizers.oak));
		dispatcher.addDispatch(TFBlocks.LEAVES_CANOPY, new BlockColorCustom(Colorizers.pine));
		dispatcher.addDispatch(TFBlocks.LEAVES_MANGROVE, new BlockColorCustom(Colorizers.birch));

		dispatcher.addDispatch(TFBlocks.LEAVES_RAINBOW, new BlockColorTFRainbow());

		dispatcher.addDispatch(TFBlocks.LEAVES_TIMEWOOD, BlockColorTFMagicLeaves.timewood());
		dispatcher.addDispatch(TFBlocks.LEAVES_TRANSFORMATION, BlockColorTFMagicLeaves.transformation());
		dispatcher.addDispatch(TFBlocks.LEAVES_MINERS, BlockColorTFMagicLeaves.miners());
		dispatcher.addDispatch(TFBlocks.LEAVES_SORTING, BlockColorTFMagicLeaves.sorting());

		BlockColorTFTowerWood towerWoodShade = new BlockColorTFTowerWood();
		dispatcher.addDispatch(TFBlocks.TOWER_WOOD, towerWoodShade);
		dispatcher.addDispatch(TFBlocks.TOWER_WOOD_CRACKED, towerWoodShade);
		dispatcher.addDispatch(TFBlocks.TOWER_WOOD_MOSSY, towerWoodShade);
		dispatcher.addDispatch(TFBlocks.TOWER_WOOD_INFESTED, towerWoodShade);
	}

	private static final String[] BRIDGED_TEXTURES = Stream.of(
			Stream.of(LOG_TOP, LOG_OAK_SIDE, LOG_CANOPY_SIDE, LOG_MANGROVE_SIDE, FIREFLY, CICADA, HEDGE,
				LOG_DARKWOOD_SIDE, LEAVES_DARKWOOD, ROOTS, TORCHBERRIES, ROOT_STRANDS,
				LOG_TIMEWOOD_SIDE, LOG_TIMEWOOD_TOP, LOG_TRANSWOOD_SIDE, LOG_TRANSWOOD_TOP,
				LOG_MINEWOOD_SIDE, LOG_MINEWOOD_TOP, LOG_SORTINGWOOD_SIDE, LOG_SORTINGWOOD_TOP,
				LEAVES_TIMEWOOD, LEAVES_TRANSFORMATION, LEAVES_SORTING,
				SAPLING_TIMEWOOD, SAPLING_TRANSFORMATION, SAPLING_MINERS, SAPLING_SORTING,
				TOWER_WOOD, TOWER_WOOD_ENCASED, TOWER_WOOD_CRACKED, TOWER_WOOD_MOSSY,
				TOWER_WOOD_INFESTED),
			Stream.of(BlockModelTFTowerDevice.BRIDGED),
			Stream.of(BlockModelTFTowerTranslucent.BRIDGED))
		.flatMap(s -> s)
		.toArray(String[]::new);

	private static BlockModelStandard<?> log(Block<?> block, String side) {
		String top = bridged(LOG_TOP, V_LOG_TOP);
		return new BlockModelAxisAligned<>(block).withTextures(top, top, bridged(side, V_LOG_SIDE));
	}

	private static BlockModelStandard<?> magicLog(Block<?> block, String side, String top) {
		return new BlockModelAxisAligned<>(block)
			.withTextures(bridged(top, V_LOG_TOP), bridged(top, V_LOG_TOP), bridged(side, V_LOG_SIDE));
	}

	private static String bridgedModel(String textureId, String model) {
		return hasTexture(textureId) ? model : M_LEAVES_OAK;
	}

	private static BlockModelGeneric<?> cross(Block<?> block, String textureId, String model) {
		String chosen = hasTexture(textureId) ? model : M_SAPLING_OAK;
		return new BlockModelGeneric<>(block, BlockModelDispatcher.loadDataModel(chosen))
			.render3D(false);
	}

	private static String bridged(String id, String fallback) {
		return hasTexture(id) ? id : fallback;
	}

	static boolean hasTexture(String id) {
		TFBlockTextureBridge.ensureScanned();
		if (TFBlockTextureBridge.writtenTextureIds.contains(id)) {
			return true;
		}
		try {
			return TextureRegistry.hasTexture(id);
		} catch (RuntimeException e) {

			return false;
		}
	}
}
