package com.twilightforest;

import com.twilightforest.achievement.TFAchievements;
import com.twilightforest.block.TFBlocks;
import com.twilightforest.entity.TFEntities;
import com.twilightforest.item.TFItems;
import com.twilightforest.world.TFDimension;
import com.twilightforest.world.biome.TFBiomes;
import com.twilightforest.world.treasure.TFTreasure;
import com.twilightforest.world.type.WorldTypeTwilightForest;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.HalpLibe;
import turniplabs.halplibe.event.defs.CommonEvents;
import turniplabs.halplibe.util.dependency.Key;

public class TwilightForest implements ModInitializer {
	public static final String MOD_ID = HalpLibe.registerMod("twilightforest", true);
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TFConfig.init();
		CommonEvents.BEFORE_GAME_START.listen(Key.of(MOD_ID), this::beforeGameStart);
		CommonEvents.AFTER_GAME_START.listen(Key.of(MOD_ID), this::afterGameStart);

		CommonEvents.RECIPES_NAMESPACE_INIT.listen(Key.of(MOD_ID), TFRecipeNamespaces::initNamespaces);
		LOGGER.info("Twilight Forest initialized.");
	}

	private void beforeGameStart() {

		TFItems.register();
		TFBlocks.register();

		TFBiomes.init();

		WorldTypeTwilightForest.register();

		TFDimension.create();

		TFBlocks.registerPortal();

		TFEntities.initEntities();
	}

	private void afterGameStart() {

		TFDimension.register();

		TFDimension.registerWorldTypeGroups();

		TFRecipes.register();

		TFAchievements.register();

		TFTreasure.verify();
	}
}
