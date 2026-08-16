package com.twilightforest;

import net.minecraft.core.data.registry.Registries;
import turniplabs.halplibe.helper.RecipeBuilder;

public final class TFRecipeNamespaces {
	private TFRecipeNamespaces() {}

	public static void initNamespaces() {
		if (Registries.RECIPES == null) {

			TwilightForest.LOGGER.error("Recipe namespace init ran with no recipe registry -- "
				+ "Twilight Forest recipes will not survive a multiplayer login.");
			return;
		}

		RecipeBuilder.initNameSpace(TwilightForest.MOD_ID);
	}
}
