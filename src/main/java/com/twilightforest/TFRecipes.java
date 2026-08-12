package com.twilightforest;

import com.twilightforest.block.TFBlocks;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.item.ItemStack;
import turniplabs.halplibe.helper.RecipeBuilder;

public final class TFRecipes {
	private TFRecipes() {}

	private static final int PLANKS_PER_LOG = 4;

	public static void register() {
		RecipeBuilder.initNameSpace(TwilightForest.MOD_ID);

		planksFrom(TFBlocks.LOG_TWILIGHT_OAK, "twilight_oak_log_to_planks");
		planksFrom(TFBlocks.LOG_CANOPY, "canopy_log_to_planks");
		planksFrom(TFBlocks.LOG_MANGROVE, "mangrove_log_to_planks");

		TwilightForest.LOGGER.info("Registered 3 Twilight Forest recipes.");
	}

	private static void planksFrom(net.minecraft.core.block.Block<?> log, String name) {
		if (log == null) {

			return;
		}
		RecipeBuilder.Shapeless(TwilightForest.MOD_ID)
			.addInput(log)
			.create(name, new ItemStack(Blocks.PLANKS_OAK, PLANKS_PER_LOG));
	}
}
