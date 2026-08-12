package com.twilightforest;

import com.twilightforest.asset.TFAssetBridge;
import com.twilightforest.block.entity.TileEntityTFCicada;
import com.twilightforest.block.entity.TileEntityTFFirefly;
import com.twilightforest.client.render.TFBlockModels;
import com.twilightforest.client.render.TFEntityRenderers;
import com.twilightforest.client.render.TFItemModels;
import com.twilightforest.client.render.TileEntityRendererTFCicada;
import com.twilightforest.client.render.TileEntityRendererTFFirefly;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.TileEntityRenderDispatcher;
import net.minecraft.client.sound.SoundRepository;
import turniplabs.halplibe.event.defs.ClientEvents;
import turniplabs.halplibe.util.dependency.Key;

public class TwilightForestClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientEvents.AFTER_CLIENT_START.listen(Key.of(TwilightForest.MOD_ID), this::afterClientStart);
		ClientEvents.ENTITY_RENDERER_RELOAD.listen(Key.of(TwilightForest.MOD_ID),
			TFEntityRenderers::registerRenderers);

		ClientEvents.BLOCK_MODEL_RELOAD.listen(Key.of(TwilightForest.MOD_ID),
			TFBlockModels::registerBlockModels);

		ClientEvents.ITEM_MODEL_RELOAD.listen(Key.of(TwilightForest.MOD_ID),
			TFItemModels::registerItemModels);

		ClientEvents.TILE_ENTITY_RENDERER_RELOAD.listen(Key.of(TwilightForest.MOD_ID),
			TwilightForestClient::registerTileEntityRenderers);

		ClientEvents.BLOCK_COLOR_RELOAD.listen(Key.of(TwilightForest.MOD_ID),
			TFBlockModels::registerBlockColors);
	}

	private static void registerTileEntityRenderers(TileEntityRenderDispatcher dispatcher) {
		dispatcher.assignRenderer(TileEntityTFFirefly.class, new TileEntityRendererTFFirefly());
		dispatcher.assignRenderer(TileEntityTFCicada.class, new TileEntityRendererTFCicada());
	}

	private void afterClientStart() {

		TFAssetBridge.run();
		SoundRepository.namespaceAdded(TwilightForest.MOD_ID);
	}
}
