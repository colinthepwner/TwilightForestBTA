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
import net.minecraft.client.render.block.model.generic.BlockModelGenericLeaves;
import net.minecraft.client.render.colorizer.Colorizers;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.core.block.Block;

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

	private static final String V_LOG_TOP = "minecraft:block/log/oak_top";
	private static final String V_LOG_SIDE = "minecraft:block/log/oak_side";
	private static final String V_LEAVES_OAK = "minecraft:block/leaves/oak";

	private static final String M_LEAVES_OAK = "minecraft:block/leaves/oak";
	private static final String M_LEAVES_PINE = "minecraft:block/leaves/pine";
	private static final String M_LEAVES_BIRCH = "minecraft:block/leaves/birch";
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

		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_TWILIGHT_OAK, M_LEAVES_OAK));
		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_CANOPY, M_LEAVES_PINE));
		dispatcher.addDispatch(new BlockModelGenericLeaves<>(TFBlocks.LEAVES_MANGROVE, M_LEAVES_BIRCH));

		dispatcher.addDispatch(new BlockModelTFGiantMushroom<>(TFBlocks.MUSHROOM_GIANT_BROWN,
			"twilightforest:block/mushroom_skin_brown"));
		dispatcher.addDispatch(new BlockModelTFGiantMushroom<>(TFBlocks.MUSHROOM_GIANT_RED,
			"twilightforest:block/mushroom_skin_red"));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE).withTextures(V_STONE));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE_COBBLE).withTextures(V_COBBLE));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE_MOSSY).withTextures(V_COBBLE_MOSSY));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.HEDGE)
			.withTextures(bridged(HEDGE, V_LEAVES_OAK)));

		dispatcher.addDispatch(new BlockModelTFCritter<>(TFBlocks.FIREFLY)
			.withTextures(bridged(FIREFLY, V_TORCH)));
		dispatcher.addDispatch(new BlockModelTFCritter<>(TFBlocks.CICADA)
			.withTextures(bridged(CICADA, V_TORCH)));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.BOSS_SPAWNER).withTextures(V_MOBSPAWNER));

		dispatcher.addDispatch(new BlockModelTransparent<>(TFBlocks.PORTAL_TWILIGHT, true)
			.withTextures(V_PORTAL));

		int available = 0;
		for (String id : BRIDGED_TEXTURES) {
			if (hasTexture(id)) available++;
		}
		TwilightForest.LOGGER.info(
			"Registered block models for 14 Twilight Forest blocks; {} of {} bridged tiles were available.",
			available, BRIDGED_TEXTURES.length);
	}

	public static void registerBlockColors(BlockColorDispatcher dispatcher) {
		dispatcher.addDispatch(TFBlocks.LEAVES_TWILIGHT_OAK, new BlockColorCustom(Colorizers.oak));
		dispatcher.addDispatch(TFBlocks.LEAVES_CANOPY, new BlockColorCustom(Colorizers.pine));
		dispatcher.addDispatch(TFBlocks.LEAVES_MANGROVE, new BlockColorCustom(Colorizers.birch));
	}

	private static final String[] BRIDGED_TEXTURES = {
		LOG_TOP, LOG_OAK_SIDE, LOG_CANOPY_SIDE, LOG_MANGROVE_SIDE, FIREFLY, CICADA, HEDGE,
	};

	private static BlockModelStandard<?> log(Block<?> block, String side) {
		String top = bridged(LOG_TOP, V_LOG_TOP);
		return new BlockModelAxisAligned<>(block).withTextures(top, top, bridged(side, V_LOG_SIDE));
	}

	private static String bridged(String id, String fallback) {
		return hasTexture(id) ? id : fallback;
	}

	private static boolean hasTexture(String id) {
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
