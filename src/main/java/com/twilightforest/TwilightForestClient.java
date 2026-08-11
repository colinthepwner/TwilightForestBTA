package com.twilightforest;

import com.twilightforest.asset.TFAssetBridge;
import com.twilightforest.client.render.TFBlockModels;
import com.twilightforest.client.render.TFEntityRenderers;
import net.fabricmc.api.ClientModInitializer;
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
	}

	private void afterClientStart() {
		SoundRepository.namespaceAdded(TwilightForest.MOD_ID);

		TFAssetBridge.run();
	}
}
