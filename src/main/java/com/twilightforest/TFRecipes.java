package com.twilightforest;

import com.twilightforest.block.TFBlocks;
import com.twilightforest.item.TFItems;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.data.registry.Registries;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.item.Items;
import turniplabs.halplibe.helper.RecipeBuilder;

public final class TFRecipes {
	private TFRecipes() {}

	private static final int PLANKS_PER_LOG = 4;

	private static final int PLANKS_BROWN = 12;
	private static final int PLANKS_WHITE = 0;

	private static final int CHARCOAL_META = 1;

	public static void register() {
		RecipeBuilder.initNameSpace(TwilightForest.MOD_ID);

		planksFrom(TFBlocks.LOG_TWILIGHT_OAK, "twilight_oak_log_to_planks", oakPlanks());
		planksFrom(TFBlocks.LOG_CANOPY, "canopy_log_to_planks", paintedPlanks(PLANKS_BROWN));
		planksFrom(TFBlocks.LOG_MANGROVE, "mangrove_log_to_planks", paintedPlanks(PLANKS_WHITE));
		planksFrom(TFBlocks.LOG_DARKWOOD, "darkwood_log_to_planks", paintedPlanks(PLANKS_BROWN));

		planksFrom(TFBlocks.LOG_TIMEWOOD, "timewood_log_to_planks", paintedPlanks(PLANKS_BROWN));
		planksFrom(TFBlocks.LOG_TRANSWOOD, "transwood_log_to_planks", paintedPlanks(PLANKS_BROWN));
		planksFrom(TFBlocks.LOG_MINEWOOD, "minewood_log_to_planks", paintedPlanks(PLANKS_BROWN));
		planksFrom(TFBlocks.LOG_SORTINGWOOD, "sortingwood_log_to_planks", paintedPlanks(PLANKS_BROWN));

		charcoalFrom(TFBlocks.LOG_TWILIGHT_OAK, "twilight_oak_log_to_charcoal");
		charcoalFrom(TFBlocks.LOG_CANOPY, "canopy_log_to_charcoal");
		charcoalFrom(TFBlocks.LOG_MANGROVE, "mangrove_log_to_charcoal");
		charcoalFrom(TFBlocks.LOG_DARKWOOD, "darkwood_log_to_charcoal");

		charcoalFrom(TFBlocks.LOG_TIMEWOOD, "timewood_log_to_charcoal");
		charcoalFrom(TFBlocks.LOG_TRANSWOOD, "transwood_log_to_charcoal");
		charcoalFrom(TFBlocks.LOG_MINEWOOD, "minewood_log_to_charcoal");
		charcoalFrom(TFBlocks.LOG_SORTINGWOOD, "sortingwood_log_to_charcoal");

		torchesFromBerries();

		nagaArmour(TFItems.NAGA_SCALE_TUNIC, "naga_scale_tunic",
			new String[]{"n n", "nnn", "nnn"});
		nagaArmour(TFItems.NAGA_SCALE_LEGGINGS, "naga_scale_leggings",
			new String[]{"nnn", "n n", "n n"});

		towerWood();

		invalidateRecipeCache();

		TwilightForest.LOGGER.info("Registered 20 Twilight Forest recipes.");
	}

	private static void invalidateRecipeCache() {
		if (Registries.RECIPES == null) {
			TwilightForest.LOGGER.error("Recipe registry does not exist yet -- Twilight Forest recipes "
				+ "will not be craftable. Is register() being called before AFTER_GAME_START?");
			return;
		}

		Registries.RECIPES.invalidateCaches();
	}

	private static ItemStack oakPlanks() {
		return new ItemStack(Blocks.PLANKS_OAK, PLANKS_PER_LOG);
	}

	private static ItemStack paintedPlanks(int colour) {
		return new ItemStack(Blocks.PLANKS_OAK_PAINTED, PLANKS_PER_LOG, colour);
	}

	private static void planksFrom(Block<?> log, String name, ItemStack result) {
		if (log == null) {

			return;
		}
		RecipeBuilder.Shapeless(TwilightForest.MOD_ID)
			.addInput(log)
			.create(name, result);
	}

	private static void charcoalFrom(Block<?> log, String name) {
		if (log == null) {
			return;
		}
		RecipeBuilder.Furnace(TwilightForest.MOD_ID)
			.setInput(log)
			.create(name, new ItemStack(Items.COAL, 1, CHARCOAL_META));
	}

	private static void torchesFromBerries() {
		if (TFBlocks.TORCHBERRIES == null) {

			return;
		}
		RecipeBuilder.Shaped(TwilightForest.MOD_ID)
			.setShape(new String[]{"B", "S"})
			.addInput('B', TFBlocks.TORCHBERRIES)
			.addInput('S', Items.STICK)
			.create("torchberries_to_torches", new ItemStack(Blocks.TORCH_COAL, TORCHES_PER_BERRY));
	}

	private static final int TORCHES_PER_BERRY = 5;

	private static void towerWood() {
		if (TFBlocks.TOWER_WOOD == null || TFBlocks.LOG_DARKWOOD == null) {

			return;
		}
		RecipeBuilder.Shaped(TwilightForest.MOD_ID)
			.setShape(new String[]{"##", "##"})
			.addInput('#', TFBlocks.LOG_DARKWOOD)
			.create("darkwood_log_to_tower_wood",
				new ItemStack(TFBlocks.TOWER_WOOD, TOWER_WOOD_PER_CRAFT));
	}

	private static final int TOWER_WOOD_PER_CRAFT = 4;

	private static void nagaArmour(Item piece, String name, String[] shape) {
		if (piece == null || TFItems.NAGA_SCALE == null) {

			return;
		}
		RecipeBuilder.Shaped(TwilightForest.MOD_ID)
			.setShape(shape)
			.addInput('n', TFItems.NAGA_SCALE)
			.create(name, new ItemStack(piece, 1));
	}
}
