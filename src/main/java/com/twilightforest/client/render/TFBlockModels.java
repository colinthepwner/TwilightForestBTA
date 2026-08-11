package com.twilightforest.client.render;

import com.twilightforest.TwilightForest;
import com.twilightforest.block.TFBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.model.BlockModelAxisAligned;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.BlockModelLeaves;
import net.minecraft.client.render.block.model.BlockModelStandard;
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
	private static final String V_LEAVES_PINE = "minecraft:block/leaves/pine";
	private static final String V_LEAVES_BIRCH = "minecraft:block/leaves/birch";
	private static final String V_STONE = "minecraft:block/stone";
	private static final String V_COBBLE = "minecraft:block/cobbled_stone";
	private static final String V_COBBLE_MOSSY = "minecraft:block/cobbled_stone_mossy";
	private static final String V_MOBSPAWNER = "minecraft:block/mobspawner";

	public static void registerBlockModels(BlockModelDispatcher dispatcher) {

		dispatcher.addDispatch(log(TFBlocks.LOG_TWILIGHT_OAK, LOG_OAK_SIDE));
		dispatcher.addDispatch(log(TFBlocks.LOG_CANOPY, LOG_CANOPY_SIDE));
		dispatcher.addDispatch(log(TFBlocks.LOG_MANGROVE, LOG_MANGROVE_SIDE));

		dispatcher.addDispatch(new BlockModelLeaves<>(TFBlocks.LEAVES_TWILIGHT_OAK, V_LEAVES_OAK));
		dispatcher.addDispatch(new BlockModelLeaves<>(TFBlocks.LEAVES_CANOPY, V_LEAVES_PINE));
		dispatcher.addDispatch(new BlockModelLeaves<>(TFBlocks.LEAVES_MANGROVE, V_LEAVES_BIRCH));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE).withTextures(V_STONE));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE_COBBLE).withTextures(V_COBBLE));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.MAZESTONE_MOSSY).withTextures(V_COBBLE_MOSSY));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.HEDGE)
			.withTextures(bridged(HEDGE, V_LEAVES_OAK)));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.FIREFLY)
			.withTextures(bridged(FIREFLY, V_LEAVES_OAK)));
		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.CICADA)
			.withTextures(bridged(CICADA, V_LEAVES_OAK)));

		dispatcher.addDispatch(new BlockModelStandard<>(TFBlocks.BOSS_SPAWNER).withTextures(V_MOBSPAWNER));

		int available = 0;
		for (String id : BRIDGED_TEXTURES) {
			if (hasTexture(id)) available++;
		}
		TwilightForest.LOGGER.info(
			"Registered block models for 13 Twilight Forest blocks; {} of {} bridged tiles were available.",
			available, BRIDGED_TEXTURES.length);
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
		try {
			return TextureRegistry.hasTexture(id);
		} catch (RuntimeException e) {

			return false;
		}
	}
}
